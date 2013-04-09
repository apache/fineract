/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanaccount.data.PaymentDetailData;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_payment_detail")
public final class PaymentDetail extends AbstractPersistable<Long> {

    @Column(name = "payment_type_enum", nullable = false)
    private Integer paymentType;

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

    protected PaymentDetail() {

    }

    public static PaymentDetail generatePaymentDetail(final JsonCommand command, final Map<String, Object> changes) {
        // create a payment detail
        final Integer paymentTypeId = command.integerValueSansLocaleOfParameterNamed("paymentTypeId");

        if (paymentTypeId == null) { return null; }

        changes.put("paymentTypeId", paymentTypeId);

        final String accountNumber = command.stringValueOfParameterNamed("accountNumber");
        final String checkNumber = command.stringValueOfParameterNamed("checkNumber");
        final String routingCode = command.stringValueOfParameterNamed("routingCode");
        final String receiptNumber = command.stringValueOfParameterNamed("receiptNumber");
        final String bankNumber = command.stringValueOfParameterNamed("bankNumber");
        final PaymentType paymentType = PaymentType.fromInt(paymentTypeId);

        PaymentDetail paymentDetail = null;
        if (paymentType.isCheckPayment()) {
            paymentDetail = PaymentDetail.checkPaymentDetail(accountNumber, checkNumber, routingCode);
            if (StringUtils.isNotBlank(accountNumber)) {
                changes.put("accountNumber", accountNumber);
            }
            if (StringUtils.isNotBlank(checkNumber)) {
                changes.put("checkNumber", checkNumber);
            }
            if (StringUtils.isNotBlank(routingCode)) {
                changes.put("routingCode", routingCode);
            }
        } else if (paymentType.isEFTPayment()) {
            paymentDetail = PaymentDetail.eftPaymentDetail(accountNumber, routingCode);
            if (StringUtils.isNotBlank(accountNumber)) {
                changes.put("accountNumber", accountNumber);
            }
            if (StringUtils.isNotBlank(routingCode)) {
                changes.put("routingCode", routingCode);
            }
        } else if (paymentType.isReceiptPayment()) {
            paymentDetail = PaymentDetail.receiptPaymentDetail(receiptNumber, bankNumber);
            if (StringUtils.isNotBlank(receiptNumber)) {
                changes.put("receiptNumber", receiptNumber);
            }
            if (StringUtils.isNotBlank(bankNumber)) {
                changes.put("bankNumber", bankNumber);
            }
        } else {
            paymentDetail = PaymentDetail.cashPaymentDetail();
        }
        return paymentDetail;
    }

    public static PaymentDetail cashPaymentDetail() {
        return new PaymentDetail(PaymentType.CASH, null, null, null, null, null);
    }

    public static PaymentDetail checkPaymentDetail(final String accountNumber, final String checkNumber, final String routingCode) {
        return new PaymentDetail(PaymentType.CHECK, accountNumber, checkNumber, routingCode, null, null);
    }

    public static PaymentDetail receiptPaymentDetail(final String receiptNumber, final String bankNumber) {
        return new PaymentDetail(PaymentType.RECEIPT, null, null, null, receiptNumber, bankNumber);
    }

    public static PaymentDetail eftPaymentDetail(final String accountNumber, final String routingCode) {
        return new PaymentDetail(PaymentType.ELECTRONIC_FUND_TRANSFER, accountNumber, null, routingCode, null, null);
    }

    private PaymentDetail(final PaymentType paymentType, final String accountNumber, final String checkNumber, final String routingCode,
            final String receiptNumber, final String bankNumber) {
        this.paymentType = paymentType.getValue();
        this.accountNumber = accountNumber;
        this.checkNumber = checkNumber;
        this.routingCode = routingCode;
        this.receiptNumber = receiptNumber;
        this.bankNumber = bankNumber;
    }

    public PaymentDetailData toData() {
        final EnumOptionData paymentTypeData = LoanEnumerations.paymentType(this.paymentType);
        final PaymentDetailData paymentDetailData = new PaymentDetailData(getId(), paymentTypeData, this.accountNumber, this.checkNumber,
                this.routingCode, this.receiptNumber, this.bankNumber);
        return paymentDetailData;
    }
}