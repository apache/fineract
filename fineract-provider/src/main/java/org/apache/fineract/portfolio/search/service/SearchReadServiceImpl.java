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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.portfolio.group.domain.GroupingTypeEnumerations;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.portfolio.search.SearchConstants;
import org.apache.fineract.portfolio.search.data.AdHocQuerySearchConditions;
import org.apache.fineract.portfolio.search.data.AdHocQuerySearchRequest;
import org.apache.fineract.portfolio.search.data.AdHocSearchQueryData;
import org.apache.fineract.portfolio.search.data.SearchConditions;
import org.apache.fineract.portfolio.search.data.SearchData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountStatusEnumData;
import org.apache.fineract.portfolio.shareaccounts.service.SharesEnumerations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@RequiredArgsConstructor
public class SearchReadServiceImpl implements SearchReadService {

    private static final String CONDITION_BETWEEN = "between";
    private static final String PARAM_LOAN_DATE_OPTION = "loanDateOption";
    private static final String PARAM_OUTSTANDING_AMOUNT_PERCENTAGE_CONDITION = "outStandingAmountPercentageCondition";
    private static final String PARAM_OUTSTANDING_AMOUNT_CONDITION = "outstandingAmountCondition";
    private static final String VALID_SEARCH_CONDITIONS = "=, >, >=, <, <=, between";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final DatabaseSpecificSQLGenerator sqlGenerator;

    @Override
    public List<SearchData> retriveMatchingData(final SearchConditions searchConditions) {
        final String hierarchy = searchConditions.getHierarchy();

        final SearchMapper rm = new SearchMapper();

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("hierarchy", hierarchy + "%");
        if (searchConditions.getExactMatch()) {
            params.addValue("search", searchConditions.getSearchQuery());
        } else {
            params.addValue("search", "%" + searchConditions.getSearchQuery() + "%");
        }
        params.addValue("searchTransactionId", parseSearchTransactionId(searchConditions.getSearchQuery()));
        params.addValue("loanRepaymentTransactionType", LoanTransactionType.REPAYMENT.getValue());
        params.addValue("savingsDepositTransactionType", SavingsAccountTransactionType.DEPOSIT.getValue());
        params.addValue("savingsWithdrawalTransactionType", SavingsAccountTransactionType.WITHDRAWAL.getValue());
        return namedParameterJdbcTemplate.query(searchSchema(searchConditions), params, rm);
    }

