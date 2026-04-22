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
package org.apache.fineract.test.support;

import java.lang.reflect.Constructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

public final class PropertiesFactory {

    private static final ConversionService CONVERSION_SERVICE = new DefaultConversionService();

    private PropertiesFactory() {}

    public static <T> T get(Environment environment, Class<T> clazz) {
        try {
            Constructor<T> constructor = ReflectionUtils.accessibleConstructor(clazz);
            T object = constructor.newInstance();
            ReflectionUtils.doWithFields(clazz, field -> {
                field.setAccessible(true);
                Value valueAnnotation = field.getAnnotation(Value.class);
                if (valueAnnotation != null) {
                    String expressionString = valueAnnotation.value();
                    String propertyValue = environment.resolveRequiredPlaceholders(expressionString);
                    Object valueToSet = CONVERSION_SERVICE.convert(propertyValue, field.getType());
                    field.set(object, valueToSet);
                }
            });
            return object;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No no-arg constructor is available for class " + clazz.getSimpleName(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
