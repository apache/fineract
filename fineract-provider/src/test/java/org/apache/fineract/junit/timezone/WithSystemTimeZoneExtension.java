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
package org.apache.fineract.junit.timezone;

import java.lang.reflect.Method;
import java.util.TimeZone;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class WithSystemTimeZoneExtension implements BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(WithSystemTimeZoneExtension.class);

    private static final String ORIGINAL_TIMEZONE_KEY = "originalTimeZone";

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Method method = context.getTestMethod().orElse(null);
        if (hasAnnotation(method)) {
            context.getStore(NAMESPACE).put(ORIGINAL_TIMEZONE_KEY, TimeZone.getDefault());

            String timeZoneId = getTimeZoneId(method);
            TimeZone.setDefault(TimeZone.getTimeZone(timeZoneId));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Method method = context.getTestMethod().orElse(null);
        if (hasAnnotation(method)) {
            TimeZone originalTimeZone = (TimeZone) context.getStore(NAMESPACE).get(ORIGINAL_TIMEZONE_KEY);

            TimeZone.setDefault(originalTimeZone);
        }
    }

    private String getTimeZoneId(Method method) {
        return method.getAnnotation(WithSystemTimeZone.class).value();
    }

    private boolean hasAnnotation(Method method) {
        return method != null && method.getAnnotation(WithSystemTimeZone.class) != null;
    }
}
