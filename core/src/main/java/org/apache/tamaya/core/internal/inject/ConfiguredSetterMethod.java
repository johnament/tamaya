/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.core.internal.inject;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.tamaya.core.properties.PropertyChangeSet;
import org.apache.tamaya.ConfigException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.annotation.*;
import org.apache.tamaya.core.internal.Utils;

/**
 * Small class that contains and manages all information and access to a configured field and a concrete instance current
 * it (referenced by a weak reference). It also implements all aspects current keys filtering, conversions any applying the
 * final keys by reflection.
 */
public class ConfiguredSetterMethod {

    /**
     * The configured field instance.
     */
    private Method setterMethod;

    /**
     * Models a configured field and provides mechanisms for injection.
     *
     * @param method the method instance.
     */
    public ConfiguredSetterMethod(Method method) {
        this.setterMethod = Optional.of(method).filter(
                (m) -> void.class.equals(m.getReturnType()) &&
                        m.getParameterCount() == 1).get();
    }

    public Consumer<PropertyChangeSet> createConsumer(Object instance, Configuration... configurations){
        // TODO consider environment as well
        return event -> {
            for(Configuration cfg:configurations){
                if(event.getPropertySource().getName().equals(cfg.getName())){
                    // ignore these changes, since this config is overridden.
                    return;
                }
            }
            String configValue = InjectionUtils.getConfigValue(setterMethod, configurations);
            applyValue(instance,configValue, false, configurations);
        };
    }


    /**
     * Evaluate the initial keys fromMap the configuration and applyChanges it to the field.
     *
     * @param target the target instance.
     * @param configurations Configuration instances that replace configuration served by services. This allows
     *                       more easily testing and adaption.
     * @throws ConfigException if evaluation or conversion failed.
     */
    public void applyInitialValue(Object target, Configuration... configurations) throws ConfigException {
        String configValue = InjectionUtils.getConfigValue(this.setterMethod, configurations);
        applyValue(target, configValue, false, configurations);
    }

    /**
     * This method reapplies a changed configuration keys to the field.
     *
     * @param target      the target instance, not null.
     * @param configValue the new keys to be applied, null will trigger the evaluation current the configured default keys.
     * @param resolve     set to true, if expression resolution should be applied on the keys passed.
     * @throws org.apache.tamaya.ConfigException if the configuration required could not be resolved or converted.
     */
    public void applyValue(Object target, String configValue, boolean resolve, Configuration... configurations) throws ConfigException {
        Objects.requireNonNull(target);
        try {
            if (resolve && configValue != null) {
                // net step perform exression resolution, if any
                configValue = Configuration.evaluateValue(configValue, configurations);
            }
            // Check for adapter/filter
            Object value = InjectionUtils.adaptValue(this.setterMethod, this.setterMethod.getParameterTypes()[0], configValue);
            setterMethod.setAccessible(true);
            setterMethod.invoke(target, value);
        } catch (Exception e) {
            throw new ConfigException("Failed to annotation configured method: " + this.setterMethod.getDeclaringClass()
                    .getName() + '.' + setterMethod.getName(), e);
        }
    }



    /**
     * This method checks if the given (qualified) configuration key is referenced fromMap this field.
     * This is useful to determine, if a key changed in a configuration should trigger any change events
     * on the related instances.
     *
     * @param key the (qualified) configuration key, not null.
     * @return true, if the key is referenced.
     */
    public boolean matchesKey(String key) {
        DefaultAreas areasAnnot = this.setterMethod.getDeclaringClass().getAnnotation(DefaultAreas.class);
        Collection<ConfiguredProperty> configuredProperties =
                Utils.getAnnotations(this.setterMethod, ConfiguredProperty.class, ConfiguredProperties.class);
        for(ConfiguredProperty prop: configuredProperties) {
            if (InjectionUtils.evaluateKeys(this.setterMethod, areasAnnot, prop).contains(key)) {
                return true;
            }
        }
        if (InjectionUtils.evaluateKeys(this.setterMethod, areasAnnot).contains(key)) {
            return true;
        }
        return false;
    }



}
