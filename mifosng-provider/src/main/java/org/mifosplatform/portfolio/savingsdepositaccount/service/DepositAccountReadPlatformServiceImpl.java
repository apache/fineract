package org.mifosplatform.portfolio.savingsdepositaccount.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.portfolio.savingsaccountproduct.service.SavingsDepositEnumerations;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountData;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountTransactionData;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountsForLookup;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositPermissionData;
import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductData;
import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductLookup;
import org.mifosplatform.portfolio.savingsdepositproduct.service.DepositProductReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class DepositAccountReadPlatformServiceImpl implements DepositAccountReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final DepositProductReadPlatformService depositProductReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;

    @Autowired
    public DepositAccountReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            final DepositProductReadPlatformService depositProductReadPlatformService,
            final ClientReadPlatformService clientReadPlatformService) {
        this.context = context;
        jdbcTemplate = new JdbcTemplate(dataSource);
        this.depositProductReadPlatformService = depositProductReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
    }

    @Override
    public Collection<DepositAccountData> retrieveAllDepositAccounts() {

        this.context.authenticatedUser();

        DepositAccountMapper mapper = new DepositAccountMapper();

        String sql = "select " + mapper.schema() + " where da.is_deleted=0";

        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }

    @Override
    public DepositAccountData retrieveDepositAccount(final Long accountId) {
        try {
            DepositAccountMapper mapper = new DepositAccountMapper();

            String sql = "select " + mapper.schema() + " where da.id = ? and da.is_deleted=0";

            DepositAccountData depositAccountData = this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { accountId });

            // FIXME - kw - the call to find transactions for deposit account to
            // add to the deposit account payload can be decided at the api
            // level
            // depending on whether the requester wants them or not instead of
            // always getting the transactions
            DepositAccountTransactionMapper transactionMapper = new DepositAccountTransactionMapper();
            String transactionSchema = "select " + transactionMapper.schema() + " where txn.deposit_account_id = ? ";

            Collection<DepositAccountTransactionData> depositAccountTransactions = this.jdbcTemplate.query(transactionSchema,
                    transactionMapper, new Object[] { accountId });
            if (!depositAccountTransactions.isEmpty()) {
                DepositPermissionData permissions = null;
                depositAccountData = new DepositAccountData(depositAccountData, permissions, depositAccountTransactions);
            }

            return depositAccountData;
        } catch (EmptyResultDataAccessException e) {
            throw new LoanProductNotFoundException(accountId);
        }
    }

    @Override
    public DepositAccountData retrieveNewDepositAccountDetails(final Long clientId, final Long productId) {

        context.authenticatedUser();

        ClientData clientAccount = this.clientReadPlatformService.retrieveIndividualClient(clientId);

        DepositAccountData accountData = null;

        Collection<DepositProductLookup> depositProducts = this.depositProductReadPlatformService.retrieveAllDepositProductsForLookup();

        if (productId != null) {
            DepositProductData selectedProduct = findDepositProductById(depositProducts, productId);

            CurrencyData currency = new CurrencyData(selectedProduct.getCurrencyCode(), "", selectedProduct.getDigitsAfterDecimal(), "", "");

            accountData = new DepositAccountData(clientAccount.id(), clientAccount.displayName(), selectedProduct.getId(),
                    selectedProduct.getName(), currency, selectedProduct.getMinimumBalance(),
                    selectedProduct.getMaturityDefaultInterestRate(), selectedProduct.getTenureInMonths(),
                    selectedProduct.getInterestCompoundedEvery(), selectedProduct.getInterestCompoundedEveryPeriodType(),
                    selectedProduct.isRenewalAllowed(), selectedProduct.isPreClosureAllowed(), selectedProduct.getPreClosureInterestRate(),
                    selectedProduct.isInterestCompoundingAllowed(),selectedProduct.getLockinPeriod(),selectedProduct.getLockinPeriodType(),selectedProduct.isLockinPeriodAllowed());
        } else {
            accountData = DepositAccountData.createFrom(clientAccount.id(), clientAccount.displayName());
        }

        return accountData;
    }

    private DepositProductData findDepositProductById(final Collection<DepositProductLookup> depositProducts, final Long productId) {
        DepositProductData match = this.depositProductReadPlatformService.retrieveNewDepositProductDetails();
        for (DepositProductLookup depositProductLookup : depositProducts) {
            if (depositProductLookup.hasId(productId)) {
                match = this.depositProductReadPlatformService.retrieveDepositProductData(depositProductLookup.id());
                break;
            }
        }
        return match;
    }

    private static final class DepositAccountMapper implements RowMapper<DepositAccountData> {

        public String schema() {
            return "da.id as id, da.external_id as externalId, da.client_id as clientId, da.product_id as productId, "
                    + " da.currency_code as currencyCode, da.currency_digits as currencyDigits, "
                    + " da.deposit_amount as depositAmount, da.status_enum as statusId, "
                    + " da.maturity_nominal_interest_rate as interestRate, da.tenure_months as termInMonths, da.projected_commencement_date as projectedCommencementDate,"
                    + " da.actual_commencement_date as actualCommencementDate, da.matures_on_date as maturedOn,"
                    + " da.projected_interest_accrued_on_maturity as projectedInterestAccrued, da.actual_interest_accrued as actualInterestAccrued, "
                    + " da.projected_total_maturity_amount as projectedMaturityAmount, da.actual_total_amount as actualMaturityAmount, "
                    + " da.interest_compounded_every as interestCompoundedEvery, da.interest_compounded_every_period_enum as interestCompoundedEveryPeriodType, "
                    + " da.is_renewal_allowed as renewalAllowed, da.is_preclosure_allowed as preClosureAllowed, da.pre_closure_interest_rate as preClosureInterestRate, "
                    + " da.withdrawnon_date as withdrawnonDate, da.rejectedon_date as rejectedonDate, da.closedon_date as closedonDate, "
                    + " da.interest_paid as interestPaid, da.is_interest_withdrawable as isInterestWithdrawable, da.is_compounding_interest_allowed as interestCompoundingAllowed, "
                    + " da.is_lock_in_period_allowed as isLockinPeriodAllowed, da.lock_in_period as lockinPeriod, da.lock_in_period_type lockinPeriodType, "
                    + " da.available_interest as availableInterest, da.interest_posted_amount as interestPostedAmount, da.last_interest_posted_date as lastInterestPostedDate, da.next_interest_posting_date as nextInterestPostedDate,"
                    + " c.firstname as firstname, c.lastname as lastname, pd.name as productName, c.image_key as imageKey, "
                    + " curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, curr.display_symbol as currencyDisplaySymbol, "
                    + " ecd.father_name as fatherName, ecd.client_address as clientAddress " + " from m_deposit_account da "
                    + " join m_currency curr on curr.code = da.currency_code " + " join m_client c on c.id = da.client_id "
                    + " join m_product_deposit pd on pd.id = da.product_id"
                    + " left join extra_client_details ecd on da.client_id = ecd.client_id ";
        }

        @Override
        public DepositAccountData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            String externalId = rs.getString("externalId");
            Long clientId = rs.getLong("clientId");
            String clientName = rs.getString("firstname") + " " + rs.getString("lastname");
            Long productId = rs.getLong("productId");
            String productName = rs.getString("productName");

            Integer statusId = JdbcSupport.getInteger(rs, "statusId");
            EnumOptionData status = DepositAccountEnumerations.status(statusId);

            String currencyCode = rs.getString("currencyCode");
            String currencyName = rs.getString("currencyName");
            String currencyNameCode = rs.getString("currencyNameCode");
            String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, currencyDisplaySymbol,
                    currencyNameCode);

            BigDecimal depositAmount = rs.getBigDecimal("depositAmount");
            BigDecimal interestRate = rs.getBigDecimal("interestRate");

            Integer termInMonths = JdbcSupport.getInteger(rs, "termInMonths");

            LocalDate projectedCommencementDate = JdbcSupport.getLocalDate(rs, "projectedCommencementDate");
            LocalDate actualCommencementDate = JdbcSupport.getLocalDate(rs, "actualCommencementDate");
            LocalDate maturedOn = JdbcSupport.getLocalDate(rs, "maturedOn");
            BigDecimal projectedInterestAccrued = rs.getBigDecimal("projectedInterestAccrued");
            BigDecimal actualInterestAccrued = rs.getBigDecimal("actualInterestAccrued");

            BigDecimal projectedMaturityAmount = rs.getBigDecimal("projectedMaturityAmount");
            BigDecimal actualMaturityAmount = rs.getBigDecimal("actualMaturityAmount");

            Integer interestCompoundedEvery = JdbcSupport.getInteger(rs, "interestCompoundedEvery");
            Integer interestCompoundedEveryPeriodTypeValue = JdbcSupport.getInteger(rs, "interestCompoundedEveryPeriodType");
            EnumOptionData interestCompoundedEveryPeriodType = SavingsDepositEnumerations
                    .interestCompoundingPeriodType(interestCompoundedEveryPeriodTypeValue);

            boolean renewalAllowed = rs.getBoolean("renewalAllowed");
            boolean preClosureAllowed = rs.getBoolean("preClosureAllowed");
            boolean interestCompoundingAllowed = rs.getBoolean("interestCompoundingAllowed");

            BigDecimal preClosureInterestRate = rs.getBigDecimal("preClosureInterestRate");

            LocalDate withdrawnonDate = JdbcSupport.getLocalDate(rs, "withdrawnonDate");
            LocalDate rejectedonDate = JdbcSupport.getLocalDate(rs, "rejectedonDate");
            LocalDate closedonDate = JdbcSupport.getLocalDate(rs, "closedonDate");

            boolean isInterestWithdrawable = rs.getBoolean("isInterestWithdrawable");
            BigDecimal interestPaid = rs.getBigDecimal("interestPaid");

            boolean isLockinPeriodAllowed = rs.getBoolean("isLockinPeriodAllowed");
            Integer lockinPeriod = JdbcSupport.getInteger(rs, "lockinPeriod");
            Integer lockinPeriodTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodType");
            EnumOptionData lockinPeriodType = SavingsDepositEnumerations.interestCompoundingPeriodType(lockinPeriodTypeValue);

            BigDecimal availableInterest = rs.getBigDecimal("availableInterest");
            BigDecimal interestPostedAmount = rs.getBigDecimal("interestPostedAmount");
            LocalDate lastInterestPostedDate = JdbcSupport.getLocalDate(rs, "lastInterestPostedDate");
            LocalDate nextInterestPostedDate = JdbcSupport.getLocalDate(rs, "nextInterestPostedDate");
            String imageKey = rs.getString("imageKey");
            String fatherName = rs.getString("fatherName");
            String address = rs.getString("clientAddress");

            return new DepositAccountData(id, externalId, status, clientId, clientName, productId, productName, currencyData,
                    depositAmount, interestRate, termInMonths, projectedCommencementDate, actualCommencementDate, maturedOn,
                    projectedInterestAccrued, actualInterestAccrued, projectedMaturityAmount, actualMaturityAmount,
                    interestCompoundedEvery, interestCompoundedEveryPeriodType, renewalAllowed, preClosureAllowed, preClosureInterestRate,
                    withdrawnonDate, rejectedonDate, closedonDate, isInterestWithdrawable, interestPaid, interestCompoundingAllowed,
                    isLockinPeriodAllowed, lockinPeriod, lockinPeriodType, availableInterest, interestPostedAmount, lastInterestPostedDate,
                    nextInterestPostedDate, fatherName, address, imageKey);
        }
    }

    private static final class DepositAccountTransactionMapper implements RowMapper<DepositAccountTransactionData> {

        public String schema() {
            return " txn.id as transactionId, txn.deposit_account_id as accountId, txn.transaction_type_enum as transactionType, txn.transaction_date as transactionDate, txn.amount as transactionAmount, txn.interest as interestAmount, "
                    + "txn.total as total from m_deposit_account_transaction txn";
        }

        @Override
        public DepositAccountTransactionData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            Long transactionId = rs.getLong("transactionId");
            Long accountId = rs.getLong("accountId");
            Integer transactionTypeValue = JdbcSupport.getInteger(rs, "transactionType");
            EnumOptionData transactionType = DepositAccountTransactionEnumerations.depositType(transactionTypeValue);
            LocalDate transactionDate = JdbcSupport.getLocalDate(rs, "transactionDate");
            BigDecimal transactionAmount = rs.getBigDecimal("transactionAmount");
            BigDecimal interestAmount = rs.getBigDecimal("interestAmount");
            BigDecimal total = rs.getBigDecimal("total");

            return new DepositAccountTransactionData(transactionId, accountId, transactionType, transactionDate, transactionAmount,
                    interestAmount, total);
        }

    }

    @Override
    public DepositPermissionData retrieveDepositAccountsPermissions(DepositAccountData depositAccountData) {
        boolean pendingApproval = (depositAccountData.getStatus().getId().equals(100L));
        boolean undoApprovalAllowed = (depositAccountData.getStatus().getId().equals(300L));
        boolean renewelAllowed = false;
        if (depositAccountData.getMaturedOn() != null) {
            if (new LocalDate().isAfter(depositAccountData.getMaturedOn())/*
                                                                           * ||
                                                                           * depositAccountData
                                                                           * .
                                                                           * getMaturedOn
                                                                           * ().
                                                                           * isEqual
                                                                           * (
                                                                           * new
                                                                           * LocalDate
                                                                           * ())
                                                                           */) {
                renewelAllowed = depositAccountData.isRenewalAllowed();
            }
        }
        boolean rejectAllowed = pendingApproval;
        boolean withdrawnByApplicantAllowed = pendingApproval;
        boolean interestWithdrawAllowed = depositAccountData.isInterestWithdrawable();
        boolean isMaturedDepositAccount = (depositAccountData.getStatus().getId().equals(700L));

        return new DepositPermissionData(rejectAllowed, withdrawnByApplicantAllowed, undoApprovalAllowed, pendingApproval,
                interestWithdrawAllowed, renewelAllowed, isMaturedDepositAccount);
    }

    @Override
    public BigDecimal retrieveAvailableInterestForWithdrawal(DepositAccountData account) {
        BigDecimal interstGettingForPeriod = BigDecimal.valueOf(account.getActualInterestAccrued().doubleValue()
                / new Double(account.getTenureInMonths()));
        Integer noOfMonthsforInterestCal = Months.monthsBetween(account.getActualCommencementDate(), new LocalDate()).getMonths();
        Integer noOfPeriods = noOfMonthsforInterestCal / account.getInterestCompoundedEvery();
        return BigDecimal.valueOf(interstGettingForPeriod.multiply(new BigDecimal(noOfPeriods)).doubleValue()
                - account.getInterestPaid().doubleValue());
    }

    @Override
    public Collection<DepositAccountsForLookup> retrieveDepositAccountForLookup() {

        this.context.authenticatedUser();
        DepositAccountLookupMapper mapper = new DepositAccountLookupMapper();
        String sql = "select " + mapper.depositAccountLookupSchema();
        return this.jdbcTemplate.query(sql, mapper, new Object[] {});

    }

    private static final class DepositAccountLookupMapper implements RowMapper<DepositAccountsForLookup> {

        public String depositAccountLookupSchema() {
            return " da.id as id from m_deposit_account da where da.status_enum=300 and da.is_deleted=0";
        }

        @Override
        public DepositAccountsForLookup mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            return new DepositAccountsForLookup(id);
        }

    }

}