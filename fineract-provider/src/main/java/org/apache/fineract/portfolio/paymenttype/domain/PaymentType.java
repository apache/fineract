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
package org.apache.fineract.portfolio.paymenttype.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.paymenttype.api.PaymentTypeApiResourceConstants;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_payment_type")
public class PaymentType extends AbstractPersistableCustom<Long> {

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

	public Boolean isCashPayment() {
		return isCashPayment;
	}
	
}
