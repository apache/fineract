/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.service.StaffReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientAccountSummaryCollectionData;
import org.mifosplatform.portfolio.client.data.ClientLoanAccountSummaryData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.data.ClientSavingsAccountSummaryData;
import org.mifosplatform.portfolio.client.domain.ClientEnumerations;
import org.mifosplatform.portfolio.client.domain.ClientStatus;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;
import org.mifosplatform.portfolio.group.service.SearchParameters;
import org.mifosplatform.portfolio.loanaccount.data.LoanStatusEnumData;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ClientReadPlatformServiceImpl implements ClientReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    // data mappers
    private final PaginationHelper<ClientData> paginationHelper = new PaginationHelper<ClientData>();
    private final ClientMapper clientMapper = new ClientMapper();
    private final ClientLookupMapper lookupMapper = new ClientLookupMapper();
    private final ClientMembersOfGroupMapper membersOfGroupMapper = new ClientMembersOfGroupMapper();
    private final ParentGroupsMapper clientGroupsMapper = new ParentGroupsMapper();
    private final ClientSavingsAccountSummaryDataMapper savingsAccountSummaryDataMapper = new ClientSavingsAccountSummaryDataMapper();

    @Autowired
    public ClientReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final OfficeReadPlatformService officeReadPlatformService, final StaffReadPlatformService staffReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService) {
        this.context = context;
        this.officeReadPlatformService = officeReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.staffReadPlatformService = staffReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
    }

    @Override
    public ClientData retrieveTemplate(final Long officeId, final boolean staffInSelectedOfficeOnly) {
        this.context.authenticatedUser();

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);

        final Collection<OfficeData> offices = officeReadPlatformService.retrieveAllOfficesForDropdown();

        Collection<StaffData> staffOptions = null;

        final boolean loanOfficersOnly = false;
        if (staffInSelectedOfficeOnly) {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(defaultOfficeId);
        } else {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(defaultOfficeId,
                    loanOfficersOnly);
        }
        if (CollectionUtils.isEmpty(staffOptions)) {
            staffOptions = null;
        }

        return ClientData.template(defaultOfficeId, new LocalDate(), offices, staffOptions, null);
    }

    @Override
    public Page<ClientData> retrieveAll(final SearchParameters searchParameters) {

        final AppUser currentUser = context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.clientMapper.schema());
        sqlBuilder.append(" where o.hierarchy like ?");

        final String extraCriteria = buildSqlStringFromClientCriteria(searchParameters);

        if (StringUtils.isNotBlank(extraCriteria)) {
            sqlBuilder.append(" and (").append(extraCriteria).append(")");
        }

        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());

            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
            }
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),
                new Object[] { hierarchySearchString }, this.clientMapper);
    }

    private String buildSqlStringFromClientCriteria(final SearchParameters searchParameters) {

        String sqlSearch = searchParameters.getSqlSearch();
        final Long officeId = searchParameters.getOfficeId();
        final String externalId = searchParameters.getExternalId();
        final String displayName = searchParameters.getName();
        final String firstname = searchParameters.getFirstname();
        final String lastname = searchParameters.getLastname();
        final String hierarchy = searchParameters.getHierarchy();

        String extraCriteria = "";
        if (sqlSearch != null) {
            sqlSearch = sqlSearch.replaceAll(" display_name ", " c.display_name ");
            sqlSearch = sqlSearch.replaceAll("display_name ", "c.display_name ");
            extraCriteria = " and (" + sqlSearch + ")";
        }

        if (officeId != null) {
            extraCriteria += " and office_id = " + officeId;
        }

        if (externalId != null) {
            extraCriteria += " and c.external_id like " + ApiParameterHelper.sqlEncodeString(externalId);
        }

        if (displayName != null) {
            extraCriteria += " and concat(ifnull(firstname, ''), if(firstname > '',' ', '') , ifnull(lastname, '')) like "
                    + ApiParameterHelper.sqlEncodeString(displayName);
        }

        if (firstname != null) {
            extraCriteria += " and firstname like " + ApiParameterHelper.sqlEncodeString(firstname);
        }

        if (lastname != null) {
            extraCriteria += " and lastname like " + ApiParameterHelper.sqlEncodeString(lastname);
        }

        if (hierarchy != null) {
            extraCriteria += " and o.hierarchy like " + ApiParameterHelper.sqlEncodeString(hierarchy + "%");
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }

        return extraCriteria;
    }

    @Override
    public ClientData retrieveOne(final Long clientId) {
        try {
            AppUser currentUser = context.authenticatedUser();
            String hierarchy = currentUser.getOffice().getHierarchy();
            String hierarchySearchString = hierarchy + "%";

            String sql = "select " + this.clientMapper.schema() + " where o.hierarchy like ? and c.id = ?";
            ClientData clientData = this.jdbcTemplate.queryForObject(sql, this.clientMapper,
                    new Object[] { hierarchySearchString, clientId });

            String clientGroupsSql = "select " + this.clientGroupsMapper.parentGroupsSchema();

            Collection<GroupGeneralData> parentGroups = this.jdbcTemplate.query(clientGroupsSql, this.clientGroupsMapper,
                    new Object[] { clientId });

            return ClientData.setParentGroups(clientData, parentGroups);

        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId);
        }
    }

    @Override
    public Collection<ClientData> retrieveAllForLookup(final String extraCriteria) {

        String sql = "select " + this.lookupMapper.schema();

        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " and (" + extraCriteria + ")";
        }

        return this.jdbcTemplate.query(sql, this.lookupMapper, new Object[] {});
    }

    @Override
    public Collection<ClientData> retrieveAllForLookupByOfficeId(final Long officeId) {

        final String sql = "select " + this.lookupMapper.schema() + " where c.office_id = ? and c.status_enum != ?";

        return this.jdbcTemplate.query(sql, this.lookupMapper, new Object[] { officeId, ClientStatus.CLOSED.getValue() });
    }

    @Override
    public Collection<ClientData> retrieveClientMembersOfGroup(final Long groupId) {

        final AppUser currentUser = context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select " + this.membersOfGroupMapper.schema() + " where o.hierarchy like ? and pgc.group_id = ?";

        return this.jdbcTemplate.query(sql, this.membersOfGroupMapper, new Object[] { hierarchySearchString, groupId });
    }

    @Override
    public Collection<ClientData> retrieveActiveClientMembersOfGroup(final Long groupId) {

        final AppUser currentUser = context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final String sql = "select " + this.membersOfGroupMapper.schema()
                + " where o.hierarchy like ? and pgc.group_id = ? and c.status_enum = ? ";

        return this.jdbcTemplate.query(sql, this.membersOfGroupMapper,
                new Object[] { hierarchySearchString, groupId, ClientStatus.ACTIVE.getValue() });
    }
    
    private static final class ClientMembersOfGroupMapper implements RowMapper<ClientData> {

        private final String schema;

        public ClientMembersOfGroupMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(200);

            sqlBuilder.append("c.id as id, c.account_no as accountNo, c.external_id as externalId, c.status_enum as statusEnum, ");
            sqlBuilder.append("c.office_id as officeId, o.name as officeName, ");
            sqlBuilder.append("c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, ");
            sqlBuilder.append("c.fullname as fullname, c.display_name as displayName, ");
            sqlBuilder.append("c.activation_date as activationDate, c.image_id as imageId, ");
            sqlBuilder.append("c.staff_id as staffId, s.display_name as staffName ");
            sqlBuilder.append("from m_client c ");
            sqlBuilder.append("join m_office o on o.id = c.office_id ");
            sqlBuilder.append("join m_group_client pgc on pgc.client_id = c.id ");
            sqlBuilder.append("left join m_staff s on s.id = c.staff_id ");

            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String accountNo = rs.getString("accountNo");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = ClientEnumerations.status(statusEnum);

            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final Long id = JdbcSupport.getLong(rs, "id");
            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");
            final String externalId = rs.getString("externalId");
            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            final Long imageId = JdbcSupport.getLong(rs, "imageId");
            final String officeName = rs.getString("officeName");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");

            return ClientData.instance(accountNo, status, officeId, officeName, id, firstname, middlename, lastname, fullname, displayName,
                    externalId, activationDate, imageId, staffId, staffName);
        }
    }

    private static final class ClientMapper implements RowMapper<ClientData> {

        private final String schema;

        public ClientMapper() {
            StringBuilder builder = new StringBuilder(400);

            builder.append("c.id as id, c.account_no as accountNo, c.external_id as externalId, c.status_enum as statusEnum, ");
            builder.append("c.office_id as officeId, o.name as officeName, ");
            builder.append("c.office_id as officeId, o.name as officeName, ");
            builder.append("c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, ");
            builder.append("c.fullname as fullname, c.display_name as displayName, ");
            builder.append("c.activation_date as activationDate, c.image_id as imageId, ");
            builder.append("c.staff_id as staffId, s.display_name as staffName ");
            builder.append("from m_client c ");
            builder.append("join m_office o on o.id = c.office_id ");
            builder.append("left join m_staff s on s.id = c.staff_id ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String accountNo = rs.getString("accountNo");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = ClientEnumerations.status(statusEnum);

            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final Long id = JdbcSupport.getLong(rs, "id");
            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");
            final String externalId = rs.getString("externalId");
            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            final String officeName = rs.getString("officeName");
            final Long imageId = JdbcSupport.getLong(rs, "imageId");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");

            return ClientData.instance(accountNo, status, officeId, officeName, id, firstname, middlename, lastname, fullname, displayName,
                    externalId, activationDate, imageId, staffId, staffName);
        }
    }

    private static final class ParentGroupsMapper implements RowMapper<GroupGeneralData> {

        public String parentGroupsSchema() {
            return "gp.id As groupId , gp.display_name As groupName from m_client cl JOIN m_group_client gc ON cl.id = gc.client_id "
                    + "JOIN m_group gp ON gp.id = gc.group_id WHERE cl.id  = ?";
        }

        @Override
        public GroupGeneralData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");

            return GroupGeneralData.lookup(groupId, groupName);
        }
    }

    private static final class ClientLookupMapper implements RowMapper<ClientData> {

        private final String schema;

        public ClientLookupMapper() {
            StringBuilder builder = new StringBuilder(200);

            builder.append("c.id as id, c.display_name as displayName, ");
            builder.append("c.office_id as officeId, o.name as officeName ");
            builder.append("from m_client c ");
            builder.append("join m_office o on o.id = c.office_id ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String displayName = rs.getString("displayName");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");

            return ClientData.lookup(id, displayName, officeId, officeName);
        }
    }

    @Override
    public ClientAccountSummaryCollectionData retrieveClientAccountDetails(final Long clientId) {

        try {
            // Check if client exists
            retrieveOne(clientId);

            ClientLoanAccountSummaryDataMapper rm = new ClientLoanAccountSummaryDataMapper();

            String sql = "select " + rm.loanAccountSummarySchema() + " where l.client_id = ?";

            List<ClientLoanAccountSummaryData> allLoanAccounts = this.jdbcTemplate.query(sql, rm, new Object[] { clientId });

            final String savingsSql = "select " + this.savingsAccountSummaryDataMapper.schema()
                    + " where sa.client_id = ? order by sa.status_enum ASC, sa.account_no ASC";

            final List<ClientSavingsAccountSummaryData> allSavingsAccounts = this.jdbcTemplate.query(savingsSql,
                    savingsAccountSummaryDataMapper, new Object[] { clientId });

            return new ClientAccountSummaryCollectionData(allLoanAccounts, allSavingsAccounts);
        } catch (EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId);
        }
    }

    @Override
    public Collection<ClientLoanAccountSummaryData> retrieveClientLoanAccountsByLoanOfficerId(final Long clientId, final Long loanOfficerId) {

        this.context.authenticatedUser();

        // Check if client exists
        retrieveOne(clientId);

        ClientLoanAccountSummaryDataMapper rm = new ClientLoanAccountSummaryDataMapper();

        String sql = "select " + rm.loanAccountSummarySchema() + " where l.client_id = ? and l.loan_officer_id = ?";

        List<ClientLoanAccountSummaryData> loanAccounts = this.jdbcTemplate.query(sql, rm, new Object[] { clientId, loanOfficerId });

        return loanAccounts;
    }

    private static final class ClientSavingsAccountSummaryDataMapper implements RowMapper<ClientSavingsAccountSummaryData> {

        final String schemaSql;

        public ClientSavingsAccountSummaryDataMapper() {
            StringBuilder accountsSummary = new StringBuilder();
            accountsSummary.append("sa.id as id, sa.account_no as accountNo, sa.external_id as externalId, sa.status_enum as statusEnum, ");
            accountsSummary.append("sa.account_balance_derived as accountBalance, ");
            accountsSummary.append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, ");
            accountsSummary.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            accountsSummary.append("curr.display_symbol as currencyDisplaySymbol, ");
            accountsSummary.append("sa.product_id as productId, p.name as productName ");
            accountsSummary.append("from m_savings_account sa ");
            accountsSummary.append("join m_savings_product as p on p.id = sa.product_id ");
            accountsSummary.append("join m_currency curr on curr.code = sa.currency_code ");

            this.schemaSql = accountsSummary.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public ClientSavingsAccountSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String productName = rs.getString("productName");
            final Integer statusId = JdbcSupport.getInteger(rs, "statusEnum");
            final BigDecimal accountBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "accountBalance");
            final SavingsAccountStatusEnumData status = SavingsEnumerations.status(statusId);

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, currencyDisplaySymbol,
                    currencyNameCode);

            return new ClientSavingsAccountSummaryData(id, accountNo, externalId, productId, productName, status, currency, accountBalance);
        }
    }

    private static final class ClientLoanAccountSummaryDataMapper implements RowMapper<ClientLoanAccountSummaryData> {

        public String loanAccountSummarySchema() {

            StringBuilder accountsSummary = new StringBuilder("l.id as id, l.account_no as accountNo, l.external_id as externalId,");
            accountsSummary.append("l.product_id as productId, lp.name as productName,")
                    .append("l.loan_status_id as statusId, l.loan_type_enum as loanType, ").append("l.loan_product_counter as loanCycle ")
                    .append(" from m_loan l ").append("LEFT JOIN m_product_loan AS lp ON lp.id = l.product_id");

            return accountsSummary.toString();
        }

        @Override
        public ClientLoanAccountSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String loanProductName = rs.getString("productName");
            final Integer loanStatusId = JdbcSupport.getInteger(rs, "statusId");
            final LoanStatusEnumData loanStatus = LoanEnumerations.status(loanStatusId);
            final Integer loanTypeId = JdbcSupport.getInteger(rs, "loanType");
            final EnumOptionData loanType = LoanEnumerations.loanType(loanTypeId);
            final Integer loanCycle = JdbcSupport.getInteger(rs, "loanCycle");

            return new ClientLoanAccountSummaryData(id, accountNo, externalId, productId, loanProductName, loanStatus, loanType, loanCycle);
        }
    }

    @Override
    public ClientData retrieveClientByIdentifier(final Long identifierTypeId, final String identifierKey) {
        try {
            final ClientIdentifierMapper mapper = new ClientIdentifierMapper();

            final String sql = "select " + mapper.clientLookupByIdentifierSchema();

            return jdbcTemplate.queryForObject(sql, mapper, new Object[] { identifierTypeId, identifierKey });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final class ClientIdentifierMapper implements RowMapper<ClientData> {

        public String clientLookupByIdentifierSchema() {
            return "c.id as id, c.account_no as accountNo, c.status_enum as statusEnum, c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, "
                    + "c.fullname as fullname, c.display_name as displayName,"
                    + "c.office_id as officeId, o.name as officeName "
                    + " from m_client c, m_office o, m_client_identifier ci "
                    + "where o.id = c.office_id and c.id=ci.client_id "
                    + "and ci.document_type_id= ? and ci.document_key like ?";
        }

        @Override
        public ClientData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = ClientEnumerations.status(statusEnum);

            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");

            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");

            return ClientData.clientIdentifier(id, accountNo, status, firstname, middlename, lastname, fullname, displayName, officeId,
                    officeName);
        }
    }

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

    @Override
    public ClientData retrieveAllClosureReasons(final String clientClosureReason) {
        final List<CodeValueData> closureReasons = new ArrayList<CodeValueData>(
                codeValueReadPlatformService.retrieveCodeValuesByCode(clientClosureReason));
        return ClientData.template(null, null, null, null, closureReasons);
    }

}