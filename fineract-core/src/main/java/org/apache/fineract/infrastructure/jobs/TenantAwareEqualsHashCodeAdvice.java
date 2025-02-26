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
package org.apache.fineract.infrastructure.jobs;

import java.lang.reflect.Method;
import java.util.Objects;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

public class TenantAwareEqualsHashCodeAdvice implements MethodInterceptor {

    private final Object target;
    private final String tenantIdentifier;

    public TenantAwareEqualsHashCodeAdvice(Object target) {
        this.target = target;
        FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        this.tenantIdentifier = tenant != null ? tenant.getTenantIdentifier() : null;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        String methodName = method.getName();

        if ("equals".equals(methodName) && args.length == 1) {
            Object other = args[0];

            if (other instanceof Factory) {

                TenantAwareEqualsHashCodeAdvice otherProxy = (TenantAwareEqualsHashCodeAdvice) ((Factory) other).getCallback(0);
                return Objects.equals(target, otherProxy.target) && Objects.equals(tenantIdentifier, otherProxy.tenantIdentifier);
            }
            return false;
        }

        if ("hashCode".equals(methodName) && args.length == 0) {
            return Objects.hash(target.hashCode(), tenantIdentifier);
        }

        return proxy.invoke(target, args);
    }
}
