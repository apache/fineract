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

import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_BY_DB_FIELD;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_DATE_DB_FIELD;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_BY_DB_FIELD;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_DATE_DB_FIELD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSummaryData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Setter
public class SavingsSchedularInterestPoster {

    private static final String SAVINGS_TRANSACTION_IDENTIFIER = "S";

    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final JdbcTemplate jdbcTemplate;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final PlatformSecurityContext platformSecurityContext;

    private final List<SavingsAccountData> savingsAccountDataList = new ArrayList<>();
    private Collection<SavingsAccountData> savingAccounts;
    private boolean backdatedTxnsAllowedTill;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, rollbackFor = Exception.class)
    public void postInterest() throws JobExecutionException {
        if (!savingAccounts.isEmpty()) {
            List<Throwable> errors = new ArrayList<>();
            for (SavingsAccountData savingsAccountData : savingAccounts) {
                boolean postInterestAsOn = false;
                LocalDate transactionDate = null;
                try {
                    SavingsAccountData savingsAccountDataRet = savingsAccountWritePlatformService.postInterest(savingsAccountData,
                            postInterestAsOn, transactionDate, backdatedTxnsAllowedTill);
                    savingsAccountDataList.add(savingsAccountDataRet);
                } catch (Exception e) {
                    errors.add(e);
                }
            }
            if (errors.isEmpty()) {
                try {
                    batchUpdate(savingsAccountDataList);
                } catch (DataAccessException exception) {
                    log.error("Batch update failed due to DataAccessException", exception);
                    errors.add(exception);
                } catch (Exception exception) {
                    log.error("Batch update failed", exception);
                    errors.add(exception);
                }
            }

            if (!errors.isEmpty()) {
                throw new JobExecutionException(errors);
            }
        }
    }

    private void batchUpdateJournalEntries(final List<SavingsAccountData> savingsAccountDataList,
            final HashMap<String, SavingsAccountTransactionData> savingsAccountTransactionDataHashMap)
            throws DataAccessException, NullPointerException {
        Long userId = platformSecurityContext.authenticatedUser().getId();
        String queryForJGLUpdate = batchQueryForJournalEntries();
        List<Object[]> paramsForGLInsertion = new ArrayList<>();
        for (SavingsAccountData savingsAccountData : savingsAccountDataList) {
            String currencyCode = savingsAccountData.getCurrency().getCode();

            List<SavingsAccountTransactionData> savingsAccountTransactionDataList = savingsAccountData.getSavingsAccountTransactionData();
            for (SavingsAccountTransactionData savingsAccountTransactionData : savingsAccountTransactionDataList) {
                if (savingsAccountTransactionData.getId() == null) {
                    final String key = savingsAccountTransactionData.getRefNo();
                    if (savingsAccountTransactionDataHashMap.containsKey(key)) {
                        final SavingsAccountTransactionData dataFromFetch = savingsAccountTransactionDataHashMap.get(key);
                        savingsAccountTransactionData.setId(dataFromFetch.getId());
                        if (savingsAccountData.getGlAccountIdForSavingsControl() != 0
                                && savingsAccountData.getGlAccountIdForInterestOnSavings() != 0) {
                            OffsetDateTime auditDatetime = DateUtils.getAuditOffsetDateTime();
                            paramsForGLInsertion.add(new Object[] { savingsAccountData.getGlAccountIdForSavingsControl(),
                                    savingsAccountData.getOfficeId(), null, currencyCode,
                                    SAVINGS_TRANSACTION_IDENTIFIER + savingsAccountTransactionData.getId().toString(),
                                    savingsAccountTransactionData.getId(), null, false, null, false,
                                    savingsAccountTransactionData.getTransactionDate(), JournalEntryType.CREDIT.getValue().longValue(),
                                    savingsAccountTransactionData.getAmount(), null, JournalEntryType.CREDIT.getValue().longValue(),
                                    savingsAccountData.getId(), auditDatetime, auditDatetime, false, BigDecimal.ZERO, BigDecimal.ZERO, null,
                                    savingsAccountTransactionData.getTransactionDate(), null, userId, userId,
                                    DateUtils.getBusinessLocalDate() });

                            paramsForGLInsertion.add(new Object[] { savingsAccountData.getGlAccountIdForInterestOnSavings(),
                                    savingsAccountData.getOfficeId(), null, currencyCode,
                                    SAVINGS_TRANSACTION_IDENTIFIER + savingsAccountTransactionData.getId().toString(),
                                    savingsAccountTransactionData.getId(), null, false, null, false,
                                    savingsAccountTransactionData.getTransactionDate(), JournalEntryType.DEBIT.getValue().longValue(),
                                    savingsAccountTransactionData.getAmount(), null, JournalEntryType.DEBIT.getValue().longValue(),
                                    savingsAccountData.getId(), auditDatetime, auditDatetime, false, BigDecimal.ZERO, BigDecimal.ZERO, null,
                                    savingsAccountTransactionData.getTransactionDate(), null, userId, userId,
                                    DateUtils.getBusinessLocalDate() });
                        }
                    }
                }
            }
        }

        if (paramsForGLInsertion != null && paramsForGLInsertion.size() > 0) {
            this.jdbcTemplate.batchUpdate(queryForJGLUpdate, paramsForGLInsertion);
        }
    }

    private String batchQueryForJournalEntries() {
        return "INSERT INTO acc_gl_journal_entry(account_id,office_id,reversal_id,currency_code,transaction_id,"
                + "savings_transaction_id,client_transaction_id,reversed,ref_num,manual_entry,entry_date,type_enum,"
                + "amount,description,entity_type_enum,entity_id,created_on_utc,"
                + "last_modified_on_utc,is_running_balance_calculated,office_running_balance,organization_running_balance,"
                + "payment_details_id,transaction_date,share_transaction_id, created_by, last_modified_by, submitted_on_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private List<SavingsAccountTransactionData> fetchTransactionsFromIds(final List<String> refNo) throws DataAccessException {
        return this.savingsAccountReadPlatformService.retrieveAllTransactionData(refNo);
    }

    @SuppressWarnings("unused")
    private void batchUpdate(final List<SavingsAccountData> savingsAccountDataList) throws DataAccessException {
        String queryForSavingsUpdate = batchQueryForSavingsSummaryUpdate();
        String queryForTransactionInsertion = batchQueryForTransactionInsertion();
        String queryForTransactionUpdate = batchQueryForTransactionsUpdate();
        List<Object[]> paramsForTransactionInsertion = new ArrayList<>();
        List<Object[]> paramsForSavingsSummary = new ArrayList<>();
        List<Object[]> paramsForTransactionUpdate = new ArrayList<>();
        List<String> transRefNo = new ArrayList<>();
        LocalDate currentDate = DateUtils.getBusinessLocalDate();
        Long userId = platformSecurityContext.authenticatedUser().getId();
        for (SavingsAccountData savingsAccountData : savingsAccountDataList) {
            OffsetDateTime auditTime = DateUtils.getAuditOffsetDateTime();
            SavingsAccountSummaryData savingsAccountSummaryData = savingsAccountData.getSummary();
            paramsForSavingsSummary.add(new Object[] { savingsAccountSummaryData.getTotalDeposits(),
                    savingsAccountSummaryData.getTotalWithdrawals(), savingsAccountSummaryData.getTotalInterestEarned(),
                    savingsAccountSummaryData.getTotalInterestPosted(), savingsAccountSummaryData.getTotalWithdrawalFees(),
                    savingsAccountSummaryData.getTotalFeeCharge(), savingsAccountSummaryData.getTotalPenaltyCharge(),
                    savingsAccountSummaryData.getTotalAnnualFees(), savingsAccountSummaryData.getAccountBalance(),
                    savingsAccountSummaryData.getTotalOverdraftInterestDerived(), savingsAccountSummaryData.getTotalWithholdTax(),
                    savingsAccountSummaryData.getLastInterestCalculationDate(),
                    savingsAccountSummaryData.getInterestPostedTillDate() != null ? savingsAccountSummaryData.getInterestPostedTillDate()
                            : savingsAccountSummaryData.getLastInterestCalculationDate(),
                    auditTime, userId, savingsAccountData.getId() });
            List<SavingsAccountTransactionData> savingsAccountTransactionDataList = savingsAccountData.getSavingsAccountTransactionData();
            for (SavingsAccountTransactionData savingsAccountTransactionData : savingsAccountTransactionDataList) {
                if (savingsAccountTransactionData.getId() == null) {
                    UUID uuid = UUID.randomUUID();
                    savingsAccountTransactionData.setRefNo(uuid.toString());
                    transRefNo.add(uuid.toString());
                    paramsForTransactionInsertion.add(new Object[] { savingsAccountData.getId(), savingsAccountData.getOfficeId(),
                            savingsAccountTransactionData.isReversed(), savingsAccountTransactionData.getTransactionType().getId(),
                            savingsAccountTransactionData.getTransactionDate(), savingsAccountTransactionData.getAmount(),
                            savingsAccountTransactionData.getBalanceEndDate(), savingsAccountTransactionData.getBalanceNumberOfDays(),
                            savingsAccountTransactionData.getRunningBalance(), savingsAccountTransactionData.getCumulativeBalance(),
                            auditTime, userId, auditTime, userId, savingsAccountTransactionData.isManualTransaction(),
                            savingsAccountTransactionData.getRefNo(), savingsAccountTransactionData.isReversalTransaction(),
                            savingsAccountTransactionData.getOverdraftAmount(), currentDate });
                } else {
                    paramsForTransactionUpdate.add(new Object[] { savingsAccountTransactionData.isReversed(),
                            savingsAccountTransactionData.getAmount(), savingsAccountTransactionData.getOverdraftAmount(),
                            savingsAccountTransactionData.getBalanceEndDate(), savingsAccountTransactionData.getBalanceNumberOfDays(),
                            savingsAccountTransactionData.getRunningBalance(), savingsAccountTransactionData.getCumulativeBalance(),
                            savingsAccountTransactionData.isReversalTransaction(), auditTime, userId,
                            savingsAccountTransactionData.getId() });
                }
            }
            savingsAccountData.setUpdatedTransactions(savingsAccountTransactionDataList);
        }

        if (transRefNo.size() > 0) {
            this.jdbcTemplate.batchUpdate(queryForSavingsUpdate, paramsForSavingsSummary);
            this.jdbcTemplate.batchUpdate(queryForTransactionInsertion, paramsForTransactionInsertion);
            this.jdbcTemplate.batchUpdate(queryForTransactionUpdate, paramsForTransactionUpdate);
            log.debug("`Total No Of Interest Posting:` {}", transRefNo.size());
            List<SavingsAccountTransactionData> savingsAccountTransactionDataList = fetchTransactionsFromIds(transRefNo);
            if (savingsAccountDataList != null) {
                log.debug("Fetched Transactions from DB: {}", savingsAccountTransactionDataList.size());
            }

            HashMap<String, SavingsAccountTransactionData> savingsAccountTransactionMap = new HashMap<>();
            for (SavingsAccountTransactionData savingsAccountTransactionData : savingsAccountTransactionDataList) {
                final String key = savingsAccountTransactionData.getRefNo();
                savingsAccountTransactionMap.put(key, savingsAccountTransactionData);
            }
            batchUpdateJournalEntries(savingsAccountDataList, savingsAccountTransactionMap);
        }

    }

    private String batchQueryForTransactionInsertion() {
        return "INSERT INTO m_savings_account_transaction (savings_account_id, office_id, is_reversed, transaction_type_enum, transaction_date, amount, balance_end_date_derived, "
                + "balance_number_of_days_derived, running_balance_derived, cumulative_balance_derived, " + CREATED_DATE_DB_FIELD + ", "
                + CREATED_BY_DB_FIELD + ", " + LAST_MODIFIED_DATE_DB_FIELD + ", " + LAST_MODIFIED_BY_DB_FIELD
                + ", is_manual, ref_no, is_reversal, "
                + "overdraft_amount_derived, submitted_on_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private String batchQueryForSavingsSummaryUpdate() {
        return "update m_savings_account set total_deposits_derived=?, total_withdrawals_derived=?, total_interest_earned_derived=?, total_interest_posted_derived=?, total_withdrawal_fees_derived=?, "
                + "total_fees_charge_derived=?, total_penalty_charge_derived=?, total_annual_fees_derived=?, account_balance_derived=?, total_overdraft_interest_derived=?, total_withhold_tax_derived=?, "
                + "last_interest_calculation_date=?, interest_posted_till_date=?, " + LAST_MODIFIED_DATE_DB_FIELD + " = ?, "
                + LAST_MODIFIED_BY_DB_FIELD + " = ? WHERE id=? ";
    }

    private String batchQueryForTransactionsUpdate() {
        return "UPDATE m_savings_account_transaction "
                + "SET is_reversed=?, amount=?, overdraft_amount_derived=?, balance_end_date_derived=?, balance_number_of_days_derived=?, running_balance_derived=?, cumulative_balance_derived=?, is_reversal=?, "
                + LAST_MODIFIED_DATE_DB_FIELD + " = ?, " + LAST_MODIFIED_BY_DB_FIELD + " = ? " + "WHERE id=?";
    }
}
