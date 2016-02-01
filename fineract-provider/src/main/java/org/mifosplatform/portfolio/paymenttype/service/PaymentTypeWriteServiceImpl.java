/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.paymenttype.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.portfolio.paymenttype.api.PaymentTypeApiResourceConstants;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeDataValidator;
import org.mifosplatform.portfolio.paymenttype.domain.PaymentType;
import org.mifosplatform.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.mifosplatform.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class PaymentTypeWriteServiceImpl implements PaymentTypeWriteService {

    private final PaymentTypeRepository repository;
    private final PaymentTypeRepositoryWrapper repositoryWrapper;
    private final PaymentTypeDataValidator fromApiJsonDeserializer;

    @Autowired
    public PaymentTypeWriteServiceImpl(PaymentTypeRepository repository, PaymentTypeRepositoryWrapper repositoryWrapper,
            PaymentTypeDataValidator fromApiJsonDeserializer) {
        this.repository = repository;
        this.repositoryWrapper = repositoryWrapper;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;

    }

    @Override
    public CommandProcessingResult createPaymentType(JsonCommand command) {
        this.fromApiJsonDeserializer.validateForCreate(command.json());
        String name = command.stringValueOfParameterNamed(PaymentTypeApiResourceConstants.NAME);
        String description = command.stringValueOfParameterNamed(PaymentTypeApiResourceConstants.DESCRIPTION);
        Boolean isCashPayment = command.booleanObjectValueOfParameterNamed(PaymentTypeApiResourceConstants.ISCASHPAYMENT);
        Long position = command.longValueOfParameterNamed(PaymentTypeApiResourceConstants.POSITION);

        PaymentType newPaymentType = PaymentType.create(name, description, isCashPayment, position);
        this.repository.save(newPaymentType);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(newPaymentType.getId()).build();
    }

    @Override
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
    public CommandProcessingResult deletePaymentType(Long paymentTypeId) {
        final PaymentType paymentType = this.repositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
        try {
            this.repository.delete(paymentType);
            this.repository.flush();
        } catch (final DataIntegrityViolationException e) {
            handleDataIntegrityIssues(e);
        }
        return new CommandProcessingResultBuilder().withEntityId(paymentType.getId()).build();
    }

    private void handleDataIntegrityIssues(final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("acc_product_mapping")) {
            throw new PlatformDataIntegrityException("error.msg.payment.type.association.exist",
                    "cannot.delete.payment.type.with.association");
        } else if (realCause.getMessage().contains("payment_type_id")) { throw new PlatformDataIntegrityException(
                "error.msg.payment.type.association.exist", "cannot.delete.payment.type.with.association"); }

        throw new PlatformDataIntegrityException("error.msg.paymenttypes.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }
}
