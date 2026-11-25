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
package org.apache.fineract.portfolio.savings.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountApplicationTimelineData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSubStatusEnumData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSummaryData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargesPaidByData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountSubStatusEnum;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.data.TaxDetailsData;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.springframework.jdbc.core.ResultSetExtractor;

final class SavingAccountMapperForInterestPosting implements ResultSetExtractor<List<SavingsAccountData>> {

    private final String schemaSql;

    SavingAccountMapperForInterestPosting() {
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("sa.id as id, sa.account_no as accountNo, sa.external_id as externalId, ");
        sqlBuilder.append("sa.deposit_type_enum as depositType, ");
        sqlBuilder.append("c.id as clientId, c.office_id as clientOfficeId, ");
        sqlBuilder.append("g.id as groupId, g.office_id as groupOfficeId, ");
        sqlBuilder.append("sa.status_enum as statusEnum, ");
        sqlBuilder.append("sa.sub_status_enum as subStatusEnum, ");
        sqlBuilder.append("sa.submittedon_date as submittedOnDate,");
        sqlBuilder.append("sa.rejectedon_date as rejectedOnDate,");
        sqlBuilder.append("sa.withdrawnon_date as withdrawnOnDate,");
        sqlBuilder.append("sa.approvedon_date as approvedOnDate,");
        sqlBuilder.append("sa.activatedon_date as activatedOnDate,");
        sqlBuilder.append("sa.closedon_date as closedOnDate,");
        sqlBuilder.append(
                "sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
        sqlBuilder.append("sa.nominal_annual_interest_rate as nominalAnnualInterestRate, ");
        sqlBuilder.append("sa.interest_compounding_period_enum as interestCompoundingPeriodType, ");
        sqlBuilder.append("sa.interest_posting_period_enum as interestPostingPeriodType, ");
        sqlBuilder.append("sa.interest_calculation_type_enum as interestCalculationType, ");
        sqlBuilder.append("sa.interest_calculation_days_in_year_type_enum as interestCalculationDaysInYearType, ");
        sqlBuilder.append("sa.min_required_opening_balance as minRequiredOpeningBalance, ");
        sqlBuilder.append("sa.lockin_period_frequency as lockinPeriodFrequency,");
        sqlBuilder.append("sa.lockin_period_frequency_enum as lockinPeriodFrequencyType, ");
        sqlBuilder.append("sa.withdrawal_fee_for_transfer as withdrawalFeeForTransfers, ");
        sqlBuilder.append("sa.allow_overdraft as allowOverdraft, ");
        sqlBuilder.append("sa.overdraft_limit as overdraftLimit, ");
        sqlBuilder.append("sa.nominal_annual_interest_rate_overdraft as nominalAnnualInterestRateOverdraft, ");
        sqlBuilder.append("sa.min_overdraft_for_interest_calculation as minOverdraftForInterestCalculation, ");
        sqlBuilder.append("sa.total_deposits_derived as totalDeposits, ");
        sqlBuilder.append("sa.total_withdrawals_derived as totalWithdrawals, ");
        sqlBuilder.append("sa.total_withdrawal_fees_derived as totalWithdrawalFees, ");
        sqlBuilder.append("sa.total_annual_fees_derived as totalAnnualFees, ");
        sqlBuilder.append("sa.total_interest_earned_derived as totalInterestEarned, ");
        sqlBuilder.append("sa.total_interest_posted_derived as totalInterestPosted, ");
        sqlBuilder.append("sa.total_overdraft_interest_derived as totalOverdraftInterestDerived, ");
        sqlBuilder.append("sa.account_balance_derived as accountBalance, ");
        sqlBuilder.append("sa.total_fees_charge_derived as totalFeeCharge, ");
        sqlBuilder.append("sa.total_penalty_charge_derived as totalPenaltyCharge, ");
        sqlBuilder.append("sa.min_balance_for_interest_calculation as minBalanceForInterestCalculation,");
        sqlBuilder.append("sa.min_required_balance as minRequiredBalance, ");
        sqlBuilder.append("sa.enforce_min_required_balance as enforceMinRequiredBalance, ");
        sqlBuilder.append("sa.max_allowed_lien_limit as maxAllowedLienLimit, ");
        sqlBuilder.append("sa.is_lien_allowed as lienAllowed, ");
        sqlBuilder.append("sa.on_hold_funds_derived as onHoldFunds, ");
        sqlBuilder.append("sa.withhold_tax as withHoldTax, ");
        sqlBuilder.append("sa.total_withhold_tax_derived as totalWithholdTax, ");
        sqlBuilder.append("sa.last_interest_calculation_date as lastInterestCalculationDate, ");
        sqlBuilder.append("sa.total_savings_amount_on_hold as onHoldAmount, ");
        sqlBuilder.append("sa.interest_posted_till_date as interestPostedTillDate, ");
        sqlBuilder.append("sa.version as version, ");
        sqlBuilder.append("tg.id as taxGroupId, ");
        sqlBuilder.append("(select COALESCE(max(sat.transaction_date),sa.activatedon_date) ");
        sqlBuilder.append("from m_savings_account_transaction as sat ");
        sqlBuilder.append("where sat.is_reversed = false and sat.is_reversal = false ");
        sqlBuilder.append("and sat.transaction_type_enum in (1,2) ");
        sqlBuilder.append("and sat.savings_account_id = sa.id) as lastActiveTransactionDate, ");
        sqlBuilder.append("sp.id as productId, ");
        sqlBuilder.append("sp.is_dormancy_tracking_active as isDormancyTrackingActive, ");
        sqlBuilder.append("sp.days_to_inactive as daysToInactive, ");
        sqlBuilder.append("sp.days_to_dormancy as daysToDormancy, ");
        sqlBuilder.append("sp.days_to_escheat as daysToEscheat, ");
        sqlBuilder.append("sp.accounting_type as accountingType, ");
        sqlBuilder.append("tr.id as transactionId, tr.transaction_type_enum as transactionType, ");
        sqlBuilder.append("tr.transaction_date as transactionDate, tr.amount as transactionAmount,");
        sqlBuilder.append("tr.submitted_on_date as transSubmittedOnDate,tr.cumulative_balance_derived as cumulativeBalance,");
        sqlBuilder.append("tr.running_balance_derived as runningBalance, tr.is_reversed as reversed, tr.is_reversal as reversal,");
        sqlBuilder.append("tr.balance_end_date_derived as balanceEndDate, tr.overdraft_amount_derived as overdraftAmount,");
        sqlBuilder.append("tr.is_manual as manualTransaction, tr.is_force_transaction as forceTransaction, tr.office_id as officeId, ");
        sqlBuilder.append("pd.payment_type_id as paymentType,pd.account_number as accountNumber,pd.check_number as checkNumber, ");
        sqlBuilder.append("pd.receipt_number as receiptNumber, pd.bank_number as bankNumber,pd.routing_code as routingCode, ");
        sqlBuilder.append("pt.value as paymentTypeName, ");
        sqlBuilder.append("msacpb.amount as paidByAmount, msacpb.id as chargesPaidById, ");
        sqlBuilder.append(
                "msac.id as chargeId, msac.amount as chargeAmount, msac.charge_time_enum as chargeTimeType, msac.is_penalty as isPenaltyCharge, ");
        sqlBuilder.append("txd.id as taxDetailsId, txd.amount as taxAmount, ");
        sqlBuilder.append("apm.gl_account_id as glAccountIdForInterestOnSavings, apm1.gl_account_id as glAccountIdForSavingsControl, ");
        sqlBuilder.append("apm6.gl_account_id as glAccountIdForSavingsReference, ");
        sqlBuilder.append("apm2.gl_account_id as glAccountIdForInterestReceivable,apm3.gl_account_id as glAccountIdForOverdraftPorfolio, ");
        sqlBuilder.append("apm4.gl_account_id as glAccountIdForInterestPayable, ");
        sqlBuilder.append("apm5.gl_account_id as glAccountIdForIncomeFromInterest, ");
        sqlBuilder.append(
                "mtc.id as taxComponentId, mtc.debit_account_id as debitAccountId, mtc.credit_account_id as creditAccountId, mtc.percentage as taxPercentage ");
        sqlBuilder.append("from m_savings_account sa ");
        sqlBuilder.append("join m_savings_product sp ON sa.product_id = sp.id ");
        sqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");
        sqlBuilder.append("join m_savings_account_transaction tr on sa.id = tr.savings_account_id ");
        sqlBuilder.append("left join m_payment_detail pd on pd.id = tr.payment_detail_id ");
        sqlBuilder.append("left join m_payment_type pt on pt.id = pd.payment_type_id ");
        sqlBuilder.append("left join m_savings_account_charge_paid_by msacpb on msacpb.savings_account_transaction_id = tr.id ");
        sqlBuilder.append("left join m_savings_account_charge msac on msac.id = msacpb.savings_account_charge_id ");
        sqlBuilder.append("left join m_client c ON c.id = sa.client_id ");
        sqlBuilder.append("left join m_group g ON g.id = sa.group_id ");
        sqlBuilder.append("left join m_tax_group tg on tg.id = sa.tax_group_id ");
        sqlBuilder.append("left join m_savings_account_transaction_tax_details txd on txd.savings_transaction_id = tr.id ");
        sqlBuilder.append("left join m_tax_component mtc on mtc.id = txd.tax_component_id ");
        sqlBuilder.append(
                "left join acc_product_mapping apm on apm.product_type = 2 and apm.product_id = sp.id and apm.financial_account_type=3 ");
        sqlBuilder.append(
                "left join acc_product_mapping apm1 on apm1.product_type = 2 and apm1.product_id = sp.id and apm1.financial_account_type=2 ");
        sqlBuilder.append(
                "left join acc_product_mapping apm2 on apm2.product_type = 2 and apm2.product_id = sp.id and apm2.financial_account_type=18 ");
        sqlBuilder.append(
                "left join acc_product_mapping apm3 on apm3.product_type = 2 and apm3.product_id = sp.id and apm3.financial_account_type=11 ");
        sqlBuilder.append(
                "left join acc_product_mapping apm4 on apm4.product_type = 2 and apm4.product_id = sp.id and apm4.financial_account_type=17 ");
        sqlBuilder.append(
                "left join acc_product_mapping apm5 on apm5.product_type = 2 and apm5.product_id = sp.id and apm5.financial_account_type=12 ");
        sqlBuilder.append(
                "left join acc_product_mapping apm6 on apm6.product_type = 2 and apm6.product_id = sp.id and apm6.financial_account_type=1 "
                        + "and apm6.payment_type is null and apm6.charge_id is null ");

        this.schemaSql = sqlBuilder.toString();
    }

    String schema() {
        return this.schemaSql;
    }

    @Override
    public List<SavingsAccountData> extractData(final ResultSet rs) throws SQLException {

        List<SavingsAccountData> savingsAccountDataList = new ArrayList<>();
        HashMap<String, Long> savingsMap = new HashMap<>();
        String currencyCode = null;
        Integer currencyDigits = null;
        Integer inMultiplesOf = null;
        CurrencyData currency = null;
        HashMap<String, Long> transMap = new HashMap<>();
        HashMap<String, Long> taxDetails = new HashMap<>();
        HashMap<String, Long> chargeDetails = new HashMap<>();
        SavingsAccountTransactionData savingsAccountTransactionData = null;
        SavingsAccountData savingsAccountData = null;
        int count = 0;

        while (rs.next()) {
            final Long id = rs.getLong("id");
            final Long transactionId = rs.getLong("transactionId");
            final Long taxDetailId = JdbcSupport.getLongDefaultToNullIfZero(rs, "taxDetailsId");
            final Long taxComponentId = JdbcSupport.getLongDefaultToNullIfZero(rs, "taxComponentId");
            final String accountNo = rs.getString("accountNo");
            final Long chargeId = rs.getLong("chargeId");

            if (!savingsMap.containsValue(id)) {
                if (count > 0) {
                    savingsAccountDataList.add(savingsAccountData);
                }
                count++;
                savingsMap.put("id", id);

                final String externalId = rs.getString("externalId");
                final Integer depositTypeId = rs.getInt("depositType");
                final EnumOptionData depositType = SavingsEnumerations.depositType(depositTypeId);
                final Long groupId = JdbcSupport.getLong(rs, "groupId");
                final Long groupOfficeId = JdbcSupport.getLong(rs, "groupOfficeId");
                final GroupGeneralData groupGeneralData = new GroupGeneralData(groupId, groupOfficeId);

                final Long clientId = JdbcSupport.getLong(rs, "clientId");
                final Long clientOfficeId = JdbcSupport.getLong(rs, "clientOfficeId");
                final ClientData clientData = ClientData.createClientForInterestPosting(clientId, clientOfficeId);

                final Long glAccountIdForInterestOnSavings = rs.getLong("glAccountIdForInterestOnSavings");
                final Long glAccountIdForSavingsControl = rs.getLong("glAccountIdForSavingsControl");
                final Long glAccountIdForSavingsReference = rs.getLong("glAccountIdForSavingsReference");

                final Long glAccountIdForOverdraftPorfolio = rs.getLong("glAccountIdForOverdraftPorfolio");
                final Long glAccountIdForInterestReceivable = rs.getLong("glAccountIdForInterestReceivable");

                final Long glAccountIdForInterestPayable = rs.getLong("glAccountIdForInterestPayable");

                final Long glAccountIdForIncomeFromInterest = rs.getLong("glAccountIdForIncomeFromInterest");

                final Long productId = rs.getLong("productId");
                final Integer accountType = rs.getInt("accountingType");
                final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(accountType);
                final EnumOptionData enumOptionDataForAccounting = new EnumOptionData(accountType.longValue(), accountingRuleType.getCode(),
                        accountingRuleType.getValue().toString());
                final SavingsProductData savingsProductData = SavingsProductData.createForInterestPosting(productId,
                        enumOptionDataForAccounting);

                final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
                final SavingsAccountStatusEnumData status = SavingsEnumerations.status(statusEnum);
                final Integer subStatusEnum = JdbcSupport.getInteger(rs, "subStatusEnum");
                final SavingsAccountSubStatusEnumData subStatus = SavingsEnumerations.subStatus(subStatusEnum);
                final LocalDate lastActiveTransactionDate = JdbcSupport.getLocalDate(rs, "lastActiveTransactionDate");
                final boolean isDormancyTrackingActive = rs.getBoolean("isDormancyTrackingActive");
                final Integer numDaysToInactive = JdbcSupport.getInteger(rs, "daysToInactive");
                final Integer numDaysToDormancy = JdbcSupport.getInteger(rs, "daysToDormancy");
                final Integer numDaysToEscheat = JdbcSupport.getInteger(rs, "daysToEscheat");
                Integer daysToInactive = null;
                Integer daysToDormancy = null;
                Integer daysToEscheat = null;

                LocalDate currentDate = DateUtils.getBusinessLocalDate();
                if (isDormancyTrackingActive && statusEnum.equals(SavingsAccountStatusType.ACTIVE.getValue())) {
                    if (subStatusEnum < SavingsAccountSubStatusEnum.ESCHEAT.getValue()) {
                        daysToEscheat = DateUtils.getExactDifferenceInDays(currentDate,
                                lastActiveTransactionDate.plusDays(numDaysToEscheat));
                    }
                    if (subStatusEnum < SavingsAccountSubStatusEnum.DORMANT.getValue()) {
                        daysToDormancy = DateUtils.getExactDifferenceInDays(currentDate,
                                lastActiveTransactionDate.plusDays(numDaysToDormancy));
                    }
                    if (subStatusEnum < SavingsAccountSubStatusEnum.INACTIVE.getValue()) {
                        daysToInactive = DateUtils.getExactDifferenceInDays(currentDate,
                                lastActiveTransactionDate.plusDays(numDaysToInactive));
                    }
                }
                final LocalDate approvedOnDate = JdbcSupport.getLocalDate(rs, "approvedOnDate");
                final LocalDate withdrawnOnDate = JdbcSupport.getLocalDate(rs, "withdrawnOnDate");
                final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
                final LocalDate activatedOnDate = JdbcSupport.getLocalDate(rs, "activatedOnDate");
                final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
                final SavingsAccountApplicationTimelineData timeline = new SavingsAccountApplicationTimelineData(submittedOnDate, null,
                        null, null, null, null, null, null, withdrawnOnDate, null, null, null, approvedOnDate, null, null, null,
                        activatedOnDate, null, null, null, closedOnDate, null, null, null);

                currencyCode = rs.getString("currencyCode");
                currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
                inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
                currency = new CurrencyData(currencyCode, currencyDigits, inMultiplesOf);

                final BigDecimal totalDeposits = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalDeposits");
                final BigDecimal totalWithdrawals = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalWithdrawals");
                final BigDecimal totalWithdrawalFees = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalWithdrawalFees");
                final BigDecimal totalAnnualFees = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalAnnualFees");

                final BigDecimal totalInterestEarned = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalInterestEarned");
                final BigDecimal totalInterestPosted = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalInterestPosted");
                final BigDecimal accountBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "accountBalance");
                final BigDecimal totalFeeCharge = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalFeeCharge");
                final BigDecimal totalPenaltyCharge = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalPenaltyCharge");
                final BigDecimal totalOverdraftInterestDerived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,
                        "totalOverdraftInterestDerived");
                final BigDecimal totalWithholdTax = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalWithholdTax");
                final LocalDate interestPostedTillDate = JdbcSupport.getLocalDate(rs, "interestPostedTillDate");

                final BigDecimal minBalanceForInterestCalculation = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                        "minBalanceForInterestCalculation");
                final BigDecimal onHoldFunds = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "onHoldFunds");