    public String searchSchema(final SearchConditions searchConditions) {

        final String union = " union ";
        final String nullLong = sqlGenerator.castInteger("null");
        final String clientMatchSql = """
                ( (select 'CLIENT' as entityType, c.id as entityId, c.display_name as entityName, \
                c.external_id as entityExternalId, c.account_no as entityAccountNo, \
                c.office_id as parentId, o.name as parentName, c.mobile_no as entityMobileNo, \
                c.status_enum as entityStatusEnum, null as subEntityType, null as parentType, \
                %s as transactionId, null as transactionType, null as transactionExternalId, null as transactionRefNo, \
                %s as accountId, null as accountNo, null as accountType \
                from m_client c join m_office o on o.id = c.office_id \
                where o.hierarchy like :hierarchy \
                and (c.account_no like :search or c.display_name like :search \
                or c.external_id like :search or c.mobile_no like :search)) \
                order by c.id desc)""".formatted(nullLong, nullLong);

        final String loanMatchSql = """
                ( (select 'LOAN' as entityType, l.id as entityId, pl.name as entityName, \
                l.external_id as entityExternalId, l.account_no as entityAccountNo, \
                coalesce(c.id,g.id) as parentId, coalesce(c.display_name,g.display_name) as parentName, \
                null as entityMobileNo, l.loan_status_id as entityStatusEnum, null as subEntityType, \
                CASE WHEN g.id is null THEN 'client' ELSE 'group' END as parentType, \
                %s as transactionId, null as transactionType, null as transactionExternalId, null as transactionRefNo, \
                %s as accountId, null as accountNo, null as accountType \
                from m_loan l left join m_client c on l.client_id = c.id \
                left join m_group g ON l.group_id = g.id \
                left join m_office o on o.id = c.office_id \
                left join m_product_loan pl on pl.id=l.product_id \
                where (o.hierarchy IS NULL OR o.hierarchy like :hierarchy) \
                and (l.account_no like :search or l.external_id like :search)) \
                order by l.id desc)""".formatted(nullLong, nullLong);

        final String savingMatchSql = """
                ( (select 'SAVING' as entityType, s.id as entityId, sp.name as entityName, \
                s.external_id as entityExternalId, s.account_no as entityAccountNo, \
                coalesce(c.id,g.id) as parentId, coalesce(c.display_name, g.display_name) as parentName, \
                null as entityMobileNo, s.status_enum as entityStatusEnum, \
                concat(s.deposit_type_enum, '') as subEntityType, \
                CASE WHEN g.id is null THEN 'client' ELSE 'group' END as parentType, \
                %s as transactionId, null as transactionType, null as transactionExternalId, null as transactionRefNo, \
                %s as accountId, null as accountNo, null as accountType \
                from m_savings_account s left join m_client c on s.client_id = c.id \
                left join m_group g ON s.group_id = g.id \
                left join m_office o on o.id = c.office_id \
                left join m_savings_product sp on sp.id=s.product_id \
                where (o.hierarchy IS NULL OR o.hierarchy like :hierarchy) \
                and (s.account_no like :search or s.external_id like :search)) \
                order by s.id desc)""".formatted(nullLong, nullLong);

        final String shareMatchSql = """
                ( (select 'SHARE' as entityType, s.id as entityId, sp.name as entityName, \
                s.external_id as entityExternalId, s.account_no as entityAccountNo, \
                c.id as parentId, c.display_name as parentName, null as entityMobileNo, \
                s.status_enum as entityStatusEnum, null as subEntityType, 'client' as parentType, \
                %s as transactionId, null as transactionType, null as transactionExternalId, null as transactionRefNo, \
                %s as accountId, null as accountNo, null as accountType \
                from m_share_account s left join m_client c on s.client_id = c.id \
                left join m_office o on o.id = c.office_id \
                left join m_share_product sp on sp.id=s.product_id \
                where (o.hierarchy IS NULL OR o.hierarchy like :hierarchy) \
                and (s.account_no like :search or s.external_id like :search)) \
                order by s.id desc)""".formatted(nullLong, nullLong);

        final String clientIdentifierMatchSql = """
                ( (select 'CLIENTIDENTIFIER' as entityType, ci.id as entityId, ci.document_key as entityName, \
                null as entityExternalId, null as entityAccountNo, c.id as parentId, \
                c.display_name as parentName, null as entityMobileNo, \
                c.status_enum as entityStatusEnum, null as subEntityType, null as parentType, \
                %s as transactionId, null as transactionType, null as transactionExternalId, null as transactionRefNo, \
                %s as accountId, null as accountNo, null as accountType \
                from m_client_identifier ci join m_client c on ci.client_id=c.id \
                join m_office o on o.id = c.office_id \
                where o.hierarchy like :hierarchy and ci.document_key like :search) \
                order by ci.id desc)""".formatted(nullLong, nullLong);

        final String groupMatchSql = """
                ( (select CASE WHEN g.level_id=1 THEN 'CENTER' ELSE 'GROUP' END as entityType, \
                g.id as entityId, g.display_name as entityName, \
                g.external_id as entityExternalId, g.account_no as entityAccountNo, \
                g.office_id as parentId, o.name as parentName, null as entityMobileNo, \
                g.status_enum as entityStatusEnum, null as subEntityType, null as parentType, \
                %s as transactionId, null as transactionType, null as transactionExternalId, null as transactionRefNo, \
                %s as accountId, null as accountNo, null as accountType \
                from m_group g join m_office o on o.id = g.office_id \
                where o.hierarchy like :hierarchy \
                and (g.account_no like :search or g.display_name like :search \
                or g.external_id like :search)) \
                order by g.id desc)""".formatted(nullLong, nullLong);

        final String loanTransactionMatchSql = """
                ( (select 'LOAN_TRANSACTION' as entityType, l.id as entityId, pl.name as entityName, \
                lt.external_id as entityExternalId, l.account_no as entityAccountNo, \
                coalesce(c.id,g.id) as parentId, coalesce(c.display_name,g.display_name) as parentName, \
                null as entityMobileNo, l.loan_status_id as entityStatusEnum, null as subEntityType, \
                CASE WHEN g.id is null THEN 'client' ELSE 'group' END as parentType, \
                lt.id as transactionId, 'repayment' as transactionType, lt.external_id as transactionExternalId, \
                null as transactionRefNo, l.id as accountId, l.account_no as accountNo, 'loan' as accountType \
                from m_loan_transaction lt join m_loan l on l.id = lt.loan_id \
                left join m_client c on l.client_id = c.id \
                left join m_group g ON l.group_id = g.id \
                left join m_office o on o.id = coalesce(c.office_id, g.office_id) \
                left join m_product_loan pl on pl.id=l.product_id \
                left join m_account_transfer_transaction att on lt.id in (att.from_loan_transaction_id, att.to_loan_transaction_id) \
                and att.is_reversed = false \
                left join m_payment_detail pd on pd.id = lt.payment_detail_id \
                left join m_payment_detail atpd on atpd.id = att.payment_detail_id \
                where o.hierarchy like :hierarchy \
                and lt.transaction_type_enum = :loanRepaymentTransactionType \
                and (lt.id = :searchTransactionId or lt.external_id like :search or pd.check_number like :search \
                or pd.routing_code like :search or pd.receipt_number like :search or atpd.check_number like :search \
                or atpd.routing_code like :search or atpd.receipt_number like :search)) \
                order by lt.id desc)""";

        final String savingTransactionMatchSql = """
                ( (select 'SAVINGS_TRANSACTION' as entityType, s.id as entityId, sp.name as entityName, \
                st.external_id as entityExternalId, s.account_no as entityAccountNo, \
                coalesce(c.id,g.id) as parentId, coalesce(c.display_name, g.display_name) as parentName, \
                null as entityMobileNo, s.status_enum as entityStatusEnum, \
                concat(s.deposit_type_enum, '') as subEntityType, \
                CASE WHEN g.id is null THEN 'client' ELSE 'group' END as parentType, \
                st.id as transactionId, CASE WHEN st.transaction_type_enum = :savingsDepositTransactionType THEN 'deposit' ELSE 'withdrawal' END as transactionType, \
                st.external_id as transactionExternalId, st.ref_no as transactionRefNo, \
                s.id as accountId, s.account_no as accountNo, 'savings' as accountType \
                from m_savings_account_transaction st join m_savings_account s on s.id = st.savings_account_id \
                left join m_client c on s.client_id = c.id \
                left join m_group g ON s.group_id = g.id \
                left join m_office o on o.id = coalesce(c.office_id, g.office_id) \
                left join m_savings_product sp on sp.id=s.product_id \
                left join m_account_transfer_transaction att on st.id in (att.from_savings_transaction_id, att.to_savings_transaction_id) \
                and att.is_reversed = false \
                left join m_payment_detail pd on pd.id = st.payment_detail_id \
                left join m_payment_detail atpd on atpd.id = att.payment_detail_id \
                where o.hierarchy like :hierarchy \
                and st.transaction_type_enum in (:savingsDepositTransactionType, :savingsWithdrawalTransactionType) \
                and st.is_reversal = false \
                and (st.id = :searchTransactionId or st.external_id like :search or st.ref_no like :search or pd.check_number like :search \
                or pd.routing_code like :search or pd.receipt_number like :search or atpd.check_number like :search \
                or atpd.routing_code like :search or atpd.receipt_number like :search)) \
                order by st.id desc)""";

        final StringBuilder sql = new StringBuilder();
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
        if (searchConditions.isLoanTransactionSearch()) {
            sql.append(loanTransactionMatchSql).append(union);
        }
        if (searchConditions.isSavingTransactionSearch()) {
            sql.append(savingTransactionMatchSql).append(union);
        }
        if (sql.isEmpty()) {
            sql.append("""
                    select null as entityType, null as entityId, null as entityName, null as entityExternalId, \
                    null as entityAccountNo, null as parentId, null as parentName, null as entityMobileNo, \
                    null as entityStatusEnum, null as subEntityType, null as parentType, null as transactionId, \
                    null as transactionType, null as transactionExternalId, null as transactionRefNo, null as accountId, \
                    null as accountNo, null as accountType where 1 = 0""");
            sql.append(" ").append(sqlGenerator.limit(50, 0));
            return sql.toString();
        }
        // remove last occurrence of "union all" string
        sql.replace(sql.lastIndexOf(union), sql.length(), "");
        // only get the first 50 rows in case of searcing
        sql.append(" ").append(sqlGenerator.limit(50, 0));
        return sql.toString();
    }

