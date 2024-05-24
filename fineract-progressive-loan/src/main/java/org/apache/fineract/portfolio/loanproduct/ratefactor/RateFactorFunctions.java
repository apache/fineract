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
package org.apache.fineract.portfolio.loanproduct.ratefactor;

import java.math.BigDecimal;
import java.math.MathContext;

public class RateFactorFunctions {

    protected RateFactorFunctions() {}

    /**
     * To calculate the monthly payment, we first need to calculate something called the Rate Factor. We're going to be
     * using simple interest. The Rate Factor for simple interest is calculated by the following formula:
     *
     *
     * R = 1 + (r * d / y)
     *
     * @param interestRate
     *            (r)
     * @param daysInPeriod
     *            (d)
     * @param daysInYear
     *            (y)
     */
    public static BigDecimal rateFactor(final BigDecimal interestRate, final Long daysInPeriod, final Integer daysInYear,
            final MathContext mc) {
        final BigDecimal daysPeriod = BigDecimal.valueOf(daysInPeriod);
        final BigDecimal daysYear = BigDecimal.valueOf(daysInYear);

        return BigDecimal.ONE.add(interestRate.multiply(daysPeriod.divide(daysYear, mc), mc), mc);
    }

}
