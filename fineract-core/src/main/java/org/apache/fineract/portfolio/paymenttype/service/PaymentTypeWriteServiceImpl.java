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

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.paymenttype.api.PaymentTypeApiResourceConstants;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeDataValidator;
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
    private final PaymentTypeDataValidator fromApiJsonDeserializer;

    @Override
    @CacheEvict(value = "payment_types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    public CommandProcessingResult createPaymentType(JsonCommand command) {
        this.fromApiJsonDeserializer.validateForCreate(command.json());
        String name = command.stringValueOfParameterNamed(PaymentTypeApiResourceConstants.NAME);
        String description = command.stringValueOfParameterNamed(PaymentTypeApiResourceConstants.DESCRIPTION);
        Boolean isCashPayment = command.booleanObjectValueOfParameterNamed(PaymentTypeApiResourceConstants.ISCASHPAYMENT);
        Long position = command.longValueOfParameterNamed(PaymentTypeApiResourceConstants.POSITION);
        String codeName = command.stringValueOfParameterNamed(PaymentTypeApiResourceConstants.CODE_NAME);
        Boolean isSystemDefined = command.booleanObjectValueOfParameterNamed(PaymentTypeApiResourceConstants.IS_SYSTEM_DEFINED);
        if (isSystemDefined == null) {
            isSystemDefined = false;
        }

        PaymentType newPaymentType = new PaymentType(name, description, isCashPayment, position, codeName, isSystemDefined);
        this.repository.saveAndFlush(newPaymentType);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(newPaymentType.getId()).build();
    }

    @Override
    @CacheEvict(value = "payment_types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    public CommandProcessingResult updatePaymentType(Long paymentTypeId, JsonCommand command) {

        this.fromApiJsonDeserializer.validateForUpdate(command.json());
        final PaymentType paymentType = this.repositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
        final Map<String, Object> changes = paymentType.update(command);

        if (!changes.isEmpty()) {
            this.repository.save(paymentType);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(command.entityId()).build();
    }

    @Override
    @CacheEvict(value = "payment_types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('payment_types')")
    public CommandProcessingResult deletePaymentType(Long paymentTypeId) {
        final PaymentType paymentType = this.repositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
        try {
            this.repository.delete(paymentType);
            this.repository.flush();
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable throwable = e.getMostSpecificCause();
            handleDataIntegrityIssues(throwable, e);
        }
        return new CommandProcessingResultBuilder().withEntityId(paymentType.getId()).build();
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
