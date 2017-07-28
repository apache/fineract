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
package org.apache.fineract.portfolio.shareaccounts.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.accountdetails.data.ShareAccountSummaryData;
import org.apache.fineract.portfolio.accounts.constants.AccountsApiConstants;
import org.apache.fineract.portfolio.accounts.constants.ShareAccountApiConstants;
import org.apache.fineract.portfolio.accounts.data.AccountData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.products.constants.ProductsApiConstants;
import org.apache.fineract.portfolio.products.data.ProductData;
import org.apache.fineract.portfolio.products.service.ProductReadPlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountApplicationTimelineData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountChargeData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountDividendData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountStatusEnumData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountTransactionData;
import org.apache.fineract.portfolio.shareaccounts.domain.PurchasedSharesStatusType;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountStatusType;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductData;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductMarketPriceData;
import org.apache.fineract.portfolio.shareproducts.service.ShareProductDropdownReadPlatformService;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service(value = "share" + AccountsApiConstants.READPLATFORM_NAME)
public class ShareAccountReadPlatformServiceImpl implements ShareAccountReadPlatformService {

    private final ApplicationContext applicationContext;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final ShareProductDropdownReadPlatformService shareProductDropdownReadPlatformService;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ShareAccountChargeReadPlatformService shareAccountChargeReadPlatformService;
    private final PurchasedSharesReadPlatformService purchasedSharesReadPlatformService;
    private final JdbcTemplate jdbcTemplate;
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    private final PaginationHelper<AccountData> shareAccountDataPaginationHelper = new PaginationHelper<>();
    
    @Autowired
    public ShareAccountReadPlatformServiceImpl(final RoutingDataSource dataSource, final ApplicationContext applicationContext,
            final ChargeReadPlatformService chargeReadPlatformService,
            final ShareProductDropdownReadPlatformService shareProductDropdownReadPlatformService,
            final SavingsAccountReadPlatformService savingsAccountReadPlatformService,
            final ClientReadPlatformService clientReadPlatformService,
            final ShareAccountChargeReadPlatformService shareAccountChargeReadPlatformService,
            final PurchasedSharesReadPlatformService purchasedSharesReadPlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.applicationContext = applicationContext;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.shareProductDropdownReadPlatformService = shareProductDropdownReadPlatformService;
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.shareAccountChargeReadPlatformService = shareAccountChargeReadPlatformService;
        this.purchasedSharesReadPlatformService = purchasedSharesReadPlatformService;

    }

    @Override
    public ShareAccountData retrieveTemplate(Long clientId, Long productId) {
        ShareAccountData toReturn = null;
        String serviceName = "share" + ProductsApiConstants.READPLATFORM_NAME;
        ProductReadPlatformService service = (ProductReadPlatformService) this.applicationContext.getBean(serviceName);
        ClientData client = this.clientReadPlatformService.retrieveOne(clientId);

        if (productId != null) {
            final ShareProductData productData = (ShareProductData) service.retrieveOne(productId, false);
            final BigDecimal marketPrice = deriveMarketPrice(productData);
            final Collection<ChargeData> productCharges = this.chargeReadPlatformService.retrieveShareProductCharges(productId);
            final Collection<ShareAccountChargeData> charges = convertChargesToShareAccountCharges(productCharges);
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = this.shareProductDropdownReadPlatformService
                    .retrieveLockinPeriodFrequencyTypeOptions();
            final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions = this.shareProductDropdownReadPlatformService
                    .retrieveMinimumActivePeriodFrequencyTypeOptions();
            final Collection<SavingsAccountData> clientSavingsAccounts = this.savingsAccountReadPlatformService
                    .retrieveActiveForLookup(clientId, DepositAccountType.SAVINGS_DEPOSIT, productData.getCurrency().code());
            toReturn = new ShareAccountData(client.id(), client.displayName(), productData.getCurrency(), charges, marketPrice,
                    minimumActivePeriodFrequencyTypeOptions, lockinPeriodFrequencyTypeOptions, clientSavingsAccounts, productData.getNominaltShares());
        } else {
            Collection<ProductData> productOptions = service.retrieveAllForLookup();
            final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSharesApplicableCharges();
            toReturn = new ShareAccountData(client.id(), client.displayName(), productOptions, chargeOptions);
        }
        return toReturn;
    }

