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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.paymenttype.exception.PaymentTypeNotFoundException;
import org.apache.fineract.portfolio.paymenttype.mapper.PaymentTypeMapper;
import org.springframework.cache.annotation.Cacheable;

@RequiredArgsConstructor
public class PaymentTypeReadServiceImpl implements PaymentTypeReadService {

    private final PaymentTypeRepository repository;
    private final PaymentTypeMapper paymentTypeMapper;

    @Override
    @Cacheable(value = "payment_types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    public List<PaymentTypeData> retrieveAllPaymentTypes() {
        var paymentType = repository.findAllByOrderByPositionAsc();

        return paymentTypeMapper.map(paymentType);
    }

    @Override
    @Cacheable(value = "paymentTypesWithCode", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    public List<PaymentTypeData> retrieveAllPaymentTypesWithCode() {
        var paymentType = repository.findAllByCodeNameIsNotNullOrderByPositionAsc();

        return paymentTypeMapper.map(paymentType);
    }

    @Override
    public PaymentTypeData retrieveOne(Long paymentTypeId) {
        final var paymentType = repository.findById(paymentTypeId).orElseThrow(() -> new PaymentTypeNotFoundException(paymentTypeId));

        return paymentTypeMapper.map(paymentType);
    }

    /*
     * // TODO: do proper jakarta validation to fineract api error mapping private void
     * throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) { if
     * (!dataValidationErrors.isEmpty()) { // throw new PlatformApiDataValidationException(dataValidationErrors); } }
     */
}
