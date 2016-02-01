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
package org.apache.fineract.organisation.provisioning.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.organisation.provisioning.data.ProvisioningCategoryData;
import org.apache.fineract.organisation.provisioning.data.ProvisioningCriteriaData;
import org.apache.fineract.organisation.provisioning.data.ProvisioningCriteriaDefinitionData;
import org.apache.fineract.organisation.provisioning.exception.ProvisioningCriteriaNotFoundException;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ProvisioningCriteriaReadPlatformServiceImpl implements ProvisioningCriteriaReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ProvisioningCategoryReadPlatformService provisioningCategoryReadPlatformService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final GLAccountReadPlatformService glAccountReadPlatformService;
    private final LoanProductReadPlatformService loanProductReaPlatformService;

    @Autowired
    public ProvisioningCriteriaReadPlatformServiceImpl(final RoutingDataSource dataSource,
            final ProvisioningCategoryReadPlatformService provisioningCategoryReadPlatformService,
            final LoanProductReadPlatformService loanProductReadPlatformService,
            final GLAccountReadPlatformService glAccountReadPlatformService,
            final LoanProductReadPlatformService loanProductReaPlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.provisioningCategoryReadPlatformService = provisioningCategoryReadPlatformService;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.glAccountReadPlatformService = glAccountReadPlatformService;
        this.loanProductReaPlatformService = loanProductReaPlatformService;
    }

    @Override
    public ProvisioningCriteriaData retrievePrivisiongCriteriaTemplate() {
        boolean onlyActive = true;
        final Collection<ProvisioningCategoryData> categories = this.provisioningCategoryReadPlatformService
                .retrieveAllProvisionCategories();
        final Collection<LoanProductData> allLoanProducts = this.loanProductReadPlatformService
                .retrieveAllLoanProductsForLookup(onlyActive);
        final Collection<GLAccountData> glAccounts = this.glAccountReadPlatformService.retrieveAllEnabledDetailGLAccounts();
        return ProvisioningCriteriaData.toTemplate(constructCriteriaTemplate(categories), allLoanProducts, glAccounts);
    }

    @Override
    public ProvisioningCriteriaData retrievePrivisiongCriteriaTemplate(ProvisioningCriteriaData data) {
        boolean onlyActive = true;
        final Collection<ProvisioningCategoryData> categories = this.provisioningCategoryReadPlatformService
                .retrieveAllProvisionCategories();
        final Collection<LoanProductData> allLoanProducts = this.loanProductReadPlatformService
                .retrieveAllLoanProductsForLookup(onlyActive);
        final Collection<GLAccountData> glAccounts = this.glAccountReadPlatformService.retrieveAllEnabledDetailGLAccounts();
        return ProvisioningCriteriaData.toTemplate(data, constructCriteriaTemplate(categories), allLoanProducts, glAccounts);
    }
    
    private Collection<ProvisioningCriteriaDefinitionData> constructCriteriaTemplate(Collection<ProvisioningCategoryData> categories) {
        List<ProvisioningCriteriaDefinitionData> definitions = new ArrayList<>();
        for (ProvisioningCategoryData data : categories) {
            definitions.add(ProvisioningCriteriaDefinitionData.template(data.getId(), data.getCategoryName()));
        }
        return definitions;
    }
    @Override
    public Collection<ProvisioningCriteriaData> retrieveAllProvisioningCriterias() {
        ProvisioningCriteriaRowMapper mapper = new ProvisioningCriteriaRowMapper() ;
        final String sql = "select " + mapper.schema() ;
        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }
    
    private static final class ProvisioningCriteriaRowMapper implements RowMapper<ProvisioningCriteriaData> {

        @Override
        public ProvisioningCriteriaData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            Long criteriaId = rs.getLong("id") ;
            String criteriaName = rs.getString("criteriaName");
            String createdBy = rs.getString("username") ;
            return ProvisioningCriteriaData.toLookup(criteriaId, criteriaName, createdBy) ;
        }
        public String schema() {
            return "mpc.id as id, mpc.criteria_name as criteriaName, appu.username as username from m_provisioning_criteria as mpc LEFT JOIN m_appuser appu on mpc.createdby_id=appu.id";
        }
    }

    @Override
    public ProvisioningCriteriaData retrieveProvisioningCriteria(Long criteriaId) {
        try {
            String criteriaName = retrieveCriteriaName(criteriaId);
            Collection<LoanProductData> loanProducts = loanProductReaPlatformService
                    .retrieveAllLoanProductsForLookup("select product_id from m_loanproduct_provisioning_mapping where m_loanproduct_provisioning_mapping.criteria_id="
                            + criteriaId);
            List<ProvisioningCriteriaDefinitionData> definitions = retrieveProvisioningDefinitions(criteriaId);
            return ProvisioningCriteriaData.toLookup(criteriaId, criteriaName, loanProducts, definitions);
        }catch(EmptyResultDataAccessException e) {
            throw new ProvisioningCriteriaNotFoundException(criteriaId) ;
        }
       
    }

    private List<ProvisioningCriteriaDefinitionData> retrieveProvisioningDefinitions(Long criteriaId) {
        ProvisioningCriteriaDefinitionRowMapper rowMapper = new ProvisioningCriteriaDefinitionRowMapper();
        final String sql = "select " + rowMapper.schema() + " where pc.criteria_id = ?";
        return this.jdbcTemplate.query(sql, rowMapper, new Object[] { criteriaId });
    }

    private static final class ProvisioningCriteriaDefinitionRowMapper implements RowMapper<ProvisioningCriteriaDefinitionData> {

        private final StringBuilder sqlQuery = new StringBuilder()
                .append("pc.id, pc.criteria_id, pc.category_id, mpc.category_name, pc.min_age, pc.max_age, ")
                .append("pc.provision_percentage, pc.liability_account, pc.expense_account, lia.gl_code as liabilitycode, expe.gl_code as expensecode, ")
                .append("lia.name as liabilityname, expe.name as expensename ")
                .append("from m_provisioning_criteria_definition as pc ")
                .append("LEFT JOIN acc_gl_account lia ON lia.id = pc.liability_account ")
                .append("LEFT JOIN acc_gl_account expe ON expe.id = pc.expense_account ")
                .append("LEFT JOIN m_provision_category mpc ON mpc.id = pc.category_id");

        @Override
        public ProvisioningCriteriaDefinitionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {
            Long id = rs.getLong("id");
            // Long criteriaId = rs.getLong("criteria_id");
            Long categoryId = rs.getLong("category_id");
            String categoryName = rs.getString("category_name");
            Long minAge = rs.getLong("min_age");
            Long maxAge = rs.getLong("max_age");
            BigDecimal provisioningPercentage = rs.getBigDecimal("provision_percentage");
            Long liabilityAccount = rs.getLong("liability_account");
            String liabilityAccountCode = rs.getString("liabilitycode");
            String liabilityAccountName = rs.getString("liabilityname") ;
            Long expenseAccount = rs.getLong("expense_account");
            String expenseAccountCode = rs.getString("expensecode");
            String expenseAccountName = rs.getString("expensename") ;
            
            return new ProvisioningCriteriaDefinitionData(id, categoryId, categoryName, minAge, maxAge, provisioningPercentage,
                    liabilityAccount, liabilityAccountCode, liabilityAccountName, expenseAccount, expenseAccountCode, expenseAccountName);
        }

        public String schema() {
            return sqlQuery.toString();
        }
    }

    private String retrieveCriteriaName(Long criteriaId) {
        ProvisioningCriteriaNameRowMapper rowMapper = new ProvisioningCriteriaNameRowMapper();
        final String sql = "select " + rowMapper.schema() + " from m_provisioning_criteria pc where pc.id = ?";
        return this.jdbcTemplate.queryForObject(sql, rowMapper, new Object[] { criteriaId });
    }

    private static final class ProvisioningCriteriaNameRowMapper implements RowMapper<String> {

        @Override
        public String mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            return rs.getString("criteriaName");
        }

        public String schema() {
            return " pc.criteria_name as criteriaName";
        }
    }
}