    private BigDecimal deriveMarketPrice(final ShareProductData shareProductData) {
        BigDecimal marketValue = shareProductData.getUnitPrice();
        Collection<ShareProductMarketPriceData> marketDataSet = shareProductData.getMarketPrice();
        if (marketDataSet != null && !marketDataSet.isEmpty()) {
            Date currentDate = DateUtils.getDateOfTenant();
            for (ShareProductMarketPriceData data : marketDataSet) {
                Date futureDate = data.getStartDate();
                if (currentDate.after(futureDate)) {
                    marketValue = data.getShareValue();
                }
            }
        }
        return marketValue;
    }

    @Override
    public ShareAccountData retrieveOne(final Long id, final boolean includeTemplate) {
        Collection<ShareAccountChargeData> charges = this.shareAccountChargeReadPlatformService.retrieveAccountCharges(id, "active");
        Collection<ShareAccountTransactionData> purchasedShares = this.purchasedSharesReadPlatformService.retrievePurchasedShares(id);
        
        ShareAccountMapper mapper = new ShareAccountMapper(charges, purchasedShares);
        String query = "select " + mapper.schema() + "where sa.id=?";
        ShareAccountData data = (ShareAccountData)this.jdbcTemplate.queryForObject(query, mapper, new Object[] { id });
        String serviceName = "share" + ProductsApiConstants.READPLATFORM_NAME;
        ProductReadPlatformService service = (ProductReadPlatformService) this.applicationContext.getBean(serviceName);
        final ShareProductData productData = (ShareProductData) service.retrieveOne(data.getProductId(), false);
        final BigDecimal currentMarketPrice = deriveMarketPrice(productData);
        data.setCurrentMarketPrice(currentMarketPrice);
        if(!includeTemplate) {
            Collection<ShareAccountDividendData> dividends = this.retrieveAssociatedDividends(id) ;
            data.setDividends(dividends);    
        }
        if (includeTemplate) {
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = this.shareProductDropdownReadPlatformService
                    .retrieveLockinPeriodFrequencyTypeOptions();
            final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions = lockinPeriodFrequencyTypeOptions;
            final Collection<SavingsAccountData> clientSavingsAccounts = this.savingsAccountReadPlatformService
                    .retrieveActiveForLookup(data.getClientId(), DepositAccountType.SAVINGS_DEPOSIT, productData.getCurrency().code());
            Collection<ProductData> productOptions = service.retrieveAllForLookup();
            final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSharesApplicableCharges();
            data = ShareAccountData.template(data, productOptions, chargeOptions, clientSavingsAccounts, lockinPeriodFrequencyTypeOptions,
                    minimumActivePeriodFrequencyTypeOptions);
        }
        return data;
    }

    private Collection<ShareAccountDividendData> retrieveAssociatedDividends(final Long shareAccountId) {
        ShareAccountDividendRowMapper mapper = new ShareAccountDividendRowMapper();
        String query = "select " + mapper.schema() + "where sadd.account_id=?";
        return this.jdbcTemplate.query(query, mapper, new Object[] { shareAccountId });
    }

