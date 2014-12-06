/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.mifosplatform.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

@Service
public class LoanScheduleHistoryReadPlatformServiceImpl implements LoanScheduleHistoryReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public LoanScheduleHistoryReadPlatformServiceImpl(final RoutingDataSource dataSource, final PlatformSecurityContext context) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Integer fetchCurrentVersionNumber(Long loanId) {
        final String sql = "select MAX(lrs.version) from m_loan_repayment_schedule_history lrs where lrs.loan_id = ?";
        return this.jdbcTemplate.queryForInt(sql, loanId);
    }

    @Override
    public LoanScheduleData retrieveRepaymentArchiveSchedule(final Long loanId,
            final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData, Collection<DisbursementData> disbursementData) {

        try {
            this.context.authenticatedUser();
            Integer versionNumber = fetchCurrentVersionNumber(loanId);
            if (versionNumber == 0) { return null; }
            final LoanScheduleArchiveResultSetExtractor fullResultsetExtractor = new LoanScheduleArchiveResultSetExtractor(
                    repaymentScheduleRelatedLoanData, disbursementData);
            final String sql = "select " + fullResultsetExtractor.schema()
                    + " where ls.loan_id = ? and ls.version = ? order by ls.loan_id, ls.installment";

            return this.jdbcTemplate.query(sql, fullResultsetExtractor, new Object[] { loanId, versionNumber });
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanNotFoundException(loanId);
        }
    }

    private static final class LoanScheduleArchiveResultSetExtractor implements ResultSetExtractor<LoanScheduleData> {

        private final CurrencyData currency;
        private final DisbursementData disbursement;
        private final BigDecimal totalFeeChargesDueAtDisbursement;
        private final Collection<DisbursementData> disbursementData;
        private LocalDate lastDueDate;
        private BigDecimal outstandingLoanPrincipalBalance;

        public LoanScheduleArchiveResultSetExtractor(final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData,
                Collection<DisbursementData> disbursementData) {
            this.currency = repaymentScheduleRelatedLoanData.getCurrency();
            this.disbursement = repaymentScheduleRelatedLoanData.disbursementData();
            this.totalFeeChargesDueAtDisbursement = repaymentScheduleRelatedLoanData.getTotalFeeChargesAtDisbursement();
            this.lastDueDate = this.disbursement.disbursementDate();
            this.outstandingLoanPrincipalBalance = this.disbursement.amount();
            this.disbursementData = disbursementData;
        }

        public String schema() {
            StringBuilder stringBuilder = new StringBuilder(200);
            stringBuilder.append(" ls.installment as period, ls.fromdate as fromDate, ls.duedate as dueDate, ");
            stringBuilder
                    .append("ls.principal_amount as principalDue, ls.interest_amount as interestDue, ls.fee_charges_amount as feeChargesDue, ls.penalty_charges_amount as penaltyChargesDue ");
            stringBuilder.append(" from m_loan_repayment_schedule_history ls ");
            return stringBuilder.toString();
        }

        @Override
        public LoanScheduleData extractData(final ResultSet rs) throws SQLException, DataAccessException {

            final LoanSchedulePeriodData disbursementPeriod = LoanSchedulePeriodData.disbursementOnlyPeriod(
                    this.disbursement.disbursementDate(), this.disbursement.amount(), this.totalFeeChargesDueAtDisbursement,
                    this.disbursement.isDisbursed());

            final Collection<LoanSchedulePeriodData> periods = new ArrayList<>();
            final MonetaryCurrency monCurrency = new MonetaryCurrency(this.currency.code(), this.currency.decimalPlaces(),
                    this.currency.currencyInMultiplesOf());
            BigDecimal totalPrincipalDisbursed = BigDecimal.ZERO;
            if (disbursementData == null || disbursementData.isEmpty()) {
                periods.add(disbursementPeriod);
                totalPrincipalDisbursed = Money.of(monCurrency, this.disbursement.amount()).getAmount();
            } else {
                this.outstandingLoanPrincipalBalance = BigDecimal.ZERO;
            }

            Money totalPrincipalExpected = Money.zero(monCurrency);
            Money totalInterestCharged = Money.zero(monCurrency);
            Money totalFeeChargesCharged = Money.zero(monCurrency);
            Money totalPenaltyChargesCharged = Money.zero(monCurrency);
            Money totalRepaymentExpected = Money.zero(monCurrency);

            // update totals with details of fees charged during disbursement
            totalFeeChargesCharged = totalFeeChargesCharged.plus(disbursementPeriod.feeChargesDue());
            totalRepaymentExpected = totalRepaymentExpected.plus(disbursementPeriod.feeChargesDue());

            Integer loanTermInDays = Integer.valueOf(0);
            while (rs.next()) {
                final Integer period = JdbcSupport.getInteger(rs, "period");
                LocalDate fromDate = JdbcSupport.getLocalDate(rs, "fromDate");
                final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "dueDate");
                if (disbursementData != null) {
                    BigDecimal principal = BigDecimal.ZERO;
                    for (DisbursementData data : disbursementData) {
                        if (fromDate.equals(this.disbursement.disbursementDate()) && data.disbursementDate().equals(fromDate)) {
                            principal = principal.add(data.amount());
                            final LoanSchedulePeriodData periodData = LoanSchedulePeriodData.disbursementOnlyPeriod(
                                    data.disbursementDate(), data.amount(), this.totalFeeChargesDueAtDisbursement, data.isDisbursed());
                            periods.add(periodData);
                            this.outstandingLoanPrincipalBalance = this.outstandingLoanPrincipalBalance.add(data.amount());
                        } else if (data.isDueForDisbursement(fromDate, dueDate)
                                && this.outstandingLoanPrincipalBalance.compareTo(BigDecimal.ZERO) == 1) {
                            principal = principal.add(data.amount());
                            final LoanSchedulePeriodData periodData = LoanSchedulePeriodData.disbursementOnlyPeriod(
                                    data.disbursementDate(), data.amount(), BigDecimal.ZERO, data.isDisbursed());
                            periods.add(periodData);
                            this.outstandingLoanPrincipalBalance = this.outstandingLoanPrincipalBalance.add(data.amount());
                        }
                    }
                    totalPrincipalDisbursed = totalPrincipalDisbursed.add(principal);
                }

                Integer daysInPeriod = Integer.valueOf(0);
                if (fromDate != null) {
                    daysInPeriod = Days.daysBetween(fromDate, dueDate).getDays();
                    loanTermInDays = Integer.valueOf(loanTermInDays.intValue() + daysInPeriod.intValue());
                }

                final BigDecimal principalDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalDue");
                totalPrincipalExpected = totalPrincipalExpected.plus(principalDue);

                final BigDecimal interestExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestDue");
                totalInterestCharged = totalInterestCharged.plus(interestExpectedDue);

                final BigDecimal feeChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesDue");
                totalFeeChargesCharged = totalFeeChargesCharged.plus(feeChargesExpectedDue);

                final BigDecimal penaltyChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesDue");
                totalPenaltyChargesCharged = totalPenaltyChargesCharged.plus(penaltyChargesExpectedDue);

                final BigDecimal totalExpectedCostOfLoanForPeriod = interestExpectedDue.add(feeChargesExpectedDue).add(
                        penaltyChargesExpectedDue);

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
                        penaltyChargesExpectedDue, totalDueForPeriod);

                periods.add(periodData);
            }

            return new LoanScheduleData(this.currency, periods, loanTermInDays, totalPrincipalDisbursed,
                    totalPrincipalExpected.getAmount(), totalInterestCharged.getAmount(), totalFeeChargesCharged.getAmount(),
                    totalPenaltyChargesCharged.getAmount(), totalRepaymentExpected.getAmount());
        }

    }

}
