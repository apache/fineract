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
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.joda.time.LocalDate;

@SuppressWarnings("unused")
public class ClientTransactionData {

    private final Long id;
    private final Long officeId;
    private final String officeName;
    private final EnumOptionData type;
    private final LocalDate date;
    private final CurrencyData currency;
    private final PaymentDetailData paymentDetailData;
    private final BigDecimal amount;
    private final String externalId;
    private final LocalDate submittedOnDate;
    private final boolean reversed;

    // templates
    final Collection<PaymentTypeData> paymentTypeOptions;

    public static ClientTransactionData create(Long id, Long officeId, String officeName, EnumOptionData type, LocalDate date,
            CurrencyData currency, PaymentDetailData paymentDetailData, BigDecimal amount, String externalId, LocalDate submittedOnDate,
            boolean reversed) {
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        return new ClientTransactionData(id, officeId, officeName, type, date, currency, paymentDetailData, amount, externalId,
                submittedOnDate, reversed, paymentTypeOptions);
    }

    private ClientTransactionData(Long id, Long officeId, String officeName, EnumOptionData type, LocalDate date, CurrencyData currency,
            PaymentDetailData paymentDetailData, BigDecimal amount, String externalId, LocalDate submittedOnDate, boolean reversed,
            Collection<PaymentTypeData> paymentTypeOptions) {
        super();
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.type = type;
        this.date = date;
        this.currency = currency;
        this.paymentDetailData = paymentDetailData;
        this.amount = amount;
        this.externalId = externalId;
        this.submittedOnDate = submittedOnDate;
        this.reversed = reversed;
        this.paymentTypeOptions = paymentTypeOptions;
    }

}
