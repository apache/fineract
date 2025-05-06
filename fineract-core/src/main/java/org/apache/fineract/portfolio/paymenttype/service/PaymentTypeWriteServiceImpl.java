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

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.paymenttype.data.CreatePaymentTypeRequest;
import org.apache.fineract.portfolio.paymenttype.data.DeletePaymentTypeRequest;
import org.apache.fineract.portfolio.paymenttype.data.UpdatablePaymentTypeResponse;
import org.apache.fineract.portfolio.paymenttype.data.UpdatePaymentTypeRequest;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;

@RequiredArgsConstructor
public class PaymentTypeWriteServiceImpl implements PaymentTypeWriteService {

    private final PaymentTypeRepository repository;
    private final PaymentTypeRepositoryWrapper repositoryWrapper;

    @Override
    @CacheEvict(value = "payment_types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    public UpdatablePaymentTypeResponse createPaymentType(CreatePaymentTypeRequest paymentTypeRequest) {
        boolean isSystemDefined = Optional.ofNullable(paymentTypeRequest.getIsSystemDefined()).orElse(false);
        Long position = Optional.ofNullable(paymentTypeRequest.getPosition()).map(Long::valueOf).orElse(null);

        PaymentType newPaymentType = new PaymentType(paymentTypeRequest.getName(), paymentTypeRequest.getDescription(),
                paymentTypeRequest.getIsCashPayment(), position, paymentTypeRequest.getCodeName(), isSystemDefined);

        this.repository.saveAndFlush(newPaymentType);

        return new UpdatablePaymentTypeResponse(newPaymentType.getId());
    }

    @Override
    @CacheEvict(value = "payment_types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    public UpdatablePaymentTypeResponse updatePaymentType(UpdatePaymentTypeRequest paymentTypeRequest) {
        final PaymentType paymentType = this.repositoryWrapper.findOneWithNotFoundDetection(paymentTypeRequest.getId());
        paymentType.setName(paymentTypeRequest.getName());
        paymentType.setDescription(paymentTypeRequest.getDescription());
        paymentType.setIsCashPayment(paymentTypeRequest.getIsCashPayment());
        paymentType.setPosition(Optional.ofNullable(paymentTypeRequest.getPosition()).map(Integer::longValue).orElse(null));

        this.repository.save(paymentType);
        return new UpdatablePaymentTypeResponse(paymentType.getId());
    }

    @Override
    @CacheEvict(value = "payment_types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    public UpdatablePaymentTypeResponse deletePaymentType(DeletePaymentTypeRequest deletePaymentTypeRequest) {
        final PaymentType paymentType = this.repositoryWrapper.findOneWithNotFoundDetection(deletePaymentTypeRequest.getId());
        try {
            this.repository.delete(paymentType);
            this.repository.flush();
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable throwable = e.getMostSpecificCause();
            handleDataIntegrityIssues(throwable, e);
        }
        return new UpdatablePaymentTypeResponse(paymentType.getId());
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
