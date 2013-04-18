/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.mifosplatform.commands.data.AuditData;
import org.mifosplatform.commands.data.AuditSearchData;
import org.mifosplatform.commands.data.ProcessingResultLookup;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.data.AppUserData;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.service.AppUserReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditReadPlatformServiceImpl implements AuditReadPlatformService {

    private final static Logger logger = LoggerFactory.getLogger(AuditReadPlatformServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final AppUserReadPlatformService appUserReadPlatformService;

    @Autowired
    public AuditReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            final AppUserReadPlatformService appUserReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.appUserReadPlatformService = appUserReadPlatformService;
    }

    private static final class AuditMapper implements RowMapper<AuditData> {

        public String schema(final boolean includeJson, final String hierarchy) {

            String commandAsJsonString = "";
            if (includeJson) commandAsJsonString = ", aud.command_as_json as commandAsJson ";

            String partSql = " aud.id as id, aud.action_name as actionName, aud.entity_name as entityName,"
                    + " aud.resource_id as resourceId, aud.subresource_id as subresourceId,"
                    + " mk.username as maker, aud.made_on_date as madeOnDate, "
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
            final Long subresourceId = JdbcSupport.getLong(rs, "subresourceId");
            final String maker = rs.getString("maker");
            final DateTime madeOnDate = JdbcSupport.getDateTime(rs, "madeOnDate");
            final String checker = rs.getString("checker");
            final DateTime checkedOnDate = JdbcSupport.getDateTime(rs, "checkedOnDate");
            final String processingResult = rs.getString("processingResult");
            String commandAsJson;
            // commandAsJson might not be on the select list of columns
            try {
                commandAsJson = rs.getString("commandAsJson");
            } catch (SQLException e) {
                commandAsJson = null;
            }

            String officeName = rs.getString("officeName");
            String groupLevelName = rs.getString("groupLevelName");
            String groupName = rs.getString("groupName");
            String clientName = rs.getString("clientName");
            String loanAccountNo = rs.getString("loanAccountNo");
            String savingsAccountNo = rs.getString("savingsAccountNo");

            return new AuditData(id, actionName, entityName, resourceId, subresourceId, maker, madeOnDate, checker, checkedOnDate,
                    processingResult, commandAsJson, officeName, groupLevelName, groupName, clientName, loanAccountNo, savingsAccountNo);
        }
    }

    @Override
    public Collection<AuditData> retrieveAuditEntries(final String extraCriteria, final boolean includeJson) {

        String updatedExtraCriteria = "";
        if (StringUtils.isNotBlank(extraCriteria)) updatedExtraCriteria = " where (" + extraCriteria + ")";

        updatedExtraCriteria += " order by aud.id DESC";
        return retrieveEntries("audit", updatedExtraCriteria, includeJson);
    }

    @Override
    public Collection<AuditData> retrieveAllEntriesToBeChecked(final String extraCriteria, final boolean includeJson) {

        String updatedExtraCriteria = "";
        if (StringUtils.isNotBlank(extraCriteria))
            updatedExtraCriteria = " where (" + extraCriteria + ")" + " and aud.processing_result_enum = 2";
        else
            updatedExtraCriteria = " where aud.processing_result_enum = 2";

        updatedExtraCriteria += " order by aud.id";

        return retrieveEntries("makerchecker", updatedExtraCriteria, includeJson);
    }

    public Collection<AuditData> retrieveEntries(final String useType, final String extraCriteria, final boolean includeJson) {

        if (!(useType.equals("audit") || useType.equals("makerchecker"))) { throw new PlatformDataIntegrityException(
                "error.msg.invalid.auditSearchTemplate.useType", "Invalid Audit Search Template UseType: " + useType); }

        AppUser currentUser = context.authenticatedUser();
        String hierarchy = currentUser.getOffice().getHierarchy();

        final AuditMapper rm = new AuditMapper();
        String sql = "select " + rm.schema(includeJson, hierarchy);

        Boolean isLimitedChecker = false;
        if (useType.equals("makerchecker")) {
            if (currentUser.hasNotPermissionForAnyOf("ALL_FUNCTIONS", "CHECKER_SUPER_USER")) {
                isLimitedChecker = true;
            }
        }

        if (isLimitedChecker) {
            sql += " join m_permission p on p.action_name = aud.action_name and p.entity_name = aud.entity_name and p.code like '%\\_CHECKER'"
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

        AppUser currentUser = context.authenticatedUser();
        String hierarchy = currentUser.getOffice().getHierarchy();

        final AuditMapper rm = new AuditMapper();

        String sql = "select " + rm.schema(true, hierarchy);
        sql += " where aud.id = " + auditId;

        return this.jdbcTemplate.queryForObject(sql, rm, new Object[] {});
    }

    @Override
    public AuditSearchData retrieveSearchTemplate(final String useType) {

        if (!(useType.equals("audit") || useType.equals("makerchecker"))) { throw new PlatformDataIntegrityException(
                "error.msg.invalid.auditSearchTemplate.useType", "Invalid Audit Search Template UseType: " + useType); }

        AppUser currentUser = context.authenticatedUser();

        final Collection<AppUserData> appUsers = appUserReadPlatformService.retrieveSearchTemplate();

        String sql = " SELECT distinct(action_name) as actionName FROM m_permission p ";
        sql += makercheckerCapabilityOnly(useType, currentUser);
        sql += " order by if(action_name in ('CREATE', 'DELETE', 'UPDATE'), action_name, 'ZZZ'), action_name";
        ActionNamesMapper mapper = new ActionNamesMapper();
        List<String> actionNames = this.jdbcTemplate.query(sql, mapper, new Object[] {});

        sql = " select distinct(entity_name) as entityName from m_permission p ";
        sql += makercheckerCapabilityOnly(useType, currentUser);
        sql += " order by if(grouping = 'datatable', 'ZZZ', entity_name), entity_name";
        EntityNamesMapper mapper2 = new EntityNamesMapper();
        List<String> entityNames = this.jdbcTemplate.query(sql, mapper2, new Object[] {});

        Collection<ProcessingResultLookup> processingResults = null;
        if (useType.equals("audit")) {
            ProcessingResultsMapper mapper3 = new ProcessingResultsMapper();
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
            Long id = JdbcSupport.getLong(rs, "id");
            String processingResult = rs.getString("processingResult");

            return new ProcessingResultLookup(id, processingResult);
        }

        public String schema() {
            return " select enum_id as id, enum_message_property as processingResult from r_enum_value where enum_name = 'processing_result_enum' "
                    + " order by enum_id";
        }
    }
}