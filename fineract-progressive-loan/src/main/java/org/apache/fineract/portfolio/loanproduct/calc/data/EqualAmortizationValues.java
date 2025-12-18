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
package org.apache.fineract.portfolio.loanproduct.calc.data;

import java.math.BigDecimal;
import java.util.Objects;
import org.apache.fineract.organisation.monetary.domain.Money;

public record EqualAmortizationValues(Money totalOutstanding, Integer numberOfInstallments, Money value, Money adjustment) {

    public Money getAdjustedValue() {
        return value.add(adjustment);
    }

    /**
     * calculates value according to the index of the installments
     *
     * @param index
     *            index accepted 0 to number of (installments - 1)
     * @return calculated value for the given index
     */
    public Money calculateValue(Integer index) {
        if (getAdjustedValue().isLessThanZero()) {
            return totalOutstanding.minus(value.multipliedBy(index + 1)).isLessThanZero() ? value.zero() : value;
        }
        return (index == numberOfInstallments - 1 ? getAdjustedValue() : value);
    }

    public BigDecimal calculateValueBigDecimal(Integer index) {
        return calculateValue(index).getAmount();
    }

    public EqualAmortizationValues add(EqualAmortizationValues other) {
        if (!Objects.equals(numberOfInstallments, other.numberOfInstallments)) {
            throw new RuntimeException("Incompatible EqualAmortizationValues. numberOfInstallments parameter should match.");
        }
        return new EqualAmortizationValues(totalOutstanding.add(other.totalOutstanding), numberOfInstallments, value.add(other.value),
                adjustment.add(other.adjustment));
    }
}