    private static final class SearchMapper implements RowMapper<SearchData> {

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
            final Integer subEntityTypeValue = JdbcSupport.getInteger(rs, "subEntityType");
            final EnumOptionData subEntityTypeCode = SavingsEnumerations.depositType(subEntityTypeValue);
            final Long transactionId = JdbcSupport.getLong(rs, "transactionId");
            final String transactionType = rs.getString("transactionType");
            final String transactionExternalId = rs.getString("transactionExternalId");
            final String transactionRefNo = rs.getString("transactionRefNo");
            final Long accountId = JdbcSupport.getLong(rs, "accountId");
            final String accountNo = rs.getString("accountNo");
            final String accountType = rs.getString("accountType");

            EnumOptionData entityStatus = new EnumOptionData(0L, "", "");

            if (entityType.equalsIgnoreCase("client") || entityType.equalsIgnoreCase("clientidentifier")) {
                entityStatus = ClientEnumerations.status(entityStatusEnum);
            } else if (entityType.equalsIgnoreCase("group") || entityType.equalsIgnoreCase("center")) {
                entityStatus = GroupingTypeEnumerations.status(entityStatusEnum);
            } else if (entityType.equalsIgnoreCase("loan") || entityType.equalsIgnoreCase("loan_transaction")) {
                LoanStatusEnumData loanStatusEnumData = LoanEnumerations.status(entityStatusEnum);
                entityStatus = LoanEnumerations.status(loanStatusEnumData);
            } else if (entityType.equalsIgnoreCase("saving") || entityType.equalsIgnoreCase("savings_transaction")) {
                SavingsAccountStatusEnumData savingsAccountStatusEnumData = SavingsEnumerations.status(entityStatusEnum);
                entityStatus = SavingsEnumerations.status(savingsAccountStatusEnumData);
            } else if (entityType.equalsIgnoreCase("share")) {
                ShareAccountStatusEnumData shareAccountStatusEnumData = SharesEnumerations.status(entityStatusEnum);
                entityStatus = SharesEnumerations.status(shareAccountStatusEnumData);
            }

