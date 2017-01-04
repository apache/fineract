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
package org.apache.fineract.portfolio.shareproducts.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingReadPlatformService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.products.data.ProductData;
import org.apache.fineract.portfolio.products.exception.ProductNotFoundException;
import org.apache.fineract.portfolio.products.service.ProductReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.service.SharesEnumerations;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductData;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductMarketPriceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service(value = "shareReadPlatformService")
public class ShareProductReadPlatformServiceImpl implements ProductReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final ShareProductDropdownReadPlatformService shareProductDropdownReadPlatformService;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService;
    private final PaginationHelper<ProductData> shareProductDataPaginationHelper = new PaginationHelper<>();

    @Autowired
    public ShareProductReadPlatformServiceImpl(final RoutingDataSource dataSource,
            final CurrencyReadPlatformService currencyReadPlatformService, final ChargeReadPlatformService chargeReadPlatformService,
            final ShareProductDropdownReadPlatformService shareProductDropdownReadPlatformService,
            final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService,
            final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.shareProductDropdownReadPlatformService = shareProductDropdownReadPlatformService;
        this.accountingDropdownReadPlatformService = accountingDropdownReadPlatformService;
        this.accountMappingReadPlatformService = accountMappingReadPlatformService;
    }

    @Override
    public Page<ProductData> retrieveAllProducts(Integer offSet, Integer limit) {
        final Collection<ShareProductMarketPriceData> shareMarketCollection = null ;
        final Collection<ChargeData> charges = null ;
        ShareProductRowMapper mapper = new ShareProductRowMapper(shareMarketCollection, charges);
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(mapper.schema());
        if (limit != null) {
            sqlBuilder.append(" limit ").append(limit);
        }
        if (offSet != null) {
            sqlBuilder.append(" offset ").append(offSet);
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        Object[] whereClauseItemsitems = new Object[] {};
        return this.shareProductDataPaginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),
                whereClauseItemsitems, mapper);
    }

    @Override
    public ProductData retrieveOne(Long productId, boolean includeTemplate) {
        MarketPriceRowMapper marketRowMapper = new MarketPriceRowMapper();

        try {
            final String sql1 = "select " + marketRowMapper.schema() + " where marketData.product_id = ?";
            final Collection<ShareProductMarketPriceData> shareMarketCollection = this.jdbcTemplate.query(sql1, marketRowMapper,
                    new Object[] { productId });
            final Collection<ChargeData> charges = this.chargeReadPlatformService.retrieveShareProductCharges(productId);
            ShareProductRowMapper mapper = new ShareProductRowMapper(shareMarketCollection, charges);
            final String sql = "select " + mapper.schema() + " where shareproduct.id = ?";
            ShareProductData data = (ShareProductData) this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { productId });

            if (data.hasAccountingEnabled()) {
                final Map<String, Object> accountingMappings = this.accountMappingReadPlatformService
                        .fetchAccountMappingDetailsForShareProduct(productId, data.accountingRuleTypeId());
                final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = this.accountMappingReadPlatformService
                        .fetchPaymentTypeToFundSourceMappingsForShareProduct(productId);
                Collection<ChargeToGLAccountMapper> feeToGLAccountMappings = this.accountMappingReadPlatformService
                        .fetchFeeToIncomeAccountMappingsForShareProduct(productId);
                data = ShareProductData.withAccountingDetails(data, accountingMappings, paymentChannelToFundSourceMappings,
                        feeToGLAccountMappings);
            }

            if (includeTemplate) {
                Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSharesApplicableCharges();
                final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
                final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = this.shareProductDropdownReadPlatformService
                        .retrieveLockinPeriodFrequencyTypeOptions();
                final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions = this.shareProductDropdownReadPlatformService
                        .retrieveMinimumActivePeriodFrequencyTypeOptions();
                final Map<String, List<GLAccountData>> accountingMappingOptions = this.accountingDropdownReadPlatformService
                        .retrieveAccountMappingOptionsForShareProducts();
                data = ShareProductData.template(data, currencyOptions, chargeOptions, minimumActivePeriodFrequencyTypeOptions,
                        lockinPeriodFrequencyTypeOptions, accountingMappingOptions);
            }
            return data;
        } catch (final EmptyResultDataAccessException e) {
            throw new ProductNotFoundException(productId, "share");
        }
    }

    @Override
    public ProductData retrieveTemplate() {
        Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSharesApplicableCharges();
        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = this.shareProductDropdownReadPlatformService
                .retrieveLockinPeriodFrequencyTypeOptions();
        final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions = this.shareProductDropdownReadPlatformService
                .retrieveMinimumActivePeriodFrequencyTypeOptions();
        final Map<String, List<GLAccountData>> accountingMappingOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountMappingOptionsForShareProducts();
        return ShareProductData.template(currencyOptions, chargeOptions, minimumActivePeriodFrequencyTypeOptions,
                lockinPeriodFrequencyTypeOptions, accountingMappingOptions);
    }

    @Override
    public Collection<ProductData> retrieveAllForLookup() {
        AllShareProductRowMapper mapper = new AllShareProductRowMapper();
        String sql = "select " + mapper.schema();
        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }

    @Override
    public Set<String> getResponseDataParams() {
        return null;
    }

    private static final class AllShareProductRowMapper implements RowMapper<ProductData> {

        @SuppressWarnings("unused")
        @Override
        public ShareProductData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String shortName = rs.getString("short_name");
            final Long totalShares = rs.getLong("total_shares");
            return ShareProductData.generic(id, name, shortName, totalShares);
        }

        public String schema() {
            return "shareproduct.id, shareproduct.name, shareproduct.short_name, shareproduct.total_shares from m_share_product shareproduct";
        }
    }

    private static final class MarketPriceRowMapper implements RowMapper<ShareProductMarketPriceData> {

        @SuppressWarnings("unused")
        @Override
        public ShareProductMarketPriceData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Date fromDate = rs.getDate("from_date");
            final BigDecimal shareValue = rs.getBigDecimal("share_value");
            return new ShareProductMarketPriceData(id, fromDate, shareValue);
        }

        public String schema() {
            return "marketData.id, marketData.from_date, marketData.share_value from m_share_product_market_price marketData";
        }
    }

    private final static class ShareProductRowMapper implements RowMapper<ProductData> {

        Collection<ShareProductMarketPriceData> shareMarketCollection;
        Collection<ChargeData> charges;
        private StringBuffer buff = new StringBuffer();

        ShareProductRowMapper(Collection<ShareProductMarketPriceData> shareMarketCollection, Collection<ChargeData> charges) {
            this.shareMarketCollection = shareMarketCollection;
            this.charges = charges;
            buff.append("shareproduct.id, shareproduct.name, shareproduct.short_name, ")
                    .append("shareproduct.external_id, shareproduct.description, shareproduct.start_date,")
                    .append("shareproduct.end_date, shareproduct.currency_code, shareproduct.currency_digits, ")
                    .append("shareproduct.currency_multiplesof, shareproduct.total_shares, shareproduct.issued_shares, ")
                    .append("shareproduct.unit_price, shareproduct.capital_amount,  ")
                    .append("shareproduct.accounting_type as accountingType, ")
                    .append("shareproduct.minimum_client_shares, shareproduct.nominal_client_shares, ")
                    .append("shareproduct.maximum_client_shares, shareproduct.minimum_active_period_frequency, ")
                    .append("shareproduct.minimum_active_period_frequency_enum, shareproduct.lockin_period_frequency, ")
                    .append("shareproduct.lockin_period_frequency_enum, shareproduct.allow_dividends_inactive_clients, ")
                    .append("shareproduct.createdby_id, created.username as createdName, modified.username as modifiedName, ")
                    .append("shareproduct.created_date, shareproduct.lastmodifiedby_id, shareproduct.lastmodified_date, ")
                    .append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ")
                    .append("curr.display_symbol as currencyDisplaySymbol ").append("from m_share_product shareproduct ")
                    .append("LEFT JOIN m_currency curr on curr.code = shareproduct.currency_code ")
                    .append("LEFT JOIN m_appuser created ON created.id = shareproduct.createdby_id ")
                    .append("LEFT JOIN m_appuser modified ON modified.id = shareproduct.lastmodifiedby_id ");

        }

        @SuppressWarnings("unused")
        @Override
        public ShareProductData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String shortName = rs.getString("short_name");
            final String externalId = rs.getString("external_id");
            final String description = rs.getString("description");
            final String currencyCode = rs.getString("currency_code");
            final Integer currencyDigits = rs.getInt("currency_digits");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "currency_multiplesof");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final Long totalShares = rs.getLong("total_shares");
            final Long issuedShares = JdbcSupport.getLongDefaultToNullIfZero(rs, "issued_shares");
            final BigDecimal unitPrice = rs.getBigDecimal("unit_price");
            final BigDecimal capitalAmount = rs.getBigDecimal("capital_amount");
            final Long minimumClientShares = JdbcSupport.getLong(rs, "minimum_client_shares");
            final Long nominalClientShares = JdbcSupport.getLong(rs, "nominal_client_shares");
            final Long maximumClientShares = JdbcSupport.getLong(rs, "maximum_client_shares");
            final Boolean allowDividendsForInactiveClients = rs.getBoolean("allow_dividends_inactive_clients");
            // final Long createdById = rs.getLong("createdby_id") ;
            // final Date createdDate = rs.getDate("created_date") ;
            // final Long modifiedById = rs.getLong("lastmodifiedby_id") ;
            // final Date modifiedDate = rs.getDate("lastmodified_date") ;
            final Integer minimumActivePeriod = JdbcSupport.getInteger(rs, "minimum_active_period_frequency");
            final Integer minimumActviePeriodEnumValue = JdbcSupport.getInteger(rs, "minimum_active_period_frequency_enum");
            EnumOptionData minimumActivePeriodType = null;
            if (minimumActviePeriodEnumValue != null) {
                minimumActivePeriodType = SharesEnumerations.minimumActivePeriodFrequencyType(minimumActviePeriodEnumValue);
            }

            final Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockin_period_frequency");
            EnumOptionData lockinPeriodFrequencyType = null;
            final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockin_period_frequency_enum");
            if (lockinPeriodFrequencyTypeValue != null) {
                lockinPeriodFrequencyType = SharesEnumerations.lockinPeriodFrequencyType(lockinPeriodFrequencyTypeValue);
            }
            final Integer accountingRuleId = JdbcSupport.getInteger(rs, "accountingType");
            final EnumOptionData accountingRuleType = AccountingEnumerations.accountingRuleType(accountingRuleId);

            return ShareProductData.data(id, name, shortName, description, externalId, currency, totalShares, issuedShares, unitPrice,
                    capitalAmount, minimumClientShares, nominalClientShares, maximumClientShares, shareMarketCollection, charges,
                    allowDividendsForInactiveClients, lockinPeriodFrequency, lockinPeriodFrequencyType, minimumActivePeriod,
                    minimumActivePeriodType, accountingRuleType);
        }

        public String schema() {
            return this.buff.toString();
        }
    }
}