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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

public class FinanicalFunctions {

    /**
     * PMT calculates a fixed monthly payment to be paid by borrower every
     * 'period' to ensure loan is paid off in full (with interest).
     * 
     * This monthly payment c depends upon the monthly interest rate r
     * (expressed as a fraction, not a percentage, i.e., divide the quoted
     * yearly percentage rate by 100 and by 12 to obtain the monthly interest
     * rate), the number of monthly payments N called the loan's term, and the
     * amount borrowed P known as the loan's principal; c is given by the
     * formula:
     * 
     * c = (r / (1 - (1 + r)^-N))P
     * 
     * @param interestRateFraction
     * @param numberOfPayments
     * @param principal
     * @param futureValue
     * @param type
     */
    public static double pmt(final double interestRateFraction, final double numberOfPayments, final double principal,
            final double futureValue, final boolean type) {
        double payment = 0;
        if (interestRateFraction == 0) {
            payment = -1 * (futureValue + principal) / numberOfPayments;
        } else {
            final double r1 = interestRateFraction + 1;
            payment = (futureValue + principal * Math.pow(r1, numberOfPayments)) * interestRateFraction
                    / ((type ? r1 : 1) * (1 - Math.pow(r1, numberOfPayments)));
        }
        return payment;
    }

    public static int nop(final double interestRateFraction, final double emiAmount, final double principal, final double futureValue,
            final boolean type) {
        double numberOfPayments = 0;
        if (interestRateFraction == 0) {
            numberOfPayments = ((Double) (-1 * (futureValue + principal) / emiAmount)).intValue();
        } else {
            final double r1 = interestRateFraction + 1;
            numberOfPayments = (futureValue + principal * Math.pow(r1, emiAmount)) * interestRateFraction
                    / ((type ? r1 : 1) * (1 - Math.pow(r1, emiAmount)));
        }
        return Double.valueOf(numberOfPayments).intValue();
    }
}