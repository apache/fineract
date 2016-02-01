/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.paymenttype.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.portfolio.paymenttype.api.PaymentTypeApiResourceConstants;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_payment_type")
public class PaymentType extends AbstractPersistable<Long> {

    @Column(name = "value")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_cash_payment")
    private Boolean isCashPayment;

    @Column(name = "order_position")
    private Long position;

    protected PaymentType() {}

    public PaymentType(final String name, final String description, final Boolean isCashPayment, final Long position) {
        this.name = name;
        this.description = description;
        this.isCashPayment = isCashPayment;
        this.position = position;
    }

    public static PaymentType create(String name, String description, Boolean isCashPayment, Long position) {
        return new PaymentType(name, description, isCashPayment, position);
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(3);

        if (command.isChangeInStringParameterNamed(PaymentTypeApiResourceConstants.NAME, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(PaymentTypeApiResourceConstants.NAME);
            actualChanges.put(PaymentTypeApiResourceConstants.NAME, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInStringParameterNamed(PaymentTypeApiResourceConstants.DESCRIPTION, this.description)) {
            final String newDescription = command.stringValueOfParameterNamed(PaymentTypeApiResourceConstants.DESCRIPTION);
            actualChanges.put(PaymentTypeApiResourceConstants.DESCRIPTION, newDescription);
            this.description = StringUtils.defaultIfEmpty(newDescription, null);
        }

        if (command.isChangeInBooleanParameterNamed(PaymentTypeApiResourceConstants.ISCASHPAYMENT, this.isCashPayment)) {
            final Boolean newCashPaymentType = command.booleanObjectValueOfParameterNamed(PaymentTypeApiResourceConstants.ISCASHPAYMENT);
            actualChanges.put(PaymentTypeApiResourceConstants.ISCASHPAYMENT, newCashPaymentType);
            this.isCashPayment = newCashPaymentType.booleanValue();
        }

        if (command.isChangeInLongParameterNamed(PaymentTypeApiResourceConstants.POSITION, this.position)) {
            final Long newPosition = command.longValueOfParameterNamed(PaymentTypeApiResourceConstants.POSITION);
            actualChanges.put(PaymentTypeApiResourceConstants.POSITION, newPosition);
            this.position = newPosition;
        }

        return actualChanges;
    }

    public PaymentTypeData toData() {
        return PaymentTypeData.instance(getId(), this.name, this.description, this.isCashPayment, this.position);
    }
}
