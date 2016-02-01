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
package org.apache.fineract.portfolio.client.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.joda.time.LocalDate;

@SuppressWarnings("unused")
public class ClientChargeData {

    private final Long id;

    private final Long clientId;

    private final Long chargeId;

    private final String name;

    private final EnumOptionData chargeTimeType;

    private final LocalDate dueDate;

    private final EnumOptionData chargeCalculationType;

    private final CurrencyData currency;

    private final BigDecimal amount;

    private final BigDecimal amountPaid;

    private final BigDecimal amountWaived;

    private final BigDecimal amountWrittenOff;

    private final BigDecimal amountOutstanding;

    private final boolean penalty;

    private final Boolean isActive;

    private final Boolean isPaid;

    private final Boolean isWaived;

    private final LocalDate inactivationDate;

    private final Collection<ChargeData> chargeOptions;

    private final Collection<ClientTransactionData> clientTransactionDatas;

    public static ClientChargeData instance(Long id, Long clientId, Long chargeId, String name, EnumOptionData chargeTimeType,
            LocalDate dueDate, EnumOptionData chargeCalculationType, CurrencyData currency, BigDecimal amount, BigDecimal amountPaid,
            BigDecimal amountWaived, BigDecimal amountWrittenOff, BigDecimal amountOutstanding, boolean penalty, Boolean isPaid,
            Boolean isWaived, Boolean isActive, LocalDate inactivationDate, Collection<ChargeData> chargeOptions) {
        Collection<ClientTransactionData> clientTransactionDatas = null;
        return new ClientChargeData(id, clientId, chargeId, name, chargeTimeType, dueDate, chargeCalculationType, currency, amount,
                amountPaid, amountWaived, amountWrittenOff, amountOutstanding, penalty, isPaid, isWaived, isActive, inactivationDate,
                chargeOptions, clientTransactionDatas);
    }

    public static ClientChargeData addAssociations(ClientChargeData clientChargeData,
            Collection<ClientTransactionData> clientTransactionDatas) {
        return new ClientChargeData(clientChargeData.id, clientChargeData.clientId, clientChargeData.chargeId, clientChargeData.name,
                clientChargeData.chargeTimeType, clientChargeData.dueDate, clientChargeData.chargeCalculationType,
                clientChargeData.currency, clientChargeData.amount, clientChargeData.amountPaid, clientChargeData.amountWaived,
                clientChargeData.amountWrittenOff, clientChargeData.amountOutstanding, clientChargeData.penalty, clientChargeData.isPaid,
                clientChargeData.isWaived, clientChargeData.isActive, clientChargeData.inactivationDate, clientChargeData.chargeOptions,
                clientTransactionDatas);
    }

    public static ClientChargeData template(final Collection<ChargeData> chargeOptions) {
        final Long id = null;
        final Long clientId = null;
        final Long chargeId = null;
        final String name = null;
        final EnumOptionData chargeTimeType = null;
        final LocalDate dueDate = null;
        final EnumOptionData chargeCalculationType = null;
        final CurrencyData currency = null;
        final BigDecimal amount = null;
        final BigDecimal amountPaid = null;
        final BigDecimal amountWaived = null;
        final BigDecimal amountWrittenOff = null;
        final BigDecimal amountOutstanding = null;
        final Boolean penalty = false;
        final Boolean isPaid = null;
        final Boolean isActive = null;
        final Boolean isWaived = null;
        final LocalDate inactivationDate = null;
        final Collection<ClientTransactionData> clientTransactionDatas = null;

        return new ClientChargeData(id, clientId, chargeId, name, chargeTimeType, dueDate, chargeCalculationType, currency, amount,
                amountPaid, amountWaived, amountWrittenOff, amountOutstanding, penalty, isPaid, isWaived, isActive, inactivationDate,
                chargeOptions, clientTransactionDatas);
    }

    private ClientChargeData(Long id, Long clientId, Long chargeId, String name, EnumOptionData chargeTimeType, LocalDate dueDate,
            EnumOptionData chargeCalculationType, CurrencyData currency, BigDecimal amount, BigDecimal amountPaid, BigDecimal amountWaived,
            BigDecimal amountWrittenOff, BigDecimal amountOutstanding, boolean penalty, Boolean isPaid, Boolean isWaived, Boolean isActive,
            LocalDate inactivationDate, Collection<ChargeData> chargeOptions, Collection<ClientTransactionData> clientTransactionDatas) {
        super();
        this.id = id;
        this.clientId = clientId;
        this.chargeId = chargeId;
        this.name = name;
        this.chargeTimeType = chargeTimeType;
        this.dueDate = dueDate;
        this.chargeCalculationType = chargeCalculationType;
        this.currency = currency;
        this.amount = amount;
        this.amountPaid = amountPaid;
        this.amountWaived = amountWaived;
        this.amountWrittenOff = amountWrittenOff;
        this.amountOutstanding = amountOutstanding;
        this.penalty = penalty;
        this.isPaid = isPaid;
        this.isWaived = isWaived;
        this.isActive = isActive;
        this.inactivationDate = inactivationDate;

        // template related fields
        this.chargeOptions = chargeOptions;

        /// associations
        this.clientTransactionDatas = clientTransactionDatas;
    }

}
