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
package org.apache.tamaya;

import org.apache.tamaya.annotation.WithCodec;
import org.apache.tamaya.spi.CodecsSingletonSpi;
import org.apache.tamaya.spi.ServiceContext;

/**
 * Singleton manager that provides {@link Codec} instance, usable for converting String
 * based configuration entries into any other target types and vice versa.
 * @see org.apache.tamaya.Codec
 */
final class Codecs {

    /**
     * Orivate singleton constructor.
     */
    private Codecs(){}

    /**
     * Registers a new {@link Codec} for the given target type, hereby replacing any existing adapter for
     * this type.
     * @param targetType The target class, not null.
     * @param adapter The adapter, not null.
     * @param <T> The target type
     * @return any adapter replaced with the new adapter, or null.
     */
    public static <T> Codec<T> register(Class<T> targetType, Codec<T> adapter){
        return ServiceContext.getInstance().getSingleton(CodecsSingletonSpi.class).register(targetType, adapter);
    }

    /**
     * Checks if a {@link Codec} for given target type is available.
     * @param targetType the target type class
     * @return true, if the given target type is supported.
     */
    public static boolean isTargetTypeSupported(Class<?> targetType){
        return ServiceContext.getInstance().getSingleton(CodecsSingletonSpi.class).isTargetTypeSupported(targetType);
    }

    /**
     * Get an {@link Codec} converting to and from the given target type.
     * @param targetType the target type class
     * @param <T> the target type
     * @return the corresponding {@link Codec}, never null.
     * @throws ConfigException if the target type is not supported.
     */
    public static  <T> Codec<T> getCodec(Class<T> targetType){
        return getCodec(targetType, null);
    }

    /**
     * Get an {@link Codec} converting to the given target type.
     * @param targetType the target type class
     * @param annotation the {@link org.apache.tamaya.annotation.WithCodec} annotation, or null. If the annotation is not null and
     *                   defines an overriding adapter, this instance is created and returned.
     * @param <T> the target type
     * @return the corresponding {@link Codec}, never null.
     * @throws ConfigException if the target type is not supported, or the overriding adapter cannot be
     * instantiated.
     */
    public static  <T> Codec<T> getCodec(Class<T> targetType, WithCodec annotation){
        return ServiceContext.getInstance().getSingleton(CodecsSingletonSpi.class).getCodec(targetType, annotation);
    }

}
