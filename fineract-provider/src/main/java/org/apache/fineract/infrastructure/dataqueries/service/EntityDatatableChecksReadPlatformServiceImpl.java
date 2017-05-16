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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
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
    private final RegisterDataTableMapper registerDataTableMapper;
    private final EntityDataTableChecksMapper entityDataTableChecksMapper;
    private final EntityDatatableChecksRepository entityDatatableChecksRepository;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    private final PaginationHelper<EntityDataTableChecksData> paginationHelper = new PaginationHelper<>();

    @Autowired
    public EntityDatatableChecksReadPlatformServiceImpl(final RoutingDataSource dataSource,
            final LoanProductReadPlatformService loanProductReadPlatformService,
            final SavingsProductReadPlatformService savingsProductReadPlatformService,
            final EntityDatatableChecksRepository entityDatatableChecksRepository,
            final ReadWriteNonCoreDataService readWriteNonCoreDataService) {

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.registerDataTableMapper = new RegisterDataTableMapper();
        this.entityDataTableChecksMapper = new EntityDataTableChecksMapper();
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.savingsProductReadPlatformService = savingsProductReadPlatformService;
        this.entityDatatableChecksRepository = entityDatatableChecksRepository;
        this.readWriteNonCoreDataService = readWriteNonCoreDataService;
    }

    @Override
    public Page<EntityDataTableChecksData> retrieveAll(SearchParameters searchParameters, final Long status, final String entity,
            final Long productId) {
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.entityDataTableChecksMapper.schema());

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
        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(jdbcTemplate, sqlCountRows, sqlBuilder.toString(),paramList.toArray(),
                entityDataTableChecksMapper);

    }

    @Override
    public List<DatatableData> retrieveTemplates(final Long status, final String entity, final Long productId) {

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
        List<String> entities = EntityTables.getEntitiesList();
        List<DatatableCheckStatusData> statusClient = getStatusList(EntityTables.getStatus("m_client"));
        List<DatatableCheckStatusData> statusLoan = getStatusList(EntityTables.getStatus("m_loan"));
        List<DatatableCheckStatusData> statusGroup = getStatusList(EntityTables.getStatus("m_group"));
        List<DatatableCheckStatusData> statusSavings = getStatusList(EntityTables.getStatus("m_savings_account"));

        Collection<LoanProductData> loanProductDatas = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup(true);
        Collection<SavingsProductData> savingsProductDatas = this.savingsProductReadPlatformService.retrieveAllForLookup();

        return new EntityDataTableChecksTemplateData(entities, statusClient, statusGroup, statusSavings, statusLoan, dataTables,
                loanProductDatas, savingsProductDatas);

    }

    private List<DatatableCheckStatusData> getStatusList(Integer[] statuses) {
        List<DatatableCheckStatusData> ret = new ArrayList<>();
        if (statuses != null) {
            for (Integer status : statuses) {
                StatusEnum statusEnum = StatusEnum.fromInt(status);
                ret.add(new DatatableCheckStatusData(statusEnum.name(), statusEnum.getCode()));
            }
        }
        return ret;
    }

    private List<DatatableChecksData> getDataTables() {
        final String sql = "select " + this.registerDataTableMapper.schema();

        return this.jdbcTemplate.query(sql, this.registerDataTableMapper);
    }

    protected static final class RegisterDataTableMapper implements RowMapper<DatatableChecksData> {

        @Override
        public DatatableChecksData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String entity = rs.getString("entity");
            final String tableName = rs.getString("tableName");

            return new DatatableChecksData(entity, tableName);
        }

        public String schema() {
            return " t.application_table_name as entity, t.registered_table_name as tableName " + " from x_registered_table t "
                    + " where application_table_name IN( 'm_client','m_group','m_savings_account','m_loan')";
        }
    }

    protected static final class EntityDataTableChecksMapper implements RowMapper<EntityDataTableChecksData> {

        @Override
        public EntityDataTableChecksData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String entity = rs.getString("entity");
            final Long status = rs.getLong("status");
            EnumOptionData statusEnum = null;
            if (status != null) {
                statusEnum = StatusEnum.statusTypeEnum(status.intValue());
            }
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