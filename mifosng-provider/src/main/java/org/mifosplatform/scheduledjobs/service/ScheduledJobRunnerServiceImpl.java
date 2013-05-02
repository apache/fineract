package org.mifosplatform.scheduledjobs.service;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.service.DataSourcePerTenantService;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.security.service.TenantDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service(value = "scheduledJobRunnerService")
public class ScheduledJobRunnerServiceImpl implements ScheduledJobRunnerService {

    private final static Logger logger = LoggerFactory.getLogger(ScheduledJobRunnerServiceImpl.class);

    private final TenantDetailsService tenantDetailsService;
    private final DataSourcePerTenantService dataSourcePerTenantService;

    @Autowired
    public ScheduledJobRunnerServiceImpl(final DataSourcePerTenantService dataSourcePerTenantService,
            final TenantDetailsService tenantDetailsService) {
        this.dataSourcePerTenantService = dataSourcePerTenantService;
        this.tenantDetailsService = tenantDetailsService;
    }

    @Transactional
    @Override
    public void updateLoanSummaryDetails() {
        
        final MifosPlatformTenant tenant = this.tenantDetailsService.loadTenantById("default");

        ThreadLocalContextUtil.setTenant(tenant);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourcePerTenantService.retrieveTenantAwareDataSource());

        StringBuilder updateSqlBuilder = new StringBuilder(900);
        updateSqlBuilder.append("update m_loan ");
        updateSqlBuilder.append("join (");
        updateSqlBuilder.append("SELECT ml.id AS loanId,");
        updateSqlBuilder.append("SUM(mr.principal_amount) as principal_disbursed_derived, ");
        updateSqlBuilder.append("SUM(IFNULL(mr.principal_completed_derived,0)) as principal_repaid_derived, ");
        updateSqlBuilder.append("SUM(IFNULL(mr.principal_writtenoff_derived,0)) as principal_writtenoff_derived,");
        updateSqlBuilder.append("SUM(IFNULL(mr.interest_amount,0)) as interest_charged_derived,");
        updateSqlBuilder.append("SUM(IFNULL(mr.interest_completed_derived,0)) as interest_repaid_derived,");
        updateSqlBuilder.append("SUM(IFNULL(mr.interest_waived_derived,0)) as interest_waived_derived,");
        updateSqlBuilder.append("SUM(IFNULL(mr.interest_writtenoff_derived,0)) as interest_writtenoff_derived,");
        updateSqlBuilder.append("SUM(IFNULL(mr.fee_charges_amount,0)) as fee_charges_charged_derived,");
        updateSqlBuilder.append("SUM(IFNULL(mr.fee_charges_completed_derived,0)) as fee_charges_repaid_derived,");
        updateSqlBuilder.append("SUM(IFNULL(mr.fee_charges_waived_derived,0)) as fee_charges_waived_derived,");
        updateSqlBuilder.append("SUM(IFNULL(mr.fee_charges_writtenoff_derived,0)) as fee_charges_writtenoff_derived,");
        updateSqlBuilder.append("SUM(IFNULL(mr.penalty_charges_amount,0)) as penalty_charges_charged_derived,");
        updateSqlBuilder.append("SUM(IFNULL(mr.penalty_charges_completed_derived,0)) as penalty_charges_repaid_derived,");
        updateSqlBuilder.append("SUM(IFNULL(mr.penalty_charges_waived_derived,0)) as penalty_charges_waived_derived,");
        updateSqlBuilder.append("SUM(IFNULL(mr.penalty_charges_writtenoff_derived,0)) as penalty_charges_writtenoff_derived ");
        updateSqlBuilder.append(" FROM m_loan ml ");
        updateSqlBuilder.append("INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id ");
        updateSqlBuilder.append("WHERE ml.disbursedon_date is not null ");
        updateSqlBuilder.append("GROUP BY ml.id ");
        updateSqlBuilder.append(") x on x.loanId = m_loan.id ");
        
