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
package org.apache.fineract.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoanScheduleHistoryReadPlatformServiceImpl implements LoanScheduleHistoryReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public LoanScheduleHistoryReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate, final PlatformSecurityContext context) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Integer fetchCurrentVersionNumber(Long loanId) {
        final String sql = "select MAX(lrs.version) from m_loan_repayment_schedule_history lrs where lrs.loan_id = ?";
        Integer max = this.jdbcTemplate.queryForObject(sql, new Object[] { loanId }, Integer.class);
        return ObjectUtils.defaultIfNull(max, 0);
    }

    @Override
    public LoanScheduleData retrieveRepaymentArchiveSchedule(final Long loanId,
            final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData, Collection<DisbursementData> disbursementData,
            LoanScheduleType loanScheduleType) {

        try {
            this.context.authenticatedUser();
            Integer versionNumber = fetchCurrentVersionNumber(loanId);
            if (versionNumber == 0) {
                return null;
            }
            final LoanScheduleArchiveResultSetExtractor fullResultsetExtractor = new LoanScheduleArchiveResultSetExtractor(
                    repaymentScheduleRelatedLoanData, disbursementData, loanScheduleType);
            final String sql = "select " + fullResultsetExtractor.schema()
                    + " where ls.loan_id = ? and ls.version = ? order by ls.loan_id, ls.installment";

            return this.jdbcTemplate.query(sql, fullResultsetExtractor, loanId, versionNumber); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanNotFoundException(loanId, e);
        }
    }

    @Override
    public Map<String, Object> fetchOldAuditDates(Long id) {
        final String sql = "select lrs.created_date, lrs.lastmodified_date from m_loan_repayment_schedule lrs where lrs.id = ?";
        return this.jdbcTemplate.queryForMap(sql, id);
    }

    private static final class LoanScheduleArchiveResultSetExtractor implements ResultSetExtractor<LoanScheduleData> {

        private final CurrencyData currency;
        private final DisbursementData disbursement;
        private final BigDecimal totalFeeChargesDueAtDisbursement;
        private final Collection<DisbursementData> disbursementData;
        private final LoanScheduleType loanScheduleType;
        private LocalDate lastDueDate;
        private BigDecimal outstandingLoanPrincipalBalance;

        LoanScheduleArchiveResultSetExtractor(final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData,
                Collection<DisbursementData> disbursementData, LoanScheduleType loanScheduleType) {
            this.currency = repaymentScheduleRelatedLoanData.getCurrency();
            this.disbursement = repaymentScheduleRelatedLoanData.disbursementData();
            this.totalFeeChargesDueAtDisbursement = repaymentScheduleRelatedLoanData.getTotalFeeChargesAtDisbursement();
            this.lastDueDate = this.disbursement.disbursementDate();
            this.outstandingLoanPrincipalBalance = this.disbursement.getPrincipal();
            this.disbursementData = disbursementData;
            this.loanScheduleType = loanScheduleType;
        }

        public String schema() {
            StringBuilder stringBuilder = new StringBuilder(200);
            stringBuilder.append(" ls.installment as period, ls.fromdate as fromDate, ls.duedate as dueDate, ");
            stringBuilder.append(
                    "ls.principal_amount as principalDue, ls.interest_amount as interestDue, ls.fee_charges_amount as feeChargesDue, ls.penalty_charges_amount as penaltyChargesDue ");
            stringBuilder.append(" from m_loan_repayment_schedule_history ls ");
            return stringBuilder.toString();
        }

        @Override
        public LoanScheduleData extractData(final ResultSet rs) throws SQLException, DataAccessException {

            final LoanSchedulePeriodData disbursementPeriod = LoanSchedulePeriodData.disbursementOnlyPeriod(
                    this.disbursement.disbursementDate(), this.disbursement.getPrincipal(), this.totalFeeChargesDueAtDisbursement,
                    this.disbursement.isDisbursed());

            final Collection<LoanSchedulePeriodData> periods = new ArrayList<>();
            final MonetaryCurrency monCurrency = new MonetaryCurrency(this.currency.getCode(), this.currency.getDecimalPlaces(),
                    this.currency.getInMultiplesOf());
            BigDecimal totalPrincipalDisbursed = BigDecimal.ZERO;
            if (disbursementData == null || disbursementData.isEmpty()) {
                periods.add(disbursementPeriod);
                totalPrincipalDisbursed = Money.of(monCurrency, this.disbursement.getPrincipal()).getAmount();
            } else {
                this.outstandingLoanPrincipalBalance = BigDecimal.ZERO;
            }

            Money totalPrincipalExpected = Money.zero(monCurrency);
            Money totalInterestCharged = Money.zero(monCurrency);
            Money totalFeeChargesCharged = Money.zero(monCurrency);
            Money totalPenaltyChargesCharged = Money.zero(monCurrency);
            Money totalRepaymentExpected = Money.zero(monCurrency);

            // update totals with details of fees charged during disbursement
            totalFeeChargesCharged = totalFeeChargesCharged.plus(disbursementPeriod.getFeeChargesDue());
            totalRepaymentExpected = totalRepaymentExpected.plus(disbursementPeriod.getFeeChargesDue());

            Integer loanTermInDays = 0;
            while (rs.next()) {
                final Integer period = JdbcSupport.getInteger(rs, "period");
                LocalDate fromDate = JdbcSupport.getLocalDate(rs, "fromDate");
                final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "dueDate");
                if (disbursementData != null) {
                    BigDecimal principal = BigDecimal.ZERO;
                    for (DisbursementData data : disbursementData) {
                        if (fromDate.equals(this.disbursement.disbursementDate()) && data.disbursementDate().equals(fromDate)) {
                            principal = principal.add(data.getPrincipal());
                            final LoanSchedulePeriodData periodData = LoanSchedulePeriodData.disbursementOnlyPeriod(data.disbursementDate(),
                                    data.getPrincipal(), this.totalFeeChargesDueAtDisbursement, data.isDisbursed());
                            periods.add(periodData);
                            this.outstandingLoanPrincipalBalance = this.outstandingLoanPrincipalBalance.add(data.getPrincipal());
                        } else if (data.isDueForDisbursement(loanScheduleType, fromDate, dueDate)
                                && this.outstandingLoanPrincipalBalance.compareTo(BigDecimal.ZERO) > 0) {
                            principal = principal.add(data.getPrincipal());
                            final LoanSchedulePeriodData periodData = LoanSchedulePeriodData.disbursementOnlyPeriod(data.disbursementDate(),
                                    data.getPrincipal(), BigDecimal.ZERO, data.isDisbursed());
                            periods.add(periodData);
                            this.outstandingLoanPrincipalBalance = this.outstandingLoanPrincipalBalance.add(data.getPrincipal());
                        }
                    }
                    totalPrincipalDisbursed = totalPrincipalDisbursed.add(principal);
                }

                Integer daysInPeriod = 0;
                if (fromDate != null) {
                    daysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(fromDate, dueDate));
                    loanTermInDays = loanTermInDays + daysInPeriod;
                }

                final BigDecimal principalDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalDue");
                totalPrincipalExpected = totalPrincipalExpected.plus(principalDue);

                final BigDecimal interestExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestDue");
                totalInterestCharged = totalInterestCharged.plus(interestExpectedDue);

                final BigDecimal totalInstallmentAmount = totalPrincipalExpected.zero().plus(principalDue).plus(interestExpectedDue)
                        .getAmount();

                final BigDecimal feeChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesDue");
                totalFeeChargesCharged = totalFeeChargesCharged.plus(feeChargesExpectedDue);

                final BigDecimal penaltyChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesDue");
                totalPenaltyChargesCharged = totalPenaltyChargesCharged.plus(penaltyChargesExpectedDue);

                final BigDecimal totalExpectedCostOfLoanForPeriod = interestExpectedDue.add(feeChargesExpectedDue)
                        .add(penaltyChargesExpectedDue);

                final BigDecimal totalDueForPeriod = principalDue.add(totalExpectedCostOfLoanForPeriod);

                totalRepaymentExpected = totalRepaymentExpected.plus(totalDueForPeriod);

                if (fromDate == null) {
                    fromDate = this.lastDueDate;
                }
                final BigDecimal outstandingPrincipalBalanceOfLoan = this.outstandingLoanPrincipalBalance.subtract(principalDue);

                // update based on current period values
                this.lastDueDate = dueDate;
                this.outstandingLoanPrincipalBalance = this.outstandingLoanPrincipalBalance.subtract(principalDue);

                final LoanSchedulePeriodData periodData = LoanSchedulePeriodData.repaymentOnlyPeriod(period, fromDate, dueDate,
                        principalDue, outstandingPrincipalBalanceOfLoan, interestExpectedDue, feeChargesExpectedDue,
                        penaltyChargesExpectedDue, totalDueForPeriod, totalInstallmentAmount);

                periods.add(periodData);
            }

            return new LoanScheduleData(this.currency, periods, loanTermInDays, totalPrincipalDisbursed, totalPrincipalExpected.getAmount(),
                    totalInterestCharged.getAmount(), totalFeeChargesCharged.getAmount(), totalPenaltyChargesCharged.getAmount(),
                    totalRepaymentExpected.getAmount());
        }

    }

}
