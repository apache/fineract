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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestType;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.api.ApiFacingEnum;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.accountdetails.service.AccountEnumerations;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.common.domain.MonthEndDueDateStrategy;
import org.apache.fineract.portfolio.common.service.CommonEnumerations;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.apache.fineract.portfolio.loanaccount.data.LoanInterestRecalculationData;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.fineract.portfolio.loanaccount.data.LoanSummaryData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeIncomeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeStrategy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeOffBehaviour;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSubStatus;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.springframework.jdbc.core.RowMapper;

final class LoanAccountDataMapper implements RowMapper<LoanAccountData> {

    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;

    LoanAccountDataMapper(DatabaseSpecificSQLGenerator sqlGenerator, DelinquencyReadPlatformService delinquencyReadPlatformService) {
        this.sqlGenerator = sqlGenerator;
        this.delinquencyReadPlatformService = delinquencyReadPlatformService;
    }

    public String loanSchema() {
        return "l.id as id, l.account_no as accountNo, l.external_id as externalId, l.fund_id as fundId, f.name as fundName,"
                + " l.loan_type_enum as loanType, l.loanpurpose_cv_id as loanPurposeId, cv.code_value as loanPurposeName,"
                + " lp.id as loanProductId, lp.name as loanProductName, lp.description as loanProductDescription,"
                + " lp.is_linked_to_floating_interest_rates as isLoanProductLinkedToFloatingRate, "
                + " lp.allow_variabe_installments as isvariableInstallmentsAllowed, "
                + " lp.allow_multiple_disbursals as multiDisburseLoan, lp.disallow_expected_disbursements as disallowExpectedDisbursements, "
                + " lp.can_define_fixed_emi_amount as canDefineInstallmentAmount,"
                + " c.id as clientId, c.account_no as clientAccountNo, c.display_name as clientName, c.office_id as clientOfficeId, c.external_id as clientExternalId,"
                + " g.id as groupId, g.account_no as groupAccountNo, g.display_name as groupName,"
                + " g.office_id as groupOfficeId, g.staff_id As groupStaffId , g.parent_id as groupParentId, (select mg.display_name from m_group mg where mg.id = g.parent_id) as centerName, "
                + " g.hierarchy As groupHierarchy , g.level_id as groupLevel, g.external_id As groupExternalId, "
                + " g.status_enum as statusEnum, g.activation_date as activationDate, "
                + " l.submittedon_date as submittedOnDate, sbu.username as submittedByUsername, sbu.firstname as submittedByFirstname, sbu.lastname as submittedByLastname,"
                + " l.rejectedon_date as rejectedOnDate, rbu.username as rejectedByUsername, rbu.firstname as rejectedByFirstname, rbu.lastname as rejectedByLastname,"
                + " l.withdrawnon_date as withdrawnOnDate, wbu.username as withdrawnByUsername, wbu.firstname as withdrawnByFirstname, wbu.lastname as withdrawnByLastname,"
                + " l.approvedon_date as approvedOnDate, abu.username as approvedByUsername, abu.firstname as approvedByFirstname, abu.lastname as approvedByLastname,"
                + " l.expected_disbursedon_date as expectedDisbursementDate, l.disbursedon_date as actualDisbursementDate, dbu.username as disbursedByUsername, dbu.firstname as disbursedByFirstname, dbu.lastname as disbursedByLastname,"
                + " l.closedon_date as closedOnDate, cbu.username as closedByUsername, cbu.firstname as closedByFirstname, cbu.lastname as closedByLastname, l.writtenoffon_date as writtenOffOnDate, "
                + " l.expected_firstrepaymenton_date as expectedFirstRepaymentOnDate, l.interest_calculated_from_date as interestChargedFromDate, l.maturedon_date as actualMaturityDate, l.expected_maturedon_date as expectedMaturityDate, "
                + " l.principal_amount_proposed as proposedPrincipal, l.principal_amount as principal, l.total_principal_derived as totalPrincipal, l.approved_principal as approvedPrincipal, l.net_disbursal_amount as netDisbursalAmount, l.arrearstolerance_amount as inArrearsTolerance, l.number_of_repayments as numberOfRepayments, l.repay_every as repaymentEvery, l.first_repayment_day_of_month as firstRepaymentDayOfMonth, l.second_repayment_day_of_month as secondRepaymentDayOfMonth,"
                + " l.grace_on_principal_periods as graceOnPrincipalPayment, l.recurring_moratorium_principal_periods as recurringMoratoriumOnPrincipalPeriods, l.grace_on_interest_periods as graceOnInterestPayment, l.grace_interest_free_periods as graceOnInterestCharged,l.grace_on_arrears_ageing as graceOnArrearsAgeing,"
                + " l.nominal_interest_rate_per_period as interestRatePerPeriod, l.annual_nominal_interest_rate as annualInterestRate, "
                + " l.repayment_period_frequency_enum as repaymentFrequencyType, l.interest_period_frequency_enum as interestRateFrequencyType, "
                + " l.fixed_length as fixedLength, "
                + " l.term_frequency as termFrequency, l.term_period_frequency_enum as termPeriodFrequencyType, "
                + " l.amortization_method_enum as amortizationType, l.interest_method_enum as interestType, l.is_equal_amortization as isEqualAmortization, l.interest_calculated_in_period_enum as interestCalculationPeriodType,"
                + " l.fixed_principal_percentage_per_installment fixedPrincipalPercentagePerInstallment, "
                + " l.allow_partial_period_interest_calcualtion as allowPartialPeriodInterestCalculation,"
                + " l.loan_status_id as lifeCycleStatusId, l.loan_transaction_strategy_code as transactionStrategyCode, "
                + " l.loan_transaction_strategy_name as transactionStrategyName, l.enable_installment_level_delinquency as enableInstallmentLevelDelinquency, "
                + " l.currency_code as currencyCode, l.currency_digits as currencyDigits, l.currency_multiplesof as inMultiplesOf, rc."
                + sqlGenerator.escape("name")
                + " as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, "
                + " l.loan_officer_id as loanOfficerId, s.display_name as loanOfficerName, "
                + " l.principal_disbursed_derived as principalDisbursed, l.capitalized_income_derived as totalCapitalizedIncome, l.capitalized_income_adjustment_derived as totalCapitalizedIncomeAdjustment, l.principal_repaid_derived as principalPaid,"
                + " l.principal_adjustments_derived as principalAdjustments, l.principal_writtenoff_derived as principalWrittenOff,"
                + " l.fee_adjustments_derived as feeAdjustments, l.penalty_adjustments_derived as penaltyAdjustments,"
                + " l.principal_outstanding_derived as principalOutstanding, l.interest_charged_derived as interestCharged,"
                + " l.interest_repaid_derived as interestPaid, l.interest_waived_derived as interestWaived,"
                + " l.interest_writtenoff_derived as interestWrittenOff, l.interest_outstanding_derived as interestOutstanding,"
                + " l.fee_charges_charged_derived as feeChargesCharged,"
                + " l.total_charges_due_at_disbursement_derived as feeChargesDueAtDisbursementCharged,"
                + " l.fee_charges_repaid_derived as feeChargesPaid, l.fee_charges_waived_derived as feeChargesWaived,"
                + " l.fee_charges_writtenoff_derived as feeChargesWrittenOff,"
                + " l.fee_charges_outstanding_derived as feeChargesOutstanding,"
                + " l.penalty_charges_charged_derived as penaltyChargesCharged,"
                + " l.penalty_charges_repaid_derived as penaltyChargesPaid," + " l.penalty_charges_waived_derived as penaltyChargesWaived,"
                + " l.penalty_charges_writtenoff_derived as penaltyChargesWrittenOff,"
                + " l.penalty_charges_outstanding_derived as penaltyChargesOutstanding, "
                + " l.total_expected_repayment_derived as totalExpectedRepayment, l.total_repayment_derived as totalRepayment,"
                + " l.total_expected_costofloan_derived as totalExpectedCostOfLoan, l.total_costofloan_derived as totalCostOfLoan,"
                + " l.total_waived_derived as totalWaived, l.total_writtenoff_derived as totalWrittenOff,"
                + " l.writeoff_reason_cv_id as writeoffReasonId, codev.code_value as writeoffReason,"
                + " l.total_outstanding_derived as totalOutstanding, l.total_overpaid_derived as totalOverpaid,"
                + " l.fixed_emi_amount as fixedEmiAmount, l.max_outstanding_loan_balance as outstandingLoanBalance,"
                + " l.loan_sub_status_id as loanSubStatusId, la.principal_overdue_derived as principalOverdue, l.is_fraud as isFraud, "
                + " la.interest_overdue_derived as interestOverdue, la.fee_charges_overdue_derived as feeChargesOverdue,"
                + " la.penalty_charges_overdue_derived as penaltyChargesOverdue, la.total_overdue_derived as totalOverdue,"
                + " la.overdue_since_date_derived as overdueSinceDate, "
                + " l.sync_disbursement_with_meeting as syncDisbursementWithMeeting,"
                + " l.loan_counter as loanCounter, l.loan_product_counter as loanProductCounter,"
                + " l.is_npa as isNPA, l.days_in_month_enum as daysInMonth, l.days_in_year_enum as daysInYear, "
                + " l.interest_recalculation_enabled as isInterestRecalculationEnabled, "
                + " lir.id as lirId, lir.loan_id as loanId, lir.compound_type_enum as compoundType, lir.reschedule_strategy_enum as rescheduleStrategy, "
                + " lir.rest_frequency_type_enum as restFrequencyEnum, lir.rest_frequency_interval as restFrequencyInterval, "
                + " lir.rest_frequency_nth_day_enum as restFrequencyNthDayEnum, "
                + " lir.rest_frequency_weekday_enum as restFrequencyWeekDayEnum, " + " lir.rest_frequency_on_day as restFrequencyOnDay, "
                + " lir.compounding_frequency_type_enum as compoundingFrequencyEnum, lir.compounding_frequency_interval as compoundingInterval, "
                + " lir.compounding_frequency_nth_day_enum as compoundingFrequencyNthDayEnum, "
                + " lir.compounding_frequency_weekday_enum as compoundingFrequencyWeekDayEnum, "
                + " lir.compounding_frequency_on_day as compoundingFrequencyOnDay, "
                + " lir.is_compounding_to_be_posted_as_transaction as isCompoundingToBePostedAsTransaction, "
                + " lir.allow_compounding_on_eod as allowCompoundingOnEod, "
                + " lir.disallow_interest_calc_on_past_due as disallowInterestCalculationOnPastDue, "
                + " l.is_floating_interest_rate as isFloatingInterestRate, " + " l.interest_rate_differential as interestRateDifferential, "
                + " l.days_in_year_custom_strategy as daysInYearCustomStrategy, "
                + " l.month_end_due_date_strategy as monthEndDueDateStrategy, "
                + " l.enable_income_capitalization as enableIncomeCapitalization, "
                + " l.capitalized_income_calculation_type as capitalizedIncomeCalculationType, "
                + " l.capitalized_income_strategy as capitalizedIncomeStrategy, "
                + " l.capitalized_income_type as capitalizedIncomeType, l.is_merchant_buy_down_fee as merchantBuyDownFee, " //
                + " l.enable_buy_down_fee as enableBuyDownFee, l.buy_down_fee_calculation_type as buyDownFeeCalculationType, "
                + " l.buy_down_fee_strategy as buyDownFeeStrategy, l.buy_down_fee_income_type as buyDownFeeIncomeType, "
                + " l.create_standing_instruction_at_disbursement as createStandingInstructionAtDisbursement, "
                + " lpvi.minimum_gap as minimuminstallmentgap, lpvi.maximum_gap as maximuminstallmentgap, "
                + " lp.can_use_for_topup as canUseForTopup, l.is_topup as isTopup, topup.closure_loan_id as closureLoanId, "
                + " l.total_recovered_derived as totalRecovered, topuploan.account_no as closureLoanAccountNo, "
                + " topup.topup_amount as topupAmount, l.last_closed_business_date as lastClosedBusinessDate,l.overpaidon_date as overpaidOnDate, "
                + " l.is_charged_off as isChargedOff, l.charge_off_reason_cv_id as chargeOffReasonId, codec.code_value as chargeOffReason, l.charged_off_on_date as chargedOffOnDate, l.enable_down_payment as enableDownPayment, l.disbursed_amount_percentage_for_down_payment as disbursedAmountPercentageForDownPayment, l.enable_auto_repayment_for_down_payment as enableAutoRepaymentForDownPayment,"
                + " cobu.username as chargedOffByUsername, cobu.firstname as chargedOffByFirstname, cobu.lastname as chargedOffByLastname, l.loan_schedule_type as loanScheduleType, l.loan_schedule_processing_type as loanScheduleProcessingType, "
                + " l.charge_off_behaviour as chargeOffBehaviour, l.interest_recognition_on_disbursement_date as interestRecognitionOnDisbursementDate, l.allow_full_term_for_tranche as allowFullTermForTranche, l.repayment_start_date_type_enum as repaymentStartDateType " //
                + " from m_loan l" //
                + " join m_product_loan lp on lp.id = l.product_id" //
                + " left join m_loan_recalculation_details lir on lir.loan_id = l.id join m_currency rc on rc."
                + sqlGenerator.escape("code") + " = l.currency_code" //
                + " left join m_client c on c.id = l.client_id" //
                + " left join m_group g on g.id = l.group_id" //
                + " left join m_loan_arrears_aging la on la.loan_id = l.id" //
                + " left join m_fund f on f.id = l.fund_id" //
                + " left join m_staff s on s.id = l.loan_officer_id" //
                + " left join m_appuser sbu on sbu.id = l.created_by left join m_appuser rbu on rbu.id = l.rejectedon_userid"
                + " left join m_appuser wbu on wbu.id = l.withdrawnon_userid" + " left join m_appuser abu on abu.id = l.approvedon_userid"
                + " left join m_appuser dbu on dbu.id = l.disbursedon_userid left join m_appuser cbu on cbu.id = l.closedon_userid"
                + " left join m_appuser cobu on cobu.id = l.charged_off_by_userid "
                + " left join m_code_value cv on cv.id = l.loanpurpose_cv_id"
                + " left join m_code_value codev on codev.id = l.writeoff_reason_cv_id"
                + " left join m_code_value codec on codec.id = l.charge_off_reason_cv_id"
                + " left join m_product_loan_variable_installment_config lpvi on lpvi.loan_product_id = l.product_id"
                + " left join m_loan_topup as topup on l.id = topup.loan_id"
                + " left join m_loan as topuploan on topuploan.id = topup.closure_loan_id ";

    }