        updateSqlBuilder.append("SET m_loan.principal_disbursed_derived = x.principal_disbursed_derived,");
        updateSqlBuilder.append("m_loan.principal_repaid_derived = x.principal_repaid_derived,");
        updateSqlBuilder.append("m_loan.principal_writtenoff_derived = x.principal_writtenoff_derived,");
        updateSqlBuilder.append("m_loan.principal_outstanding_derived = (x.principal_disbursed_derived - (x.principal_repaid_derived + x.principal_writtenoff_derived)),");
        updateSqlBuilder.append("m_loan.interest_charged_derived = x.interest_charged_derived,");
        updateSqlBuilder.append("m_loan.interest_repaid_derived = x.interest_repaid_derived,");
        updateSqlBuilder.append("m_loan.interest_waived_derived = x.interest_waived_derived,");
        updateSqlBuilder.append("m_loan.interest_writtenoff_derived = x.interest_writtenoff_derived,");
        updateSqlBuilder.append("m_loan.interest_outstanding_derived = (x.interest_charged_derived - (x.interest_repaid_derived + x.interest_waived_derived + x.interest_writtenoff_derived)),");
        updateSqlBuilder.append("m_loan.fee_charges_charged_derived = x.fee_charges_charged_derived,");
        updateSqlBuilder.append("m_loan.fee_charges_repaid_derived = x.fee_charges_repaid_derived,");
        updateSqlBuilder.append("m_loan.fee_charges_waived_derived = x.fee_charges_waived_derived,");
        updateSqlBuilder.append("m_loan.fee_charges_writtenoff_derived = x.fee_charges_writtenoff_derived,");
        updateSqlBuilder.append("m_loan.fee_charges_outstanding_derived = (x.fee_charges_charged_derived - (x.fee_charges_repaid_derived + x.fee_charges_waived_derived + x.fee_charges_writtenoff_derived)),");
        updateSqlBuilder.append("m_loan.penalty_charges_charged_derived = x.penalty_charges_charged_derived,");
        updateSqlBuilder.append("m_loan.penalty_charges_repaid_derived = x.penalty_charges_repaid_derived,");
        updateSqlBuilder.append("m_loan.penalty_charges_waived_derived = x.penalty_charges_waived_derived,");
        updateSqlBuilder.append("m_loan.penalty_charges_writtenoff_derived = x.penalty_charges_writtenoff_derived,");
        updateSqlBuilder.append("m_loan.penalty_charges_outstanding_derived = (x.penalty_charges_charged_derived - (x.penalty_charges_repaid_derived + x.penalty_charges_waived_derived + x.penalty_charges_writtenoff_derived)),");
        updateSqlBuilder.append("m_loan.total_expected_repayment_derived = (x.principal_disbursed_derived + x.interest_charged_derived + x.fee_charges_charged_derived + x.penalty_charges_charged_derived),");
        updateSqlBuilder.append("m_loan.total_repayment_derived = (x.principal_repaid_derived + x.interest_repaid_derived + x.fee_charges_repaid_derived + x.penalty_charges_repaid_derived),");
        updateSqlBuilder.append("m_loan.total_expected_costofloan_derived = (x.interest_charged_derived + x.fee_charges_charged_derived + x.penalty_charges_charged_derived),");
        updateSqlBuilder.append("m_loan.total_costofloan_derived = (x.interest_repaid_derived + x.fee_charges_repaid_derived + x.penalty_charges_repaid_derived),");
        updateSqlBuilder.append("m_loan.total_waived_derived = (x.interest_waived_derived + x.fee_charges_waived_derived + x.penalty_charges_waived_derived),");
        updateSqlBuilder.append("m_loan.total_writtenoff_derived = (x.interest_writtenoff_derived +  x.fee_charges_writtenoff_derived + x.penalty_charges_writtenoff_derived),");
        updateSqlBuilder.append("m_loan.total_outstanding_derived=");
        updateSqlBuilder.append(" (x.principal_disbursed_derived - (x.principal_repaid_derived + x.principal_writtenoff_derived)) + ");
        updateSqlBuilder.append(" (x.interest_charged_derived - (x.interest_repaid_derived + x.interest_waived_derived + x.interest_writtenoff_derived)) +");
        updateSqlBuilder.append(" (x.fee_charges_charged_derived - (x.fee_charges_repaid_derived + x.fee_charges_waived_derived + x.fee_charges_writtenoff_derived)) +");
        updateSqlBuilder.append(" (x.penalty_charges_charged_derived - (x.penalty_charges_repaid_derived + x.penalty_charges_waived_derived + x.penalty_charges_writtenoff_derived))");
        
        int result = jdbcTemplate.update(updateSqlBuilder.toString());

        logger.info("Results affected by update: " + result);
    }

    @Override
    public void updateLoanArrearsAgeingDetails() {
        final MifosPlatformTenant tenant = this.tenantDetailsService.loadTenantById("default");

        ThreadLocalContextUtil.setTenant(tenant);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourcePerTenantService.retrieveTenantAwareDataSource());

        jdbcTemplate.execute("truncate table m_loan_arrears_aging");

        
        StringBuilder updateSqlBuilder = new StringBuilder(900);
        
        updateSqlBuilder.append("INSERT INTO m_loan_arrears_aging(`loan_id`,`principal_overdue_derived`,`interest_overdue_derived`,`fee_charges_overdue_derived`,`penalty_charges_overdue_derived`,`total_overdue_derived`,`overdue_since_date_derived`)");
        updateSqlBuilder.append("select ml.id as loanId,");
        updateSqlBuilder.append("SUM((ifnull(mr.principal_amount,0) - ifnull(mr.principal_completed_derived, 0))) as principal_overdue_derived,");
        updateSqlBuilder.append("SUM((ifnull(mr.interest_amount,0)  - ifnull(mr.interest_completed_derived, 0))) as interest_overdue_derived,");
        updateSqlBuilder.append("SUM((ifnull(mr.fee_charges_amount,0)  - ifnull(mr.fee_charges_completed_derived, 0))) as fee_charges_overdue_derived,");
        updateSqlBuilder.append("SUM((ifnull(mr.penalty_charges_amount,0)  - ifnull(mr.penalty_charges_completed_derived, 0))) as penalty_charges_overdue_derived,");
        updateSqlBuilder.append("SUM((ifnull(mr.principal_amount,0) - ifnull(mr.principal_completed_derived, 0))) +");
        updateSqlBuilder.append("SUM((ifnull(mr.interest_amount,0)  - ifnull(mr.interest_completed_derived, 0))) +");
        updateSqlBuilder.append("SUM((ifnull(mr.fee_charges_amount,0)  - ifnull(mr.fee_charges_completed_derived, 0))) +");
        updateSqlBuilder.append("SUM((ifnull(mr.penalty_charges_amount,0)  - ifnull(mr.penalty_charges_completed_derived, 0))) as total_overdue_derived,");
        updateSqlBuilder.append("MIN(mr.duedate) as overdue_since_date_derived ");
        updateSqlBuilder.append(" FROM m_loan ml ");
        updateSqlBuilder.append(" INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id ");
        updateSqlBuilder.append(" WHERE ml.loan_status_id = 300 "); // active
        updateSqlBuilder.append(" and mr.completed_derived is false ");
        updateSqlBuilder.append(" and mr.duedate < CURDATE() ");
        updateSqlBuilder.append(" GROUP BY ml.id");
        
        int result = jdbcTemplate.update(updateSqlBuilder.toString());

        logger.info("Results affected by update: " + result);
    }
}