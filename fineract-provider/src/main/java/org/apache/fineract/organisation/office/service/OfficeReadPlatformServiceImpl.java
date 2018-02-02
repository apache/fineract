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
package org.apache.fineract.organisation.office.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.data.OfficeTransactionData;
import org.apache.fineract.organisation.office.exception.OfficeNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class OfficeReadPlatformServiceImpl implements OfficeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final ColumnValidator columnValidator;
    private final static String nameDecoratedBaseOnHierarchy = "concat(substring('........................................', 1, ((LENGTH(o.hierarchy) - LENGTH(REPLACE(o.hierarchy, '.', '')) - 1) * 4)), o.name)";

    @Autowired
    public OfficeReadPlatformServiceImpl(final PlatformSecurityContext context,
            final CurrencyReadPlatformService currencyReadPlatformService,
            final RoutingDataSource dataSource,
            final ColumnValidator columnValidator) {
        this.context = context;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.columnValidator = columnValidator;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class OfficeMapper implements RowMapper<OfficeData> {

        public String officeSchema() {
            return " o.id as id, o.name as name, "
                    + nameDecoratedBaseOnHierarchy
                    + " as nameDecorated, o.external_id as externalId, o.opening_date as openingDate, o.hierarchy as hierarchy, parent.id as parentId, parent.name as parentName "
                    + "from m_office o LEFT JOIN m_office AS parent ON parent.id = o.parent_id ";
        }

        @Override
        public OfficeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String nameDecorated = rs.getString("nameDecorated");
            final String externalId = rs.getString("externalId");
            final LocalDate openingDate = JdbcSupport.getLocalDate(rs, "openingDate");
            final String hierarchy = rs.getString("hierarchy");
            final Long parentId = JdbcSupport.getLong(rs, "parentId");
            final String parentName = rs.getString("parentName");

            return new OfficeData(id, name, nameDecorated, externalId, openingDate, hierarchy, parentId, parentName, null);
        }
    }

    private static final class OfficeDropdownMapper implements RowMapper<OfficeData> {

        public String schema() {
            return " o.id as id, " + nameDecoratedBaseOnHierarchy + " as nameDecorated, o.name as name from m_office o ";
        }

        @Override
        public OfficeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String nameDecorated = rs.getString("nameDecorated");

            return OfficeData.dropdown(id, name, nameDecorated);
        }
    }

    private static final class OfficeTransactionMapper implements RowMapper<OfficeTransactionData> {

        public String schema() {
            return " ot.id as id, ot.transaction_date as transactionDate, ot.from_office_id as fromOfficeId, fromoff.name as fromOfficeName, "
                    + " ot.to_office_id as toOfficeId, tooff.name as toOfficeName, ot.transaction_amount as transactionAmount, ot.description as description, "
                    + " ot.currency_code as currencyCode, rc.decimal_places as currencyDigits, rc.currency_multiplesof as inMultiplesOf, "
                    + " rc.name as currencyName, rc.internationalized_name_code as currencyNameCode, rc.display_symbol as currencyDisplaySymbol "
                    + " from m_office_transaction ot "
                    + " left join m_office fromoff on fromoff.id = ot.from_office_id "
                    + " left join m_office tooff on tooff.id = ot.to_office_id " + " join m_currency rc on rc.`code` = ot.currency_code";
        }

        @Override
        public OfficeTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final LocalDate transactionDate = JdbcSupport.getLocalDate(rs, "transactionDate");
            final Long fromOfficeId = JdbcSupport.getLong(rs, "fromOfficeId");
            final String fromOfficeName = rs.getString("fromOfficeName");
            final Long toOfficeId = JdbcSupport.getLong(rs, "toOfficeId");
            final String toOfficeName = rs.getString("toOfficeName");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");

            final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final BigDecimal transactionAmount = rs.getBigDecimal("transactionAmount");
            final String description = rs.getString("description");

            return OfficeTransactionData.instance(id, transactionDate, fromOfficeId, fromOfficeName, toOfficeId, toOfficeName,
                    currencyData, transactionAmount, description);
        }
    }

    @Override
    @Cacheable(value = "offices", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'of')")
    public Collection<OfficeData> retrieveAllOffices(final boolean includeAllOffices, final SearchParameters searchParameters) {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        String hierarchySearchString = null;
        if (includeAllOffices) {
            hierarchySearchString = "." + "%";
        } else {
            hierarchySearchString = hierarchy + "%";
        }
        final OfficeMapper rm = new OfficeMapper();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(rm.officeSchema());
        sqlBuilder.append(" where o.hierarchy like ? ");
        if(searchParameters!=null) {
            if (searchParameters.isOrderByRequested()) {
                sqlBuilder.append("order by ").append(searchParameters.getOrderBy());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
                if (searchParameters.isSortOrderProvided()) {
                    sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                    this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
                }
            } else {
                sqlBuilder.append("order by o.hierarchy");
            }
        }
        return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] { hierarchySearchString });
    }

    @Override
    @Cacheable(value = "officesForDropdown", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'ofd')")
    public Collection<OfficeData> retrieveAllOfficesForDropdown() {
        final AppUser currentUser = this.context.authenticatedUser();

        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final OfficeDropdownMapper rm = new OfficeDropdownMapper();
        final String sql = "select " + rm.schema() + "where o.hierarchy like ? order by o.hierarchy";

        return this.jdbcTemplate.query(sql, rm, new Object[] { hierarchySearchString });
    }

    @Override
    @Cacheable(value = "officesById", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#officeId)")
    public OfficeData retrieveOffice(final Long officeId) {

        try {
            this.context.authenticatedUser();

            final OfficeMapper rm = new OfficeMapper();
            final String sql = "select " + rm.officeSchema() + " where o.id = ?";

            final OfficeData selectedOffice = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { officeId });

            return selectedOffice;
        } catch (final EmptyResultDataAccessException e) {
            throw new OfficeNotFoundException(officeId);
        }
    }

    @Override
    public OfficeData retrieveNewOfficeTemplate() {

        this.context.authenticatedUser();

        return OfficeData.template(null, new LocalDate());
    }

    @Override
    public Collection<OfficeData> retrieveAllowedParents(final Long officeId) {

        this.context.authenticatedUser();
        final Collection<OfficeData> filterParentLookups = new ArrayList<>();

        if (isNotHeadOffice(officeId)) {
            final Collection<OfficeData> parentLookups = retrieveAllOfficesForDropdown();

            for (final OfficeData office : parentLookups) {
                if (!office.hasIdentifyOf(officeId)) {
                    filterParentLookups.add(office);
                }
            }
        }

        return filterParentLookups;
    }

    private boolean isNotHeadOffice(final Long officeId) {
        return !Long.valueOf(1).equals(officeId);
    }

    @Override
    public Collection<OfficeTransactionData> retrieveAllOfficeTransactions() {

        final AppUser currentUser = this.context.authenticatedUser();

        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final OfficeTransactionMapper rm = new OfficeTransactionMapper();
        final String sql = "select " + rm.schema()
                + " where (fromoff.hierarchy like ? or tooff.hierarchy like ?) order by ot.transaction_date, ot.id";

        return this.jdbcTemplate.query(sql, rm, new Object[] { hierarchySearchString, hierarchySearchString });
    }

    @Override
    public OfficeTransactionData retrieveNewOfficeTransactionDetails() {
        this.context.authenticatedUser();

        final Collection<OfficeData> parentLookups = retrieveAllOfficesForDropdown();
        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();

        return OfficeTransactionData.template(new LocalDate(), parentLookups, currencyOptions);
    }

    public PlatformSecurityContext getContext() {
        return this.context;
    }
}