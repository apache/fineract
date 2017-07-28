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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.RescheduleLoansApiConstants;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestData;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestEnumerations;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestStatusEnumData;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestTimelineData;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.amazonaws.util.StringUtils;

@Service
public class LoanRescheduleRequestReadPlatformServiceImpl implements LoanRescheduleRequestReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanRescheduleRequestRowMapper loanRescheduleRequestRowMapper = new LoanRescheduleRequestRowMapper();
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public LoanRescheduleRequestReadPlatformServiceImpl(final RoutingDataSource dataSource, LoanRepositoryWrapper loanRepositoryWrapper,
            final CodeValueReadPlatformService codeValueReadPlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
    }

    private static final class LoanRescheduleRequestRowMapper implements RowMapper<LoanRescheduleRequestData> {

        private final String schema;

        public LoanRescheduleRequestRowMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(200);

            sqlBuilder.append("lr.id as id, lr.loan_id as loanId, lr.status_enum as statusEnum, ");
            sqlBuilder.append("mc.display_name as clientName, ");
            sqlBuilder.append("mc.id as clientId, ");
            sqlBuilder.append("ml.account_no as loanAccountNumber, ");
            sqlBuilder.append("lr.reschedule_from_installment as rescheduleFromInstallment, ");
            sqlBuilder.append("lr.reschedule_from_date as rescheduleFromDate, ");
            sqlBuilder.append("lr.recalculate_interest as recalculateInterest, ");
            sqlBuilder.append("lr.reschedule_reason_cv_id as rescheduleReasonCvId, ");
            sqlBuilder.append("cv.code_value as rescheduleReasonCvValue, ");
            sqlBuilder.append("lr.reschedule_reason_comment as rescheduleReasonComment, ");

            sqlBuilder.append("lr.submitted_on_date as submittedOnDate, ");
            sqlBuilder.append("sbu.username as submittedByUsername, ");
            sqlBuilder.append("sbu.firstname as submittedByFirstname, ");
            sqlBuilder.append("sbu.lastname as submittedByLastname, ");

            sqlBuilder.append("lr.approved_on_date as approvedOnDate, ");
            sqlBuilder.append("abu.username as approvedByUsername, ");
            sqlBuilder.append("abu.firstname as approvedByFirstname, ");
            sqlBuilder.append("abu.lastname as approvedByLastname, ");

            sqlBuilder.append("lr.rejected_on_date as rejectedOnDate, ");
            sqlBuilder.append("rbu.username as rejectedByUsername, ");
            sqlBuilder.append("rbu.firstname as rejectedByFirstname, ");
            sqlBuilder.append("rbu.lastname as rejectedByLastname, ");
            
            sqlBuilder.append("tv.id as termId,");
            sqlBuilder.append("tv.term_type as termType,");
            sqlBuilder.append("tv.applicable_date as variationApplicableFrom, ");
            sqlBuilder.append("tv.decimal_value as decimalValue, ");
            sqlBuilder.append("tv.date_value as dateValue, ");
            sqlBuilder.append("tv.is_specific_to_installment as isSpecificToInstallment ");

            sqlBuilder.append("from " + loanRescheduleRequestTableName() + " lr ");
            sqlBuilder.append("left join m_code_value cv on cv.id = lr.reschedule_reason_cv_id ");
            sqlBuilder.append("left join m_appuser sbu on sbu.id = lr.submitted_by_user_id ");
            sqlBuilder.append("left join m_appuser abu on abu.id = lr.approved_by_user_id ");
            sqlBuilder.append("left join m_appuser rbu on rbu.id = lr.rejected_by_user_id ");
            sqlBuilder.append("left join m_loan ml on ml.id = lr.loan_id ");
            sqlBuilder.append("left join m_client mc on mc.id = ml.client_id ");
            sqlBuilder.append("join m_loan_reschedule_request_term_variations_mapping rrtvm on lr.id = rrtvm.loan_reschedule_request_id ");
            sqlBuilder.append("join m_loan_term_variations tv on tv.id = rrtvm.loan_term_variations_id and tv.parent_id is null") ;

            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        public String loanRescheduleRequestTableName() {
            return "m_loan_reschedule_request";
        }

        @Override
        @SuppressWarnings("unused")
        public LoanRescheduleRequestData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long loanId = rs.getLong("loanId");
            final Integer statusEnumId = JdbcSupport.getInteger(rs, "statusEnum");
            final LoanRescheduleRequestStatusEnumData statusEnum = LoanRescheduleRequestEnumerations.status(statusEnumId);
            final String clientName = rs.getString("clientName");
            final String loanAccountNumber = rs.getString("loanAccountNumber");
            final Long clientId = rs.getLong("clientId");
            final Integer rescheduleFromInstallment = JdbcSupport.getInteger(rs, "rescheduleFromInstallment");
            final LocalDate rescheduleFromDate = JdbcSupport.getLocalDate(rs, "rescheduleFromDate");
            final Long rescheduleReasonCvId = JdbcSupport.getLong(rs, "rescheduleReasonCvId");
            final String rescheduleReasonCvValue = rs.getString("rescheduleReasonCvValue");
            final CodeValueData rescheduleReasonCodeValue = CodeValueData.instance(rescheduleReasonCvId, rescheduleReasonCvValue);
            final String rescheduleReasonComment = rs.getString("rescheduleReasonComment");
            final Boolean recalculateInterest = rs.getBoolean("recalculateInterest");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final LocalDate approvedOnDate = JdbcSupport.getLocalDate(rs, "approvedOnDate");
            final String approvedByUsername = rs.getString("approvedByUsername");
            final String approvedByFirstname = rs.getString("approvedByFirstname");
            final String approvedByLastname = rs.getString("approvedByLastname");

            final LocalDate rejectedOnDate = JdbcSupport.getLocalDate(rs, "rejectedOnDate");
            final String rejectedByUsername = rs.getString("rejectedByUsername");
            final String rejectedByFirstname = rs.getString("rejectedByFirstname");
            final String rejectedByLastname = rs.getString("rejectedByLastname");
            final Collection<CodeValueData> rescheduleReasons = null;
            final LoanRescheduleRequestTimelineData timeline = new LoanRescheduleRequestTimelineData(submittedOnDate, submittedByUsername,
                    submittedByFirstname, submittedByLastname, approvedOnDate, approvedByUsername, approvedByFirstname, approvedByLastname,
                    rejectedOnDate, rejectedByUsername, rejectedByFirstname, rejectedByLastname);
            
            Collection<LoanTermVariationsData> loanTermVariations = new ArrayList<>();
            
            do {
                Long tempId = rs.getLong("id");
                if (id.equals(tempId)) {
                    loanTermVariations.add(fetchLoanTermVariation(rs));
                } else {
                    rs.previous();
                    break;
                }
            } while (rs.next());

            return LoanRescheduleRequestData.instance(id, loanId, statusEnum, rescheduleFromInstallment, rescheduleFromDate,
                    rescheduleReasonCodeValue, rescheduleReasonComment, timeline, clientName, loanAccountNumber, clientId,
                    recalculateInterest, rescheduleReasons, loanTermVariations);
        }
        
        private LoanTermVariationsData fetchLoanTermVariation(final ResultSet rs) throws SQLException {
            final Long id = rs.getLong("termId");
            final LocalDate variationApplicableFrom = JdbcSupport.getLocalDate(rs, "variationApplicableFrom");
            final BigDecimal decimalValue = rs.getBigDecimal("decimalValue");
            final LocalDate dateValue = JdbcSupport.getLocalDate(rs, "dateValue");
            final boolean isSpecificToInstallment = rs.getBoolean("isSpecificToInstallment");
            final int termType = rs.getInt("termType");

            final LoanTermVariationsData loanTermVariationsData = new LoanTermVariationsData(id,
                    LoanEnumerations.loanvariationType(termType), variationApplicableFrom, decimalValue, dateValue,
                    isSpecificToInstallment);
            return loanTermVariationsData;
        }

    }
    
    private static final class LoanRescheduleRequestRowMapperForBulkApproval implements RowMapper<LoanRescheduleRequestData> {

        public String schema() {
            final StringBuilder sqlBuilder = new StringBuilder(200);

            sqlBuilder.append("lrr.id as id, lrr.status_enum as statusEnum, lrr.reschedule_from_date as rescheduleFromDate, ");
            sqlBuilder.append("cv.id as rescheduleReasonCvId, cv.code_value as rescheduleReasonCvValue, ");
            sqlBuilder
                    .append(" loan.id as loanId, loan.account_no as loanAccountNumber, client.id as clientId, client.display_name as clientName ");
            sqlBuilder.append("from m_loan_reschedule_request lrr ");
            sqlBuilder.append("left join m_loan loan on loan.id = lrr.loan_id ");
            sqlBuilder.append("left join m_client client on client.id = loan.client_id ");
            sqlBuilder.append("left join m_code_value cv on cv.id = lrr.reschedule_reason_cv_id ");
            return sqlBuilder.toString();
        }

        @Override
        public LoanRescheduleRequestData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long loanId = rs.getLong("loanId");
            final Integer statusEnumId = JdbcSupport.getInteger(rs, "statusEnum");
            final LoanRescheduleRequestStatusEnumData statusEnum = LoanRescheduleRequestEnumerations.status(statusEnumId);
            final String clientName = rs.getString("clientName");
            final String loanAccountNumber = rs.getString("loanAccountNumber");
            final Long clientId = rs.getLong("clientId");
            final LocalDate rescheduleFromDate = JdbcSupport.getLocalDate(rs, "rescheduleFromDate");
            final Long rescheduleReasonCvId = JdbcSupport.getLong(rs, "rescheduleReasonCvId");
            final String rescheduleReasonCvValue = rs.getString("rescheduleReasonCvValue");
            final CodeValueData rescheduleReasonCodeValue = CodeValueData.instance(rescheduleReasonCvId, rescheduleReasonCvValue);
            return LoanRescheduleRequestData.instance(id, loanId, statusEnum, clientName, loanAccountNumber, clientId, rescheduleFromDate,
                    rescheduleReasonCodeValue);
        }
    }
    
    @Override
    public List<LoanRescheduleRequestData> readLoanRescheduleRequests(Long loanId) {
        this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        final String sql = "select " + this.loanRescheduleRequestRowMapper.schema() + " where lr.loan_id = ?";

        return this.jdbcTemplate.query(sql, this.loanRescheduleRequestRowMapper, new Object[] { loanId });
    }

    @Override
    public LoanRescheduleRequestData readLoanRescheduleRequest(Long requestId) {

        try {
            final String sql = "select " + this.loanRescheduleRequestRowMapper.schema() + " where lr.id = ?";

            return this.jdbcTemplate.queryForObject(sql, this.loanRescheduleRequestRowMapper, new Object[] { requestId });
        }

        catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<LoanRescheduleRequestData> readLoanRescheduleRequests(Long loanId, Integer statusEnum) {
        this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        final String sql = "select " + this.loanRescheduleRequestRowMapper.schema() + " where lr.loan_id = ?" + " and lr.status_enum = ?";
        return this.jdbcTemplate.query(sql, this.loanRescheduleRequestRowMapper, new Object[] { loanId, statusEnum });
    }

    @Override
    public LoanRescheduleRequestData retrieveAllRescheduleReasons(String loanRescheduleReason) {
        final List<CodeValueData> rescheduleReasons = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(loanRescheduleReason));
        final Long id = null;
        final Long loanId = null;
        final LoanRescheduleRequestStatusEnumData statusEnum = null;
        final Integer rescheduleFromInstallment = null;
        final LocalDate rescheduleFromDate = null;
        final CodeValueData rescheduleReasonCodeValue = null;
        final String rescheduleReasonComment = null;
        final LoanRescheduleRequestTimelineData timeline = null;
        final String clientName = null;
        final String loanAccountNumber = null;
        final Long clientId = null;
        final Boolean recalculateInterest = null;
        final Collection<LoanTermVariationsData> loanTermVariationsData = null;

        return LoanRescheduleRequestData.instance(id, loanId, statusEnum, rescheduleFromInstallment, rescheduleFromDate,
                rescheduleReasonCodeValue, rescheduleReasonComment, timeline, clientName, loanAccountNumber, clientId, recalculateInterest,
                rescheduleReasons, loanTermVariationsData);
    }

    @Override
    public List<LoanRescheduleRequestData> retrieveAllRescheduleRequests(String command) {
        LoanRescheduleRequestRowMapperForBulkApproval loanRescheduleRequestRowMapperForBulkApproval = new LoanRescheduleRequestRowMapperForBulkApproval();
        String sql = "select " + loanRescheduleRequestRowMapperForBulkApproval.schema();
        if (!StringUtils.isNullOrEmpty(command) && !command.equalsIgnoreCase(RescheduleLoansApiConstants.allCommandParamName)) {
            sql = sql + " where lrr.status_enum = ? ";
            Integer statusParam = 100;
            if (command.equalsIgnoreCase(RescheduleLoansApiConstants.approveCommandParamName)) {
                statusParam = 200;
            } else if (command.equalsIgnoreCase(RescheduleLoansApiConstants.rejectCommandParamName)) {
                statusParam = 300;
            }
            return this.jdbcTemplate.query(sql, loanRescheduleRequestRowMapperForBulkApproval, new Object[] { statusParam });
        }
        return this.jdbcTemplate.query(sql, loanRescheduleRequestRowMapperForBulkApproval, new Object[] {});
    }
}
