/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.search.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.search.data.SearchConditions;
import org.mifosplatform.portfolio.search.data.SearchData;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SearchReadPlatformServiceImpl implements SearchReadPlatformService {

    private final NamedParameterJdbcTemplate namedParameterjdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public SearchReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.namedParameterjdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Collection<SearchData> retriveMatchingData(final SearchConditions searchConditions) {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();

        final SearchMapper rm = new SearchMapper();

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("hierarchy", hierarchy + "%");
        params.addValue("search", searchConditions.getSearchQuery());
        params.addValue("partialSearch", "%" + searchConditions.getSearchQuery() + "%");

        return this.namedParameterjdbcTemplate.query(rm.searchSchema(searchConditions), params, rm);
    }

    private static final class SearchMapper implements RowMapper<SearchData> {

        public String searchSchema(final SearchConditions searchConditions) {

            final String union = " union ";
            final String clientExactMatchSql = " (select 'CLIENT' as entityType, c.id as entityId, c.display_name as entityName, c.external_id as entityExternalId, c.account_no as entityAccountNo "
                    + " , c.office_id as parentId, o.name as parentName "
                    + " from m_client c join m_office o on o.id = c.office_id where o.hierarchy like :hierarchy and (c.account_no like :search or c.display_name like :search or c.external_id like :search)) ";

            final String clientMatchSql = " (select 'CLIENT' as entityType, c.id as entityId, c.display_name as entityName, c.external_id as entityExternalId, c.account_no as entityAccountNo "
                    + " , c.office_id as parentId, o.name as parentName "
                    + " from m_client c join m_office o on o.id = c.office_id where o.hierarchy like :hierarchy and (c.account_no like :partialSearch and c.account_no not like :search) or "
                    + "(c.display_name like :partialSearch and c.display_name not like :search) or "
                    + "(c.external_id like :partialSearch and c.external_id not like :search))";

            final String loanExactMatchSql = " (select 'LOAN' as entityType, l.id as entityId, pl.name as entityName, l.external_id as entityExternalId, l.account_no as entityAccountNo "
                    + " , c.id as parentId, c.display_name as parentName "
                    + " from m_loan l join m_client c on l.client_id = c.id join m_office o on o.id = c.office_id join m_product_loan pl on pl.id=l.product_id where o.hierarchy like :hierarchy and l.account_no like :search) ";

            final String loanMatchSql = " (select 'LOAN' as entityType, l.id as entityId, pl.name as entityName, l.external_id as entityExternalId, l.account_no as entityAccountNo "
                    + " , c.id as parentId, c.display_name as parentName "
                    + " from m_loan l join m_client c on l.client_id = c.id join m_office o on o.id = c.office_id join m_product_loan pl on pl.id=l.product_id where o.hierarchy like :hierarchy and l.account_no like :partialSearch and l.account_no not like :search) ";

            final String clientIdentifierExactMatchSql = " (select 'CLIENTIDENTIFIER' as entityType, ci.id as entityId, ci.document_key as entityName, "
                    + " null as entityExternalId, null as entityAccountNo, c.id as parentId, c.display_name as parentName "
                    + " from m_client_identifier ci join m_client c on ci.client_id=c.id join m_office o on o.id = c.office_id "
                    + " where o.hierarchy like :hierarchy and ci.document_key like :search) ";

            final String clientIdentifierMatchSql = " (select 'CLIENTIDENTIFIER' as entityType, ci.id as entityId, ci.document_key as entityName, "
                    + " null as entityExternalId, null as entityAccountNo, c.id as parentId, c.display_name as parentName "
                    + " from m_client_identifier ci join m_client c on ci.client_id=c.id join m_office o on o.id = c.office_id "
                    + " where o.hierarchy like :hierarchy and ci.document_key like :partialSearch and ci.document_key not like :search) ";

            final String groupExactMatchSql = " (select IF(g.level_id=1,'CENTER','GROUP') as entityType, g.id as entityId, g.display_name as entityName, g.external_id as entityExternalId, NULL as entityAccountNo "
                    + " , g.office_id as parentId, o.name as parentName "
                    + " from m_group g join m_office o on o.id = g.office_id where o.hierarchy like :hierarchy and g.display_name like :search) ";

            final String groupMatchSql = " (select IF(g.level_id=1,'CENTER','GROUP') as entityType, g.id as entityId, g.display_name as entityName, g.external_id as entityExternalId, NULL as entityAccountNo "
                    + " , g.office_id as parentId, o.name as parentName "
                    + " from m_group g join m_office o on o.id = g.office_id where o.hierarchy like :hierarchy and g.display_name like :partialSearch and g.display_name not like :search) ";

            final StringBuffer sql = new StringBuffer();

            // first include all exact matches
            if (searchConditions.isClientSearch()) {
                sql.append(clientExactMatchSql).append(union);
            }

            if (searchConditions.isLoanSeach()) {
                sql.append(loanExactMatchSql).append(union);
            }

            if (searchConditions.isClientIdentifierSearch()) {
                sql.append(clientIdentifierExactMatchSql).append(union);
            }

            if (searchConditions.isGroupSearch()) {
                sql.append(groupExactMatchSql).append(union);
            }

            // include all matching records
            if (searchConditions.isClientSearch()) {
                sql.append(clientMatchSql).append(union);
            }

            if (searchConditions.isLoanSeach()) {
                sql.append(loanMatchSql).append(union);
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
            return new SearchData(entityId, entityAccountNo, entityExternalId, entityName, entityType, parentId, parentName);
        }

    }

}
