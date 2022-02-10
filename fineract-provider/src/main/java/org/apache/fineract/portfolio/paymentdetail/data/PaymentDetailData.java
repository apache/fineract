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
package org.apache.fineract.portfolio.paymentdetail.data;

import java.io.Serializable;
import java.util.Objects;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;

/**
 * Immutable data object representing a payment.
 */
public class PaymentDetailData implements Serializable {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final PaymentTypeData paymentType;
    @SuppressWarnings("unused")
    private final String accountNumber;
    @SuppressWarnings("unused")
    private final String checkNumber;
    @SuppressWarnings("unused")
    private final String routingCode;
    @SuppressWarnings("unused")
    private final String receiptNumber;
    @SuppressWarnings("unused")
    private final String bankNumber;

    public PaymentDetailData(final Long id, final PaymentTypeData paymentType, final String accountNumber, final String checkNumber,
            final String routingCode, final String receiptNumber, final String bankNumber) {
        this.id = id;
        this.paymentType = paymentType;
        this.accountNumber = accountNumber;
        this.checkNumber = checkNumber;
        this.routingCode = routingCode;
        this.receiptNumber = receiptNumber;
        this.bankNumber = bankNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentDetailData)) {
            return false;
        }
        PaymentDetailData that = (PaymentDetailData) o;
        return Objects.equals(id, that.id) && Objects.equals(paymentType, that.paymentType)
                && Objects.equals(accountNumber, that.accountNumber) && Objects.equals(checkNumber, that.checkNumber)
                && Objects.equals(routingCode, that.routingCode) && Objects.equals(receiptNumber, that.receiptNumber)
                && Objects.equals(bankNumber, that.bankNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, paymentType, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber);
    }

    public PaymentTypeData getPaymentType() {
        return this.paymentType;
    }
}
