/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.paymentdetail.domain;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.portfolio.paymentdetail.PaymentDetailConstants;
import org.mifosplatform.portfolio.paymentdetail.data.PaymentDetailData;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
import org.mifosplatform.portfolio.paymenttype.domain.PaymentType;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_payment_detail")
public final class PaymentDetail extends AbstractPersistable<Long> {

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

    protected PaymentDetail() {

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

}