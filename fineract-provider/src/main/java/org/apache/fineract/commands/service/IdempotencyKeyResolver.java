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
package org.apache.fineract.commands.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class IdempotencyKeyResolver {

    private final IdempotencyKeyGenerator idempotencyKeyGenerator;

    private final FineractProperties fineractProperties;

    public String resolve(CommandWrapper wrapper) {
        return Optional.ofNullable(wrapper.getIdempotencyKey())
                .orElseGet(() -> getHeaderAttribute().orElseGet(idempotencyKeyGenerator::create));
    }

    private Optional<String> getHeaderAttribute() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes()) //
                .filter(ServletRequestAttributes.class::isInstance) //
                .map(ServletRequestAttributes.class::cast) //
                .map(ServletRequestAttributes::getRequest) //
                .map(request -> request.getHeader(fineractProperties.getIdempotencyKeyHeaderName()));
    }
}
