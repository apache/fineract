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

import jakarta.validation.Valid;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeCreateRequest;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeCreateResponse;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeDeleteRequest;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeDeleteResponse;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeUpdateRequest;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeUpdateResponse;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.paymenttype.exception.PaymentTypeNotFoundException;
import org.apache.fineract.portfolio.paymenttype.mapper.PaymentTypeCreateRequestMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;

@RequiredArgsConstructor
public class PaymentTypeWriteServiceImpl implements PaymentTypeWriteService {

    private final PaymentTypeRepository repository;
    private final PaymentTypeCreateRequestMapper createRequestMapper;

    @Override
    @CacheEvict(value = "payment_types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    public PaymentTypeCreateResponse createPaymentType(@Valid PaymentTypeCreateRequest request) {
        final var paymentType = createRequestMapper.map(request);

        repository.saveAndFlush(paymentType);

        return PaymentTypeCreateResponse.builder().resourceId(paymentType.getId()).build();
    }

    @Override
    @CacheEvict(value = "payment_types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    @SuppressWarnings("AvoidHidingCauseException")
    public PaymentTypeUpdateResponse updatePaymentType(@Valid PaymentTypeUpdateRequest request) {
        try {
            final var paymentType = repository.findById(request.getId())
                    .orElseThrow(() -> new PaymentTypeNotFoundException(request.getId()));

            if (!Strings.CS.equals(request.getName(), paymentType.getName())) {
                paymentType.setName(request.getName());
            }
            if (!Strings.CS.equals(request.getDescription(), paymentType.getDescription())) {
                paymentType.setDescription(request.getDescription());
            }
            if (!Strings.CS.equals(request.getCodeName(), paymentType.getCodeName())) {
                paymentType.setCodeName(request.getCodeName());
            }
            if (!Objects.equals(request.getPosition(), paymentType.getPosition())) {
                paymentType.setPosition(request.getPosition());
            }
            if (!Objects.equals(request.getIsCashPayment(), paymentType.getIsCashPayment())) {
                paymentType.setIsCashPayment(request.getIsCashPayment());
            }
            if (!Objects.equals(request.getIsSystemDefined(), paymentType.getIsSystemDefined())) {
                paymentType.setIsSystemDefined(request.getIsSystemDefined());
            }

            repository.saveAndFlush(paymentType);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            throw new PaymentTypeNotFoundException(request.getId());
        }

        return PaymentTypeUpdateResponse.builder().resourceId(request.getId()).build();
    }

    @Override
    @CacheEvict(value = "payment_types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    public PaymentTypeDeleteResponse deletePaymentType(@Valid PaymentTypeDeleteRequest request) {
        final var paymentType = repository.findById(request.getId()).orElseThrow(() -> new PaymentTypeNotFoundException(request.getId()));

        try {
            repository.delete(paymentType);
            repository.flush();
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable throwable = e.getMostSpecificCause();
            handleDataIntegrityIssues(throwable, e);
        }

        return PaymentTypeDeleteResponse.builder().resourceId(paymentType.getId()).build();
    }

    private void handleDataIntegrityIssues(final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("acc_product_mapping")) {
            throw new PlatformDataIntegrityException("error.msg.payment.type.association.exist",
                    "cannot.delete.payment.type.with.association");
        } else if (realCause.getMessage().contains("payment_type_id")) {
            throw new PlatformDataIntegrityException("error.msg.payment.type.association.exist",
                    "cannot.delete.payment.type.with.association");
        }
        throw ErrorHandler.getMappable(dve, "error.msg.paymenttypes.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }
}
