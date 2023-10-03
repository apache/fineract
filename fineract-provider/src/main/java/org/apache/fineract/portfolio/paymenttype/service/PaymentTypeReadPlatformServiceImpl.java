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
package org.apache.fineract.portfolio.paymenttype.service;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.mapper.PaymentTypeMapper;
import org.springframework.cache.annotation.Cacheable;

@RequiredArgsConstructor
public class PaymentTypeReadPlatformServiceImpl implements PaymentTypeReadPlatformService {

    private final PlatformSecurityContext context;
    private final PaymentTypeMapper paymentTypeMapper;
    private final PaymentTypeRepositoryWrapper paymentTypeRepository;

    @Override
    @Cacheable(value = "payment_types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    public Collection<PaymentTypeData> retrieveAllPaymentTypes() {
        // TODO Auto-generated method stub
        this.context.authenticatedUser();

        List<PaymentType> paymentType = this.paymentTypeRepository.findAll();
        return this.paymentTypeMapper.map(paymentType);
    }

    @Override
    @Cacheable(value = "paymentTypesWithCode", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    public Collection<PaymentTypeData> retrieveAllPaymentTypesWithCode() {
        // TODO Auto-generated method stub
        this.context.authenticatedUser();

        List<PaymentType> paymentType = this.paymentTypeRepository.findAllWithCodeName();
        return this.paymentTypeMapper.map(paymentType);
    }

    @Override
    public PaymentTypeData retrieveOne(Long paymentTypeId) {
        // TODO Auto-generated method stub
        this.context.authenticatedUser();

        final PaymentType paymentType = this.paymentTypeRepository.findOneWithNotFoundDetection(paymentTypeId);
        return this.paymentTypeMapper.map(paymentType);
    }

}
