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

import java.time.LocalDate;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.paymentdetail.PaymentDetailConstants;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;

@Entity
@Table(name = "m_payment_detail")
public final class PaymentDetail extends AbstractPersistableCustom {

    @ManyToOne
    @JoinColumn(name = "payment_type_id", nullable = false)
    private PaymentType paymentType;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "check_number", length = 50)
    private String checkNumber;

    @Column(name = "routing_code", length = 50)
    private String routingCode;

    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    @Column(name = "bank_number", length = 50)
    private String bankNumber;

    @Column(name = "actual_transaction_type", length = 50)
    private String actualTransactionType;

    @Column(name = "parent_savings_account_transaction_id")
    private Integer parentSavingsAccountTransactionId;

    @Column(name = "parent_transaction_payment_details_id")
    private Integer parentTransactionPaymentDetailsId;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    PaymentDetail() {

    }

    public static PaymentDetail generatePaymentDetail(final PaymentType paymentType, final JsonCommand command,
            final Map<String, Object> changes) {
        final String accountNumber = command.stringValueOfParameterNamed(PaymentDetailConstants.accountNumberParamName);
        final String checkNumber = command.stringValueOfParameterNamed(PaymentDetailConstants.checkNumberParamName);
        final String routingCode = command.stringValueOfParameterNamed(PaymentDetailConstants.routingCodeParamName);
        final String receiptNumber = command.stringValueOfParameterNamed(PaymentDetailConstants.receiptNumberParamName);
        final String bankNumber = command.stringValueOfParameterNamed(PaymentDetailConstants.bankNumberParamName);

        if (StringUtils.isNotBlank(accountNumber)) {
            changes.put(PaymentDetailConstants.accountNumberParamName, accountNumber);
        }
        if (StringUtils.isNotBlank(checkNumber)) {
            changes.put(PaymentDetailConstants.checkNumberParamName, checkNumber);
        }
        if (StringUtils.isNotBlank(routingCode)) {
            changes.put(PaymentDetailConstants.routingCodeParamName, routingCode);
        }
        if (StringUtils.isNotBlank(receiptNumber)) {
            changes.put(PaymentDetailConstants.receiptNumberParamName, receiptNumber);
        }
        if (StringUtils.isNotBlank(bankNumber)) {
            changes.put(PaymentDetailConstants.bankNumberParamName, bankNumber);
        }

        final PaymentDetail paymentDetail = new PaymentDetail(paymentType, accountNumber, checkNumber, routingCode, receiptNumber,
                bankNumber);
        return paymentDetail;
    }

    public static PaymentDetail instance(final PaymentType paymentType, final String accountNumber, final String checkNumber,
            final String routingCode, final String receiptNumber, final String bankNumber) {
        return new PaymentDetail(paymentType, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber);
    }

    private PaymentDetail(final PaymentType paymentType, final String accountNumber, final String checkNumber, final String routingCode,
            final String receiptNumber, final String bankNumber) {
        this.paymentType = paymentType;
        this.accountNumber = accountNumber;
        this.checkNumber = checkNumber;
        this.routingCode = routingCode;
        this.receiptNumber = receiptNumber;
        this.bankNumber = bankNumber;
    }

    public PaymentDetailData toData() {
        final PaymentTypeData paymentTypeData = this.paymentType.toData();
        final PaymentDetailData paymentDetailData = new PaymentDetailData(getId(), paymentTypeData, this.accountNumber, this.checkNumber,
                this.routingCode, this.receiptNumber, this.bankNumber);
        return paymentDetailData;
    }

    public PaymentType getPaymentType() {
        return this.paymentType;
    }

    public String getReceiptNumber() {
        return this.receiptNumber;
    }

    public String getRoutingCode() {
        return routingCode;
    }

    public void setActualTransactionType(String actualTransactionType) {
        this.actualTransactionType = actualTransactionType;
    }

    public void setParentSavingsAccountTransactionId(Integer parentSavingsAccountTransactionId) {
        this.parentSavingsAccountTransactionId = parentSavingsAccountTransactionId;
    }

    public void setParentTransactionPaymentDetailsId(Integer parentTransactionPaymentDetailsId) {
        this.parentTransactionPaymentDetailsId = parentTransactionPaymentDetailsId;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getActualTransactionType() {
        return actualTransactionType;
    }

    public Integer getParentSavingsAccountTransactionId() {
        return parentSavingsAccountTransactionId;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public static PaymentDetail paymentDetails(PaymentType paymentType, String accountNumber) {
        PaymentDetail pay = new PaymentDetail();
        pay.setPaymentType(paymentType);
        pay.setAccountNumber(accountNumber);
        return pay;
    }
}
