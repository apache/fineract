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
package org.apache.fineract.client.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class TenantIdRequestInterceptor implements RequestInterceptor {

    private final String tenantId;

    public TenantIdRequestInterceptor(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Fineract-Platform-TenantId", tenantId);
        if (!template.headers().containsKey("Content-Type")) {
            template.header("Content-Type", "application/json");
        }
        if (!template.headers().containsKey("Accept")) {
            template.header("Accept", "application/json");
        }
    }
}
