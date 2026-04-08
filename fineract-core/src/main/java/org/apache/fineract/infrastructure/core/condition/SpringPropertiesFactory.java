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
package org.apache.fineract.infrastructure.core.condition;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

public abstract class SpringPropertiesFactory {

    private SpringPropertiesFactory() {}

    public static <T> T get(Environment environment, Class<T> clazz) {
        ConfigurationProperties configurationProperties = clazz.getAnnotation(ConfigurationProperties.class);
        if (configurationProperties == null) {
            throw new IllegalArgumentException("Not a class with @ConfigurationProperties annotation");
        }
        String prefix = configurationProperties.prefix();
        String value = configurationProperties.value();
        if (isBlank(prefix) && isBlank(value)) {
            throw new IllegalArgumentException("@ConfigurationProperties is missing both prefix and value properties");
        }
        String propertyName = isNotBlank(prefix) ? prefix : value;
        return get(environment, propertyName, clazz);
    }

    public static <T> T get(Environment environment, String propertyName, Class<T> clazz) {
        return Binder.get(environment).bind(propertyName, clazz).orElseThrow(
                () -> new IllegalArgumentException("Couldn't bind " + clazz.getSimpleName() + " to the '" + propertyName + "' property"));
    }
}
