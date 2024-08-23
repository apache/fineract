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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.paymenttype.api.PaymentTypeApiResourceConstants;

@Getter
@Setter
@Entity
@Table(name = "m_payment_type")
@AllArgsConstructor
public class PaymentType extends AbstractPersistableCustom<Long> {

    @Column(name = "value")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_cash_payment")
    private Boolean isCashPayment;

    @Column(name = "order_position")
    private Long position;

    @Column(name = "code_name")
    private String codeName;

    @Column(name = "is_system_defined")
    private Boolean isSystemDefined;

    protected PaymentType() {}

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

}
