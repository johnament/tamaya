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
package org.apache.tamaya.core.properties;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Models a {@link org.apache.tamaya.PropertySource} that can be build using a builder pattern.
 */
class MapBasedPropertySource extends AbstractPropertySource {

    private static final long serialVersionUID = 7601389831472839249L;

    private static final Logger LOG = Logger.getLogger(MapBasedPropertySource.class.getName());
    /**
     * The unit's entries.
     */
    private Map<String,String> entries = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param entries the config entries, not null.
     */
    MapBasedPropertySource(String name, Map<String, String> entries){
        super(name);
        Objects.requireNonNull(entries, "entries required.");
        this.entries.putAll(entries);
    }


    /**
     * Constructor.
     *
     * @param entries the entries
     * @param sources the sources
     * @param errors  the errors
     */
    MapBasedPropertySource(String name, Map<String, String> entries, Set<String> sources,
                           Collection<Throwable> errors){
        super(name);
        Objects.requireNonNull(entries, "entries required.");
        this.entries.putAll(entries);
        addSources(sources);
    }

    MapBasedPropertySource(String name, Set<String> sources){
        super(name);
        addSources(sources);
    }

    @Override
    public Map<String, String> getProperties() {
        return new HashMap<>(this.entries);
    }

}
