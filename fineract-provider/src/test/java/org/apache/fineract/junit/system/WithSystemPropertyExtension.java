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
package org.apache.fineract.junit.system;

import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class WithSystemPropertyExtension implements BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(WithSystemPropertyExtension.class);

    private static final String ORIGINAL_SYSTEM_PROPERTY_KEY_PATTERN = "originalSystemProperty_%s";

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Method method = context.getTestMethod().orElse(null);
        if (hasAnnotation(method)) {
            WithSystemProperty[] annotations = method.getAnnotationsByType(WithSystemProperty.class);
            for (WithSystemProperty annotation : annotations) {
                String propKey = annotation.key();
                String propValue = annotation.value();

                saveOriginalSystemPropertyValue(context, propKey);
                overrideSystemPropertyValue(propKey, propValue);
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Method method = context.getTestMethod().orElse(null);
        if (hasAnnotation(method)) {
            WithSystemProperty[] annotations = method.getAnnotationsByType(WithSystemProperty.class);
            for (WithSystemProperty annotation : annotations) {
                String propKey = annotation.key();
                String propValue = loadOriginalSystemPropertyValue(context, propKey);

                overrideSystemPropertyValue(propKey, propValue);
            }
        }
    }

    private void overrideSystemPropertyValue(String propKey, String propValue) {
        if (propValue == null) {
            System.clearProperty(propKey);
        } else {
            System.setProperty(propKey, propValue);
        }
    }

    private void saveOriginalSystemPropertyValue(ExtensionContext context, String propKey) {
        String originalKey = ORIGINAL_SYSTEM_PROPERTY_KEY_PATTERN.formatted(propKey);
        context.getStore(NAMESPACE).put(originalKey, System.getProperty(propKey));
    }

    private String loadOriginalSystemPropertyValue(ExtensionContext context, String propKey) {
        String originalKey = ORIGINAL_SYSTEM_PROPERTY_KEY_PATTERN.formatted(propKey);
        return (String) context.getStore(NAMESPACE).get(originalKey);
    }

    private boolean hasAnnotation(Method method) {
        return method != null && method.getAnnotation(WithSystemProperty.class) != null;
    }
}
