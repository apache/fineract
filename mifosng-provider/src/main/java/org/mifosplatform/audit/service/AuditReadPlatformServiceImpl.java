package org.mifosplatform.audit.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.mifosplatform.audit.api.ProcessingResultLookup;
import org.mifosplatform.audit.data.AuditData;
import org.mifosplatform.audit.data.AuditSearchData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.data.AppUserData;
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

        public String schema(boolean includeJson) {

            String commandAsJsonString = "";
            if (includeJson) commandAsJsonString = ", aud.command_as_json as commandAsJson ";

            return " aud.id as id, aud.action_name as actionName, aud.entity_name as entityName," + " aud.resource_id as resourceId,"
                    + " mk.username as maker, aud.made_on_date as madeOnDate, "
                    + "ck.username as checker, aud.checked_on_date as checkedOnDate, ev.enum_message_property as processingResult "
                    + commandAsJsonString + " from m_portfolio_command_source aud " + " left join m_appuser mk on mk.id = aud.maker_id"
                    + " left join m_appuser ck on ck.id = aud.checker_id"
                    + " left join r_enum_value ev on ev.enum_name = 'processing_result_enum' and ev.enum_id = aud.processing_result_enum";
        }

        @Override
        public AuditData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String actionName = rs.getString("actionName");
            final String entityName = rs.getString("entityName");
            final Long resourceId = JdbcSupport.getLong(rs, "resourceId");
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
            return new AuditData(id, actionName, entityName, resourceId, maker, madeOnDate, checker, checkedOnDate, processingResult,
                    commandAsJson);
        }
    }

    @Override
    public Collection<AuditData> retrieveAuditEntries(String extraCriteria, boolean includeJson) {
        context.authenticatedUser();

        final AuditMapper rm = new AuditMapper();

        String sql = "select " + rm.schema(includeJson);
        if (StringUtils.isNotBlank(extraCriteria)) sql += " where (" + extraCriteria + ")";
        sql += " order by aud.id DESC";
        logger.info("sql: " + sql);
        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public AuditData retrieveAuditEntry(Long auditId) {
        context.authenticatedUser();

        final AuditMapper rm = new AuditMapper();

        String sql = "select " + rm.schema(true);
        sql += " where aud.id = " + auditId;

        return this.jdbcTemplate.queryForObject(sql, rm, new Object[] {});
    }

    @Override
    public AuditSearchData retrieveSearchTemplate() {
        final Collection<AppUserData> appUsers = appUserReadPlatformService.retrieveSearchTemplate();

        ActionNamesMapper mapper = new ActionNamesMapper();
        List<String> actionNames = this.jdbcTemplate.query(mapper.schema(), mapper, new Object[] {});

        EntityNamesMapper mapper2 = new EntityNamesMapper();
        List<String> entityNames = this.jdbcTemplate.query(mapper2.schema(), mapper2, new Object[] {});

        ProcessingResultsMapper mapper3 = new ProcessingResultsMapper();
        Collection<ProcessingResultLookup> processingResults = this.jdbcTemplate.query(mapper3.schema(), mapper3, new Object[] {});

        return new AuditSearchData(appUsers, actionNames, entityNames, processingResults);
    }

    private static final class ActionNamesMapper implements RowMapper<String> {

        @Override
        public String mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            return rs.getString("actionName");
        }

        public String schema() {

            return " SELECT distinct(action_name) as actionName FROM m_permission "
                    + " where action_name is not null and action_name <> 'READ' "
                    + " order by if(action_name in ('CREATE', 'DELETE', 'UPDATE'), action_name, 'ZZZ'), action_name";
        }

    }

    private static final class EntityNamesMapper implements RowMapper<String> {

        @Override
        public String mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            return rs.getString("entityName");
        }

        public String schema() {
            return " select distinct(entity_name) as entityName from m_permission where action_name is not null and action_name <> 'READ' "
                    + " order by if(grouping = 'datatable', 'ZZZ', entity_name), entity_name";
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