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
package org.apache.fineract.junit.context;

import java.lang.reflect.Method;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class WithTenantContextExtension implements BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(WithTenantContextExtension.class);

    private static final String ORIGINAL_TENANT_KEY = "originalTenant";

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Method method = context.getTestMethod().orElse(null);
        if (hasAnnotation(method)) {
            context.getStore(NAMESPACE).put(ORIGINAL_TENANT_KEY, ThreadLocalContextUtil.getTenant());

            WithTenantContext annotation = method.getAnnotation(WithTenantContext.class);
            ThreadLocalContextUtil
                    .setTenant(new FineractPlatformTenant(1L, annotation.tenantName(), "Default", annotation.tenantTimeZoneId(), null));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Method method = context.getTestMethod().orElse(null);
        if (hasAnnotation(method)) {
            FineractPlatformTenant originalTenant = (FineractPlatformTenant) context.getStore(NAMESPACE).get(ORIGINAL_TENANT_KEY);

            ThreadLocalContextUtil.setTenant(originalTenant);
        }
    }

    private boolean hasAnnotation(Method method) {
        return method != null && method.getAnnotation(WithTenantContext.class) != null;
    }
}
