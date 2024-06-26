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
package org.apache.fineract.infrastructure.dataqueries.service;

import jakarta.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.core.service.database.JdbcJavaType;
import org.apache.fineract.infrastructure.core.service.database.SqlOperator;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableCheckStatusData;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableChecksData;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityDataTableChecksData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityDataTableChecksTemplateData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecks;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecksRepository;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class EntityDatatableChecksReadPlatformServiceImpl implements EntityDatatableChecksReadService {

    private final JdbcTemplate jdbcTemplate;
    protected final DatabaseTypeResolver databaseTypeResolver;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final RegisterDataTableMapper registerDataTableMapper;
    private final EntityDataTableChecksMapper entityDataTableChecksMapper;
    private final EntityDatatableChecksRepository entityDatatableChecksRepository;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    private final PaginationHelper paginationHelper;

    @Autowired
    public EntityDatatableChecksReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate, DatabaseTypeResolver databaseTypeResolver,
            DatabaseSpecificSQLGenerator sqlGenerator, final LoanProductReadPlatformService loanProductReadPlatformService,
            final SavingsProductReadPlatformService savingsProductReadPlatformService,
            final EntityDatatableChecksRepository entityDatatableChecksRepository,
            final ReadWriteNonCoreDataService readWriteNonCoreDataService, PaginationHelper paginationHelper) {

        this.jdbcTemplate = jdbcTemplate;
        this.databaseTypeResolver = databaseTypeResolver;
        this.sqlGenerator = sqlGenerator;
        this.registerDataTableMapper = new RegisterDataTableMapper();
        this.entityDataTableChecksMapper = new EntityDataTableChecksMapper();
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.savingsProductReadPlatformService = savingsProductReadPlatformService;
        this.entityDatatableChecksRepository = entityDatatableChecksRepository;
        this.readWriteNonCoreDataService = readWriteNonCoreDataService;
        this.paginationHelper = paginationHelper;
    }

    @Override
    public Page<EntityDataTableChecksData> retrieveAll(@NotNull SearchParameters searchParameters, final Integer status,
            final String entity, final Long productId) {
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ").append(sqlGenerator.calcFoundRows()).append(" ").append(this.entityDataTableChecksMapper.schema());

        if (status != null || entity != null || productId != null) {
            sqlBuilder.append(" where ");
        }
        List<Object> paramList = new ArrayList<>();
        if (status != null) {
            sqlBuilder.append(" status_enum = ? ");
            paramList.add(status);
        }

        if (entity != null) {
            sqlBuilder.append(" and t.application_table_name = ? ");
            paramList.add(entity);
        }

        if (productId != null) {
            sqlBuilder.append(" and t.product_id = ? ");
            paramList.add(productId);
        }

        if (searchParameters.hasLimit()) {
            sqlBuilder.append(" ");
            if (searchParameters.hasOffset()) {
                sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
            } else {
                sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit()));
            }
        }
        return this.paginationHelper.fetchPage(jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), entityDataTableChecksMapper);

    }

    @Override
    public List<DatatableData> retrieveTemplates(final Integer status, final String entity, final Long productId) {
        List<EntityDatatableChecks> tableRequiredBeforeAction = null;
        if (productId != null) {
            tableRequiredBeforeAction = this.entityDatatableChecksRepository.findByEntityStatusAndProduct(entity, status, productId);
        }

        if (tableRequiredBeforeAction == null || tableRequiredBeforeAction.size() < 1) {
            tableRequiredBeforeAction = this.entityDatatableChecksRepository.findByEntityStatusAndNoProduct(entity, status);
        }
        if (tableRequiredBeforeAction != null && tableRequiredBeforeAction.size() > 0) {
            List<DatatableData> ret = new ArrayList<>();
            for (EntityDatatableChecks t : tableRequiredBeforeAction) {
                ret.add(this.readWriteNonCoreDataService.retrieveDatatable(t.getDatatableName()));
            }
            return ret;
        }
        return null;
    }

    @Override
    public EntityDataTableChecksTemplateData retrieveTemplate() {
        List<DatatableChecksData> dataTables = getDataTables();
        List<String> entities = EntityTables.getEntityNames();
        List<DatatableCheckStatusData> clientStatuses = getStatusList(EntityTables.CLIENT.getCheckStatuses());
        List<DatatableCheckStatusData> loanStatuses = getStatusList(EntityTables.LOAN.getCheckStatuses());
        List<DatatableCheckStatusData> groupstatuses = getStatusList(EntityTables.GROUP.getCheckStatuses());
        List<DatatableCheckStatusData> savingsStatuses = getStatusList(EntityTables.SAVINGS.getCheckStatuses());

        Collection<LoanProductData> loanProductDatas = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup(true);
        Collection<SavingsProductData> savingsProductDatas = this.savingsProductReadPlatformService.retrieveAllForLookup();

        return new EntityDataTableChecksTemplateData(entities, clientStatuses, groupstatuses, savingsStatuses, loanStatuses, dataTables,
                loanProductDatas, savingsProductDatas);
    }

    private List<DatatableCheckStatusData> getStatusList(List<StatusEnum> statuses) {
        List<DatatableCheckStatusData> ret = new ArrayList<>();
        if (statuses != null) {
            for (StatusEnum status : statuses) {
                ret.add(new DatatableCheckStatusData(status.name(), status.getValue()));
            }
        }
        return ret;
    }

    private List<DatatableChecksData> getDataTables() {
        final String sql = "select " + this.registerDataTableMapper.schema();

        return this.jdbcTemplate.query(sql, this.registerDataTableMapper); // NOSONAR
    }

    protected final class RegisterDataTableMapper implements RowMapper<DatatableChecksData> {

        public static final String SELECT_FROM = " t.application_table_name as entity, t.registered_table_name as tableName FROM x_registered_table t WHERE ";

        @Override
        public DatatableChecksData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final String entity = rs.getString("entity");
            final String tableName = rs.getString("tableName");

            return new DatatableChecksData(entity, tableName);
        }

        public String schema() {
            String[] values = EntityTables.getFiltered(EntityTables::hasCheck).stream().map(EntityTables::getName).toArray(String[]::new);
            return SELECT_FROM + SqlOperator.IN.formatSql(sqlGenerator, JdbcJavaType.VARCHAR, "application_table_name", null, values);
        }
    }

    protected static final class EntityDataTableChecksMapper implements RowMapper<EntityDataTableChecksData> {

        @Override
        public EntityDataTableChecksData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String entity = rs.getString("entity");
            final int status = rs.getInt("status");
            EnumOptionData statusEnum = StatusEnum.toEnumOptionData(status);
            final String datatableName = rs.getString("datatableName");
            final boolean systemDefined = rs.getBoolean("systemDefined");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String productName = rs.getString("productName");

            return new EntityDataTableChecksData(id, entity, statusEnum, datatableName, systemDefined, productId, productName);
        }

        public String schema() {
            return " t.id as id, " + "t.application_table_name as entity, " + "t.status_enum as status,  "
                    + "t.system_defined as systemDefined,  " + "t.x_registered_table_name as datatableName,  "
                    + "t.product_id as productId,  " + "(CASE t.application_table_name " + "WHEN 'm_loan' THEN lp.name "
                    + "WHEN 'm_savings_account' THEN sp.name " + "ELSE NULL  " + "END) as productName "
                    + "from m_entity_datatable_check as t  "
                    + "left join m_product_loan lp on lp.id = t.product_id and t.application_table_name = 'm_loan' "
                    + "left join m_savings_product sp on sp.id = t.product_id and t.application_table_name = 'm_savings_account' ";
        }
    }
}
