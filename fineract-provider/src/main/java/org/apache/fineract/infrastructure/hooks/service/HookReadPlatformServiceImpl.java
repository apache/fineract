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
package org.apache.fineract.infrastructure.hooks.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.hooks.data.Event;
import org.apache.fineract.infrastructure.hooks.data.EventResultSetExtractor;
import org.apache.fineract.infrastructure.hooks.data.Field;
import org.apache.fineract.infrastructure.hooks.data.Grouping;
import org.apache.fineract.infrastructure.hooks.data.HookData;
import org.apache.fineract.infrastructure.hooks.data.HookTemplateData;
import org.apache.fineract.infrastructure.hooks.domain.Hook;
import org.apache.fineract.infrastructure.hooks.domain.HookRepository;
import org.apache.fineract.infrastructure.hooks.exception.HookNotFoundException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HookReadPlatformServiceImpl implements HookReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final HookRepository hookRepository;
    private final PlatformSecurityContext context;

    @Override
    public Collection<HookData> retrieveAllHooks() {
        context.authenticatedUser();
        final HookMapper rm = new HookMapper(jdbcTemplate);
        final String sql = "select " + rm.schema() + " order by h.name";

        return jdbcTemplate.query(sql, rm); // NOSONAR
    }

    @Override
    public HookData retrieveHook(final Long hookId) {
        try {
            context.authenticatedUser();
            final HookMapper rm = new HookMapper(jdbcTemplate);
            final String sql = "select " + rm.schema() + " where h.id = ?";

            return jdbcTemplate.queryForObject(sql, rm, hookId); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new HookNotFoundException(hookId, e);
        }

    }

    @Override
    @Cacheable(value = "hooks", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('HK')")
    public List<Hook> retrieveHooksByEvent(final String entityName, final String actionName) {
        return hookRepository.findAllHooksListeningToEvent(entityName, actionName);
    }

    @Override
    public HookData retrieveNewHookDetails(final String templateName) {

        context.authenticatedUser();
        final TemplateMapper rm = new TemplateMapper(jdbcTemplate);
        final String sql;
        List<HookTemplateData> templateData;

        if (templateName == null) {
            sql = "select " + rm.schema() + " order by s.name";
            templateData = jdbcTemplate.query(sql, rm); // NOSONAR
        } else {
            sql = "select " + rm.schema() + " where s.name = ? order by s.name";
            templateData = jdbcTemplate.query(sql, rm, templateName); // NOSONAR
        }

        final List<Grouping> events = getTemplateForEvents();

        return HookData.template(templateData, events);
    }

    private List<Grouping> getTemplateForEvents() {
        final String sql = "select p.grouping, p.entity_name, p.action_name from m_permission p "
                + " where p.action_name NOT LIKE '%CHECKER%' AND p.action_name NOT LIKE '%READ%' " + " order by p.grouping, p.entity_name ";
        final EventResultSetExtractor extractor = new EventResultSetExtractor();
        return jdbcTemplate.query(sql, extractor);
    }

    @RequiredArgsConstructor
    private static final class HookMapper implements RowMapper<HookData> {

        private final JdbcTemplate jdbcTemplate;

        public String schema() {
            return " h.id, s.name as name, h.name as display_name, h.is_active, h.created_date,"
                    + " h.lastmodified_date, h.ugd_template_id, tp.name as ugd_template_name, "
                    + "h.ugd_template_id from m_hook h left join m_hook_templates s on h.template_id = s.id"
                    + " left join m_template tp on h.ugd_template_id = tp.id";
        }

        @Override
        public HookData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String displayname = rs.getString("display_name");
            final boolean isActive = rs.getBoolean("is_active");
            final LocalDate createdAt = JdbcSupport.getLocalDate(rs, "created_date");
            final LocalDate updatedAt = JdbcSupport.getLocalDate(rs, "lastmodified_date");
            final Long templateId = rs.getLong("ugd_template_id");
            final String templateName = rs.getString("ugd_template_name");
            final List<Event> registeredEvents = retrieveEvents(id);
            final List<Field> config = retrieveConfig(id);

            return HookData.instance(id, name, displayname, isActive, createdAt, updatedAt, templateId, registeredEvents, config,
                    templateName);
        }

        private List<Event> retrieveEvents(final Long hookId) {

            final HookEventMapper rm = new HookEventMapper();
            final String sql = "select " + rm.schema() + " where h.id= ?";

            return jdbcTemplate.query(sql, rm, hookId); // NOSONAR
        }

        private List<Field> retrieveConfig(final Long hookId) {

            final HookConfigMapper rm = new HookConfigMapper();
            final String sql = "select " + rm.schema() + " where h.id= ? order by hc.field_name";

            return jdbcTemplate.query(sql, rm, hookId); // NOSONAR
        }
    }

    private static final class HookEventMapper implements RowMapper<Event> {

        public String schema() {
            return " re.action_name, re.entity_name from m_hook h inner join m_hook_registered_events re on h.id = re.hook_id ";
        }

        @Override
        public Event mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final String actionName = rs.getString("action_name");
            final String entityName = rs.getString("entity_name");
            return Event.instance(actionName, entityName);
        }
    }

    private static final class HookConfigMapper implements RowMapper<Field> {

        public String schema() {
            return " hc.field_name, hc.field_value from m_hook h inner join m_hook_configuration hc on h.id = hc.hook_id ";
        }

        @Override
        public Field mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final String fieldName = rs.getString("field_name");
            final String fieldValue = rs.getString("field_value");
            return Field.fromConfig(fieldName, fieldValue);
        }
    }

    @RequiredArgsConstructor
    private static final class TemplateMapper implements RowMapper<HookTemplateData> {

        private final JdbcTemplate jdbcTemplate;

        public String schema() {
            return " s.id, s.name from m_hook_templates s ";
        }

        @Override
        public HookTemplateData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final List<Field> schema = retrieveSchema(id);

            return HookTemplateData.instance(id, name, schema);
        }

        private List<Field> retrieveSchema(final Long templateId) {

            final TemplateSchemaMapper rm = new TemplateSchemaMapper();
            final String sql = "select " + rm.schema() + " where s.id= ? order by hs.field_name ";

            return jdbcTemplate.query(sql, rm, templateId); // NOSONAR;
        }
    }

    private static final class TemplateSchemaMapper implements RowMapper<Field> {

        public String schema() {
            return " hs.field_type, hs.field_name, hs.placeholder, hs.optional from m_hook_templates s "
                    + " inner join m_hook_schema hs on s.id = hs.hook_template_id ";
        }

        @Override
        public Field mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final String fieldName = rs.getString("field_name");
            final String fieldType = rs.getString("field_type");
            final Boolean optional = rs.getBoolean("optional");
            final String placeholder = rs.getString("placeholder");
            return Field.fromSchema(fieldType, fieldName, optional, placeholder);
        }
    }

}
