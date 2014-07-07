/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestData;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestEnumerations;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestStatusEnumData;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.data.LoanRescheduleRequestTimelineData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class LoanRescheduleRequestReadPlatformServiceImpl implements LoanRescheduleRequestReadPlatformService {
	
	private final JdbcTemplate jdbcTemplate;
	private final LoanRepository loanRepository;
	private final LoanRescheduleRequestRowMapper loanRescheduleRequestRowMapper = new LoanRescheduleRequestRowMapper();
	
	@Autowired
	public LoanRescheduleRequestReadPlatformServiceImpl(final RoutingDataSource dataSource, 
			LoanRepository loanRepository) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.loanRepository = loanRepository;
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
			sqlBuilder.append("lr.grace_on_principal as graceOnPrincipal, ");
			sqlBuilder.append("lr.grace_on_interest as graceOnInterest, ");
			sqlBuilder.append("lr.reschedule_from_date as rescheduleFromDate, ");
			sqlBuilder.append("lr.adjusted_due_date as adjustedDueDate, ");
			sqlBuilder.append("lr.extra_terms as extraTerms, ");
			sqlBuilder.append("lr.recalculate_interest as recalculateInterest, ");
			sqlBuilder.append("lr.interest_rate as interestRate, ");
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
			sqlBuilder.append("rbu.lastname as rejectedByLastname ");
			
			sqlBuilder.append("from " + loanRescheduleRequestTableName() + " lr ");
			sqlBuilder.append("left join m_code_value cv on cv.id = lr.reschedule_reason_cv_id ");
			sqlBuilder.append("left join m_appuser sbu on sbu.id = lr.submitted_by_user_id ");
			sqlBuilder.append("left join m_appuser abu on abu.id = lr.approved_by_user_id ");
			sqlBuilder.append("left join m_appuser rbu on rbu.id = lr.rejected_by_user_id ");
			sqlBuilder.append("left join m_loan ml on ml.id = lr.loan_id ");
			sqlBuilder.append("left join m_client mc on mc.id = ml.client_id ");
			
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
            final LoanRescheduleRequestStatusEnumData statusEnum = LoanRescheduleRequestEnumerations
            		.status(statusEnumId);
            final String clientName = rs.getString("clientName");
            final String loanAccountNumber = rs.getString("loanAccountNumber");
            final Long clientId = rs.getLong("clientId");
            final Integer rescheduleFromInstallment = JdbcSupport.getInteger(rs, "rescheduleFromInstallment");
            final Integer graceOnPrincipal = JdbcSupport.getInteger(rs, "graceOnPrincipal");
            final Integer graceOnInterest = JdbcSupport.getInteger(rs, "graceOnInterest");
            final LocalDate rescheduleFromDate = JdbcSupport.getLocalDate(rs, "rescheduleFromDate");
            final LocalDate adjustedDueDate = JdbcSupport.getLocalDate(rs, "adjustedDueDate");
            final Integer extraTerms = JdbcSupport.getInteger(rs, "extraTerms");
            final BigDecimal interestRate = rs.getBigDecimal("interestRate");
            final Long rescheduleReasonCvId = JdbcSupport.getLong(rs, "rescheduleReasonCvId");
            final String rescheduleReasonCvValue = rs.getString("rescheduleReasonCvValue");
            final CodeValueData rescheduleReasonCodeValue = CodeValueData
            		.instance(rescheduleReasonCvId, rescheduleReasonCvValue);
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
            
            final LoanRescheduleRequestTimelineData timeline = new LoanRescheduleRequestTimelineData(submittedOnDate, 
            		submittedByUsername, submittedByFirstname, submittedByLastname, approvedOnDate, approvedByUsername, 
            		approvedByFirstname, approvedByLastname, rejectedOnDate, rejectedByUsername, rejectedByFirstname, 
            		rejectedByLastname);

			return LoanRescheduleRequestData.instance(id, loanId, statusEnum, rescheduleFromInstallment, graceOnPrincipal, 
					graceOnInterest, rescheduleFromDate, adjustedDueDate, extraTerms, interestRate, rescheduleReasonCodeValue, 
					rescheduleReasonComment, timeline, clientName, loanAccountNumber, clientId, recalculateInterest);
		}
		
	}

	@Override
	public List<LoanRescheduleRequestData> readLoanRescheduleRequests(Long loanId) {
		final Loan loan = this.loanRepository.findOne(loanId);
        
		if(loan == null) { 
        	throw new LoanNotFoundException(loanId); 
        }
		
		final String sql = "select " + this.loanRescheduleRequestRowMapper.schema() + " where lr.loan_id = ?";
		
		return this.jdbcTemplate.query(sql, this.loanRescheduleRequestRowMapper, new Object[] { loanId });
	}

	@Override
	public LoanRescheduleRequestData readLoanRescheduleRequest(Long requestId) {
		
		try {
			final String sql = "select " + this.loanRescheduleRequestRowMapper.schema() + " where lr.id = ?";
			
			return this.jdbcTemplate.queryForObject(sql, this.loanRescheduleRequestRowMapper, new Object[] { requestId });
		}
		
		catch(final EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<LoanRescheduleRequestData> readLoanRescheduleRequests(Long loanId, Integer statusEnum) {
		final Loan loan = this.loanRepository.findOne(loanId);
        
		if(loan == null) { 
        	throw new LoanNotFoundException(loanId); 
        }
		
		final String sql = "select " + this.loanRescheduleRequestRowMapper.schema() + " where lr.loan_id = ?" 
				+ " and lr.status_enum = ?";
		
		return this.jdbcTemplate.query(sql, this.loanRescheduleRequestRowMapper, new Object[] { loanId, statusEnum });
	}
}
