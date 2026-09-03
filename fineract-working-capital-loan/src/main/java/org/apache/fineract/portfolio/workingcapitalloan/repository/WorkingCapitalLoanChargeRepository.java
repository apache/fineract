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

package org.apache.fineract.portfolio.workingcapitalloan.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargeData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkingCapitalLoanChargeRepository
        extends JpaRepository<WorkingCapitalLoanCharge, Long>, CrudRepository<WorkingCapitalLoanCharge, Long> {

    Long findIdByExternalId(ExternalId externalId);

    List<WorkingCapitalLoanCharge> findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(Long loanId);

    @Query("select new org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargeData("
            + "lc.id, c.id, c.name, lc.chargeTimeType, lc.submittedOnDate, lc.dueDate, lc.chargeCalculationType, oc.code, oc.name, oc.decimalPlaces, oc.inMultiplesOf, oc.displaySymbol,"
            + " oc.nameCode, lc.amount, lc.amountPaid, lc.amountWrittenOff, lc.penaltyCharge, lc.chargePaymentMode, lc.paid, l.id, lc.externalId, l.externalId) from WorkingCapitalLoanCharge lc join fetch lc.charge c join OrganisationCurrency oc on c.currencyCode = oc.code join fetch lc.loan l where l.id = :loanId and lc.id = :id")
    WorkingCapitalLoanChargeData retrieveLoanChargeDetails(@Param("id") Long id, @Param("loanId") Long loanId);

    @Query("select new org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargeData("
            + "lc.id, c.id, c.name, lc.chargeTimeType, lc.submittedOnDate, lc.dueDate, lc.chargeCalculationType, oc.code, oc.name, oc.decimalPlaces, oc.inMultiplesOf, oc.displaySymbol,"
            + " oc.nameCode, lc.amount, lc.amountPaid, lc.amountWrittenOff, lc.penaltyCharge, lc.chargePaymentMode, lc.paid, l.id, lc.externalId, l.externalId) from WorkingCapitalLoanCharge lc join fetch lc.charge c join OrganisationCurrency oc on c.currencyCode = oc.code join fetch lc.loan l where l.id = :loanId and lc.active = true order by lc.chargeTimeType asc, lc.dueDate asc, lc.penaltyCharge asc")
    List<WorkingCapitalLoanChargeData> retrieveLoanCharges(@Param("loanId") Long loanId);

    /**
     * Total charged for active fee or penalty charges that were already known on {@code date}, counting each from
     * whichever of its two dates came first.
     *
     * <p>
     * Ordinarily a charge is added first and falls due later, so this is {@code submittedOnDate}: once a charge is on
     * the account it is a known obligation and can be settled, and the due date is only the deadline after which it
     * becomes overdue. That is why the payoff includes a charge that is not due yet - excluding it would leave the
     * quote unable to close a loan carrying any future-dated charge, and would disagree with the outstanding balance
     * the rest of the module reports.
     *
     * <p>
     * The order can invert: the "due date cannot be in the past" rule guards only specified-due-date charges, so
     * another charge type can carry a due date earlier than the day it was booked, and then the due date is the one
     * that comes first. A charge with no due date falls back to {@code submittedOnDate}; a charge with neither counts
     * as having always existed, the safe direction for a payoff quote.
     */
    @Query("""
            select coalesce(sum(charge.amount), 0)
            from WorkingCapitalLoanCharge charge
            where charge.loan.id = :loanId and charge.active = true
              and charge.penaltyCharge = :penalty
              and (charge.submittedOnDate is null or charge.submittedOnDate <= :date or charge.dueDate <= :date)
            """)
    BigDecimal sumChargedKnownBy(@Param("loanId") Long loanId, @Param("penalty") boolean penalty, @Param("date") LocalDate date);

    /**
     * Whether an active charge is due on or after the given date. Only active charges count: reprocessing itself
     * re-allocates against active charges only, so an inactive one cannot change any allocation.
     */
    @Query("""
            SELECT CASE WHEN COUNT(charge) > 0 THEN TRUE ELSE FALSE END
            FROM WorkingCapitalLoanCharge charge
            WHERE charge.loan.id = :loanId
            AND charge.active = true
            AND charge.dueDate >= :transactionDate
            AND charge.createdDate >= :createdDateTime
            """)
    boolean existsActiveChargeDueOnOrAfter(@Param("loanId") Long loanId, @Param("transactionDate") LocalDate transactionDate,
            @Param("createdDateTime") OffsetDateTime createdDateTime);

}
