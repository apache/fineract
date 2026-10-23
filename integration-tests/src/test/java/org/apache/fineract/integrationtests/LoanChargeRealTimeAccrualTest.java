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
package org.apache.fineract.integrationtests;

import static org.apache.fineract.integrationtests.client.feign.modules.LoanChargeAccrualTestValidators.installmentNumberOf;
import static org.apache.fineract.integrationtests.client.feign.modules.LoanChargeAccrualTestValidators.installmentWithFeeChargesDue;
import static org.apache.fineract.integrationtests.client.feign.modules.LoanChargeAccrualTestValidators.verifyAccrual;
import static org.apache.fineract.integrationtests.client.feign.modules.LoanChargeAccrualTestValidators.verifyAccrualAdjustmentCount;
import static org.apache.fineract.integrationtests.client.feign.modules.LoanChargeAccrualTestValidators.verifyAccrualCount;
import static org.apache.fineract.integrationtests.client.feign.modules.LoanChargeAccrualTestValidators.verifyAccrualFeeTotal;
import static org.apache.fineract.integrationtests.client.feign.modules.LoanChargeAccrualTestValidators.verifyChargeAccrual;
import static org.apache.fineract.integrationtests.client.feign.modules.LoanChargeAccrualTestValidators.verifyConfiguration;
import static org.apache.fineract.integrationtests.client.feign.modules.LoanChargeAccrualTestValidators.verifyInterestOnlyAccrual;
import static org.apache.fineract.integrationtests.client.feign.modules.LoanChargeAccrualTestValidators.verifyNetChargeAccruals;
import static org.apache.fineract.integrationtests.client.feign.modules.LoanChargeAccrualTestValidators.verifySinglePenaltyCharge;
import static org.apache.fineract.integrationtests.client.feign.modules.LoanChargeAccrualTestValidators.verifyWaiveChargesTransaction;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.externalevents.LoanTransactionBusinessEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class LoanChargeRealTimeAccrualTest extends FeignLoanTestBase {

    private static final String REAL_TIME = "real-time";
    private static final String EOD = "eod";
    private static final String DISBURSEMENT_DATE = "01 January 2024";
    private static final String PRE_ACTIVATION_DISBURSEMENT_DATE = "05 January 2024";
    private static final double PRINCIPAL = 1000.0;
    private static final String APPLY_PENALTY_TO_OVERDUE_LOANS_JOB = "Apply penalty to overdue loans";
    private static final String ADD_PERIODIC_ACCRUAL_TRANSACTIONS_JOB = "Add Periodic Accrual Transactions";
    private static final String ACCRUAL_CREATED_EVENT = "LoanAccrualTransactionCreatedBusinessEvent";

    private Long loanId;

    @AfterEach
    public void resetConfigurations() {
        if (globalConfigurationHelper.getGlobalConfigurationByName(GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME) != null) {
            globalConfigurationHelper.updateConfigurationStringValue(GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME, EOD);
        }
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_IMMEDIATE_CHARGE_ACCRUAL_POST_MATURITY, false);
    }

    @Test
    public void configurationIsSeededWithEodDefaultAndAcceptsRealTime() {
        GlobalConfigurationPropertyData seeded = globalConfigurationHelper
                .getGlobalConfigurationByName(GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME);
        // seeded as EOD and not rewritten on upgrade; an update through the API stores the canonical eod
        verifyConfiguration(seeded, GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME, seeded.getStringValue(), true, false);
        assertTrue(EOD.equalsIgnoreCase(seeded.getStringValue()), "seeded stringValue " + seeded.getStringValue());

        globalConfigurationHelper.updateConfigurationStringValue(GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME, "Real-Time");

        verifyConfiguration(globalConfigurationHelper.getGlobalConfigurationByName(GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME),
                GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME, REAL_TIME, true, false);
    }

    @Test
    public void eodDefault_feeIsAccruedByCobOnDueDateOnly() {
        createProgressiveLoan();
        runAt("10 January 2024", () -> {
            addCharge(loanId, false, 10.0, "15 January 2024");

            verifyTransactions(loanId, transaction(PRINCIPAL, "Disbursement", DISBURSEMENT_DATE));
            verifyAccrualCount(getLoanDetails(loanId), 0, "EOD, after add-charge");
        });
        runAt("16 January 2024", () -> {
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "EOD, after COB of 15 January");
            verifyAccrual(loan, 10.0, "15 January 2024", 10.0, 0.0);
        });
    }

    @Test
    public void realTime_singleFutureDatedFeeIsAccruedAtPosting() {
        createProgressiveLoan();
        runAt("10 January 2024", () -> {
            setPlChargeAccrualTime(REAL_TIME);
            Long feeId = addCharge(loanId, false, 10.0, "15 January 2024");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, right after add-charge, no COB");
            GetLoansLoanIdTransactions accrual = verifyChargeAccrual(loan, feeId, 10.0, "10 January 2024", 10.0, 0.0, 1);
            verifyTRJournalEntries(accrual.getId(), debit(getAccounts().getFeeReceivableAccount(), 10.0),
                    credit(getAccounts().getFeeIncomeAccount(), 10.0));
        });
        runAt("16 January 2024", () -> {
            executeInlineCOB(loanId);
            verifyAccrualCount(getLoanDetails(loanId), 1, "Real-Time, after COB of 15 January (due date)");
        });
        runAt("01 February 2024", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, after COB of 31 January (maturity)");
            verifyAccrualFeeTotal(loan, 10.0);
        });
    }

    @Test
    public void realTime_multipleChargesSameDayCreateOneAccrualEach() {
        createProgressiveLoan();
        runAt("10 January 2024", () -> {
            setPlChargeAccrualTime(REAL_TIME);
            Long fee10Id = addCharge(loanId, false, 10.0, "10 January 2024");
            Long fee15Id = addCharge(loanId, false, 15.0, "10 January 2024");
            Long penalty5Id = addCharge(loanId, true, 5.0, "10 January 2024");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 3, "Real-Time, three charges added on 10 January");
            GetLoansLoanIdTransactions fee10Accrual = verifyChargeAccrual(loan, fee10Id, 10.0, "10 January 2024", 10.0, 0.0, 1);
            GetLoansLoanIdTransactions fee15Accrual = verifyChargeAccrual(loan, fee15Id, 15.0, "10 January 2024", 15.0, 0.0, 1);
            GetLoansLoanIdTransactions penaltyAccrual = verifyChargeAccrual(loan, penalty5Id, 5.0, "10 January 2024", 0.0, 5.0, 1);
            verifyTRJournalEntries(fee10Accrual.getId(), debit(getAccounts().getFeeReceivableAccount(), 10.0),
                    credit(getAccounts().getFeeIncomeAccount(), 10.0));
            verifyTRJournalEntries(fee15Accrual.getId(), debit(getAccounts().getFeeReceivableAccount(), 15.0),
                    credit(getAccounts().getFeeIncomeAccount(), 15.0));
            verifyTRJournalEntries(penaltyAccrual.getId(), debit(getAccounts().getPenaltyReceivableAccount(), 5.0),
                    credit(getAccounts().getPenaltyIncomeAccount(), 5.0));
        });
        runAt("11 January 2024", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 3, "Real-Time, after COB of 10 January");
            verifyAccrualAdjustmentCount(loan, 0, "Real-Time, after COB of 10 January");
        });
    }

    @Test
    public void realTime_sameDaySameAmountChargesAreNotDeduplicated() {
        createProgressiveLoan();
        runAt("10 January 2024", () -> {
            setPlChargeAccrualTime(REAL_TIME);
            Long firstFeeId = addCharge(loanId, false, 10.0, "10 January 2024");
            Long secondFeeId = addCharge(loanId, false, 10.0, "10 January 2024");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 2, "Real-Time, two equal fees added on 10 January");
            verifyChargeAccrual(loan, firstFeeId, 10.0, "10 January 2024", 10.0, 0.0, 1);
            verifyChargeAccrual(loan, secondFeeId, 10.0, "10 January 2024", 10.0, 0.0, 1);
        });
        runAt("11 January 2024", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 2, "Real-Time, after COB of 10 January");
            verifyAccrualAdjustmentCount(loan, 0, "Real-Time, after COB of 10 January");
            verifyAccrualFeeTotal(loan, 20.0);
        });
    }

    @Test
    public void realTime_waiverBeforeDueDateOfAccruedFeeIsRecognized() {
        createProgressiveLoan();
        Long[] feeId = new Long[1];
        runAt("10 January 2024", () -> {
            setPlChargeAccrualTime(REAL_TIME);
            feeId[0] = addCharge(loanId, false, 10.0, "20 January 2024");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, after add-charge");
            verifyChargeAccrual(loan, feeId[0], 10.0, "10 January 2024", 10.0, 0.0, 1);
        });
        runAt("12 January 2024", () -> {
            waiveLoanCharge(loanId, feeId[0], LoanRequestBuilders.waiveLoanCharge());

            GetLoansLoanIdTransactions waiver = verifyWaiveChargesTransaction(getLoanDetails(loanId), 10.0, "12 January 2024", 10.0, 0.0,
                    0.0);
            verifyTRJournalEntries(waiver.getId(), debit(getAccounts().getWrittenOffAccount(), 10.0),
                    credit(getAccounts().getFeeReceivableAccount(), 10.0));
        });
        runAt("13 January 2024", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, after COB of 12 January");
            verifyAccrualAdjustmentCount(loan, 0, "Real-Time, after COB of 12 January");
        });
        runAt("21 January 2024", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, after COB of 20 January (waived fee's due date)");
            verifyAccrualAdjustmentCount(loan, 0, "Real-Time, after COB of 20 January");
        });
    }

    @Test
    public void realTime_backdatedFeeIsAccruedOnItsDueDate() {
        createProgressiveLoan();
        runAt("10 January 2024", () -> {
            setPlChargeAccrualTime(REAL_TIME);
            Long feeId = addCharge(loanId, false, 10.0, "05 January 2024");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, backdated fee added");
            verifyChargeAccrual(loan, feeId, 10.0, "05 January 2024", 10.0, 0.0, 1);
        });
        runAt("11 January 2024", () -> {
            executeInlineCOB(loanId);
            verifyAccrualCount(getLoanDetails(loanId), 1, "Real-Time, after COB of 10 January");
        });
    }

    @Test
    public void realTimeWithPostMaturityFlag_postMaturityFeeIsAccruedOnce() {
        createProgressiveLoan();
        runAt("05 February 2024", () -> {
            executeInlineCOB(loanId);
            setPlChargeAccrualTime(REAL_TIME);
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_IMMEDIATE_CHARGE_ACCRUAL_POST_MATURITY,
                    true);
            Long feeId = addCharge(loanId, false, 20.0, "05 February 2024");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time + post-maturity flag, after add-charge");
            verifyChargeAccrual(loan, feeId, 20.0, "05 February 2024", 20.0, 0.0, 2);
        });
        runAt("06 February 2024", () -> {
            executeInlineCOB(loanId);
            verifyAccrualCount(getLoanDetails(loanId), 1, "Real-Time + post-maturity flag, after COB of 05 February");
        });
    }

    @Test
    public void realTime_overduePenaltyAppliedByJobIsAccruedAtApplication() {
        Long overdueChargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanOverdueFee(5.0)).getResourceId();
        createProgressiveLoan(customizeProduct(onePeriod30DaysPeriodicAccrualWithAdvancedAllocation(),
                p -> p.charges(List.of(new LoanProductChargeData().id(overdueChargeId)))));
        Long[] penaltyId = new Long[1];
        runAt("03 February 2024", () -> {
            setPlChargeAccrualTime(REAL_TIME);
            schedulerHelper.executeAndAwaitJob(APPLY_PENALTY_TO_OVERDUE_LOANS_JOB);

            GetLoansLoanIdChargesChargeIdResponse penalty = verifySinglePenaltyCharge(getLoanCharges(loanId), 5.0);
            penaltyId[0] = penalty.getId();
            String penaltyDueDate = dateTimeFormatter.format(penalty.getDueDate());
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, right after the overdue-penalty job applied the penalty, no COB");
            verifyChargeAccrual(loan, penaltyId[0], 5.0, penaltyDueDate, 0.0, 5.0, null);
        });
        runAt("04 February 2024", () -> {
            executeInlineCOB(loanId);
            verifyAccrualCount(getLoanDetails(loanId), 1, "Real-Time, after COB of 03 February");
        });
    }

    @Test
    public void eodToRealTimeFlip_cobCatchesEarlierChargeAndNewChargeAccruesImmediately() {
        createProgressiveLoan();
        Long[] eodFeeId = new Long[1];
        runAt("10 January 2024", () -> {
            eodFeeId[0] = addCharge(loanId, false, 10.0, "20 January 2024");
            verifyAccrualCount(getLoanDetails(loanId), 0, "EOD, after add-charge");
        });
        runAt("11 January 2024", () -> {
            setPlChargeAccrualTime(REAL_TIME);
            Long realTimeFeeId = addCharge(loanId, false, 7.0, "11 January 2024");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, after add-charge of 7.0");
            verifyChargeAccrual(loan, realTimeFeeId, 7.0, "11 January 2024", 7.0, 0.0, 1);
        });
        runAt("12 January 2024", () -> {
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 2, "Real-Time, after COB of 11 January");
            verifyChargeAccrual(loan, eodFeeId[0], 10.0, "11 January 2024", 10.0, 0.0, null);
            verifyAccrualFeeTotal(loan, 17.0);
        });
    }

    @Test
    public void realTime_cumulativeLoanKeepsEodAccrual() {
        createLoan(onePeriod30DaysPeriodicAccrual(), false);
        runAt("10 January 2024", () -> {
            setPlChargeAccrualTime(REAL_TIME);
            addCharge(loanId, false, 10.0, "15 January 2024");
            verifyAccrualCount(getLoanDetails(loanId), 0, "Real-Time, cumulative loan, after add-charge");
        });
        runAt("16 January 2024", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, cumulative loan, after COB of 15 January");
            verifyAccrual(loan, 10.0, "15 January 2024", 10.0, 0.0);
        });
    }

    @Test
    public void realTime_cashAccountingProgressiveLoanHasNoAccrual() {
        createProgressiveLoan(onePeriod30DaysPeriodicAccrualWithAdvancedAllocation().accountingRule(2));
        runAt("10 January 2024", () -> {
            setPlChargeAccrualTime(REAL_TIME);
            addCharge(loanId, false, 10.0, "10 January 2024");
            verifyAccrualCount(getLoanDetails(loanId), 0, "Real-Time, cash accounting, after add-charge");
        });
        runAt("11 January 2024", () -> {
            executeInlineCOB(loanId);
            verifyAccrualCount(getLoanDetails(loanId), 0, "Real-Time, cash accounting, after COB of 10 January");
        });
    }

    @Test
    public void realTime_feeAddedAfterApprovalBeforeDisbursement_isAccruedByItsDueDate() {
        Long feeId = createPreActivationLoanWithFee(REAL_TIME, true);
        runAt("16 January 2024", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, fee added after approval, after COB of 15 January (due date)");
            verifyChargeAccrual(loan, feeId, 10.0, 10.0, 0.0, 1);
        });
        runAt("05 February 2024", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, fee added after approval, after COB of 04 February (maturity)");
            verifyNetChargeAccruals(loan, 10.0, 0.0, "Real-Time, fee added after approval, after maturity COB");
        });
    }

    @Test
    public void realTime_feeAddedAtApplicationBeforeApproval_isAccruedByItsDueDate() {
        Long feeId = createPreActivationLoanWithFee(REAL_TIME, false);
        runAt("16 January 2024", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, fee added at application, after COB of 15 January (due date)");
            verifyChargeAccrual(loan, feeId, 10.0, 10.0, 0.0, 1);
        });
        runAt("05 February 2024", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, fee added at application, after COB of 04 February (maturity)");
            verifyNetChargeAccruals(loan, 10.0, 0.0, "Real-Time, fee added at application, after maturity COB");
        });
    }

    @Test
    public void eod_feeAddedAfterApprovalBeforeDisbursement_isAccruedOnItsDueDate() {
        Long feeId = createPreActivationLoanWithFee(EOD, true);
        runAt("16 January 2024", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "EOD, fee added after approval, after COB of 15 January (due date)");
            verifyChargeAccrual(loan, feeId, 10.0, "15 January 2024", 10.0, 0.0, null);
        });
    }

    @Test
    public void realTime_multiInstallmentFeeAndOverduePenaltyKeepTheirInstallmentAccrualsAcrossReprocess() {
        Long overdueChargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanOverdueFee(5.0)).getResourceId();
        createProgressiveLoan(
                customizeProduct(onePeriod30DaysPeriodicAccrualWithAdvancedAllocation(),
                        p -> p.numberOfRepayments(3).interestRatePerPeriod(1.0)
                                .charges(List.of(new LoanProductChargeData().id(overdueChargeId)))),
                3, r -> r.interestRatePerPeriod(BigDecimal.ONE));
        Long[] feeId = new Long[1];
        Long[] penaltyId = new Long[1];
        String[] penaltyDueDate = new String[1];
        Integer[] penaltyInstallment = new Integer[1];
        runAt("10 January 2024", () -> {
            setPlChargeAccrualTime(REAL_TIME);
            feeId[0] = addCharge(loanId, false, 15.0, "15 February 2024");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyChargeAccrual(loan, feeId[0], 15.0, "10 January 2024", 15.0, 0.0, 2);
        });
        runAt("03 February 2024", () -> {
            schedulerHelper.executeAndAwaitJob(APPLY_PENALTY_TO_OVERDUE_LOANS_JOB);

            GetLoansLoanIdChargesChargeIdResponse penalty = verifySinglePenaltyCharge(getLoanCharges(loanId), 5.0);
            penaltyId[0] = penalty.getId();
            penaltyDueDate[0] = dateTimeFormatter.format(penalty.getDueDate());
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            penaltyInstallment[0] = installmentNumberOf(loan, penalty.getDueDate());
            verifyChargeAccrual(loan, penaltyId[0], 5.0, penaltyDueDate[0], 0.0, 5.0, penaltyInstallment[0]);
            verifyNetChargeAccruals(loan, 15.0, 5.0, "Real-Time, after fee and overdue penalty were posted");
        });
        runAt("05 February 2024", () -> {
            Long repaymentId = addRepayment(loanId, repayment(100.0, "20 January 2024"));
            undoRepayment(loanId, repaymentId, "05 February 2024");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyChargeAccrual(loan, feeId[0], 15.0, "10 January 2024", 15.0, 0.0, 2);
            verifyChargeAccrual(loan, penaltyId[0], 5.0, penaltyDueDate[0], 0.0, 5.0, penaltyInstallment[0]);
            verifyNetChargeAccruals(loan, 15.0, 5.0, "Real-Time, after the backdated repayment was undone (reprocess)");
        });
        runAt("06 February 2024", () -> {
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyChargeAccrual(loan, feeId[0], 15.0, "10 January 2024", 15.0, 0.0, 2);
            verifyChargeAccrual(loan, penaltyId[0], 5.0, penaltyDueDate[0], 0.0, 5.0, penaltyInstallment[0]);
            verifyNetChargeAccruals(loan, 15.0, 5.0, "Real-Time, after COB of 05 February");
        });
        runAt("07 February 2024", () -> {
            waiveLoanCharge(loanId, penaltyId[0], LoanRequestBuilders.waiveLoanCharge());
            Long repaymentId = addRepayment(loanId, repayment(100.0, "20 January 2024"));
            undoRepayment(loanId, repaymentId, "07 February 2024");
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyWaiveChargesTransaction(loan, 5.0, penaltyDueDate[0], 0.0, 5.0, 0.0);
            verifyChargeAccrual(loan, penaltyId[0], 5.0, penaltyDueDate[0], 0.0, 5.0, penaltyInstallment[0]);
            verifyNetChargeAccruals(loan, 15.0, 5.0, "Real-Time, after the penalty was waived and the loan reprocessed");
        });
    }

    @Test
    public void realTime_waivedPostMaturityFeeStaysRecognizedOnceAcrossReprocess() {
        createProgressiveLoan();
        Long[] feeId = new Long[1];
        runAt("05 February 2024", () -> {
            executeInlineCOB(loanId);
            setPlChargeAccrualTime(REAL_TIME);
            feeId[0] = addCharge(loanId, false, 20.0, "05 February 2024");

            verifyChargeAccrual(getLoanDetails(loanId), feeId[0], 20.0, "05 February 2024", 20.0, 0.0, 2);
        });
        runAt("06 February 2024", () -> {
            waiveLoanCharge(loanId, feeId[0], LoanRequestBuilders.waiveLoanCharge());
            Long repaymentId = addRepayment(loanId, repayment(10.0, "06 February 2024"));
            undoRepayment(loanId, repaymentId, "06 February 2024");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyWaiveChargesTransaction(loan, 20.0, "05 February 2024", 20.0, 0.0, 0.0);
            verifyAccrualAdjustmentCount(loan, 0, "Real-Time, post-maturity fee waived, loan reprocessed");
            verifyNetChargeAccruals(loan, 20.0, 0.0, "Real-Time, post-maturity fee waived, loan reprocessed");
        });
        runAt("07 February 2024", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualAdjustmentCount(loan, 0, "Real-Time, after COB of 06 February");
            verifyNetChargeAccruals(loan, 20.0, 0.0, "Real-Time, after COB of 06 February");
        });
    }

    @Test
    public void realTime_cobPostsSameInterestAccrualOnDayOfChargeAccrualAsControlLoan() {
        createInterestBearingThreeInstallmentLoan();
        Long controlLoanId = loanId;
        createInterestBearingThreeInstallmentLoan();
        Long chargedLoanId = loanId;
        Long[] feeId = new Long[1];
        runAt("10 January 2024", () -> {
            executeInlineCOB(List.of(controlLoanId, chargedLoanId));
            setPlChargeAccrualTime(REAL_TIME);
            feeId[0] = addCharge(chargedLoanId, false, 10.0, "10 January 2024");

            verifyChargeAccrual(getLoanDetails(chargedLoanId), feeId[0], 10.0, "10 January 2024", 10.0, 0.0, 1);
        });
        runAt("11 January 2024", () -> {
            executeInlineCOB(List.of(controlLoanId, chargedLoanId));

            double controlInterest = verifyInterestOnlyAccrual(getLoanDetails(controlLoanId), "10 January 2024");
            GetLoansLoanIdResponse chargedLoan = getLoanDetails(chargedLoanId);
            double chargedInterest = verifyInterestOnlyAccrual(chargedLoan, "10 January 2024");
            assertEquals(controlInterest, chargedInterest, "Interest accrued on 10 January with and without a real-time charge accrual");
            verifyChargeAccrual(chargedLoan, feeId[0], 10.0, "10 January 2024", 10.0, 0.0, 1);
            verifyAccrualAdjustmentCount(chargedLoan, 0, "Real-Time, interest-bearing loan, after COB of 10 January");
            verifyNetChargeAccruals(chargedLoan, 10.0, 0.0, "Real-Time, interest-bearing loan, after COB of 10 January");
        });
    }

    @Test
    public void realTime_periodicAccrualJobAddsNoChargeAccrualAfterRealTimeAccrual() {
        createProgressiveLoan();
        Long[] chargeIds = new Long[3];
        runAt("10 January 2024", () -> {
            setPlChargeAccrualTime(REAL_TIME);
            chargeIds[0] = addCharge(loanId, false, 10.0, "10 January 2024");
            chargeIds[1] = addCharge(loanId, false, 15.0, "10 January 2024");
            chargeIds[2] = addCharge(loanId, true, 5.0, "10 January 2024");

            verifyAccrualCount(getLoanDetails(loanId), 3, "Real-Time, three charges added on 10 January");
        });
        runAt("11 January 2024", () -> {
            schedulerHelper.executeAndAwaitJob(ADD_PERIODIC_ACCRUAL_TRANSACTIONS_JOB);

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 3, "Real-Time, after the periodic accrual job");
            verifyAccrualAdjustmentCount(loan, 0, "Real-Time, after the periodic accrual job");
            verifyChargeAccrual(loan, chargeIds[0], 10.0, "10 January 2024", 10.0, 0.0, 1);
            verifyChargeAccrual(loan, chargeIds[1], 15.0, "10 January 2024", 15.0, 0.0, 1);
            verifyChargeAccrual(loan, chargeIds[2], 5.0, "10 January 2024", 0.0, 5.0, 1);
            verifyNetChargeAccruals(loan, 25.0, 5.0, "Real-Time, after the periodic accrual job");
        });
    }

    @Test
    public void realTime_backdatedPostMaturityFeeIsAccruedOnceWithoutAdjustment() {
        createProgressiveLoan();
        Long[] feeId = new Long[1];
        runAt("10 March 2024", () -> {
            executeInlineCOB(loanId);
            setPlChargeAccrualTime(REAL_TIME);
            feeId[0] = addCharge(loanId, false, 10.0, "05 March 2024");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, back-dated post-maturity fee added");
            verifyChargeAccrual(loan, feeId[0], 10.0, "05 March 2024", 10.0, 0.0, 2);
        });
        runAt("11 March 2024", () -> {
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, after COB of 10 March");
            verifyAccrualAdjustmentCount(loan, 0, "Real-Time, after COB of 10 March");
            verifyChargeAccrual(loan, feeId[0], 10.0, "05 March 2024", 10.0, 0.0, 2);
        });
    }

    @Test
    public void realTime_chargeAccrualRaisesAccrualCreatedBusinessEvent() {
        createProgressiveLoan();
        externalEventHelper.enableBusinessEvent(ACCRUAL_CREATED_EVENT);
        try {
            runAt("10 January 2024", () -> {
                setPlChargeAccrualTime(REAL_TIME);
                deleteAllExternalEvents();

                addCharge(loanId, false, 10.0, "15 January 2024");

                verifyBusinessEvents(
                        new LoanTransactionBusinessEvent(ACCRUAL_CREATED_EVENT, "10 January 2024", 10.0, 0.0, 0.0, 0.0, 10.0, 0.0));
            });
        } finally {
            externalEventHelper.disableBusinessEvent(ACCRUAL_CREATED_EVENT);
        }
    }

    @Test
    public void realTime_feeAccrualStaysMappedToTheChargeScheduleInstallmentAcrossReschedule() {
        createProgressiveLoan(customizeProduct(onePeriod30DaysPeriodicAccrualWithAdvancedAllocation(), p -> p.numberOfRepayments(3)), 3,
                null);
        Long[] feeId = new Long[1];
        Integer[] chargeInstallment = new Integer[1];
        runAt("10 January 2024", () -> {
            setPlChargeAccrualTime(REAL_TIME);
            feeId[0] = addCharge(loanId, false, 10.0, "15 February 2024");

            verifyChargeAccrual(getLoanDetails(loanId), feeId[0], 10.0, "10 January 2024", 10.0, 0.0, 2);
        });
        runAt("11 January 2024", () -> {
            createAndApproveReschedule(loanId, "11 January 2024", "31 January 2024", "20 February 2024");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            chargeInstallment[0] = installmentWithFeeChargesDue(loan, 10.0);
            verifyChargeAccrual(loan, feeId[0], 10.0, "10 January 2024", 10.0, 0.0, chargeInstallment[0]);
        });
        runAt("12 January 2024", () -> {
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            verifyAccrualCount(loan, 1, "Real-Time, after reschedule and COB of 11 January");
            verifyAccrualAdjustmentCount(loan, 0, "Real-Time, after reschedule and COB of 11 January");
            verifyChargeAccrual(loan, feeId[0], 10.0, "10 January 2024", 10.0, 0.0, chargeInstallment[0]);
            verifyNetChargeAccruals(loan, 10.0, 0.0, "Real-Time, after reschedule and COB of 11 January");
        });
    }

    private void createInterestBearingThreeInstallmentLoan() {
        createProgressiveLoan(customizeProduct(onePeriod30DaysPeriodicAccrualWithAdvancedAllocation(),
                p -> p.numberOfRepayments(3).interestRatePerPeriod(1.0)), 3, r -> r.interestRatePerPeriod(BigDecimal.ONE));
    }

    private Long createPreActivationLoanWithFee(String accrualTime, boolean addFeeAfterApproval) {
        Long[] feeId = new Long[1];
        runAt(DISBURSEMENT_DATE, () -> {
            if (REAL_TIME.equals(accrualTime)) {
                setPlChargeAccrualTime(REAL_TIME);
            }
            Long clientId = createClient();
            Long productId = createLoanProduct(onePeriod30DaysPeriodicAccrualWithAdvancedAllocation());
            loanId = applyForLoan(LoanRequestBuilders.applyLoanRequest(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL, 1,
                    r -> r.expectedDisbursementDate(PRE_ACTIVATION_DISBURSEMENT_DATE).transactionProcessingStrategyCode(
                            LoanTestData.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)));
            if (!addFeeAfterApproval) {
                feeId[0] = addCharge(loanId, false, 10.0, "15 January 2024");
            }
            approveLoan(loanId, approveLoanRequest(PRINCIPAL, DISBURSEMENT_DATE, PRE_ACTIVATION_DISBURSEMENT_DATE));
            if (addFeeAfterApproval) {
                feeId[0] = addCharge(loanId, false, 10.0, "15 January 2024");
            }
            verifyAccrualCount(getLoanDetails(loanId), 0, accrualTime + ", fee added before disbursement");
        });
        runAt(PRE_ACTIVATION_DISBURSEMENT_DATE, () -> {
            disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), PRE_ACTIVATION_DISBURSEMENT_DATE);
            verifyAccrualCount(getLoanDetails(loanId), 0, accrualTime + ", right after disbursement, fee due 15 January");
        });
        return feeId[0];
    }

    private void setPlChargeAccrualTime(String value) {
        globalConfigurationHelper.updateConfigurationStringValue(GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME, value);
        verifyConfiguration(globalConfigurationHelper.getGlobalConfigurationByName(GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME),
                GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME, value, true, false);
    }

    private void createProgressiveLoan() {
        createProgressiveLoan(onePeriod30DaysPeriodicAccrualWithAdvancedAllocation());
    }

    private void createProgressiveLoan(PostLoanProductsRequest product) {
        createLoan(product, true);
    }

    private void createProgressiveLoan(PostLoanProductsRequest product, int numberOfRepayments, Consumer<PostLoansRequest> customizer) {
        createLoan(product, true, numberOfRepayments, customizer);
    }

    private void createLoan(PostLoanProductsRequest product, boolean progressive) {
        createLoan(product, progressive, 1, null);
    }

    private void createLoan(PostLoanProductsRequest product, boolean progressive, int numberOfRepayments,
            Consumer<PostLoansRequest> customizer) {
        runAt(DISBURSEMENT_DATE, () -> {
            Long clientId = createClient();
            Long productId = createLoanProduct(product);
            loanId = applyAndApproveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL, numberOfRepayments, progressive ? r -> {
                r.transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
                if (customizer != null) {
                    customizer.accept(r);
                }
            } : customizer);
            disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), DISBURSEMENT_DATE);
        });
    }
}
