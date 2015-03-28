/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.accounting.financialactivityaccount.domain.FinancialActivityAccount;
import org.mifosplatform.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.accounting.glaccount.domain.GLAccountType;
import org.mifosplatform.accounting.glaccount.service.GLAccountReadPlatformService;
import org.mifosplatform.accounting.journalentry.data.JournalEntryAssociationParametersData;
import org.mifosplatform.accounting.journalentry.data.JournalEntryData;
import org.mifosplatform.accounting.journalentry.data.OfficeOpeningBalancesData;
import org.mifosplatform.accounting.journalentry.data.TransactionDetailData;
import org.mifosplatform.accounting.journalentry.data.TransactionTypeEnumData;
import org.mifosplatform.accounting.journalentry.exception.JournalEntriesNotFoundException;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.core.service.SearchParameters;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.portfolio.note.data.NoteData;
import org.mifosplatform.portfolio.paymentdetail.data.PaymentDetailData;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class JournalEntryReadPlatformServiceImpl implements JournalEntryReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final GLAccountReadPlatformService glAccountReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper;

    private final PaginationHelper<JournalEntryData> paginationHelper = new PaginationHelper<>();

    @Autowired
    public JournalEntryReadPlatformServiceImpl(final RoutingDataSource dataSource,
            final GLAccountReadPlatformService glAccountReadPlatformService, final OfficeReadPlatformService officeReadPlatformService,
            final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.glAccountReadPlatformService = glAccountReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.financialActivityAccountRepositoryWrapper = financialActivityAccountRepositoryWrapper;
    }

    private static final class GLJournalEntryMapper implements RowMapper<JournalEntryData> {

        private final JournalEntryAssociationParametersData associationParametersData;

        public GLJournalEntryMapper(final JournalEntryAssociationParametersData associationParametersData) {
            if (associationParametersData == null) {
                this.associationParametersData = new JournalEntryAssociationParametersData();
            } else {
                this.associationParametersData = associationParametersData;
            }
        }

        public String schema() {
            StringBuilder sb = new StringBuilder();
            sb.append(" journalEntry.id as id, glAccount.classification_enum as classification ,")
                    .append("journalEntry.transaction_id,")
                    .append(" glAccount.name as glAccountName, glAccount.gl_code as glAccountCode,glAccount.id as glAccountId, ")
                    .append(" journalEntry.office_id as officeId, office.name as officeName, journalEntry.ref_num as referenceNumber, ")
                    .append(" journalEntry.manual_entry as manualEntry,journalEntry.entry_date as transactionDate, ")
                    .append(" journalEntry.type_enum as entryType,journalEntry.amount as amount, journalEntry.transaction_id as transactionId,")
                    .append(" journalEntry.entity_type_enum as entityType, journalEntry.entity_id as entityId, creatingUser.id as createdByUserId, ")
                    .append(" creatingUser.username as createdByUserName, journalEntry.description as comments, ")
                    .append(" journalEntry.created_date as createdDate, journalEntry.reversed as reversed, ")
                    .append(" journalEntry.currency_code as currencyCode, curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ")
                    .append(" curr.display_symbol as currencyDisplaySymbol, curr.decimal_places as currencyDigits, curr.currency_multiplesof as inMultiplesOf ");
            if (associationParametersData.isRunningBalanceRequired()) {
                sb.append(" ,journalEntry.is_running_balance_calculated as runningBalanceComputed, ")
                        .append(" journalEntry.office_running_balance as officeRunningBalance, ")
                        .append(" journalEntry.organization_running_balance as organizationRunningBalance ");
            }
            if (associationParametersData.isTransactionDetailsRequired()) {
                sb.append(" ,pd.receipt_number as receiptNumber, ").append(" pd.check_number as checkNumber, ")
                        .append(" pd.account_number as accountNumber, ").append(" pt.value as paymentTypeName, ")
                        .append(" pd.payment_type_id as paymentTypeId,").append(" pd.bank_number as bankNumber, ")
                        .append(" pd.routing_code as routingCode, ").append(" note.id as noteId, ")
                        .append(" note.note as transactionNote, ").append(" lt.transaction_type_enum as loanTransactionType, ")
                        .append(" st.transaction_type_enum as savingsTransactionType ");
            }
            sb.append(" from acc_gl_journal_entry as journalEntry ")
                    .append(" left join acc_gl_account as glAccount on glAccount.id = journalEntry.account_id")
                    .append(" left join m_office as office on office.id = journalEntry.office_id")
                    .append(" left join m_appuser as creatingUser on creatingUser.id = journalEntry.createdby_id ")
                    .append(" join m_currency curr on curr.code = journalEntry.currency_code ");
            if (associationParametersData.isTransactionDetailsRequired()) {
                sb.append(" left join m_loan_transaction as lt on journalEntry.loan_transaction_id = lt.id ")
                        .append(" left join m_savings_account_transaction as st on journalEntry.savings_transaction_id = st.id ")
                        .append(" left join m_payment_detail as pd on lt.payment_detail_id = pd.id or st.payment_detail_id = pd.id")
                        .append(" left join m_payment_type as pt on pt.id = pd.payment_type_id ")
                        .append(" left join m_note as note on lt.id = note.loan_transaction_id or st.id = note.savings_account_transaction_id ");
            }
            return sb.toString();

        }

        @Override
        public JournalEntryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");
            final String glCode = rs.getString("glAccountCode");
            final String glAccountName = rs.getString("glAccountName");
            final Long glAccountId = rs.getLong("glAccountId");
            final int accountTypeId = JdbcSupport.getInteger(rs, "classification");
            final EnumOptionData accountType = AccountingEnumerations.gLAccountType(accountTypeId);
            final LocalDate transactionDate = JdbcSupport.getLocalDate(rs, "transactionDate");
            final Boolean manualEntry = rs.getBoolean("manualEntry");
            final BigDecimal amount = rs.getBigDecimal("amount");
            final int entryTypeId = JdbcSupport.getInteger(rs, "entryType");
            final EnumOptionData entryType = AccountingEnumerations.journalEntryType(entryTypeId);
            final String transactionId = rs.getString("transactionId");
            final Integer entityTypeId = JdbcSupport.getInteger(rs, "entityType");
            EnumOptionData entityType = null;
            if (entityTypeId != null) {
                entityType = AccountingEnumerations.portfolioProductType(entityTypeId);

            }

            final Long entityId = JdbcSupport.getLong(rs, "entityId");
            final Long createdByUserId = rs.getLong("createdByUserId");
            final LocalDate createdDate = JdbcSupport.getLocalDate(rs, "createdDate");
            final String createdByUserName = rs.getString("createdByUserName");
            final String comments = rs.getString("comments");
            final Boolean reversed = rs.getBoolean("reversed");
            final String referenceNumber = rs.getString("referenceNumber");
            BigDecimal officeRunningBalance = null;
            BigDecimal organizationRunningBalance = null;
            Boolean runningBalanceComputed = null;

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            if (associationParametersData.isRunningBalanceRequired()) {
                officeRunningBalance = rs.getBigDecimal("officeRunningBalance");
                organizationRunningBalance = rs.getBigDecimal("organizationRunningBalance");
                runningBalanceComputed = rs.getBoolean("runningBalanceComputed");
            }
            TransactionDetailData transactionDetailData = null;

            if (associationParametersData.isTransactionDetailsRequired()) {
                PaymentDetailData paymentDetailData = null;
                final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentTypeId");
                if (paymentTypeId != null) {
                    final String typeName = rs.getString("paymentTypeName");
                    final PaymentTypeData paymentType = PaymentTypeData.instance(paymentTypeId, typeName);
                    final String accountNumber = rs.getString("accountNumber");
                    final String checkNumber = rs.getString("checkNumber");
                    final String routingCode = rs.getString("routingCode");
                    final String receiptNumber = rs.getString("receiptNumber");
                    final String bankNumber = rs.getString("bankNumber");
                    paymentDetailData = new PaymentDetailData(id, paymentType, accountNumber, checkNumber, routingCode, receiptNumber,
                            bankNumber);
                }
                NoteData noteData = null;
                final Long noteId = JdbcSupport.getLong(rs, "noteId");
                if (noteId != null) {
                    final String note = rs.getString("transactionNote");
                    noteData = new NoteData(noteId, null, null, null, null, null, null, null, note, null, null, null, null, null, null);
                }
                Long transaction = null;
                if (entityType != null) {
                    transaction = Long.parseLong(transactionId.substring(1).trim());
                }

                TransactionTypeEnumData transactionTypeEnumData = null;

                if (PortfolioAccountType.fromInt(entityTypeId).isLoanAccount()) {
                    final LoanTransactionEnumData loanTransactionType = LoanEnumerations.transactionType(JdbcSupport.getInteger(rs,
                            "loanTransactionType"));
                    transactionTypeEnumData = new TransactionTypeEnumData(loanTransactionType.id(), loanTransactionType.getCode(),
                            loanTransactionType.getValue());
                } else if (PortfolioAccountType.fromInt(entityTypeId).isSavingsAccount()) {
                    final SavingsAccountTransactionEnumData savingsTransactionType = SavingsEnumerations.transactionType(JdbcSupport
                            .getInteger(rs, "savingsTransactionType"));
                    transactionTypeEnumData = new TransactionTypeEnumData(savingsTransactionType.getId(), savingsTransactionType.getCode(),
                            savingsTransactionType.getValue());
                }

                transactionDetailData = new TransactionDetailData(transaction, paymentDetailData, noteData, transactionTypeEnumData);
            }
            return new JournalEntryData(id, officeId, officeName, glAccountName, glAccountId, glCode, accountType, transactionDate,
                    entryType, amount, transactionId, manualEntry, entityType, entityId, createdByUserId, createdDate, createdByUserName,
                    comments, reversed, referenceNumber, officeRunningBalance, organizationRunningBalance, runningBalanceComputed,
                    transactionDetailData, currency);
        }
    }

    @Override
    public Page<JournalEntryData> retrieveAll(final SearchParameters searchParameters, final Long glAccountId,
            final Boolean onlyManualEntries, final Date fromDate, final Date toDate, final String transactionId, final Integer entityType,
            final JournalEntryAssociationParametersData associationParametersData) {

        GLJournalEntryMapper rm = new GLJournalEntryMapper(associationParametersData);
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(rm.schema());

        final Object[] objectArray = new Object[15];
        int arrayPos = 0;
        String whereClose = " where ";

        if (StringUtils.isNotBlank(transactionId)) {
            sqlBuilder.append(whereClose + " journalEntry.transaction_id = ?");
            objectArray[arrayPos] = transactionId;
            arrayPos = arrayPos + 1;

            whereClose = " and ";
        }

        if (entityType != null && entityType != 0 && (onlyManualEntries == null)) {

            sqlBuilder.append(whereClose + " journalEntry.entity_type_enum = ?");

            objectArray[arrayPos] = entityType;
            arrayPos = arrayPos + 1;

            whereClose = " and ";
        }

        if (searchParameters.isOfficeIdPassed()) {
            sqlBuilder.append(whereClose + " journalEntry.office_id = ?");
            objectArray[arrayPos] = searchParameters.getOfficeId();
            arrayPos = arrayPos + 1;

            whereClose = " and ";
        }

        if (glAccountId != null && glAccountId != 0) {
            sqlBuilder.append(whereClose + " journalEntry.account_id = ?");
            objectArray[arrayPos] = glAccountId;
            arrayPos = arrayPos + 1;

            whereClose = " and ";
        }

        if (fromDate != null || toDate != null) {
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateString = null;
            String toDateString = null;
            if (fromDate != null && toDate != null) {
                sqlBuilder.append(whereClose + " journalEntry.entry_date between ? and ? ");

                whereClose = " and ";

                fromDateString = df.format(fromDate);
                toDateString = df.format(toDate);
                objectArray[arrayPos] = fromDateString;
                arrayPos = arrayPos + 1;
                objectArray[arrayPos] = toDateString;
                arrayPos = arrayPos + 1;
            } else if (fromDate != null) {
                sqlBuilder.append(whereClose + " journalEntry.entry_date >= ? ");
                fromDateString = df.format(fromDate);
                objectArray[arrayPos] = fromDateString;
                arrayPos = arrayPos + 1;
                whereClose = " and ";

            } else if (toDate != null) {
                sqlBuilder.append(whereClose + " journalEntry.entry_date <= ? ");
                toDateString = df.format(toDate);
                objectArray[arrayPos] = toDateString;
                arrayPos = arrayPos + 1;

                whereClose = " and ";
            }
        }

        if (onlyManualEntries != null) {
            if (onlyManualEntries) {
                sqlBuilder.append(whereClose + " journalEntry.manual_entry = 1");

                whereClose = " and ";
            }
        }

        if (searchParameters.isLoanIdPassed()) {
            sqlBuilder.append(whereClose + " journalEntry.loan_transaction_id  in (select id from m_loan_transaction where loan_id = ?)");
            objectArray[arrayPos] = searchParameters.getLoanId();
            arrayPos = arrayPos + 1;

            whereClose = " and ";
        }
        if (searchParameters.isSavingsIdPassed()) {
            sqlBuilder
                    .append(whereClose
                            + " journalEntry.savings_transaction_id in (select id from m_savings_account_transaction where savings_account_id = ?)");
            objectArray[arrayPos] = searchParameters.getSavingsId();
            arrayPos = arrayPos + 1;

            whereClose = " and ";
        }

        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());

            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
            }
        } else {
            sqlBuilder.append(" order by journalEntry.entry_date, journalEntry.id");
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final Object[] finalObjectArray = Arrays.copyOf(objectArray, arrayPos);
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), finalObjectArray, rm);
    }

    @Override
    public JournalEntryData retrieveGLJournalEntryById(final long glJournalEntryId,
            JournalEntryAssociationParametersData associationParametersData) {
        try {

            final GLJournalEntryMapper rm = new GLJournalEntryMapper(associationParametersData);
            final String sql = "select " + rm.schema() + " where journalEntry.id = ?";

            final JournalEntryData glJournalEntryData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { glJournalEntryId });

            return glJournalEntryData;
        } catch (final EmptyResultDataAccessException e) {
            throw new JournalEntriesNotFoundException(glJournalEntryId);
        }
    }

    @Override
    public OfficeOpeningBalancesData retrieveOfficeOpeningBalances(final Long officeId) {

        final FinancialActivityAccount financialActivityAccountId = this.financialActivityAccountRepositoryWrapper
                .findByFinancialActivityTypeWithNotFoundDetection(300);
        final Long contraId = financialActivityAccountId.getGlAccount().getId();
        if (contraId == null) { throw new GeneralPlatformDomainRuleException(
                "error.msg.financial.activity.mapping.opening.balance.contra.account.cannot.be.null",
                "office-opening-balances-contra-account value can not be null", "office-opening-balances-contra-account"); }

        final JournalEntryAssociationParametersData associationParametersData = new JournalEntryAssociationParametersData();
        final GLAccountData contraAccount = this.glAccountReadPlatformService.retrieveGLAccountById(contraId, associationParametersData);
        if (!GLAccountType.fromInt(contraAccount.getTypeId()).isEquityType()) { throw new GeneralPlatformDomainRuleException(
                "error.msg.configuration.opening.balance.contra.account.value.is.invalid.account.type",
                "Global configuration 'office-opening-balances-contra-account' value is not an equity type account", contraId); }

        final OfficeData officeData = this.officeReadPlatformService.retrieveOffice(officeId);
        final List<JournalEntryData> allOpeningTransactions = populateAllTransactionsFromGLAccounts(contraId);
        final String contraTransactionId = retrieveContraAccountTransactionId(officeId, contraId);

        List<JournalEntryData> existingOpeningBalanceTransactions = new ArrayList<>();
        if (StringUtils.isNotBlank(contraTransactionId)) {
            existingOpeningBalanceTransactions = retrieveOfficeBalanceTransactions(officeId, contraTransactionId);
        }
        final List<JournalEntryData> transactions = populateOpeningBalances(existingOpeningBalanceTransactions, allOpeningTransactions);
        final List<JournalEntryData> assetAccountOpeningBalances = new ArrayList<>();
        final List<JournalEntryData> liabityAccountOpeningBalances = new ArrayList<>();
        final List<JournalEntryData> incomeAccountOpeningBalances = new ArrayList<>();
        final List<JournalEntryData> equityAccountOpeningBalances = new ArrayList<>();
        final List<JournalEntryData> expenseAccountOpeningBalances = new ArrayList<>();

        for (final JournalEntryData journalEntryData : transactions) {
            final GLAccountType type = GLAccountType.fromInt(journalEntryData.getGlAccountType().getId().intValue());
            if (type.isAssetType()) {
                assetAccountOpeningBalances.add(journalEntryData);
            } else if (type.isLiabilityType()) {
                liabityAccountOpeningBalances.add(journalEntryData);
            } else if (type.isEquityType()) {
                equityAccountOpeningBalances.add(journalEntryData);
            } else if (type.isIncomeType()) {
                incomeAccountOpeningBalances.add(journalEntryData);
            } else if (type.isExpenseType()) {
                expenseAccountOpeningBalances.add(journalEntryData);
            }
        }

        final LocalDate transactionDate = DateUtils.getLocalDateOfTenant();

        final OfficeOpeningBalancesData officeOpeningBalancesData = OfficeOpeningBalancesData.createNew(officeId, officeData.name(),
                transactionDate, contraAccount, assetAccountOpeningBalances, liabityAccountOpeningBalances, incomeAccountOpeningBalances,
                equityAccountOpeningBalances, expenseAccountOpeningBalances);
        return officeOpeningBalancesData;
    }

    private List<JournalEntryData> populateOpeningBalances(final List<JournalEntryData> existingOpeningBalanceTransactions,
            final List<JournalEntryData> allOpeningTransactions) {
        final List<JournalEntryData> allOpeningBalnceTransactions = new ArrayList<>(allOpeningTransactions.size());
        for (final JournalEntryData newOpeningBalanceTransaction : allOpeningTransactions) {
            boolean isNewTransactionAddedToCollection = false;
            for (final JournalEntryData existingOpeningBalanceTransaction : existingOpeningBalanceTransactions) {
                if (newOpeningBalanceTransaction.getGlAccountId().equals(existingOpeningBalanceTransaction.getGlAccountId())) {
                    allOpeningBalnceTransactions.add(existingOpeningBalanceTransaction);
                    isNewTransactionAddedToCollection = true;
                    break;
                }
            }
            if (!isNewTransactionAddedToCollection) {
                allOpeningBalnceTransactions.add(newOpeningBalanceTransaction);
            }
        }
        return allOpeningBalnceTransactions;
    }

    private List<JournalEntryData> populateAllTransactionsFromGLAccounts(final Long contraId) {
        final List<GLAccountData> glAccounts = this.glAccountReadPlatformService.retrieveAllEnabledDetailGLAccounts();
        final List<JournalEntryData> openingBalanceTransactions = new ArrayList<>(glAccounts.size());

        for (final GLAccountData glAccountData : glAccounts) {
            if (!contraId.equals(glAccountData.getId())) {
                final JournalEntryData openingBalanceTransaction = JournalEntryData.fromGLAccountData(glAccountData);
                openingBalanceTransactions.add(openingBalanceTransaction);
            }
        }
        return openingBalanceTransactions;
    }

    private List<JournalEntryData> retrieveOfficeBalanceTransactions(final Long officeId, final String transactionId) {
        final Long contraId = null;
        return retrieveContraTransactions(officeId, contraId, transactionId).getPageItems();
    }

    private String retrieveContraAccountTransactionId(final Long officeId, final Long contraId) {
        final String transactionId = "";
        final Page<JournalEntryData> contraJournalEntries = retrieveContraTransactions(officeId, contraId, transactionId);
        if (!CollectionUtils.isEmpty(contraJournalEntries.getPageItems())) {
            final JournalEntryData contraTransaction = contraJournalEntries.getPageItems().get(
                    contraJournalEntries.getPageItems().size() - 1);
            return contraTransaction.getTransactionId();
        }
        return transactionId;
    }

    private Page<JournalEntryData> retrieveContraTransactions(final Long officeId, final Long contraId, final String transactionId) {
        final Integer offset = 0;
        final Integer limit = null;
        final String orderBy = "journalEntry.id";
        final String sortOrder = "ASC";
        final Integer entityType = null;
        final Boolean onlyManualEntries = null;
        final Date fromDate = null;
        final Date toDate = null;
        final JournalEntryAssociationParametersData associationParametersData = null;
        final Long loanId = null;
        final Long savingsId = null;

        final SearchParameters searchParameters = SearchParameters.forJournalEntries(officeId, offset, limit, orderBy, sortOrder, loanId,
                savingsId);
        return retrieveAll(searchParameters, contraId, onlyManualEntries, fromDate, toDate, transactionId, entityType,
                associationParametersData);

    }
}
