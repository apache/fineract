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
            + " oc.nameCode, lc.amount, lc.amountPaid, lc.penaltyCharge, lc.chargePaymentMode, lc.paid, l.id, lc.externalId, l.externalId) from WorkingCapitalLoanCharge lc join fetch lc.charge c join OrganisationCurrency oc on c.currencyCode = oc.code join fetch lc.loan l where l.id = :loanId and lc.id = :id")
    WorkingCapitalLoanChargeData retrieveLoanChargeDetails(@Param("id") Long id, @Param("loanId") Long loanId);

    @Query("select new org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargeData("
            + "lc.id, c.id, c.name, lc.chargeTimeType, lc.submittedOnDate, lc.dueDate, lc.chargeCalculationType, oc.code, oc.name, oc.decimalPlaces, oc.inMultiplesOf, oc.displaySymbol,"
            + " oc.nameCode, lc.amount, lc.amountPaid, lc.penaltyCharge, lc.chargePaymentMode, lc.paid, l.id, lc.externalId, l.externalId) from WorkingCapitalLoanCharge lc join fetch lc.charge c join OrganisationCurrency oc on c.currencyCode = oc.code join fetch lc.loan l where l.id = :loanId and lc.active = true order by lc.chargeTimeType asc, lc.dueDate asc, lc.penaltyCharge asc")
    List<WorkingCapitalLoanChargeData> retrieveLoanCharges(@Param("loanId") Long loanId);

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
