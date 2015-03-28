/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.paymentdetail.domain;

import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.paymentdetail.PaymentDetailConstants;
import org.mifosplatform.portfolio.paymenttype.domain.PaymentType;
import org.mifosplatform.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

@Service
public class PaymentDetailAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final PaymentTypeRepositoryWrapper repositoryWrapper;

    @Autowired
    public PaymentDetailAssembler(final FromJsonHelper fromApiJsonHelper, final PaymentTypeRepositoryWrapper repositoryWrapper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.repositoryWrapper = repositoryWrapper;
    }

    public PaymentDetail fetchPaymentDetail(final JsonObject json) {
        final Long paymentTypeId = this.fromApiJsonHelper.extractLongNamed(PaymentDetailConstants.paymentTypeParamName, json);
        if (paymentTypeId == null) { return null; }

        final PaymentType paymentType = this.repositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);

        final String accountNumber = this.fromApiJsonHelper.extractStringNamed(PaymentDetailConstants.accountNumberParamName, json);
        final String checkNumber = this.fromApiJsonHelper.extractStringNamed(PaymentDetailConstants.checkNumberParamName, json);
        final String routingCode = this.fromApiJsonHelper.extractStringNamed(PaymentDetailConstants.routingCodeParamName, json);
        final String receiptNumber = this.fromApiJsonHelper.extractStringNamed(PaymentDetailConstants.receiptNumberParamName, json);
        final String bankNumber = this.fromApiJsonHelper.extractStringNamed(PaymentDetailConstants.bankNumberParamName, json);
        return PaymentDetail.instance(paymentType, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber);
    }
}
