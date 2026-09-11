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
package org.apache.fineract.integrationtests.client.feign.modules;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.models.PostFixedDepositProductsChartSlabs;
import org.apache.fineract.client.models.PostRecurringDepositProductsChartSlabs;

/**
 * Constants for fixed and recurring deposit products and accounts. The types shared with plain savings -- interest
 * compounding, posting, calculation, days-in-year and period frequency -- live in {@link SavingsTestData}.
 */
public final class DepositTestData {

    public static final String MONTH_DAY_FORMAT = FeignTestConstants.MONTH_DAY_FORMAT;
    public static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("100000");
    public static final BigDecimal RECURRING_DEPOSIT_AMOUNT = new BigDecimal("2000");

    private DepositTestData() {}

    public static final class PreClosurePenalInterestOnType {

        public static final int WHOLE_TERM = 1;
        public static final int TILL_PREMATURE_WITHDRAWAL = 2;

        private PreClosurePenalInterestOnType() {}
    }

    /** Value of {@code onAccountClosureId} on a premature close, and of {@code maturityInstructionId} on an account. */
    public static final class AccountClosureType {

        public static final int WITHDRAW_DEPOSIT = 100;
        public static final int TRANSFER_TO_SAVINGS = 200;
        public static final int REINVEST = 300;
        public static final int REINVEST_PRINCIPAL_ONLY = 400;

        private AccountClosureType() {}
    }

    /**
     * The interest chart slabs of the RestAssured {@code FixedDepositProductHelper}, reproduced rate for rate. The last
     * slab of each chart deliberately has no upper bound, which is how a chart covers every longer term or larger
     * amount.
     */
    /**
     * Whether the chart is grouped by amount first. The RestAssured helper set this as a side effect of building the
     * amount-primary slabs, so no test ever names it, but a chart whose slabs carry no period is rejected without it.
     */
    public static boolean isPrimaryGroupingByAmount(String chartToBePicked) {
        return "amount".equals(chartToBePicked) || "amount_period".equals(chartToBePicked);
    }

    public static List<PostFixedDepositProductsChartSlabs> chartSlabsFor(String chartToBePicked) {
        return switch (chartToBePicked) {
            case "period" -> periodRangeChartSlabs();
            case "amount" -> amountRangeChartSlabs();
            case "period_amount" -> periodAndAmountRangeChartSlabs();
            case "amount_period" -> amountAndPeriodRangeChartSlabs();
            default -> throw new IllegalArgumentException("Unknown interest chart " + chartToBePicked);
        };
    }

    public static List<PostFixedDepositProductsChartSlabs> periodRangeChartSlabs() {
        return List.of(periodSlab("First", 1, 6, "5"), periodSlab("Second", 7, 12, "6"), periodSlab("Third", 13, 18, "7"),
                periodSlab("Fourth", 19, null, "8"));
    }

    public static List<PostFixedDepositProductsChartSlabs> amountRangeChartSlabs() {
        return List.of(amountSlab("First", "1", "5000", "5"), amountSlab("Third", "5001", "10000", "7"),
                amountSlab("Fourth", "10001", null, "8"));
    }

    public static List<PostFixedDepositProductsChartSlabs> periodAndAmountRangeChartSlabs() {
        return List.of(slab("First", 1, 6, "1", "5000", "5"), slab("First", 1, 6, "5001", null, "6"),
                slab("Second", 7, 12, "1", "5000", "6"), slab("Second", 7, 12, "5001", null, "7"), slab("Third", 13, 18, "1", "5000", "7"),
                slab("Third", 13, 18, "5001", null, "8"), slab("Fourth", 19, null, "1", "5000", "8"),
                slab("Fourth", 19, null, "5001", null, "9"));
    }

    public static List<PostFixedDepositProductsChartSlabs> amountAndPeriodRangeChartSlabs() {
        return List.of(slab("First", 1, 6, "1", "5000", "5"), slab("Second", 7, null, "1", "5000", "6"),
                slab("Third", 1, 6, "5001", null, "7"), slab("Fourth", 7, null, "5001", null, "8"));
    }

    private static PostFixedDepositProductsChartSlabs periodSlab(String description, Integer fromPeriod, Integer toPeriod, String rate) {
        return slab(description, fromPeriod, toPeriod, null, null, rate);
    }

    private static PostFixedDepositProductsChartSlabs amountSlab(String description, String amountFrom, String amountTo, String rate) {
        return new PostFixedDepositProductsChartSlabs()//
                .description(description)//
                .amountRangeFrom(new BigDecimal(amountFrom))//
                .amountRangeTo(amountTo == null ? null : new BigDecimal(amountTo))//
                .annualInterestRate(Double.valueOf(rate))//
                .locale(SavingsTestData.LOCALE);
    }

    private static PostFixedDepositProductsChartSlabs slab(String description, Integer fromPeriod, Integer toPeriod, String amountFrom,
            String amountTo, String rate) {
        return new PostFixedDepositProductsChartSlabs()//
                .description(description)//
                .periodType(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .fromPeriod(fromPeriod)//
                .toPeriod(toPeriod)//
                .amountRangeFrom(amountFrom == null ? null : new BigDecimal(amountFrom))//
                .amountRangeTo(amountTo == null ? null : new BigDecimal(amountTo))//
                .annualInterestRate(Double.valueOf(rate))//
                .locale(SavingsTestData.LOCALE);
    }

    /**
     * The recurring deposit charts, whose slab values are identical to the fixed deposit ones -- only the generated
     * type differs, so the rates are kept in one place and mapped across.
     */
    public static List<PostRecurringDepositProductsChartSlabs> recurringChartSlabsFor(String chartToBePicked) {
        return chartSlabsFor(chartToBePicked).stream().map(DepositTestData::asRecurringSlab).toList();
    }

    private static PostRecurringDepositProductsChartSlabs asRecurringSlab(PostFixedDepositProductsChartSlabs slab) {
        return new PostRecurringDepositProductsChartSlabs()//
                .description(slab.getDescription())//
                .periodType(slab.getPeriodType())//
                .fromPeriod(slab.getFromPeriod())//
                .toPeriod(slab.getToPeriod())//
                .amountRangeFrom(slab.getAmountRangeFrom())//
                .amountRangeTo(slab.getAmountRangeTo())//
                .annualInterestRate(slab.getAnnualInterestRate())//
                .locale(slab.getLocale());
    }
}