    @Override
    public Page<AccountData> retrieveAll(final Integer offSet, final Integer limit) {
        final Collection<ShareAccountChargeData> charges = null ;
        final Collection<ShareAccountTransactionData> purchasedShares = null ;
        ShareAccountMapper mapper = new ShareAccountMapper(charges, purchasedShares) ;
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(mapper.schema());
        sqlBuilder.append(" where sa.status_enum = ? ");
        if (limit != null) {
            sqlBuilder.append(" limit ").append(limit);
        }
        if (offSet != null) {
            sqlBuilder.append(" offset ").append(offSet);
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        Object[] whereClauseItemsitems = new Object[] {ShareAccountStatusType.ACTIVE.getValue()};
        return this.shareAccountDataPaginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),
                whereClauseItemsitems, mapper);
    }

    @Override
    public Set<String> getResponseDataParams() {
        return ShareAccountApiConstants.supportedParameters;
    }

    @Override
    public Collection<ShareAccountData> retrieveAllShareAccountDataForDividends(final Long id, final boolean fetchInActiveAccounts,
            final LocalDate startDate) {
        ShareAccountMapperForDividents mapper = new ShareAccountMapperForDividents();
        StringBuilder sb = new StringBuilder("select ");
        sb.append(mapper.schema);
        sb.append(" where sa.product_id = ? ");

        List<Object> params = new ArrayList<>(3);
        params.add(id);
        params.add(ShareAccountStatusType.ACTIVE.getValue());
        if (fetchInActiveAccounts) {
            sb.append(" and (sa.status_enum = ? or (sa.status_enum = ? ");
            sb.append(" and sa.closed_date >  ?)) ");
            params.add(ShareAccountStatusType.CLOSED.getValue());
            params.add(formatter.print(startDate));
        } else {
            sb.append(" and sa.status_enum = ? ");
        }
        sb.append(" and saps.status_enum = ?");
        params.add(PurchasedSharesStatusType.APPROVED.getValue());
        Object[] whereClauseItems = params.toArray();
        return this.jdbcTemplate.query(sb.toString(), whereClauseItems, mapper);
    }

    public Collection<ShareAccountChargeData> convertChargesToShareAccountCharges(Collection<ChargeData> productCharges) {
        final Collection<ShareAccountChargeData> savingsCharges = new ArrayList<>();
        for (final ChargeData chargeData : productCharges) {
            final ShareAccountChargeData savingsCharge = chargeData.toShareAccountChargeData();
            savingsCharges.add(savingsCharge);
        }
        return savingsCharges;
    }

    private final static class ShareAccountMapper implements RowMapper<AccountData> {

        private final Collection<ShareAccountChargeData> charges;
        private final Collection<ShareAccountTransactionData> purchasedShares;

        private final String schema;

        public ShareAccountMapper(final Collection<ShareAccountChargeData> charges, final Collection<ShareAccountTransactionData> purchasedShares) {
            this.charges = charges;
            this.purchasedShares = purchasedShares;
            StringBuffer buff = new StringBuffer()
                    .append("sa.id as id, sa.external_id as externalId, sa.status_enum as statusEnum, ")
                    .append("sa.savings_account_id, msa.account_no as savingsAccNo, ")
                    .append("c.id as clientId, c.display_name as clientName, ")
                    .append("sa.account_no as accountNo, sa.total_approved_shares as approvedShares, sa.total_pending_shares as pendingShares, ")
                    .append("sa.savings_account_id as savingsAccountNo, sa.minimum_active_period_frequency as minimumactivePeriod, ")
                    .append("sa.minimum_active_period_frequency_enum as minimumactivePeriodEnum, ")
                    .append("sa.lockin_period_frequency as lockinPeriod, sa.lockin_period_frequency_enum as lockinPeriodEnum, ")
                    .append("sa.allow_dividends_inactive_clients as allowdividendsforinactiveclients, ")
                    .append("sa.submitted_date as submittedDate, sbu.username as submittedByUsername, ")
                    .append("sbu.firstname as submittedByFirstname, sbu.lastname as submittedByLastname, ")
                    .append("sa.rejected_date as rejectedDate, rbu.username as rejectedByUsername, ")
                    .append("rbu.firstname as rejectedByFirstname, rbu.lastname as rejectedByLastname, ")
                    .append("sa.approved_date as approvedDate, abu.username as approvedByUsername, ")
                    .append("abu.firstname as approvedByFirstname, abu.lastname as approvedByLastname, ")
                    .append("sa.activated_date as activatedDate, avbu.username as activatedByUsername, ")
                    .append("avbu.firstname as activatedByFirstname, avbu.lastname as activatedByLastname, ")
                    .append("sa.closed_date as closedDate, cbu.username as closedByUsername, ")
                    .append("cbu.firstname as closedByFirstname, cbu.lastname as closedByLastname, ")
                    .append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ")
                    .append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ")
                    .append("curr.display_symbol as currencyDisplaySymbol, sa.product_id as productId, p.name as productName, p.short_name as shortProductName ")
                    .append("from m_share_account sa ").append("join m_share_product as p on p.id = sa.product_id ")
                    .append("join m_currency curr on curr.code = sa.currency_code ").append("left join m_client c ON c.id = sa.client_id ")
                    .append("left join m_appuser sbu on sbu.id = sa.submitted_userid ")
                    .append("left join m_appuser rbu on rbu.id = sa.rejected_userid ")
                    .append("left join m_appuser abu on abu.id = sa.approved_userid ")
                    .append("left join m_appuser avbu on rbu.id = sa.activated_userid ")
                    .append("left join m_appuser cbu on cbu.id = sa.closed_userid ")
                    .append("left join m_savings_account msa on sa.savings_account_id = msa.id ");
            this.schema = buff.toString();
        }

        @Override
        public ShareAccountData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");
            final Long savingsAccountId = JdbcSupport.getLong(rs, "savings_account_id");
            final String savingsAccountNumber = rs.getString("savingsAccNo");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final String clientName = rs.getString("clientName");
            final Long productId = rs.getLong("productId");
            final String productName = rs.getString("productName");
            final Long totalApprovedShares = JdbcSupport.getLong(rs, "approvedShares");
            final Long totalPendingShares = JdbcSupport.getLong(rs, "pendingShares");
            final Boolean allowdividendsforinactiveclients = rs.getBoolean("allowdividendsforinactiveclients");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final ShareAccountStatusEnumData status = SharesEnumerations.status(statusEnum);

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final LocalDate rejectedOnDate = JdbcSupport.getLocalDate(rs, "rejectedDate");
            final String rejectedByUsername = rs.getString("rejectedByUsername");
            final String rejectedByFirstname = rs.getString("rejectedByFirstname");
            final String rejectedByLastname = rs.getString("rejectedByLastname");

            final LocalDate approvedOnDate = JdbcSupport.getLocalDate(rs, "approvedDate");
            final String approvedByUsername = rs.getString("approvedByUsername");
            final String approvedByFirstname = rs.getString("approvedByFirstname");
            final String approvedByLastname = rs.getString("approvedByLastname");

            final LocalDate activatedOnDate = JdbcSupport.getLocalDate(rs, "activatedDate");
            final String activatedByUsername = rs.getString("activatedByUsername");
            final String activatedByFirstname = rs.getString("activatedByFirstname");
            final String activatedByLastname = rs.getString("activatedByLastname");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final ShareAccountApplicationTimelineData timeline = new ShareAccountApplicationTimelineData(submittedOnDate,
                    submittedByUsername, submittedByFirstname, submittedByLastname, rejectedOnDate, rejectedByUsername,
                    rejectedByFirstname, rejectedByLastname, approvedOnDate, approvedByUsername, approvedByFirstname, approvedByLastname,
                    activatedOnDate, activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate, closedByUsername,
                    closedByFirstname, closedByLastname);

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockinPeriod");
            EnumOptionData lockinPeriodFrequencyType = null;

            final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodEnum");
            if (lockinPeriodFrequencyTypeValue != null) {
                lockinPeriodFrequencyType = SharesEnumerations.lockinPeriodFrequencyType(lockinPeriodFrequencyTypeValue);
            }

            final Integer minimumActivePeriod = JdbcSupport.getInteger(rs, "minimumactivePeriod");
            EnumOptionData minimumActivePeriodType = null;
            final Integer minimumActivePeriodTypeValue = JdbcSupport.getInteger(rs, "minimumactivePeriodEnum");
            if (minimumActivePeriodTypeValue != null) {
                minimumActivePeriodType = SharesEnumerations.minimumActivePeriodFrequencyType(minimumActivePeriodTypeValue);
            }

            final String shortProductName = null;
            final ShareAccountSummaryData summary = new ShareAccountSummaryData(id, accountNo, externalId, productId, productName,
                    shortProductName, status, currency, totalApprovedShares, totalPendingShares, timeline);
            return new ShareAccountData(id, accountNo, externalId, savingsAccountId, savingsAccountNumber, clientId, clientName,
                    productId, productName, status, timeline, currency, summary, charges, purchasedShares, lockinPeriodFrequency,
                    lockinPeriodFrequencyType, minimumActivePeriod, minimumActivePeriodType, allowdividendsforinactiveclients);

        }

        public String schema() {
            return this.schema;
        }
    }

    private final static class ShareAccountMapperForDividents implements RowMapper<ShareAccountData> {

        private final String schema;
        final PurchasedSharesDataRowMapper purchasedSharesDataRowMapper = new PurchasedSharesDataRowMapper();

        public ShareAccountMapperForDividents() {
            StringBuilder sb = new StringBuilder();

            sb.append("sa.id as id, sa.status_enum as statusEnum, ");
            sb.append("c.id as clientId, c.display_name as clientName, ");
            sb.append("sa.account_no as accountNo, ");
            sb.append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
            sb.append(purchasedSharesDataRowMapper.schema());
            sb.append(" from m_share_account sa ");
            sb.append(" join m_client c ON c.id = sa.client_id ");
            sb.append(" join m_share_account_transactions saps ON saps.account_id = sa.id ");
            schema = sb.toString();
        }

        @Override
        public ShareAccountData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final String clientName = rs.getString("clientName");
            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final ShareAccountStatusEnumData status = SharesEnumerations.status(statusEnum);

            final CurrencyData currency = null;
            final Long totalApprovedShares = null;
            final Long totalPendingShares = null;
            final String externalId = null;
            final Long productId = null;
            final String productName = null;
            final String shortProductName = null;
            final ShareAccountApplicationTimelineData timeline = null;
            final Boolean allowdividendsforinactiveclients = null;

            final Collection<ShareAccountChargeData> charges = null;
            final Collection<ShareAccountTransactionData> purchasedSharesData = new ArrayList<>();
            final Integer lockinPeriod = null;
            final EnumOptionData lockPeriodTypeEnum = null;
            final Integer minimumActivePeriod = null;
            final EnumOptionData minimumActivePeriodTypeEnum = null;
            purchasedSharesData.add(this.purchasedSharesDataRowMapper.mapRow(rs, rowNum));

            while (rs.next()) {
                if (id.equals(rs.getLong("id"))) {
                    purchasedSharesData.add(this.purchasedSharesDataRowMapper.mapRow(rs, rowNum));
                } else {
                    rs.previous();
                    break;
                }
            }

            final ShareAccountSummaryData summary = new ShareAccountSummaryData(id, accountNo, externalId, productId, productName,
                    shortProductName, status, currency, totalApprovedShares, totalPendingShares, timeline);

            return new ShareAccountData(id, accountNo, externalId, clientId, clientName, productId, shortProductName, productId,
                    shortProductName, status, timeline, currency, summary, charges, purchasedSharesData, lockinPeriod, lockPeriodTypeEnum,
                    minimumActivePeriod, minimumActivePeriodTypeEnum, allowdividendsforinactiveclients);

        }
    }

    private final static class PurchasedSharesDataRowMapper implements RowMapper<ShareAccountTransactionData> {

        private final String schema;

        public PurchasedSharesDataRowMapper() {
            StringBuffer buff = new StringBuffer()
                    .append("saps.id as purchasedId, saps.account_id as accountId, saps.transaction_date as transactionDate, saps.total_shares as purchasedShares, saps.unit_price as unitPrice, ")
                    .append("saps.status_enum as purchaseStatus, saps.type_enum as purchaseType, saps.amount as amount, saps.charge_amount as chargeamount, ")
                    .append("saps.amount_paid as amountPaid ");
            
            schema = buff.toString();
        }

        @Override
        public ShareAccountTransactionData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final Long id = rs.getLong("purchasedId");
            final Long accountId = rs.getLong("accountId");
            final LocalDate transactionDate = new LocalDate(rs.getDate("transactionDate"));
            final Long numberOfShares = JdbcSupport.getLong(rs, "purchasedShares");
            final BigDecimal purchasedPrice = rs.getBigDecimal("unitPrice");
            final Integer status = rs.getInt("purchaseStatus");
            final EnumOptionData statusEnum = SharesEnumerations.purchasedSharesEnum(status);
            final Integer type = rs.getInt("purchaseType");
            final EnumOptionData typeEnum = SharesEnumerations.purchasedSharesEnum(type);
            final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "amount");
            final BigDecimal chargeAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "chargeamount");
            final BigDecimal amountPaid = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "amountPaid");
            return new ShareAccountTransactionData(id, accountId, transactionDate, numberOfShares, purchasedPrice, statusEnum, typeEnum, amount,
                    chargeAmount, amountPaid);
        }

        public String schema() {
            return this.schema;
        }
    }

    private final static class ShareAccountDividendRowMapper implements RowMapper<ShareAccountDividendData> {

        private final String schema;

        ShareAccountDividendRowMapper() {
            StringBuffer buff = new StringBuffer()
                    .append("spdp.created_date, sadd.id, sadd.amount, sadd.savings_transaction_id, sadd.status ")
                    .append(" from m_share_account_dividend_details sadd ")
                    .append("JOIN m_share_product_dividend_pay_out spdp ON spdp.id = sadd.dividend_pay_out_id ");
            schema = buff.toString();
        }

        @SuppressWarnings("unused")
        @Override
        public ShareAccountDividendData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Date postedDate = JdbcSupport.getLocalDate(rs, "created_date").toDate();
            final BigDecimal postedAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "amount");
            final Long savingTransactionId = rs.getLong("savings_transaction_id");
            final Integer status = rs.getInt("status") ;
            final EnumOptionData statusEnum = SharesEnumerations.ShareAccountDividendStatusEnum(status);
            final ShareAccountData shareAccountData = null;
            return new ShareAccountDividendData(id, postedDate, shareAccountData, postedAmount, statusEnum, savingTransactionId);
        }

        public String schema() {
            return this.schema;
        }
    }
}
