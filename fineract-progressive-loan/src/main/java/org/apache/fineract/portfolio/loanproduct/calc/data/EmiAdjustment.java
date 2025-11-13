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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;

public record EmiAdjustment(//
        Money originalEmi, //
        Money emiDifference, //
        List<RepaymentPeriod> relatedRepaymentPeriods, //
        long uncountablePeriods//
) {

    public boolean shouldBeAdjusted() {
        MathContext mc = originalEmi.getMc();
        BigDecimal lowerHalfOfRelatedPeriods = BigDecimal.valueOf(numberOfRelatedPeriods()).divide(BigDecimal.TWO, 0, RoundingMode.FLOOR);
        return MathUtil.isGreaterThanZero(lowerHalfOfRelatedPeriods) && !emiDifference.isZero() && emiDifference.abs() //
                .multipliedBy(100) //
                .isGreaterThan(lowerHalfOfRelatedPeriods); //
    }

    public Money adjustment() {
        return emiDifference.dividedBy(Math.max(1, numberOfRelatedPeriods() - uncountablePeriods));
    }

    public Money adjustedEmi() {
        return originalEmi.plus(adjustment());
    }

    public boolean hasLessEmiDifference(EmiAdjustment previousAdjustment) {
        return emiDifference.abs().isLessThan(previousAdjustment.emiDifference.abs());
    }

    public boolean hasUncountablePeriods() {
        return uncountablePeriods > 0;
    }

    private int numberOfRelatedPeriods() {
        return relatedRepaymentPeriods.size();
    }
}
