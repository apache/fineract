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

package org.apache.fineract.portfolio.workingcapitalloan.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.service.ChargeEnumerations;

@Getter
@Builder
@AllArgsConstructor
public final class WorkingCapitalLoanChargeData {

    private final Long id;
    private final Long chargeId;
    private final String name;
    private final EnumOptionData chargeTimeType;

    private final LocalDate submittedOnDate;

    private final LocalDate dueDate;

    private final EnumOptionData chargeCalculationType;

    private final CurrencyData currency;

    private final BigDecimal amount;

    private final BigDecimal amountPaid;

    private final BigDecimal amountOutstanding;

    private final List<ChargeData> chargeOptions;

    private final boolean penalty;

    private final EnumOptionData chargePaymentMode;

    private final boolean paid;

    private final Long loanId;

    private final ExternalId externalId;

    private final ExternalId externalLoanId;

    public WorkingCapitalLoanChargeData(Long id, Long chargeId, String name, ChargeTimeType chargeTimeType, LocalDate submittedOnDate,
            LocalDate dueDate, ChargeCalculationType chargeCalculationType, String cCode, String cName, Integer cDecimalPlaces,
            Integer cInMultiplesOf, String cDisplaySymbol, String cNameCode, BigDecimal amount, BigDecimal amountPaid, boolean penalty,
            ChargePaymentMode chargePaymentMode, boolean paid, Long loanId, ExternalId externalId, ExternalId externalLoanId) {
        this.id = id;
        this.chargeId = chargeId;
        this.name = name;
        this.chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTimeType);
        this.submittedOnDate = submittedOnDate;
        this.dueDate = dueDate;
        this.chargeCalculationType = ChargeEnumerations.chargeCalculationType(chargeCalculationType);
        this.currency = new CurrencyData(cCode, cName, cDecimalPlaces, cInMultiplesOf, cDisplaySymbol, cNameCode);
        this.amount = amount;
        this.amountPaid = amountPaid;
        this.amountOutstanding = MathUtil.subtract(amount, amountPaid);
        this.chargeOptions = null;
        this.penalty = penalty;
        this.chargePaymentMode = ChargeEnumerations.chargePaymentMode(chargePaymentMode);
        this.paid = paid;
        this.loanId = loanId;
        this.externalId = externalId;
        this.externalLoanId = externalLoanId;
    }

    public static WorkingCapitalLoanChargeData template(final List<ChargeData> chargeOptions) {
        return WorkingCapitalLoanChargeData.builder().chargeOptions(chargeOptions).build();
    }

}
