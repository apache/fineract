/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.service;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.mifosplatform.commands.data.AuditData;
import org.mifosplatform.commands.data.AuditSearchData;
import org.mifosplatform.commands.data.ProcessingResultLookup;
import org.mifosplatform.infrastructure.core.data.PaginationParameters;
import org.mifosplatform.infrastructure.core.data.PaginationParametersDataValidator;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.service.StaffReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.data.DepositProductData;
import org.mifosplatform.portfolio.savings.data.SavingsProductData;
import org.mifosplatform.portfolio.savings.service.DepositProductReadPlatformService;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;
import org.mifosplatform.portfolio.savings.service.SavingsProductReadPlatformService;
import org.mifosplatform.useradministration.data.AppUserData;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.service.AppUserReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class AuditReadPlatformServiceImpl implements AuditReadPlatformService {

    private final static Logger logger = LoggerFactory.getLogger(AuditReadPlatformServiceImpl.class);
    private final static Set<String> supportedOrderByValues = new HashSet<>(Arrays.asList("id", "actionName", "entityName", "resourceId",
            "subresourceId", "madeOnDate", "checkedOnDate", "officeName", "groupName", "clientName", "loanAccountNo", "savingsAccountNo",
            "clientId", "loanId"));

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final FromJsonHelper fromApiJsonHelper;
    private final AppUserReadPlatformService appUserReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final PaginationHelper<AuditData> paginationHelper = new PaginationHelper<>();
    private final PaginationParametersDataValidator paginationParametersDataValidator;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    private final DepositProductReadPlatformService depositProductReadPlatformService;

    @Autowired
    public AuditReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final FromJsonHelper fromApiJsonHelper, final AppUserReadPlatformService appUserReadPlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final ClientReadPlatformService clientReadPlatformService,
            final LoanProductReadPlatformService loanProductReadPlatformService, final StaffReadPlatformService staffReadPlatformService,
            final PaginationParametersDataValidator paginationParametersDataValidator,
            final SavingsProductReadPlatformService savingsProductReadPlatformService,
            final DepositProductReadPlatformService depositProductReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.appUserReadPlatformService = appUserReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.paginationParametersDataValidator = paginationParametersDataValidator;
        this.savingsProductReadPlatformService = savingsProductReadPlatformService;
        this.depositProductReadPlatformService = depositProductReadPlatformService;
    }

    private static final class AuditMapper implements RowMapper<AuditData> {

        public String schema(final boolean includeJson, final String hierarchy) {

            String commandAsJsonString = "";
            if (includeJson) {
                commandAsJsonString = ", aud.command_as_json as commandAsJson ";
            }

            String partSql = " aud.id as id, aud.action_name as actionName, aud.entity_name as entityName,"
                    + " aud.resource_id as resourceId, aud.subresource_id as subresourceId,aud.client_id as clientId, aud.loan_id as loanId,"
                    + " mk.username as maker, aud.made_on_date as madeOnDate, " + " aud.api_get_url as resourceGetUrl, "
                    + "ck.username as checker, aud.checked_on_date as checkedOnDate, ev.enum_message_property as processingResult "
                    + commandAsJsonString + ", "
                    + " o.name as officeName, gl.level_name as groupLevelName, g.display_name as groupName, c.display_name as clientName, "
                    + " l.account_no as loanAccountNo, s.account_no as savingsAccountNo " + " from m_portfolio_command_source aud "
                    + " left join m_appuser mk on mk.id = aud.maker_id" + " left join m_appuser ck on ck.id = aud.checker_id"
                    + " left join m_office o on o.id = aud.office_id" + " left join m_group g on g.id = aud.group_id"
                    + " left join m_group_level gl on gl.id = g.level_id" + " left join m_client c on c.id = aud.client_id"
                    + " left join m_loan l on l.id = aud.loan_id" + " left join m_savings_account s on s.id = aud.savings_account_id"
                    + " left join r_enum_value ev on ev.enum_name = 'processing_result_enum' and ev.enum_id = aud.processing_result_enum";

            // data scoping: head office (hierarchy = ".") can see all audit
            // entries
            if (!(hierarchy.equals("."))) {
                partSql += " join m_office o2 on o2.id = aud.office_id and o2.hierarchy like '" + hierarchy + "%' ";
            }

            return partSql;
        }

        @Override
        public AuditData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String actionName = rs.getString("actionName");
            final String entityName = rs.getString("entityName");
            final Long resourceId = JdbcSupport.getLong(rs, "resourceId");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final Long loanId = JdbcSupport.getLong(rs, "loanId");
            final Long subresourceId = JdbcSupport.getLong(rs, "subresourceId");
            final String maker = rs.getString("maker");
            final DateTime madeOnDate = JdbcSupport.getDateTime(rs, "madeOnDate");
            final String checker = rs.getString("checker");
            final DateTime checkedOnDate = JdbcSupport.getDateTime(rs, "checkedOnDate");
            final String processingResult = rs.getString("processingResult");
            final String resourceGetUrl = rs.getString("resourceGetUrl");
            String commandAsJson;
            // commandAsJson might not be on the select list of columns
            try {
                commandAsJson = rs.getString("commandAsJson");
            } catch (final SQLException e) {
                commandAsJson = null;
            }

            final String officeName = rs.getString("officeName");
            final String groupLevelName = rs.getString("groupLevelName");
            final String groupName = rs.getString("groupName");
            final String clientName = rs.getString("clientName");
            final String loanAccountNo = rs.getString("loanAccountNo");
            final String savingsAccountNo = rs.getString("savingsAccountNo");

            return new AuditData(id, actionName, entityName, resourceId, subresourceId, maker, madeOnDate, checker, checkedOnDate,
                    processingResult, commandAsJson, officeName, groupLevelName, groupName, clientName, loanAccountNo, savingsAccountNo,
                    clientId, loanId, resourceGetUrl);
        }
    }

    @Override
    public Collection<AuditData> retrieveAuditEntries(final String extraCriteria, final boolean includeJson) {

        String updatedExtraCriteria = "";
        if (StringUtils.isNotBlank(extraCriteria)) {
            updatedExtraCriteria = " where (" + extraCriteria + ")";
        }

        updatedExtraCriteria += " order by aud.id DESC limit " + PaginationParameters.getCheckedLimit(null);
        return retrieveEntries("audit", updatedExtraCriteria, includeJson);
    }

    @Override
    public Page<AuditData> retrievePaginatedAuditEntries(final String extraCriteria, final boolean includeJson,
            final PaginationParameters parameters) {

        this.paginationParametersDataValidator.validateParameterValues(parameters, supportedOrderByValues, "audits");
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();

        String updatedExtraCriteria = "";
        if (StringUtils.isNotBlank(extraCriteria)) {
            updatedExtraCriteria = " where (" + extraCriteria + ")";
        }

        final AuditMapper rm = new AuditMapper();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(rm.schema(includeJson, hierarchy));
        sqlBuilder.append(' ').append(updatedExtraCriteria);

        if (parameters.isOrderByRequested()) {
            sqlBuilder.append(' ').append(parameters.orderBySql());
        } else {
            sqlBuilder.append(' ').append(' ').append(" order by aud.id DESC");
        }

        if (parameters.isLimited()) {
            sqlBuilder.append(' ').append(parameters.limitSql());
        }

        logger.info("sql: " + sqlBuilder.toString());

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), new Object[] {}, rm);
    }

    @Override
    public Collection<AuditData> retrieveAllEntriesToBeChecked(final String extraCriteria, final boolean includeJson) {

        String updatedExtraCriteria = "";
        if (StringUtils.isNotBlank(extraCriteria)) {
            updatedExtraCriteria = " where (" + extraCriteria + ")" + " and aud.processing_result_enum = 2";
        } else {
            updatedExtraCriteria = " where aud.processing_result_enum = 2";
        }

        updatedExtraCriteria += " order by aud.id";

        return retrieveEntries("makerchecker", updatedExtraCriteria, includeJson);
    }

    public Collection<AuditData> retrieveEntries(final String useType, final String extraCriteria, final boolean includeJson) {

        if (!(useType.equals("audit") || useType.equals("makerchecker"))) { throw new PlatformDataIntegrityException(
                "error.msg.invalid.auditSearchTemplate.useType", "Invalid Audit Search Template UseType: " + useType); }

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();

        final AuditMapper rm = new AuditMapper();
        String sql = "select " + rm.schema(includeJson, hierarchy);

        Boolean isLimitedChecker = false;
        if (useType.equals("makerchecker")) {
            if (currentUser.hasNotPermissionForAnyOf("ALL_FUNCTIONS", "CHECKER_SUPER_USER")) {
                isLimitedChecker = true;
            }
        }

        if (isLimitedChecker) {
            sql += " join m_permission p on REPLACE(p.action_name, '_CHECKER', '')  = aud.action_name and p.entity_name = aud.entity_name and p.code like '%\\_CHECKER'"
                    + " join m_role_permission rp on rp.permission_id = p.id"
                    + " join m_role r on r.id = rp.role_id "
                    + " join m_appuser_role ur on ur.role_id = r.id and ur.appuser_id = " + currentUser.getId();
        }
        sql += extraCriteria;

        logger.info("sql: " + sql);

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public AuditData retrieveAuditEntry(final Long auditId) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();

        final AuditMapper rm = new AuditMapper();

        final String sql = "select " + rm.schema(true, hierarchy) + " where aud.id = " + auditId;

        final AuditData auditResult = this.jdbcTemplate.queryForObject(sql, rm, new Object[] {});

        return replaceIdsOnAuditData(auditResult);
    }

    private AuditData replaceIdsOnAuditData(final AuditData auditResult) {

        final String auditAsJson = auditResult.getCommandAsJson();

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        final Map<String, Object> commandAsJsonMap = this.fromApiJsonHelper.extractObjectMap(typeOfMap, auditAsJson);
        final JsonElement auditJsonFragment = this.fromApiJsonHelper.parse(auditAsJson);
        final JsonObject auditObject = auditJsonFragment.getAsJsonObject();

        if (commandAsJsonMap.containsKey("officeId")) {
            commandAsJsonMap.remove("officeId");

            Long officeId = null;
            final String officeIdStr = auditObject.get("officeId").getAsString();
            if (StringUtils.isNotBlank(officeIdStr)) {
                officeId = Long.valueOf(officeIdStr);
                final OfficeData office = this.officeReadPlatformService.retrieveOffice(officeId);
                commandAsJsonMap.put("officeName", office.name());
            } else {
                commandAsJsonMap.put("officeName", "");
            }
        }

        if (commandAsJsonMap.containsKey("clientId")) {
            commandAsJsonMap.remove("clientId");

            Long clientId = null;
            final String clientIdStr = auditObject.get("clientId").getAsString();
            if (StringUtils.isNotBlank(clientIdStr)) {
                clientId = Long.valueOf(clientIdStr);
                final ClientData client = this.clientReadPlatformService.retrieveOne(clientId);
                commandAsJsonMap.put("clientName", client.displayName());
            } else {
                commandAsJsonMap.put("clientName", "");
            }
        }

        if (commandAsJsonMap.containsKey("productId")) {
            commandAsJsonMap.remove("productId");

            Long productId = null;
            final String productIdStr = auditObject.get("productId").getAsString();
            if (StringUtils.isNotBlank(productIdStr)) {
                productId = Long.valueOf(productIdStr);
                if (auditResult.getEntityName().equalsIgnoreCase("LOAN")) {
                    final LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
                    commandAsJsonMap.put("productName", loanProduct.getName());
                } else if (auditResult.getEntityName().equalsIgnoreCase("SAVINGSACCOUNT")) {
                    final SavingsProductData savingProduct = this.savingsProductReadPlatformService.retrieveOne(productId);
                    commandAsJsonMap.put("productName", savingProduct.getName());
                } else if (auditResult.getEntityName().equalsIgnoreCase("RECURRINGDEPOSITACCOUNT")) {
                    final DepositProductData depositProduct = this.depositProductReadPlatformService.retrieveOne(
                            DepositAccountType.RECURRING_DEPOSIT, productId);
                    commandAsJsonMap.put("productName", depositProduct.getName());
                } else if (auditResult.getEntityName().equalsIgnoreCase("FIXEDDEPOSITACCOUNT")) {
                    final DepositProductData depositProduct = this.depositProductReadPlatformService.retrieveOne(
                            DepositAccountType.FIXED_DEPOSIT, productId);
                    commandAsJsonMap.put("productName", depositProduct.getName());
                } else {
                    commandAsJsonMap.put("productName", "");
                }
            } else {
                commandAsJsonMap.put("productName", "");
            }
        }

        if (commandAsJsonMap.containsKey("loanOfficerId") || commandAsJsonMap.containsKey("fieldOfficerId")
                || commandAsJsonMap.containsKey("staffId")) {
            String staffIdStr = "";
            String staffNameParamName = "";

            if (commandAsJsonMap.containsKey("loanOfficerId")) {
                commandAsJsonMap.remove("loanOfficerId");
                staffIdStr = auditObject.get("loanOfficerId").getAsString();
                staffNameParamName = "loanOfficerName";
            } else if (commandAsJsonMap.containsKey("fieldOfficerId")) {
                commandAsJsonMap.remove("fieldOfficerId");
                staffIdStr = auditObject.get("fieldOfficerId").getAsString();
                staffNameParamName = "fieldOfficerName";
            } else if (commandAsJsonMap.containsKey("staffId")) {
                commandAsJsonMap.remove("staffId");
                staffIdStr = auditObject.get("staffId").getAsString();
                staffNameParamName = "staffName";
            }

            replaceStaffIdWithStaffName(staffIdStr, staffNameParamName, commandAsJsonMap);

        }

        updateEnumerations(commandAsJsonMap, auditObject, auditResult.getEntityName());

        final String newAuditAsJson = this.fromApiJsonHelper.toJson(commandAsJsonMap);
        auditResult.setCommandAsJson(newAuditAsJson);

        return auditResult;
    }

    private void updateEnumerations(Map<String, Object> commandAsJsonMap, JsonObject auditObject, String entityName) {

        if (entityName.equalsIgnoreCase("LOAN") || entityName.equalsIgnoreCase("LOANPRODUCT")) {

            final String[] enumTypes = { "loanTermFrequencyType", "termFrequencyType", "repaymentFrequencyType", "amortizationType",
                    "interestType", "interestCalculationPeriodType", "interestRateFrequencyType", "accountingRule" };

            for (final String typeName : enumTypes) {
                if (commandAsJsonMap.containsKey(typeName)) {
                    commandAsJsonMap.remove(typeName);

                    final Integer enumTypeId = auditObject.get(typeName).getAsInt();
                    if (enumTypeId != null) {
                        final String code = LoanEnumerations.loanEnumueration(typeName, enumTypeId).getValue();
                        if (code != null) {
                            commandAsJsonMap.put(typeName, code);
                        }
                    }
                }
            }

        } else if (entityName.equalsIgnoreCase("SAVINGSPRODUCT") || entityName.equalsIgnoreCase("SAVINGSACCOUNT")
                || entityName.equalsIgnoreCase("RECURRINGDEPOSITPRODUCT") || entityName.equalsIgnoreCase("RECURRINGDEPOSITACCOUNT")
                || entityName.equalsIgnoreCase("FIXEDDEPOSITPRODUCT") || entityName.equalsIgnoreCase("FIXEDDEPOSITACCOUNT")) {

            final String[] enumTypes = { "interestCompoundingPeriodType", "interestPostingPeriodType", "interestCalculationType",
                    "lockinPeriodFrequencyType", "minDepositTermTypeId", "maxDepositTermTypeId", "inMultiplesOfDepositTermTypeId",
                    "depositPeriodFrequencyId", "accountingRule", "interestCalculationDaysInYearType", "preClosurePenalInterestOnTypeId",
                    "recurringFrequencyType" };

            for (final String typeName : enumTypes) {
                if (commandAsJsonMap.containsKey(typeName)) {
                    commandAsJsonMap.remove(typeName);

                    final Integer enumTypeId = auditObject.get(typeName).getAsInt();
                    if (enumTypeId != null) {
                        final String code = SavingsEnumerations.savingEnumueration(typeName, enumTypeId).getValue();
                        if (code != null) {
                            commandAsJsonMap.put(typeName, code);
                        }
                    }
                }
            }
        }
    }

    private void replaceStaffIdWithStaffName(final String staffIdStr, final String staffNameParamName, Map<String, Object> commandAsJsonMap) {

        Long staffId = null;
        if (StringUtils.isNotBlank(staffIdStr)) {
            staffId = Long.valueOf(staffIdStr);
            final StaffData officer = this.staffReadPlatformService.retrieveStaff(staffId);
            commandAsJsonMap.put(staffNameParamName, officer.getDisplayName());
        } else {
            commandAsJsonMap.put(staffNameParamName, "");
        }
    }

    @Override
    public AuditSearchData retrieveSearchTemplate(final String useType) {

        if (!(useType.equals("audit") || useType.equals("makerchecker"))) { throw new PlatformDataIntegrityException(
                "error.msg.invalid.auditSearchTemplate.useType", "Invalid Audit Search Template UseType: " + useType); }

        final AppUser currentUser = this.context.authenticatedUser();

        final Collection<AppUserData> appUsers = this.appUserReadPlatformService.retrieveSearchTemplate();

        String sql = " SELECT distinct(action_name) as actionName FROM m_permission p ";
        sql += makercheckerCapabilityOnly(useType, currentUser);
        sql += " order by if(action_name in ('CREATE', 'DELETE', 'UPDATE'), action_name, 'ZZZ'), action_name";
        final ActionNamesMapper mapper = new ActionNamesMapper();
        final List<String> actionNames = this.jdbcTemplate.query(sql, mapper, new Object[] {});

        sql = " select distinct(entity_name) as entityName from m_permission p ";
        sql += makercheckerCapabilityOnly(useType, currentUser);
        sql += " order by if(grouping = 'datatable', 'ZZZ', entity_name), entity_name";
        final EntityNamesMapper mapper2 = new EntityNamesMapper();
        final List<String> entityNames = this.jdbcTemplate.query(sql, mapper2, new Object[] {});

        Collection<ProcessingResultLookup> processingResults = null;
        if (useType.equals("audit")) {
            final ProcessingResultsMapper mapper3 = new ProcessingResultsMapper();
            processingResults = this.jdbcTemplate.query(mapper3.schema(), mapper3, new Object[] {});
        }

        return new AuditSearchData(appUsers, actionNames, entityNames, processingResults);
    }

    private String makercheckerCapabilityOnly(final String useType, final AppUser currentUser) {
        String sql = "";
        Boolean isLimitedChecker = false;
        if (useType.equals("makerchecker")) {
            if (currentUser.hasNotPermissionForAnyOf("ALL_FUNCTIONS", "CHECKER_SUPER_USER")) {
                isLimitedChecker = true;
            }
        }

        if (isLimitedChecker) {
            sql += " join m_role_permission rp on rp.permission_id = p.id" + " join m_role r on r.id = rp.role_id "
                    + " join m_appuser_role ur on ur.role_id = r.id and ur.appuser_id = " + currentUser.getId();

        }
        sql += " where p.action_name is not null and p.action_name <> 'READ' ";
        if (isLimitedChecker) {
            sql += "and p.code like '%\\_CHECKER'";
        }
        return sql;
    }

    private static final class ActionNamesMapper implements RowMapper<String> {

        @Override
        public String mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            return rs.getString("actionName");
        }

    }

    private static final class EntityNamesMapper implements RowMapper<String> {

        @Override
        public String mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            return rs.getString("entityName");
        }

    }

    private static final class ProcessingResultsMapper implements RowMapper<ProcessingResultLookup> {

        @Override
        public ProcessingResultLookup mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String processingResult = rs.getString("processingResult");

            return new ProcessingResultLookup(id, processingResult);
        }

        public String schema() {
            return " select enum_id as id, enum_message_property as processingResult from r_enum_value where enum_name = 'processing_result_enum' "
                    + " order by enum_id";
        }
    }

}