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

import org.apache.tamaya.*;
import org.apache.tamaya.core.resource.Resource;
import org.apache.tamaya.spi.ServiceContext;
import org.apache.tamaya.core.resource.ResourceLoader;

import java.util.*;

/**
 * Implementation of a PropertySource that reads configuration from some given resource paths.
 */
final class PathBasedPropertySource extends AbstractPropertySource {

	private static final long serialVersionUID = 5147019699905042582L;
	private List<String> paths = new ArrayList<>();
    private Map<String, String> properties = new HashMap<>();
    private AggregationPolicy aggregationPolicy;

    public PathBasedPropertySource(String name, Collection<String> paths, AggregationPolicy aggregationPolicy) {
        super(name);
        this.paths.addAll(Objects.requireNonNull(paths));
        this.aggregationPolicy = Objects.requireNonNull(aggregationPolicy);
        init();
    }

    @Override
    public Map<String, String> getProperties() {
        return this.properties;
    }

    private void init() {
        List<String> sources = new ArrayList<>();
        List<String> effectivePaths = new ArrayList<>();
        paths.forEach((path) -> {
            effectivePaths.add(path);
            for (Resource res : ServiceContext.getInstance().getSingleton(ResourceLoader.class).getResources(path)) {
                ConfigurationFormat format = ConfigurationFormat.from(res);
                if (format != null) {
                    try {
                        Map<String, String> read = format.readConfiguration(res);
                        sources.add(res.toString());
                        read.forEach((k, v) -> {
                            String valueToAdd = aggregationPolicy.aggregate(k,properties.get(k),v);
                            if(valueToAdd==null) {
                                properties.remove(k);
                            }
                            else{
                                properties.put(k, valueToAdd);
                            }
                        });
                    }
                    catch(ConfigException e){
                        throw e;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
//        metaInfo = MetaInfoBuilder.of(getMetaInfo())
//                .setSourceExpressions(new String[effectivePaths.size()])
//                .set("sources", sources.toString()).build();
    }
}
