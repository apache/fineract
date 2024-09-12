/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.test.support.loader;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressFBWarnings({ "VA_FORMAT_STRING_USES_NEWLINE" })
public final class FineractConfigLoader {

    public static final String FINERACT_TEST_CONFIG_LOCATION = "META-INF/fineract-test.config";

    public static final String INITIALIZER_CONFIG_KEY = "org.apache.fineract.test.initializer.Configuration";

    private FineractConfigLoader() {}

    public static Set<Class<?>> getInitializerConfigurationClasses() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader.getResource(FINERACT_TEST_CONFIG_LOCATION) == null) {
            throw new IllegalStateException("""
                    Unable to load configuration from [%s]
                    """.formatted(FINERACT_TEST_CONFIG_LOCATION));
        }

        Map<String, List<String>> configurations = PropertiesResourceLoader.load(FINERACT_TEST_CONFIG_LOCATION, classLoader);
        List<String> initializerConfigs = configurations.get(INITIALIZER_CONFIG_KEY);
        if (isEmpty(initializerConfigs)) {
            throw new IllegalStateException("""
                    Initializer configuration under key [%s] is missing from [%s]
                    """.formatted(INITIALIZER_CONFIG_KEY, FINERACT_TEST_CONFIG_LOCATION));
        }
        Set<Class<?>> result = new HashSet<>();
        for (String initializerConfigClassName : initializerConfigs) {
            try {
                Class<?> resolvedClass = classLoader.loadClass(initializerConfigClassName);
                result.add(resolvedClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("""
                        Unable to load [%s] initializer configuration class specified in [%s]
                        """.formatted(initializerConfigClassName, FINERACT_TEST_CONFIG_LOCATION), e);
            }
        }
        return result;
    }
}
