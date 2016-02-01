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
package org.apache.fineract.portfolio.loanaccount.command;

import java.math.BigDecimal;

import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.joda.time.LocalDate;

/**
 * Java object representation of {@link LoanCharge} API JSON.
 */
public class LoanChargeCommand implements Comparable<LoanChargeCommand> {

    @SuppressWarnings("unused")
    private final Long id;
    private final Long chargeId;
    private final BigDecimal amount;
    @SuppressWarnings("unused")
    private final Integer chargeTimeType;
    @SuppressWarnings("unused")
    private final Integer chargeCalculationType;
    @SuppressWarnings("unused")
    private final LocalDate dueDate;

    public LoanChargeCommand(final Long id, final Long chargeId, final BigDecimal amount, final Integer chargeTimeType,
            final Integer chargeCalculationType, final LocalDate dueDate) {
        this.id = id;
        this.chargeId = chargeId;
        this.amount = amount;
        this.chargeTimeType = chargeTimeType;
        this.chargeCalculationType = chargeCalculationType;
        this.dueDate = dueDate;
    }

    @Override
    public int compareTo(final LoanChargeCommand o) {
        int comparison = this.chargeId.compareTo(o.chargeId);
        if (comparison == 0) {
            comparison = this.amount.compareTo(o.amount);
        }
        return comparison;
    }
}