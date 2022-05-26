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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.batch.command.CommandStrategyProvider;
import org.apache.fineract.batch.service.ResolutionHelper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSummaryData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author manoj
 */

@Component
@Scope("prototype")
public class SavingsSchedularInterestPoster implements Callable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsSchedularInterestPoster.class);
    private static final SecureRandom random = new SecureRandom();
    private static final String SAVINGS_TRANSACTION_IDENTIFIER = "S";

    private Collection<SavingsAccountData> savingAccounts;
    private SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private SavingsAccountRepositoryWrapper savingsAccountRepository;
    private SavingsAccountAssembler savingAccountAssembler;
    private FineractPlatformTenant tenant;
    private ConfigurationDomainService configurationDomainService;
    private boolean backdatedTxnsAllowedTill;
    private List<SavingsAccountData> savingsAccountDataList = new ArrayList<>();
    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;
    private CommandStrategyProvider strategyProvider;
    private ResolutionHelper resolutionHelper;
    private SavingsAccountReadPlatformService savingsAccountReadPlatformService;

    public void setSavings(Collection<SavingsAccountData> savingAccounts) {
        this.savingAccounts = savingAccounts;
    }

    public void setBackdatedTxnsAllowedTill(final boolean backdatedTxnsAllowedTill) {
        this.backdatedTxnsAllowedTill = backdatedTxnsAllowedTill;
    }

    public void setSavingsAccountWritePlatformService(SavingsAccountWritePlatformService savingsAccountWritePlatformService) {
        this.savingsAccountWritePlatformService = savingsAccountWritePlatformService;
    }

    public void setSavingsAccountReadPlatformService(SavingsAccountReadPlatformService savingsAccountReadPlatformService) {
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;
    }

    public void setSavingsAccountRepository(SavingsAccountRepositoryWrapper savingsAccountRepository) {
        this.savingsAccountRepository = savingsAccountRepository;
    }

    public void setSavingAccountAssembler(SavingsAccountAssembler savingAccountAssembler) {
        this.savingAccountAssembler = savingAccountAssembler;
    }

    public void setTenant(FineractPlatformTenant tenant) {
        this.tenant = tenant;
    }

    public void setConfigurationDomainService(ConfigurationDomainService configurationDomainService) {
        this.configurationDomainService = configurationDomainService;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void setResolutionHelper(ResolutionHelper resolutionHelper) {
        this.resolutionHelper = resolutionHelper;
    }

    public void setStrategyProvider(CommandStrategyProvider commandStrategyProvider) {
        this.strategyProvider = commandStrategyProvider;
    }

    @Override
    @SuppressFBWarnings(value = {
            "DMI_RANDOM_USED_ONLY_ONCE" }, justification = "False positive for random object created and used only once")
    public Void call() throws org.apache.fineract.infrastructure.jobs.exception.JobExecutionException {
        ThreadLocalContextUtil.setTenant(tenant);
        Integer maxNumberOfRetries = tenant.getConnection().getMaxRetriesOnDeadlock();
        Integer maxIntervalBetweenRetries = tenant.getConnection().getMaxIntervalBetweenRetries();

        // List<BatchResponse> responseList = new ArrayList<>();
        long start = System.currentTimeMillis();
        LOG.info("Thread Execution Started at {}", start);

        List<Throwable> errors = new ArrayList<>();
        int i = 0;
        if (!savingAccounts.isEmpty()) {
            for (SavingsAccountData savingsAccountData : savingAccounts) {
                Integer numberOfRetries = 0;
                while (numberOfRetries <= maxNumberOfRetries) {
                    try {
                        boolean postInterestAsOn = false;
                        LocalDate transactionDate = null;
                        long startPosting = System.currentTimeMillis();
                        SavingsAccountData savingsAccountDataRet = savingsAccountWritePlatformService.postInterest(savingsAccountData,
                                postInterestAsOn, transactionDate, backdatedTxnsAllowedTill);
                        long endPosting = System.currentTimeMillis();
                        savingsAccountDataList.add(savingsAccountDataRet);

                        LOG.info("Posting Completed Within {}", endPosting - startPosting);

                        numberOfRetries = maxNumberOfRetries + 1;
                    } catch (CannotAcquireLockException | ObjectOptimisticLockingFailureException exception) {
                        LOG.info("Interest posting job for savings ID {} has been retried {} time(s)", savingsAccountData.getId(),
                                numberOfRetries);
                        // Fail if the transaction has been retired for
                        // maxNumberOfRetries
                        if (numberOfRetries >= maxNumberOfRetries) {
                            LOG.error(
                                    "Interest posting job for savings ID {} has been retried for the max allowed attempts of {} and will be rolled back",
                                    savingsAccountData.getId(), numberOfRetries);
                            errors.add(exception);
                            break;
                        }
                        // Else sleep for a random time (between 1 to 10
                        // seconds) and continue
                        try {
                            int randomNum = random.nextInt(maxIntervalBetweenRetries + 1);
                            Thread.sleep(1000 + (randomNum * 1000));
                            numberOfRetries = numberOfRetries + 1;
                        } catch (InterruptedException e) {
                            LOG.error("Interest posting job for savings retry failed due to InterruptedException", e);
                            errors.add(e);
                            break;
                        }
                    } catch (Exception e) {
                        LOG.error("Interest posting job for savings failed for account {}", savingsAccountData.getId(), e);
                        numberOfRetries = maxNumberOfRetries + 1;
                        errors.add(e);
                    }
                }
                i++;
            }

            if (errors.isEmpty()) {
                try {
                    batchUpdate(savingsAccountDataList);
                } catch (DataAccessException exception) {
                    LOG.error("Batch update failed due to DataAccessException", exception);
                    errors.add(exception);
                } catch (NullPointerException exception) {
                    LOG.error("Batch update failed due to NullPointerException", exception);
                    errors.add(exception);
                }
            }

            if (!errors.isEmpty()) {
                throw new JobExecutionException(errors);
            }
        }

        long end = System.currentTimeMillis();
        LOG.info("Time To Finish the batch {} by thread {} for accounts {}", end - start, Thread.currentThread().getId(),
                savingAccounts.size());
        return null;
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, rollbackFor = Exception.class)
    private void batchUpdateJournalEntries(final List<SavingsAccountData> savingsAccountDataList,
            final HashMap<String, SavingsAccountTransactionData> savingsAccountTransactionDataHashMap)
            throws DataAccessException, NullPointerException {
        String queryForJGLUpdate = batchQueryForJournalEntries();
        List<Object[]> paramsForGLInsertion = new ArrayList<>();
        for (SavingsAccountData savingsAccountData : savingsAccountDataList) {
            String currencyCode = savingsAccountData.getCurrency().getCode();

            List<SavingsAccountTransactionData> savingsAccountTransactionDataList = savingsAccountData.getSavingsAccountTransactionData();
            for (SavingsAccountTransactionData savingsAccountTransactionData : savingsAccountTransactionDataList) {
                if (savingsAccountTransactionData.getId() == null) {
                    final String key = savingsAccountTransactionData.getRefNo();
                    if (savingsAccountTransactionDataHashMap.containsKey(key)) {
                        LocalDate currentDate = DateUtils.getLocalDateOfTenant();
                        final SavingsAccountTransactionData dataFromFetch = savingsAccountTransactionDataHashMap.get(key);
                        savingsAccountTransactionData.setId(dataFromFetch.getId());

                        paramsForGLInsertion.add(
                                new Object[] { savingsAccountData.getGlAccountIdForSavingsControl(), savingsAccountData.getOfficeId(), null,
                                        currencyCode, SAVINGS_TRANSACTION_IDENTIFIER + savingsAccountTransactionData.getId().toString(),
                                        savingsAccountTransactionData.getId(), null, false, null, false,
                                        Date.from(savingsAccountTransactionData.getTransactionDate()
                                                .atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant()),
                                        JournalEntryType.CREDIT.getValue().longValue(), savingsAccountTransactionData.getAmount(), null,
                                        JournalEntryType.CREDIT.getValue().longValue(), savingsAccountData.getId(),
                                        Date.from(currentDate.atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant()),
                                        Date.from(currentDate.atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant()), false,
                                        BigDecimal.ZERO, BigDecimal.ZERO, null,
                                        Date.from(savingsAccountTransactionData.getTransactionDate()
                                                .atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant()),
                                        null, Integer.valueOf(1), Integer.valueOf(1) });

                        paramsForGLInsertion.add(new Object[] { savingsAccountData.getGlAccountIdForInterestOnSavings(),
                                savingsAccountData.getOfficeId(), null, currencyCode,
                                SAVINGS_TRANSACTION_IDENTIFIER + savingsAccountTransactionData.getId().toString(),
                                savingsAccountTransactionData.getId(), null, false, null, false,
                                Date.from(savingsAccountTransactionData.getTransactionDate()
                                        .atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant()),
                                JournalEntryType.DEBIT.getValue().longValue(), savingsAccountTransactionData.getAmount(), null,
                                JournalEntryType.DEBIT.getValue().longValue(), savingsAccountData.getId(),
                                Date.from(currentDate.atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant()),
                                Date.from(currentDate.atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant()), false,
                                BigDecimal.ZERO, BigDecimal.ZERO, null,
                                Date.from(savingsAccountTransactionData.getTransactionDate()
                                        .atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant()),
                                null, Integer.valueOf(1), Integer.valueOf(1) });

                    }
                }
            }
        }

        if (paramsForGLInsertion != null && paramsForGLInsertion.size() > 0) {
            this.jdbcTemplate.batchUpdate(queryForJGLUpdate, paramsForGLInsertion);
        }
    }

    private String batchQueryForJournalEntries() {
        StringBuilder query = new StringBuilder(100);

        query.append("INSERT INTO acc_gl_journal_entry(account_id,office_id,reversal_id,currency_code,transaction_id,");
        query.append("savings_transaction_id,client_transaction_id,reversed,ref_num,manual_entry,entry_date,type_enum,");
        query.append("amount,description,entity_type_enum,entity_id,created_date,");
        query.append("lastmodified_date,is_running_balance_calculated,office_running_balance,organization_running_balance,");
        query.append("payment_details_id,transaction_date,share_transaction_id, createdby_id, lastmodifiedby_id) ");
        query.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        return query.toString();
    }

    private List<SavingsAccountTransactionData> fetchTransactionsFromIds(final List<String> refNo) throws DataAccessException {
        return this.savingsAccountReadPlatformService.retrieveAllTransactionData(refNo);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @SuppressWarnings("unused")
    private void batchUpdate(final List<SavingsAccountData> savingsAccountDataList) throws DataAccessException {
        String queryForSavingsUpdate = batchQueryForSavingsSummaryUpdate();
        String queryForTransactionInsertion = batchQueryForTransactionInsertion();
        List<Object[]> paramsForTransactionInsertion = new ArrayList<>();
        List<Object[]> paramsForSavingsSummary = new ArrayList<>();
        List<String> transRefNo = new ArrayList<>();
        for (SavingsAccountData savingsAccountData : savingsAccountDataList) {
            SavingsAccountSummaryData savingsAccountSummaryData = savingsAccountData.getSummary();
            paramsForSavingsSummary.add(new Object[] { savingsAccountSummaryData.getTotalDeposits(),
                    savingsAccountSummaryData.getTotalWithdrawals(), savingsAccountSummaryData.getTotalInterestEarned(),
                    savingsAccountSummaryData.getTotalInterestPosted(), savingsAccountSummaryData.getTotalWithdrawalFees(),
                    savingsAccountSummaryData.getTotalFeeCharge(), savingsAccountSummaryData.getTotalPenaltyCharge(),
                    savingsAccountSummaryData.getTotalAnnualFees(), savingsAccountSummaryData.getAvailableBalance(),
                    savingsAccountSummaryData.getTotalOverdraftInterestDerived(), savingsAccountSummaryData.getTotalWithholdTax(),
                    Date.from(savingsAccountSummaryData.getLastInterestCalculationDate().atStartOfDay(DateUtils.getDateTimeZoneOfTenant())
                            .toInstant()),
                    Date.from(savingsAccountSummaryData.getInterestPostedTillDate().atStartOfDay(DateUtils.getDateTimeZoneOfTenant())
                            .toInstant()),
                    savingsAccountData.getId() });
            List<SavingsAccountTransactionData> savingsAccountTransactionDataList = savingsAccountData.getSavingsAccountTransactionData();
            for (SavingsAccountTransactionData savingsAccountTransactionData : savingsAccountTransactionDataList) {
                if (savingsAccountTransactionData.getId() == null) {
                    UUID uuid = UUID.randomUUID();
                    savingsAccountTransactionData.setRefNo(uuid.toString());
                    transRefNo.add(uuid.toString());
                    java.util.Date balanceEndDate = null;
                    if (savingsAccountTransactionData.getBalanceEndDate() != null) {
                        balanceEndDate = Date.from(savingsAccountTransactionData.getBalanceEndDate()
                                .atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant());
                    }
                    paramsForTransactionInsertion.add(new Object[] { savingsAccountData.getId(), savingsAccountData.getOfficeId(),
                            savingsAccountTransactionData.getTransactionType().getId(),
                            Date.from(savingsAccountTransactionData.getTransactionDate().atStartOfDay(DateUtils.getDateTimeZoneOfTenant())
                                    .toInstant()),
                            savingsAccountTransactionData.getAmount(), balanceEndDate,
                            savingsAccountTransactionData.getBalanceNumberOfDays(), savingsAccountTransactionData.getRunningBalance(),
                            savingsAccountTransactionData.getCumulativeBalance(), savingsAccountTransactionData.getSubmittedOnDate(),
                            Integer.valueOf(1), savingsAccountTransactionData.isManualTransaction(),
                            savingsAccountTransactionData.getRefNo() });
                }
            }
            savingsAccountData.setUpdatedTransactions(savingsAccountTransactionDataList);
        }

        if (transRefNo.size() > 0) {
            this.jdbcTemplate.batchUpdate(queryForSavingsUpdate, paramsForSavingsSummary);
            this.jdbcTemplate.batchUpdate(queryForTransactionInsertion, paramsForTransactionInsertion);

            List<SavingsAccountTransactionData> savingsAccountTransactionDataList = fetchTransactionsFromIds(transRefNo);
            if (savingsAccountDataList != null) {
                LOG.info("Fetched Transactions from DB: {}", savingsAccountTransactionDataList.size());
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
        StringBuilder query = new StringBuilder(100);
        query.append("INSERT INTO m_savings_account_transaction (savings_account_id, office_id, is_reversed,");
        query.append("transaction_type_enum, transaction_date, amount, balance_end_date_derived,");
        query.append("balance_number_of_days_derived, running_balance_derived, cumulative_balance_derived,");
        query.append("created_date, appuser_id, is_manual, is_loan_disbursement, ref_no) VALUES ");
        query.append("(?, ?, 0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?)");
        return query.toString();

    }

    private String batchQueryForSavingsSummaryUpdate() {
        StringBuilder query = new StringBuilder(100);
        query.append("update m_savings_account set total_deposits_derived=?, total_withdrawals_derived=?, ");
        query.append("total_interest_earned_derived=?, total_interest_posted_derived=?, total_withdrawal_fees_derived=?, ");
        query.append("total_fees_charge_derived=?, total_penalty_charge_derived=?, total_annual_fees_derived=?, ");
        query.append("account_balance_derived=?, total_overdraft_interest_derived=?, total_withhold_tax_derived=?, ");
        query.append("last_interest_calculation_date=?, interest_posted_till_date=? where id=?");
        return query.toString();
    }
}