            return new SearchData(entityId, entityAccountNo, entityExternalId, entityName, entityType, parentId, parentName, parentType,
                    entityMobileNo, entityStatus, subEntityTypeCode.getCode(), transactionId, transactionType, transactionExternalId,
                    transactionRefNo, accountId, accountNo, accountType);
        }

    }

    private static Long parseSearchTransactionId(final String searchQuery) {
        if (StringUtils.isBlank(searchQuery)) {
            return -1L;
        }
        try {
            return Long.valueOf(searchQuery);
        } catch (final NumberFormatException e) {
            return -1L;
        }
    }

    @Override
    public AdHocSearchQueryData retrieveAdHocQueryTemplate() {

        final Collection<LoanProductData> loanProducts = loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
        final Collection<OfficeData> offices = officeReadPlatformService.retrieveAllOfficesForDropdown();

        return AdHocSearchQueryData.template(loanProducts, offices);
    }

    @Override
    public List<AdHocSearchQueryData> retrieveAdHocQueryMatchingData(final AdHocQuerySearchRequest request) {
        final AdHocQuerySearchConditions searchConditions = convertToSearchConditions(request);
        final AdHocQuerySearchMapper rm = new AdHocQuerySearchMapper();
        final MapSqlParameterSource params = new MapSqlParameterSource();
        return namedParameterJdbcTemplate.query(rm.schema(searchConditions, params), params, rm);
    }

    private AdHocQuerySearchConditions convertToSearchConditions(final AdHocQuerySearchRequest request) {
        validateLoanDateOption(request.getLoanDateOption());
        final boolean includeOutStandingAmountPercentage = Boolean.TRUE.equals(request.getIncludeOutStandingAmountPercentage());
        final boolean includeOutstandingAmount = Boolean.TRUE.equals(request.getIncludeOutstandingAmount());
        final String outStandingAmountPercentageCondition = validateSearchCondition(request.getOutStandingAmountPercentageCondition(),
                PARAM_OUTSTANDING_AMOUNT_PERCENTAGE_CONDITION, includeOutStandingAmountPercentage);
        final String outstandingAmountCondition = validateSearchCondition(request.getOutstandingAmountCondition(),
                PARAM_OUTSTANDING_AMOUNT_CONDITION, includeOutstandingAmount);

        return AdHocQuerySearchConditions.instance(request.getLoanStatus(), request.getLoanProducts(), request.getOffices(),
                request.getLoanDateOption(), request.getLoanFromDate(), request.getLoanToDate(), includeOutStandingAmountPercentage,
                outStandingAmountPercentageCondition, request.getMinOutStandingAmountPercentage(),
                request.getMaxOutStandingAmountPercentage(), request.getOutStandingAmountPercentage(), includeOutstandingAmount,
                outstandingAmountCondition, request.getMinOutstandingAmount(), request.getMaxOutstandingAmount(),
                request.getOutstandingAmount());
    }

    private static void validateLoanDateOption(final String loanDateOption) {
        if (StringUtils.isBlank(loanDateOption) || SearchConstants.SearchLoanDate.getAllValues().contains(loanDateOption)) {
            return;
        }
        throw invalidSearchParameter(PARAM_LOAN_DATE_OPTION, loanDateOption,
                String.join(", ", SearchConstants.SearchLoanDate.getAllValues()));
    }

    private static String validateSearchCondition(final String condition, final String parameterName, final boolean required) {
        if (StringUtils.isBlank(condition)) {
            if (required) {
                throw invalidSearchParameter(parameterName, condition, VALID_SEARCH_CONDITIONS);
            }
            return condition;
        }
        final String trimmedCondition = condition.trim();
        if (CONDITION_BETWEEN.equalsIgnoreCase(trimmedCondition)) {
            return CONDITION_BETWEEN;
        }
        return switch (trimmedCondition) {
            case "=", ">", ">=", "<", "<=" -> trimmedCondition;
            default -> throw invalidSearchParameter(parameterName, condition, VALID_SEARCH_CONDITIONS);
        };
    }

    private static PlatformApiDataValidationException invalidSearchParameter(final String parameterName, final String value,
            final String expectedValues) {
        return new PlatformApiDataValidationException("validation.msg.search.parameter.is.not.one.of.expected.enumerations",
                "The parameter `" + parameterName + "` must be one of [ " + expectedValues + " ].", parameterName, value);
    }

    private static final class AdHocQuerySearchMapper implements RowMapper<AdHocSearchQueryData> {

        private boolean isWhereClauseAdded = false;

        // TODO- build the query dynamically based on selected entity types, for
        // now adding query for only loan entity.
        public String schema(final AdHocQuerySearchConditions searchConditions, final MapSqlParameterSource params) {

            final StringBuilder sql = new StringBuilder();
            sql.append(
                    "Select a.name as officeName, a.Product as productName, a.cnt as 'count', a.outstandingAmt as outstanding, a.percentOut as percentOut ")
                    .append("from (select mo.name, mp.name Product, SUM(COALESCE(ml.total_expected_repayment_derived,0.0)) TotalAmt, count(*) cnt, ")
                    .append("SUM(COALESCE(ml.total_outstanding_derived,0.0)) outstandingAmt, ")
                    .append("(SUM(COALESCE(ml.total_outstanding_derived,0.0)) * 100 / SUM(COALESCE(ml.total_expected_repayment_derived,0.0))) percentOut ")
                    .append("from m_loan ml inner join m_product_loan mp on mp.id=ml.product_id ")
                    .append("inner join m_client mc on mc.id=ml.client_id ").append("inner join m_office mo on mo.id=mc.office_id ");

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
                if (searchConditions.getLoanDateOption().equals(SearchConstants.SearchLoanDate.APPROVAL_DATE.getValue())) {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("loanFromDate", searchConditions.getLoanFromDate());
                    params.addValue("loanToDate", searchConditions.getLoanToDate());
                    sql.append(" ( ml.approvedon_date between :loanFromDate and :loanToDate ) ");
                } else if (searchConditions.getLoanDateOption().equals(SearchConstants.SearchLoanDate.CREATED_DATE.getValue())) {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("loanFromDate", searchConditions.getLoanFromDate());
                    params.addValue("loanToDate", searchConditions.getLoanToDate());
                    sql.append(" ( ml.submittedon_date between :loanFromDate and :loanToDate ) ");
                } else if (searchConditions.getLoanDateOption().equals(SearchConstants.SearchLoanDate.DISBURSAL_DATE.getValue())) {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("loanFromDate", searchConditions.getLoanFromDate());
                    params.addValue("loanToDate", searchConditions.getLoanToDate());
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
                    params.addValue("minOutStandingAmountPercentage", searchConditions.getMinOutStandingAmountPercentage());
                    params.addValue("maxOutStandingAmountPercentage", searchConditions.getMaxOutStandingAmountPercentage());
                    sql.append(" ( a.percentOut between :minOutStandingAmountPercentage and :maxOutStandingAmountPercentage ) ");
                } else {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("outStandingAmountPercentage", searchConditions.getOutStandingAmountPercentage());
                    sql.append(" a.percentOut ").append(searchConditions.getOutStandingAmountPercentageCondition())
                            .append(" :outStandingAmountPercentage ");
                }
            }

            if (searchConditions.getIncludeOutstandingAmount()) {
                if (searchConditions.getOutstandingAmountCondition().equals("between")) {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("minOutstandingAmount", searchConditions.getMinOutstandingAmount());
                    params.addValue("maxOutstandingAmount", searchConditions.getMaxOutstandingAmount());
                    sql.append(" ( a.outstandingAmt between :minOutstandingAmount and :maxOutstandingAmount ) ");
                } else {
                    checkAndUpdateWhereClause(sql);
                    params.addValue("outstandingAmount", searchConditions.getOutstandingAmount());
                    sql.append(" a.outstandingAmt ").append(searchConditions.getOutstandingAmountCondition())
                            .append(" :outstandingAmount ");
                }
            }

            return sql.toString();
        }

        private void checkAndUpdateWhereClause(final StringBuilder sql) {
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
                    MoneyHelper.getRoundingMode());
            final Double percentage = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "percentOut")
                    .setScale(2, MoneyHelper.getRoundingMode()).doubleValue();
            return AdHocSearchQueryData.matchedResult(officeName, loanProductName, count, loanOutStanding, percentage);
        }
    }
}