                final BigDecimal onHoldAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "onHoldAmount");

                BigDecimal availableBalance = accountBalance;
                if (availableBalance != null && onHoldFunds != null) {

                    availableBalance = availableBalance.subtract(onHoldFunds);
                }

                if (availableBalance != null && onHoldAmount != null) {

                    availableBalance = availableBalance.subtract(onHoldAmount);
                }

                BigDecimal interestNotPosted = BigDecimal.ZERO;
                LocalDate lastInterestCalculationDate = null;
                if (totalInterestEarned != null) {
                    interestNotPosted = totalInterestEarned.subtract(totalInterestPosted).add(totalOverdraftInterestDerived);
                    lastInterestCalculationDate = JdbcSupport.getLocalDate(rs, "lastInterestCalculationDate");
                }

                final SavingsAccountSummaryData summary = new SavingsAccountSummaryData(currency, totalDeposits, totalWithdrawals,
                        totalWithdrawalFees, totalAnnualFees, totalInterestEarned, totalInterestPosted, accountBalance, totalFeeCharge,
                        totalPenaltyCharge, totalOverdraftInterestDerived, totalWithholdTax, interestNotPosted, lastInterestCalculationDate,
                        availableBalance, interestPostedTillDate);
                summary.setPrevInterestPostedTillDate(interestPostedTillDate);

                final boolean withHoldTax = rs.getBoolean("withHoldTax");
                final Long taxGroupId = JdbcSupport.getLongDefaultToNullIfZero(rs, "taxGroupId");
                // id only at this point: the full tax group, with its mappings, components and component
                // histories, is loaded once per page by loadTaxGroupsForInterestPosting and set on the
                // account there
                TaxGroupData taxGroupData = null;
                if (taxGroupId != null) {
                    taxGroupData = TaxGroupData.lookup(taxGroupId, null);
                }

                final BigDecimal nominalAnnualInterestRate = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "nominalAnnualInterestRate");

                final EnumOptionData interestCompoundingPeriodType = SavingsEnumerations.compoundingInterestPeriodType(
                        SavingsCompoundingInterestPeriodType.fromInt(JdbcSupport.getInteger(rs, "interestCompoundingPeriodType")));

                final EnumOptionData interestPostingPeriodType = SavingsEnumerations.interestPostingPeriodType(
                        SavingsPostingInterestPeriodType.fromInt(JdbcSupport.getInteger(rs, "interestPostingPeriodType")));

                final EnumOptionData interestCalculationType = SavingsEnumerations.interestCalculationType(
                        SavingsInterestCalculationType.fromInt(JdbcSupport.getInteger(rs, "interestCalculationType")));

                final EnumOptionData interestCalculationDaysInYearType = SavingsEnumerations.interestCalculationDaysInYearType(
                        SavingsInterestCalculationDaysInYearType.fromInt(JdbcSupport.getInteger(rs, "interestCalculationDaysInYearType")));

                final BigDecimal minRequiredOpeningBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "minRequiredOpeningBalance");

                final Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockinPeriodFrequency");
                EnumOptionData lockinPeriodFrequencyType = null;
                final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodFrequencyType");
                if (lockinPeriodFrequencyTypeValue != null) {
                    final SavingsPeriodFrequencyType lockinPeriodType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
                    lockinPeriodFrequencyType = SavingsEnumerations.lockinPeriodFrequencyType(lockinPeriodType);
                }

                final boolean withdrawalFeeForTransfers = rs.getBoolean("withdrawalFeeForTransfers");

                final boolean allowOverdraft = rs.getBoolean("allowOverdraft");
                final BigDecimal overdraftLimit = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "overdraftLimit");
                final BigDecimal nominalAnnualInterestRateOverdraft = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                        "nominalAnnualInterestRateOverdraft");
                final BigDecimal minOverdraftForInterestCalculation = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                        "minOverdraftForInterestCalculation");

                final BigDecimal minRequiredBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "minRequiredBalance");
                final boolean enforceMinRequiredBalance = rs.getBoolean("enforceMinRequiredBalance");
                final BigDecimal maxAllowedLienLimit = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "maxAllowedLienLimit");
                final boolean lienAllowed = rs.getBoolean("lienAllowed");
                savingsAccountData = SavingsAccountData.instance(id, accountNo, depositType, externalId, null, null, null, null, productId,
                        null, null, null, status, subStatus, null, timeline, currency, nominalAnnualInterestRate,
                        interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                        interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                        withdrawalFeeForTransfers, summary, allowOverdraft, overdraftLimit, minRequiredBalance, enforceMinRequiredBalance,
                        maxAllowedLienLimit, lienAllowed, minBalanceForInterestCalculation, onHoldFunds, nominalAnnualInterestRateOverdraft,
                        minOverdraftForInterestCalculation, withHoldTax, taxGroupData, lastActiveTransactionDate, isDormancyTrackingActive,
                        daysToInactive, daysToDormancy, daysToEscheat, onHoldAmount);

                savingsAccountData.setClientData(clientData);
                savingsAccountData.setGroupGeneralData(groupGeneralData);
                savingsAccountData.setSavingsProduct(savingsProductData);

                savingsAccountData.setGlAccountIdForInterestReceivable(glAccountIdForInterestReceivable);
                savingsAccountData.setGlAccountIdForOverdraftPorfolio(glAccountIdForOverdraftPorfolio);

                savingsAccountData.setGlAccountIdForInterestPayable(glAccountIdForInterestPayable);

                savingsAccountData.setGlAccountIdForInterestOnSavings(glAccountIdForInterestOnSavings);
                savingsAccountData.setGlAccountIdForSavingsControl(glAccountIdForSavingsControl);
                savingsAccountData.setGlAccountIdForIncomeFromInterest(glAccountIdForIncomeFromInterest);
                savingsAccountData.setGlAccountIdForSavingsReference(glAccountIdForSavingsReference);
                final Integer version = JdbcSupport.getInteger(rs, "version");
                savingsAccountData.setVersion(version);
            }

            if (!transMap.containsValue(transactionId)) {

                final int transactionTypeInt = JdbcSupport.getInteger(rs, "transactionType");
                final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations.transactionType(transactionTypeInt);

                final LocalDate date = JdbcSupport.getLocalDate(rs, "transactionDate");
                final LocalDate balanceEndDate = JdbcSupport.getLocalDate(rs, "balanceEndDate");
                final LocalDate transSubmittedOnDate = JdbcSupport.getLocalDate(rs, "transSubmittedOnDate");
                final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "transactionAmount");
                final BigDecimal overdraftAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "overdraftAmount");
                final BigDecimal outstandingChargeAmount = null;
                final BigDecimal runningBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "runningBalance");
                final boolean reversed = rs.getBoolean("reversed");
                final boolean reversal = rs.getBoolean("reversal");
                final Long officeId = rs.getLong("officeId");
                final BigDecimal cumulativeBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "cumulativeBalance");

                final boolean manualTransaction = rs.getBoolean("manualTransaction");

                PaymentDetailData paymentDetailData = null;
                if (transactionType.isDepositOrWithdrawal()) {
                    final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentType");
                    if (paymentTypeId != null) {
                        final String typeName = rs.getString("paymentTypeName");
                        final PaymentTypeData paymentTypeData = new PaymentTypeData(paymentTypeId, typeName, null, false, null, null,
                                false);
                        paymentDetailData = new PaymentDetailData(id, paymentTypeData, null, null, null, null, null);
                    }
                }

                savingsAccountTransactionData = SavingsAccountTransactionData.create(transactionId, transactionType, paymentDetailData, id,
                        accountNo, date, currency, amount, outstandingChargeAmount, runningBalance, reversed, transSubmittedOnDate,
                        manualTransaction, cumulativeBalance, balanceEndDate, reversal);
                savingsAccountTransactionData.setOverdraftAmount(overdraftAmount);
                savingsAccountTransactionData.setForceTransaction(rs.getBoolean("forceTransaction"));

                transMap.put("id", transactionId);
                if (savingsAccountData.getOfficeId() == null) {
                    savingsAccountData.setOfficeId(officeId);
                }

                savingsAccountData.setSavingsAccountTransactionData(savingsAccountTransactionData);
            }

            if (chargeId != null && !chargeDetails.containsValue(chargeId)) {
                final boolean isPenalty = rs.getBoolean("isPenaltyCharge");
                final BigDecimal chargeAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "chargeAmount");
                final Integer chargesTimeType = rs.getInt("chargeTimeType");
                final EnumOptionData enumOptionDataForChargesTimeType = new EnumOptionData(chargesTimeType.longValue(), null, null);
                final SavingsAccountChargeData savingsAccountChargeData = new SavingsAccountChargeData(chargeId, chargeAmount,
                        enumOptionDataForChargesTimeType, isPenalty);

                final Long chargesPaidById = rs.getLong("chargesPaidById");
                final BigDecimal chargesPaid = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "paidByAmount");
                final SavingsAccountChargesPaidByData savingsAccountChargesPaidByData = new SavingsAccountChargesPaidByData(chargesPaidById,
                        chargesPaid);
                savingsAccountChargesPaidByData.setSavingsAccountChargeData(savingsAccountChargeData);
                if (savingsAccountChargesPaidByData != null) {
                    savingsAccountTransactionData.setChargesPaidByData(savingsAccountChargesPaidByData);
                }

                chargeDetails.put("id", chargeId);
            }

            if (taxDetailId != null && !taxDetails.containsValue(taxDetailId)) {
                final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "taxAmount");
                final BigDecimal percentage = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "taxPercentage");
                final Long debitId = rs.getLong("debitAccountId");
                final Long creditId = rs.getLong("creditAccountId");
                final GLAccountData debitAccount = GLAccountData.createFrom(debitId);
                final GLAccountData creditAccount = GLAccountData.createFrom(creditId);

                if (taxComponentId != null) {
                    final TaxComponentData taxComponent = TaxComponentData.createTaxComponent(taxComponentId, percentage, debitAccount,
                            creditAccount);
                    savingsAccountTransactionData.setTaxDetails(new TaxDetailsData(taxComponent, amount));
                }

                taxDetails.put("id", taxDetailId);
            }

        }
        if (savingsAccountData != null) {
            savingsAccountDataList.add(savingsAccountData);
        }
        return savingsAccountDataList;

    }
}
