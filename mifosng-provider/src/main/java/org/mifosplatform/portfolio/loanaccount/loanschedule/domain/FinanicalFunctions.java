/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

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