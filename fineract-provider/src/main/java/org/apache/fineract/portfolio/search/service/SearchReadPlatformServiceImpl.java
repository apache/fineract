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
package org.apache.fineract.portfolio.search.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.portfolio.group.domain.GroupingTypeEnumerations;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.search.SearchConstants;
import org.apache.fineract.portfolio.search.data.AdHocQuerySearchConditions;
import org.apache.fineract.portfolio.search.data.AdHocSearchQueryData;
import org.apache.fineract.portfolio.search.data.SearchConditions;
import org.apache.fineract.portfolio.search.data.SearchData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SearchReadPlatformServiceImpl implements SearchReadPlatformService {

    private final NamedParameterJdbcTemplate namedParameterjdbcTemplate;
    private final PlatformSecurityContext context;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;

    @Autowired
    public SearchReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final LoanProductReadPlatformService loanProductReadPlatformService, final OfficeReadPlatformService officeReadPlatformService) {
        this.context = context;
        this.namedParameterjdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
    }

    @Override
    public Collection<SearchData> retriveMatchingData(final SearchConditions searchConditions) {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();

        final SearchMapper rm = new SearchMapper();

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("hierarchy", hierarchy + "%");
        if(searchConditions.getExactMatch()){
       	 params.addValue("search", searchConditions.getSearchQuery());
       	}else{
       	 params.addValue("search", "%" + searchConditions.getSearchQuery() + "%");
       	}  
        return this.namedParameterjdbcTemplate.query(rm.searchSchema(searchConditions), params, rm);
    }

    private static final class SearchMapper implements RowMapper<SearchData> {

        public String searchSchema(final SearchConditions searchConditions) {

            final String union = " union ";
            final String clientMatchSql = " (select 'CLIENT' as entityType, c.id as entityId, c.display_name as entityName, c.external_id as entityExternalId, c.account_no as entityAccountNo "
                    + " , c.office_id as parentId, o.name as parentName, c.mobile_no as entityMobileNo,c.status_enum as entityStatusEnum, null as parentType "
                    + " from m_client c join m_office o on o.id = c.office_id where o.hierarchy like :hierarchy and (c.account_no like :search or c.display_name like :search or c.external_id like :search or c.mobile_no like :search)) ";

            final String loanMatchSql = " (select 'LOAN' as entityType, l.id as entityId, pl.name as entityName, l.external_id as entityExternalId, l.account_no as entityAccountNo "
                    + " , IFNULL(c.id,g.id) as parentId, IFNULL(c.display_name,g.display_name) as parentName, null as entityMobileNo, l.loan_status_id as entityStatusEnum, IF(g.id is null, 'client', 'group') as parentType "
                    + " from m_loan l left join m_client c on l.client_id = c.id left join m_group g ON l.group_id = g.id left join m_office o on o.id = c.office_id left join m_product_loan pl on pl.id=l.product_id where (o.hierarchy IS NULL OR o.hierarchy like :hierarchy) and (l.account_no like :search or l.external_id like :search)) ";


            final String savingMatchSql = " (select 'SAVING' as entityType, s.id as entityId, sp.name as entityName, s.external_id as entityExternalId, s.account_no as entityAccountNo "
                    + " , IFNULL(c.id,g.id) as parentId, IFNULL(c.display_name,g.display_name) as parentName, null as entityMobileNo, s.status_enum as entityStatusEnum, IF(g.id is null, 'client', 'group') as parentType "
                    + " from m_savings_account s left join m_client c on s.client_id = c.id left join m_group g ON s.group_id = g.id left join m_office o on o.id = c.office_id left join m_savings_product sp on sp.id=s.product_id "
                    + " where (o.hierarchy IS NULL OR o.hierarchy like :hierarchy) and (s.account_no like :search or s.external_id like :search)) ";
					
			final String shareMatchSql = " (select 'SHARE' as entityType, s.id as entityId, sp.name as entityName, s.external_id as entityExternalId, s.account_no as entityAccountNo "
                    + " , c.id as parentId, c.display_name as parentName, null as entityMobileNo, s.status_enum as entityStatusEnum, 'client' as parentType "
                    + " from m_share_account s left join m_client c on s.client_id = c.id left join m_office o on o.id = c.office_id left join m_share_product sp on sp.id=s.product_id "
                    + " where (o.hierarchy IS NULL OR o.hierarchy like :hierarchy) and (s.account_no like :search or s.external_id like :search)) ";
            
            final String clientIdentifierMatchSql = " (select 'CLIENTIDENTIFIER' as entityType, ci.id as entityId, ci.document_key as entityName, "
                    + " null as entityExternalId, null as entityAccountNo, c.id as parentId, c.display_name as parentName,null as entityMobileNo, c.status_enum as entityStatusEnum, null as parentType "
                    + " from m_client_identifier ci join m_client c on ci.client_id=c.id join m_office o on o.id = c.office_id "
                    + " where o.hierarchy like :hierarchy and ci.document_key like :search ) ";
            final String groupMatchSql = " (select IF(g.level_id=1,'CENTER','GROUP') as entityType, g.id as entityId, g.display_name as entityName, g.external_id as entityExternalId, g.account_no as entityAccountNo "
                    + " , g.office_id as parentId, o.name as parentName, null as entityMobileNo, g.status_enum as entityStatusEnum, null as parentType "
                    + " from m_group g join m_office o on o.id = g.office_id where o.hierarchy like :hierarchy and (g.account_no like :search or g.display_name like :search or g.external_id like :search or g.id like :search )) ";
            final StringBuffer sql = new StringBuffer();

            if (searchConditions.isClientSearch()) {
                sql.append(clientMatchSql).append(union);
            }

            if (searchConditions.isLoanSeach()) {
                sql.append(loanMatchSql).append(union);
            }

            if (searchConditions.isSavingSeach()) {
                sql.append(savingMatchSql).append(union);
            }
			
			if (searchConditions.isShareSeach()) {
                sql.append(shareMatchSql).append(union);
            }

            if (searchConditions.isClientIdentifierSearch()) {
                sql.append(clientIdentifierMatchSql).append(union);
            }

            if (searchConditions.isGroupSearch()) {
                sql.append(groupMatchSql).append(union);
            }

            

            sql.replace(sql.lastIndexOf(union), sql.length(), "");

            // remove last occurrence of "union all" string
            return sql.toString();
        }

        @Override
        public SearchData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long entityId = JdbcSupport.getLong(rs, "entityId");
            final String entityAccountNo = rs.getString("entityAccountNo");
            final String entityExternalId = rs.getString("entityExternalId");
            final String entityName = rs.getString("entityName");
            final String entityType = rs.getString("entityType");
            final Long parentId = JdbcSupport.getLong(rs, "parentId");
            final String parentName = rs.getString("parentName");
            final String entityMobileNo = rs.getString("entityMobileNo");
            final Integer entityStatusEnum = JdbcSupport.getInteger(rs, "entityStatusEnum");
            final String parentType = rs.getString("parentType");
            
            EnumOptionData entityStatus = new EnumOptionData(0L, "", "");

            if (entityType.equalsIgnoreCase("client") || entityType.equalsIgnoreCase("clientidentifier")) {
                entityStatus = ClientEnumerations.status(entityStatusEnum);
            }

            else if (entityType.equalsIgnoreCase("group") || entityType.equalsIgnoreCase("center")) {
                entityStatus = GroupingTypeEnumerations.status(entityStatusEnum);
            }

            else if (entityType.equalsIgnoreCase("loan")) {
                LoanStatusEnumData loanStatusEnumData = LoanEnumerations.status(entityStatusEnum);

                entityStatus = LoanEnumerations.status(loanStatusEnumData);
            }

            return new SearchData(entityId, entityAccountNo, entityExternalId, entityName, entityType, parentId, parentName, parentType, 
                    entityMobileNo, entityStatus);
        }

    }

    @Override
    public AdHocSearchQueryData retrieveAdHocQueryTemplate() {

        this.context.authenticatedUser();

        final Collection<LoanProductData> loanProducts = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        return AdHocSearchQueryData.template(loanProducts, offices);
    }

    @Override
    public Collection<AdHocSearchQueryData> retrieveAdHocQueryMatchingData(final AdHocQuerySearchConditions searchConditions) {

        this.context.authenticatedUser();

        final AdHocQuerySearchMapper rm = new AdHocQuerySearchMapper();
        final MapSqlParameterSource params = new MapSqlParameterSource();

        return this.namedParameterjdbcTemplate.query(rm.schema(searchConditions, params), params, rm);
    }

    private static final class AdHocQuerySearchMapper implements RowMapper<AdHocSearchQueryData> {

        private boolean isWhereClauseAdded = false;

        // TODO- build the query dynamically based on selected entity types, for
        // now adding query for only loan entity.
        public String schema(final AdHocQuerySearchConditions searchConditions, final MapSqlParameterSource params) {
            final StringBuffer sql = new StringBuffer();
            sql.append(
                    "Select a.name as officeName, a.Product as productName, a.cnt as 'count', a.outstandingAmt as outstanding, a.percentOut as percentOut  ")
                    .append("from (select mo.name, mp.name Product, sum(ifnull(ml.total_expected_repayment_derived,0.0)) TotalAmt, count(*) cnt, ")
                    .append("sum(ifnull(ml.total_outstanding_derived,0.0)) outstandingAmt,  ")
                    .append("(sum(ifnull(ml.total_outstanding_derived,0.0)) * 100 / sum(ifnull(ml.total_expected_repayment_derived,0.0))) percentOut ")
                    .append("from m_loan ml inner join m_product_loan mp on mp.id=ml.product_id  ")
                    .append("inner join m_client mc on mc.id=ml.client_id  ").append("inner join m_office mo on mo.id=mc.office_id  ");

            if (searchConditions.getLoanStatus() != null && searchConditions.getLoanStatus().size() > 0) {
                // If user requests for all statuses no need to add loanStatus
                // filter
                if (!searchConditions.getLoanStatus().contains("all")) {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("loanStatus", searchConditions.getLoanStatus());
                    sql.append(" ml.loan_status_id in (:loanStatus) ");
                }
            }

            if (searchConditions.getLoanProducts() != null && searchConditions.getLoanProducts().size() > 0) {
                checkAndUpdateWhereClause(sql);
                params.addValue("loanProducts", searchConditions.getLoanProducts());
                sql.append(" mp.id in (:loanProducts) ");
            }

            if (searchConditions.getOffices() != null && searchConditions.getOffices().size() > 0) {
                checkAndUpdateWhereClause(sql);
                params.addValue("offices", searchConditions.getOffices());
                sql.append(" mo.id in (:offices) ");
            }

            if (StringUtils.isNotBlank(searchConditions.getLoanDateOption())) {
                if (searchConditions.getLoanDateOption().equals(SearchConstants.SEARCH_LOAN_DATE.APPROVAL_DATE.getValue())) {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("loanFromDate", searchConditions.getLoanFromDate().toDate());
                    params.addValue("loanToDate", searchConditions.getLoanToDate().toDate());
                    sql.append(" ( ml.approvedon_date between :loanFromDate and :loanToDate ) ");
                } else if (searchConditions.getLoanDateOption().equals(SearchConstants.SEARCH_LOAN_DATE.CREATED_DATE.getValue())) {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("loanFromDate", searchConditions.getLoanFromDate().toDate());
                    params.addValue("loanToDate", searchConditions.getLoanToDate().toDate());
                    sql.append(" ( ml.submittedon_date between :loanFromDate and :loanToDate ) ");
                } else if (searchConditions.getLoanDateOption().equals(SearchConstants.SEARCH_LOAN_DATE.DISBURSAL_DATE.getValue())) {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("loanFromDate", searchConditions.getLoanFromDate().toDate());
                    params.addValue("loanToDate", searchConditions.getLoanToDate().toDate());
                    sql.append(" ( ml.disbursedon_date between :loanFromDate and :loanToDate ) ");
                }
            }

            sql.append(" group by mo.id, mp.name) a ");

            // update isWhereClauseAdded to false to add filters for derived
            // table
            isWhereClauseAdded = false;

            if (searchConditions.getIncludeOutStandingAmountPercentage()) {
                if (searchConditions.getOutStandingAmountPercentageCondition().equals("between")) {
                    checkAndUpdateWhereClause(sql);
                    // params.addValue("outStandingAmountPercentageCondition",
                    // searchConditions.getOutStandingAmountPercentageCondition());
                    params.addValue("minOutStandingAmountPercentage", searchConditions.getMinOutStandingAmountPercentage());
                    params.addValue("maxOutStandingAmountPercentage", searchConditions.getMaxOutStandingAmountPercentage());
                    sql.append(" ( a.percentOut between :minOutStandingAmountPercentage and :maxOutStandingAmountPercentage ) ");
                } else {
                    checkAndUpdateWhereClause(sql);
                    // params.addValue("outStandingAmountPercentageCondition",
                    // searchConditions.getOutStandingAmountPercentageCondition());
                    params.addValue("outStandingAmountPercentage", searchConditions.getOutStandingAmountPercentage());
                    sql.append(" a.percentOut ").append(searchConditions.getOutStandingAmountPercentageCondition())
                            .append(" :outStandingAmountPercentage ");
                }
            }

            if (searchConditions.getIncludeOutstandingAmount()) {
                if (searchConditions.getOutstandingAmountCondition().equals("between")) {
                    checkAndUpdateWhereClause(sql);
                    // params.addValue("outstandingAmountCondition",
                    // searchConditions.getOutstandingAmountCondition());
                    params.addValue("minOutstandingAmount", searchConditions.getMinOutstandingAmount());
                    params.addValue("maxOutstandingAmount", searchConditions.getMaxOutstandingAmount());
                    sql.append(" ( a.outstandingAmt between :minOutstandingAmount and :maxOutstandingAmount ) ");
                } else {
                    checkAndUpdateWhereClause(sql);
                    // params.addValue("outstandingAmountCondition",
                    // searchConditions.getOutstandingAmountCondition());
                    params.addValue("outstandingAmount", searchConditions.getOutstandingAmount());
                    sql.append(" a.outstandingAmt ").append(searchConditions.getOutstandingAmountCondition())
                            .append(" :outstandingAmount ");
                }
            }

            return sql.toString();
        }

        private void checkAndUpdateWhereClause(final StringBuffer sql) {
            if (isWhereClauseAdded) {
                sql.append(" and ");
            } else {
                sql.append(" where ");
                isWhereClauseAdded = true;
            }
        }

        @Override
        public AdHocSearchQueryData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            final String officeName = rs.getString("officeName");
            final String loanProductName = rs.getString("productName");
            final Integer count = JdbcSupport.getInteger(rs, "count");
            final BigDecimal loanOutStanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "outstanding").setScale(2,
                    RoundingMode.HALF_UP);
            final Double percentage = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "percentOut").setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            return AdHocSearchQueryData.matchedResult(officeName, loanProductName, count, loanOutStanding, percentage);
        }

    }

}