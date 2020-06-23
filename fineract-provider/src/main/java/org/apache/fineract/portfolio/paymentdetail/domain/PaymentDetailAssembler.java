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
package org.apache.fineract.portfolio.paymentdetail.domain;

import com.google.gson.JsonObject;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.paymentdetail.PaymentDetailConstants;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        if (paymentTypeId == null) {
            return null;
        }

        final PaymentType paymentType = this.repositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);

        final String accountNumber = this.fromApiJsonHelper.extractStringNamed(PaymentDetailConstants.accountNumberParamName, json);
        final String checkNumber = this.fromApiJsonHelper.extractStringNamed(PaymentDetailConstants.checkNumberParamName, json);
        final String routingCode = this.fromApiJsonHelper.extractStringNamed(PaymentDetailConstants.routingCodeParamName, json);
        final String receiptNumber = this.fromApiJsonHelper.extractStringNamed(PaymentDetailConstants.receiptNumberParamName, json);
        final String bankNumber = this.fromApiJsonHelper.extractStringNamed(PaymentDetailConstants.bankNumberParamName, json);
        return PaymentDetail.instance(paymentType, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber);
    }
}