    @Override
    public LoanAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

        final String currencyCode = rs.getString("currencyCode");
        final String currencyName = rs.getString("currencyName");
        final String currencyNameCode = rs.getString("currencyNameCode");
        final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
        final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
        final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
        final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                currencyNameCode);

        final Long id = rs.getLong("id");
        final String accountNo = rs.getString("accountNo");
        final String externalIdStr = rs.getString("externalId");
        final ExternalId externalId = ExternalIdFactory.produce(externalIdStr);

        final Long clientId = JdbcSupport.getLong(rs, "clientId");
        final String clientAccountNo = rs.getString("clientAccountNo");
        final Long clientOfficeId = JdbcSupport.getLong(rs, "clientOfficeId");
        final ExternalId clientExternalId = ExternalIdFactory.produce(rs.getString("clientExternalId"));
        final String clientName = rs.getString("clientName");

        final Long groupId = JdbcSupport.getLong(rs, "groupId");
        final String groupName = rs.getString("groupName");
        final String groupAccountNo = rs.getString("groupAccountNo");
        final String groupExternalId = rs.getString("groupExternalId");
        final Long groupOfficeId = JdbcSupport.getLong(rs, "groupOfficeId");
        final Long groupStaffId = JdbcSupport.getLong(rs, "groupStaffId");
        final Long groupParentId = JdbcSupport.getLong(rs, "groupParentId");
        final String centerName = rs.getString("centerName");
        final String groupHierarchy = rs.getString("groupHierarchy");
        final String groupLevel = rs.getString("groupLevel");

        final Integer loanTypeId = JdbcSupport.getInteger(rs, "loanType");
        final EnumOptionData loanType = AccountEnumerations.loanType(loanTypeId);

        final Long fundId = JdbcSupport.getLong(rs, "fundId");
        final String fundName = rs.getString("fundName");

        final Long loanOfficerId = JdbcSupport.getLong(rs, "loanOfficerId");
        final String loanOfficerName = rs.getString("loanOfficerName");

        final Long loanPurposeId = JdbcSupport.getLong(rs, "loanPurposeId");
        final String loanPurposeName = rs.getString("loanPurposeName");

        final Long loanProductId = JdbcSupport.getLong(rs, "loanProductId");
        final String loanProductName = rs.getString("loanProductName");
        final String loanProductDescription = rs.getString("loanProductDescription");
        final boolean isLoanProductLinkedToFloatingRate = rs.getBoolean("isLoanProductLinkedToFloatingRate");
        final Boolean multiDisburseLoan = rs.getBoolean("multiDisburseLoan");
        final Boolean canDefineInstallmentAmount = rs.getBoolean("canDefineInstallmentAmount");
        final BigDecimal outstandingLoanBalance = rs.getBigDecimal("outstandingLoanBalance");

        final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
        final String submittedByUsername = rs.getString("submittedByUsername");
        final String submittedByFirstname = rs.getString("submittedByFirstname");
        final String submittedByLastname = rs.getString("submittedByLastname");

        final LocalDate rejectedOnDate = JdbcSupport.getLocalDate(rs, "rejectedOnDate");
        final String rejectedByUsername = rs.getString("rejectedByUsername");
        final String rejectedByFirstname = rs.getString("rejectedByFirstname");
        final String rejectedByLastname = rs.getString("rejectedByLastname");

        final LocalDate withdrawnOnDate = JdbcSupport.getLocalDate(rs, "withdrawnOnDate");
        final String withdrawnByUsername = rs.getString("withdrawnByUsername");
        final String withdrawnByFirstname = rs.getString("withdrawnByFirstname");
        final String withdrawnByLastname = rs.getString("withdrawnByLastname");

        final LocalDate approvedOnDate = JdbcSupport.getLocalDate(rs, "approvedOnDate");
        final String approvedByUsername = rs.getString("approvedByUsername");
        final String approvedByFirstname = rs.getString("approvedByFirstname");
        final String approvedByLastname = rs.getString("approvedByLastname");

        final LocalDate expectedDisbursementDate = JdbcSupport.getLocalDate(rs, "expectedDisbursementDate");
        final LocalDate actualDisbursementDate = JdbcSupport.getLocalDate(rs, "actualDisbursementDate");
        final String disbursedByUsername = rs.getString("disbursedByUsername");
        final String disbursedByFirstname = rs.getString("disbursedByFirstname");
        final String disbursedByLastname = rs.getString("disbursedByLastname");

        final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
        final String closedByUsername = rs.getString("closedByUsername");
        final String closedByFirstname = rs.getString("closedByFirstname");
        final String closedByLastname = rs.getString("closedByLastname");

        final LocalDate writtenOffOnDate = JdbcSupport.getLocalDate(rs, "writtenOffOnDate");
        final Long writeoffReasonId = JdbcSupport.getLong(rs, "writeoffReasonId");
        final String writeoffReason = rs.getString("writeoffReason");
        final LocalDate actualMaturityDate = JdbcSupport.getLocalDate(rs, "actualMaturityDate");
        final LocalDate expectedMaturityDate = JdbcSupport.getLocalDate(rs, "expectedMaturityDate");

        final Boolean isvariableInstallmentsAllowed = rs.getBoolean("isvariableInstallmentsAllowed");
        final Integer minimumGap = rs.getInt("minimuminstallmentgap");
        final Integer maximumGap = rs.getInt("maximuminstallmentgap");

        final LocalDate chargedOffOnDate = JdbcSupport.getLocalDate(rs, "chargedOffOnDate");
        final String chargedOffByUsername = rs.getString("chargedOffByUsername");
        final String chargedOffByFirstname = rs.getString("chargedOffByFirstname");
        final String chargedOffByLastname = rs.getString("chargedOffByLastname");
        final Long chargeOffReasonId = JdbcSupport.getLong(rs, "chargeOffReasonId");
        final String chargeOffReason = rs.getString("chargeOffReason");
        final boolean isChargedOff = rs.getBoolean("isChargedOff");
        final LocalDate overpaidOnDate = JdbcSupport.getLocalDate(rs, "overpaidOnDate");

        final LoanApplicationTimelineData timeline = new LoanApplicationTimelineData(submittedOnDate, submittedByUsername,
                submittedByFirstname, submittedByLastname, rejectedOnDate, rejectedByUsername, rejectedByFirstname, rejectedByLastname,
                withdrawnOnDate, withdrawnByUsername, withdrawnByFirstname, withdrawnByLastname, approvedOnDate, approvedByUsername,
                approvedByFirstname, approvedByLastname, expectedDisbursementDate, actualDisbursementDate, disbursedByUsername,
                disbursedByFirstname, disbursedByLastname, closedOnDate, closedByUsername, closedByFirstname, closedByLastname,
                actualMaturityDate, expectedMaturityDate, writtenOffOnDate, closedByUsername, closedByFirstname, closedByLastname,
                chargedOffOnDate, chargedOffByUsername, chargedOffByFirstname, chargedOffByLastname, overpaidOnDate);

        final BigDecimal principal = rs.getBigDecimal("principal");
        final BigDecimal approvedPrincipal = rs.getBigDecimal("approvedPrincipal");
        final BigDecimal proposedPrincipal = rs.getBigDecimal("proposedPrincipal");
        final BigDecimal netDisbursalAmount = rs.getBigDecimal("netDisbursalAmount");
        final BigDecimal totalOverpaid = rs.getBigDecimal("totalOverpaid");
        final BigDecimal inArrearsTolerance = rs.getBigDecimal("inArrearsTolerance");

        final Integer numberOfRepayments = JdbcSupport.getInteger(rs, "numberOfRepayments");
        final Integer repaymentEvery = JdbcSupport.getInteger(rs, "repaymentEvery");
        final BigDecimal interestRatePerPeriod = rs.getBigDecimal("interestRatePerPeriod");
        final BigDecimal annualInterestRate = rs.getBigDecimal("annualInterestRate");
        final BigDecimal interestRateDifferential = rs.getBigDecimal("interestRateDifferential");
        final boolean isFloatingInterestRate = rs.getBoolean("isFloatingInterestRate");

        final Integer graceOnPrincipalPayment = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnPrincipalPayment");
        final Integer recurringMoratoriumOnPrincipalPeriods = JdbcSupport.getIntegerDefaultToNullIfZero(rs,
                "recurringMoratoriumOnPrincipalPeriods");
        final Integer graceOnInterestPayment = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnInterestPayment");
        final Integer graceOnInterestCharged = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnInterestCharged");
        final Integer graceOnArrearsAgeing = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnArrearsAgeing");

        final Integer termFrequency = JdbcSupport.getInteger(rs, "termFrequency");
        final Integer termPeriodFrequencyTypeInt = JdbcSupport.getInteger(rs, "termPeriodFrequencyType");
        final EnumOptionData termPeriodFrequencyType = LoanEnumerations.termFrequencyType(termPeriodFrequencyTypeInt);

        final int repaymentFrequencyTypeInt = JdbcSupport.getInteger(rs, "repaymentFrequencyType");
        final EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(repaymentFrequencyTypeInt);

        final int interestRateFrequencyTypeInt = JdbcSupport.getInteger(rs, "interestRateFrequencyType");
        final EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(interestRateFrequencyTypeInt);

        final String transactionStrategyCode = rs.getString("transactionStrategyCode");
        final String transactionStrategyName = rs.getString("transactionStrategyName");

        final int amortizationTypeInt = JdbcSupport.getInteger(rs, "amortizationType");
        final int interestTypeInt = JdbcSupport.getInteger(rs, "interestType");
        final int interestCalculationPeriodTypeInt = JdbcSupport.getInteger(rs, "interestCalculationPeriodType");
        final boolean isEqualAmortization = rs.getBoolean("isEqualAmortization");
        final EnumOptionData amortizationType = LoanEnumerations.amortizationType(amortizationTypeInt);
        final BigDecimal fixedPrincipalPercentagePerInstallment = rs.getBigDecimal("fixedPrincipalPercentagePerInstallment");
        final EnumOptionData interestType = LoanEnumerations.interestType(interestTypeInt);
        final EnumOptionData interestCalculationPeriodType = LoanEnumerations
                .interestCalculationPeriodType(interestCalculationPeriodTypeInt);
        final Boolean allowPartialPeriodInterestCalculation = rs.getBoolean("allowPartialPeriodInterestCalculation");

        final Integer lifeCycleStatusId = JdbcSupport.getInteger(rs, "lifeCycleStatusId");
        final LoanStatusEnumData status = LoanEnumerations.status(lifeCycleStatusId);

        final Integer loanSubStatusId = JdbcSupport.getInteger(rs, "loanSubStatusId");
        EnumOptionData loanSubStatus = null;
        if (loanSubStatusId != null) {
            loanSubStatus = LoanSubStatus.loanSubStatus(loanSubStatusId);
        }

        // settings
        final LocalDate expectedFirstRepaymentOnDate = JdbcSupport.getLocalDate(rs, "expectedFirstRepaymentOnDate");
        final LocalDate interestChargedFromDate = JdbcSupport.getLocalDate(rs, "interestChargedFromDate");

        final Boolean syncDisbursementWithMeeting = rs.getBoolean("syncDisbursementWithMeeting");

        final BigDecimal feeChargesDueAtDisbursementCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,
                "feeChargesDueAtDisbursementCharged");
        LoanSummaryData loanSummary = null;
        Boolean inArrears = false;
        if (status.getId().intValue() >= 300) {

            // loan summary
            final BigDecimal principalDisbursed = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalDisbursed");
            final BigDecimal totalPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalPrincipal");
            final BigDecimal totalCapitalizedIncome = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalCapitalizedIncome");
            final BigDecimal totalCapitalizedIncomeAdjustment = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,
                    "totalCapitalizedIncomeAdjustment");
            final BigDecimal principalAdjustments = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalAdjustments");
            final BigDecimal principalPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalPaid");
            final BigDecimal principalWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalWrittenOff");
            final BigDecimal principalOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalOutstanding");
            final BigDecimal principalOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalOverdue");

            final BigDecimal interestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestCharged");
            final BigDecimal interestPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestPaid");
            final BigDecimal interestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWaived");
            final BigDecimal interestWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWrittenOff");
            final BigDecimal interestOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestOutstanding");
            final BigDecimal interestOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestOverdue");

            final BigDecimal feeChargesCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesCharged");
            final BigDecimal feeAdjustments = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeAdjustments");
            final BigDecimal feeChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesPaid");
            final BigDecimal feeChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesWaived");
            final BigDecimal feeChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesWrittenOff");
            final BigDecimal feeChargesOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesOutstanding");
            final BigDecimal feeChargesOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesOverdue");

            final BigDecimal penaltyChargesCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesCharged");
            final BigDecimal penaltyAdjustments = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyAdjustments");
            final BigDecimal penaltyChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesPaid");
            final BigDecimal penaltyChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesWaived");
            final BigDecimal penaltyChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesWrittenOff");
            final BigDecimal penaltyChargesOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesOutstanding");
            final BigDecimal penaltyChargesOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesOverdue");

            final BigDecimal totalExpectedRepayment = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalExpectedRepayment");
            final BigDecimal totalRepayment = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalRepayment");
            final BigDecimal totalExpectedCostOfLoan = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalExpectedCostOfLoan");
            final BigDecimal totalCostOfLoan = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalCostOfLoan");
            final BigDecimal totalWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalWaived");
            final BigDecimal totalWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalWrittenOff");
            final BigDecimal totalOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalOutstanding");
            final BigDecimal totalOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalOverdue");
            final BigDecimal totalRecovered = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalRecovered");

            final LocalDate overdueSinceDate = JdbcSupport.getLocalDate(rs, "overdueSinceDate");
            inArrears = (overdueSinceDate != null);

            loanSummary = LoanSummaryData.builder().currency(currencyData).principalDisbursed(principalDisbursed)
                    .totalPrincipal(totalPrincipal).totalCapitalizedIncome(totalCapitalizedIncome)
                    .totalCapitalizedIncomeAdjustment(totalCapitalizedIncomeAdjustment).principalAdjustments(principalAdjustments)
                    .principalPaid(principalPaid).principalWrittenOff(principalWrittenOff).principalOutstanding(principalOutstanding)
                    .principalOverdue(principalOverdue).interestCharged(interestCharged).interestPaid(interestPaid)
                    .interestWaived(interestWaived).interestWrittenOff(interestWrittenOff).interestOutstanding(interestOutstanding)
                    .interestOverdue(interestOverdue).feeChargesCharged(feeChargesCharged).feeAdjustments(feeAdjustments)
                    .feeChargesDueAtDisbursementCharged(feeChargesDueAtDisbursementCharged).feeChargesPaid(feeChargesPaid)
                    .feeChargesWaived(feeChargesWaived).feeChargesWrittenOff(feeChargesWrittenOff)
                    .feeChargesOutstanding(feeChargesOutstanding).feeChargesOverdue(feeChargesOverdue)
                    .penaltyChargesCharged(penaltyChargesCharged).penaltyAdjustments(penaltyAdjustments)
                    .penaltyChargesPaid(penaltyChargesPaid).penaltyChargesWaived(penaltyChargesWaived)
                    .penaltyChargesWrittenOff(penaltyChargesWrittenOff).penaltyChargesOutstanding(penaltyChargesOutstanding)
                    .penaltyChargesOverdue(penaltyChargesOverdue).totalExpectedRepayment(totalExpectedRepayment)
                    .totalRepayment(totalRepayment).totalExpectedCostOfLoan(totalExpectedCostOfLoan).totalCostOfLoan(totalCostOfLoan)
                    .totalWaived(totalWaived).totalWrittenOff(totalWrittenOff).totalOutstanding(totalOutstanding).totalOverdue(totalOverdue)
                    .overdueSinceDate(overdueSinceDate).writeoffReasonId(writeoffReasonId).writeoffReason(writeoffReason)
                    .totalRecovered(totalRecovered).chargeOffReasonId(chargeOffReasonId).chargeOffReason(chargeOffReason).build();

        }

        GroupGeneralData groupData = null;
        if (groupId != null) {
            final Integer groupStatusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData groupStatus = ClientEnumerations.status(groupStatusEnum);
            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            groupData = GroupGeneralData.instance(groupId, groupAccountNo, groupName, groupExternalId, groupStatus, activationDate,
                    groupOfficeId, null, groupParentId, centerName, groupStaffId, null, groupHierarchy, groupLevel, null);
        }

        final Integer loanCounter = JdbcSupport.getInteger(rs, "loanCounter");
        final Integer loanProductCounter = JdbcSupport.getInteger(rs, "loanProductCounter");
        final BigDecimal fixedEmiAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "fixedEmiAmount");
        final Boolean isNPA = rs.getBoolean("isNPA");

        final int daysInMonth = JdbcSupport.getInteger(rs, "daysInMonth");
        final EnumOptionData daysInMonthType = CommonEnumerations.daysInMonthType(daysInMonth);
        final int daysInYear = JdbcSupport.getInteger(rs, "daysInYear");
        final EnumOptionData daysInYearType = CommonEnumerations.daysInYearType(daysInYear);
        final boolean isInterestRecalculationEnabled = rs.getBoolean("isInterestRecalculationEnabled");
        final Boolean createStandingInstructionAtDisbursement = rs.getBoolean("createStandingInstructionAtDisbursement");

        LoanInterestRecalculationData interestRecalculationData = null;
        if (isInterestRecalculationEnabled) {

            final Long lprId = JdbcSupport.getLong(rs, "lirId");
            final Long productId = JdbcSupport.getLong(rs, "loanId");
            final int compoundTypeEnumValue = JdbcSupport.getInteger(rs, "compoundType");
            final EnumOptionData interestRecalculationCompoundingType = LoanEnumerations
                    .interestRecalculationCompoundingType(compoundTypeEnumValue);
            final int rescheduleStrategyEnumValue = JdbcSupport.getInteger(rs, "rescheduleStrategy");
            final EnumOptionData rescheduleStrategyType = LoanEnumerations.rescheduleStrategyType(rescheduleStrategyEnumValue);
            final CalendarData calendarData = null;
            final int restFrequencyEnumValue = JdbcSupport.getInteger(rs, "restFrequencyEnum");
            final EnumOptionData restFrequencyType = LoanEnumerations.interestRecalculationFrequencyType(restFrequencyEnumValue);
            final int restFrequencyInterval = JdbcSupport.getInteger(rs, "restFrequencyInterval");
            final Integer restFrequencyNthDayEnumValue = JdbcSupport.getInteger(rs, "restFrequencyNthDayEnum");
            EnumOptionData restFrequencyNthDayEnum = null;
            if (restFrequencyNthDayEnumValue != null) {
                restFrequencyNthDayEnum = LoanEnumerations.interestRecalculationCompoundingNthDayType(restFrequencyNthDayEnumValue);
            }
            final Integer restFrequencyWeekDayEnumValue = JdbcSupport.getInteger(rs, "restFrequencyWeekDayEnum");
            EnumOptionData restFrequencyWeekDayEnum = null;
            if (restFrequencyWeekDayEnumValue != null) {
                restFrequencyWeekDayEnum = LoanEnumerations.interestRecalculationCompoundingDayOfWeekType(restFrequencyWeekDayEnumValue);
            }
            final Integer restFrequencyOnDay = JdbcSupport.getInteger(rs, "restFrequencyOnDay");
            final CalendarData compoundingCalendarData = null;
            final Integer compoundingFrequencyEnumValue = JdbcSupport.getInteger(rs, "compoundingFrequencyEnum");
            EnumOptionData compoundingFrequencyType = null;
            if (compoundingFrequencyEnumValue != null) {
                compoundingFrequencyType = LoanEnumerations.interestRecalculationFrequencyType(compoundingFrequencyEnumValue);
            }
            final Integer compoundingInterval = JdbcSupport.getInteger(rs, "compoundingInterval");
            final Integer compoundingFrequencyNthDayEnumValue = JdbcSupport.getInteger(rs, "compoundingFrequencyNthDayEnum");
            EnumOptionData compoundingFrequencyNthDayEnum = null;
            if (compoundingFrequencyNthDayEnumValue != null) {
                compoundingFrequencyNthDayEnum = LoanEnumerations
                        .interestRecalculationCompoundingNthDayType(compoundingFrequencyNthDayEnumValue);
            }
            final Integer compoundingFrequencyWeekDayEnumValue = JdbcSupport.getInteger(rs, "compoundingFrequencyWeekDayEnum");
            EnumOptionData compoundingFrequencyWeekDayEnum = null;
            if (compoundingFrequencyWeekDayEnumValue != null) {
                compoundingFrequencyWeekDayEnum = LoanEnumerations
                        .interestRecalculationCompoundingDayOfWeekType(compoundingFrequencyWeekDayEnumValue);
            }
            final Integer compoundingFrequencyOnDay = JdbcSupport.getInteger(rs, "compoundingFrequencyOnDay");

            final Boolean isCompoundingToBePostedAsTransaction = rs.getBoolean("isCompoundingToBePostedAsTransaction");
            final Boolean allowCompoundingOnEod = rs.getBoolean("allowCompoundingOnEod");
            final Boolean disallowInterestCalculationOnPastDue = rs.getBoolean("disallowInterestCalculationOnPastDue");
            interestRecalculationData = new LoanInterestRecalculationData(lprId, productId, interestRecalculationCompoundingType,
                    rescheduleStrategyType, calendarData, restFrequencyType, restFrequencyInterval, restFrequencyNthDayEnum,
                    restFrequencyWeekDayEnum, restFrequencyOnDay, compoundingCalendarData, compoundingFrequencyType, compoundingInterval,
                    compoundingFrequencyNthDayEnum, compoundingFrequencyWeekDayEnum, compoundingFrequencyOnDay,
                    isCompoundingToBePostedAsTransaction, allowCompoundingOnEod, disallowInterestCalculationOnPastDue);
        }

        final boolean canUseForTopup = rs.getBoolean("canUseForTopup");
        final boolean isTopup = rs.getBoolean("isTopup");
        final Long closureLoanId = rs.getLong("closureLoanId");
        final String closureLoanAccountNo = rs.getString("closureLoanAccountNo");
        final BigDecimal topupAmount = rs.getBigDecimal("topupAmount");
        final boolean disallowExpectedDisbursements = rs.getBoolean("disallowExpectedDisbursements");
        // Current Delinquency Range Data
        DelinquencyRangeData delinquencyRange = this.delinquencyReadPlatformService.retrieveCurrentDelinquencyTag(id);

        final boolean isFraud = rs.getBoolean("isFraud");
        final LocalDate lastClosedBusinessDate = JdbcSupport.getLocalDate(rs, "lastClosedBusinessDate");

        final boolean enableDownPayment = rs.getBoolean("enableDownPayment");
        final BigDecimal disbursedAmountPercentageForDownPayment = rs.getBigDecimal("disbursedAmountPercentageForDownPayment");
        final boolean enableAutoRepaymentForDownPayment = rs.getBoolean("enableAutoRepaymentForDownPayment");
        final EnumOptionData repaymentStartDateType = LoanEnumerations
                .repaymentStartDateType(RepaymentStartDateType.fromInt(JdbcSupport.getInteger(rs, "repaymentStartDateType")));
        final boolean enableInstallmentLevelDelinquency = rs.getBoolean("enableInstallmentLevelDelinquency");
        final String loanScheduleTypeStr = rs.getString("loanScheduleType");
        final LoanScheduleType loanScheduleType = LoanScheduleType.valueOf(loanScheduleTypeStr);
        final String loanScheduleProcessingTypeStr = rs.getString("loanScheduleProcessingType");
        final LoanScheduleProcessingType loanScheduleProcessingType = LoanScheduleProcessingType.valueOf(loanScheduleProcessingTypeStr);
        final Integer fixedLength = JdbcSupport.getInteger(rs, "fixedLength");
        final LoanChargeOffBehaviour chargeOffBehaviour = LoanChargeOffBehaviour.valueOf(rs.getString("chargeOffBehaviour"));
        final boolean interestRecognitionOnDisbursementDate = rs.getBoolean("interestRecognitionOnDisbursementDate");
        final boolean allowFullTermForTranche = rs.getBoolean("allowFullTermForTranche");
        final StringEnumOptionData daysInYearCustomStrategy = ApiFacingEnum.getStringEnumOptionData(DaysInYearCustomStrategyType.class,
                rs.getString("daysInYearCustomStrategy"));
        final boolean enableIncomeCapitalization = rs.getBoolean("enableIncomeCapitalization");
        final StringEnumOptionData capitalizedIncomeCalculationType = ApiFacingEnum
                .getStringEnumOptionData(LoanCapitalizedIncomeCalculationType.class, rs.getString("capitalizedIncomeCalculationType"));
        final StringEnumOptionData capitalizedIncomeStrategy = ApiFacingEnum.getStringEnumOptionData(LoanCapitalizedIncomeStrategy.class,
                rs.getString("capitalizedIncomeStrategy"));
        final StringEnumOptionData capitalizedIncomeType = ApiFacingEnum.getStringEnumOptionData(LoanCapitalizedIncomeType.class,
                rs.getString("capitalizedIncomeType"));
        final boolean enableBuyDownFee = rs.getBoolean("enableBuyDownFee");
        final StringEnumOptionData buyDownFeeCalculationType = ApiFacingEnum.getStringEnumOptionData(LoanBuyDownFeeCalculationType.class,
                rs.getString("buyDownFeeCalculationType"));
        final StringEnumOptionData buyDownFeeStrategy = ApiFacingEnum.getStringEnumOptionData(LoanBuyDownFeeStrategy.class,
                rs.getString("buyDownFeeStrategy"));
        final StringEnumOptionData buyDownFeeIncomeType = ApiFacingEnum.getStringEnumOptionData(LoanBuyDownFeeIncomeType.class,
                rs.getString("buyDownFeeIncomeType"));
        final boolean merchantBuyDownFee = rs.getBoolean("merchantBuyDownFee");
        final StringEnumOptionData monthEndDueDateStrategy = ApiFacingEnum.getStringEnumOptionData(MonthEndDueDateStrategy.class,
                rs.getString("monthEndDueDateStrategy"));

        return LoanAccountData.basicLoanDetails(id, accountNo, status, externalId, clientId, clientAccountNo, clientName, clientOfficeId,
                clientExternalId, groupData, loanType, loanProductId, loanProductName, loanProductDescription,
                isLoanProductLinkedToFloatingRate, fundId, fundName, loanPurposeId, loanPurposeName, loanOfficerId, loanOfficerName,
                currencyData, proposedPrincipal, principal, approvedPrincipal, netDisbursalAmount, totalOverpaid, inArrearsTolerance,
                termFrequency, termPeriodFrequencyType, numberOfRepayments, repaymentEvery, repaymentFrequencyType, null, null,
                transactionStrategyCode, transactionStrategyName, amortizationType, interestRatePerPeriod, interestRateFrequencyType,
                annualInterestRate, interestType, isFloatingInterestRate, interestRateDifferential, interestCalculationPeriodType,
                allowPartialPeriodInterestCalculation, expectedFirstRepaymentOnDate, graceOnPrincipalPayment,
                recurringMoratoriumOnPrincipalPeriods, graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate, timeline,
                loanSummary, feeChargesDueAtDisbursementCharged, syncDisbursementWithMeeting, loanCounter, loanProductCounter,
                multiDisburseLoan, canDefineInstallmentAmount, fixedEmiAmount, outstandingLoanBalance, inArrears, graceOnArrearsAgeing,
                isNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled, interestRecalculationData,
                createStandingInstructionAtDisbursement, isvariableInstallmentsAllowed, minimumGap, maximumGap, loanSubStatus,
                canUseForTopup, isTopup, closureLoanId, closureLoanAccountNo, topupAmount, isEqualAmortization,
                fixedPrincipalPercentagePerInstallment, delinquencyRange, disallowExpectedDisbursements, isFraud, lastClosedBusinessDate,
                overpaidOnDate, isChargedOff, enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment,
                repaymentStartDateType, enableInstallmentLevelDelinquency, loanScheduleType.asEnumOptionData(),
                loanScheduleProcessingType.asEnumOptionData(), fixedLength, chargeOffBehaviour.getValueAsStringEnumOptionData(),
                interestRecognitionOnDisbursementDate, allowFullTermForTranche, daysInYearCustomStrategy, enableIncomeCapitalization,
                capitalizedIncomeCalculationType, capitalizedIncomeStrategy, capitalizedIncomeType, enableBuyDownFee,
                buyDownFeeCalculationType, buyDownFeeStrategy, buyDownFeeIncomeType, merchantBuyDownFee)
                .setMonthEndDueDateStrategy(monthEndDueDateStrategy)
                .setFirstRepaymentDayOfMonth(JdbcSupport.getInteger(rs, "firstRepaymentDayOfMonth"))
                .setSecondRepaymentDayOfMonth(JdbcSupport.getInteger(rs, "secondRepaymentDayOfMonth"));
    }
}
