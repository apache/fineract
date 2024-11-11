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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.cob.data.LoanIdAndExternalIdAndAccountNo;
import org.apache.fineract.cob.data.LoanIdAndExternalIdAndStatus;
import org.apache.fineract.cob.data.LoanIdAndLastClosedBusinessDate;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    String FIND_GROUP_LOANS_DISBURSED_AFTER = "select l from Loan l where ( l.actualDisbursementDate IS NOT NULL and l.actualDisbursementDate > :disbursementDate) and "
            + "l.group.id = :groupId and l.loanType = :loanType order by l.actualDisbursementDate";

    String FIND_CLIENT_OR_JLG_LOANS_DISBURSED_AFTER = "select l from Loan l where (l.actualDisbursementDate IS NOT NULL and l.actualDisbursementDate > :disbursementDate) and "
            + "l.client.id = :clientId order by l.actualDisbursementDate";

    String FIND_MAX_GROUP_LOAN_COUNTER_QUERY = "Select MAX(l.loanCounter) from Loan l where l.group.id = :groupId "
            + "and l.loanType = :loanType";

    String FIND_MAX_GROUP_LOAN_PRODUCT_COUNTER_QUERY = "Select MAX(l.loanProductCounter) from Loan l where "
            + "l.group.id = :groupId and l.loanType = :loanType and l.loanProduct.id = :productId";

    String FIND_MAX_CLIENT_OR_JLG_LOAN_COUNTER_QUERY = "Select MAX(l.loanCounter) from Loan l where " + "l.client.id = :clientId";

    String FIND_MAX_CLIENT_OR_JLG_LOAN_PRODUCT_COUNTER_QUERY = "Select MAX(l.loanProductCounter) from Loan l where "
            + "l.client.id = :clientId and l.loanProduct.id = :productId";

    String FIND_GROUP_LOANS_TO_UPDATE = "select l from Loan l where l.loanCounter > :loanCounter and "
            + "l.group.id = :groupId and l.loanType = :groupLoanType order by l.loanCounter";

    String FIND_CLIENT_OR_JLG_LOANS_TO_UPDATE = "select l from Loan l where l.loanCounter > :loanCounter and "
            + "l.client.id = :clientId order by l.loanCounter";

    String FIND_GROUP_LOANS_TO_UPDATE_LOANPRODUCT_COUNTER = "select l from Loan l where l.loanProductCounter > :loanProductCounter"
            + " and l.group.id = :groupId and l.loanType = :groupLoanType and l.loanCounter is NULL order by l.loanProductCounter";

    String FIND_CLIENT_LOANS_TO_UPDATE_LOANPRODUCT_COUNTER = "select l from Loan l where l.loanProductCounter > :loanProductCounter"
            + " and l.client.id = :clientId and l.loanCounter is NULL order by l.loanProductCounter";

    String FIND_ACTIVE_LOANS_PRODUCT_IDS_BY_CLIENT = "Select loan.loanProduct.id from Loan loan where "
            + "loan.client.id = :clientId and loan.loanStatus = :loanStatus group by loan.loanProduct.id";

    String FIND_ACTIVE_LOANS_PRODUCT_IDS_BY_GROUP = "Select loan.loanProduct.id from Loan loan where "
            + "loan.group.id = :groupId and loan.loanStatus = :loanStatus and loan.client.id is NULL group by loan.loanProduct.id";

    String DOES_CLIENT_HAVE_NON_CLOSED_LOANS = "select case when (count (loan) > 0) then 'true' else 'false' end from Loan loan where loan.client.id = :clientId and loan.loanStatus in (100,200,300,303,304,700)";

    String DOES_PRODUCT_HAVE_NON_CLOSED_LOANS = "select case when (count (loan) > 0) then 'true' else 'false' end from Loan loan where loan.loanProduct.id = :productId and loan.loanStatus in (100,200,300,303,304,700)";

    String FIND_NON_CLOSED_BY_ACCOUNT_NUMBER = "select loan from Loan loan where loan.accountNumber = :accountNumber and loan.loanStatus in (100,200,300,303,304)";

    String FIND_ALL_NON_CLOSED = "select loan.id from Loan loan where loan.loanStatus in (100,200,300,303,304)";

    String FIND_NON_CLOSED_LOAN_THAT_BELONGS_TO_CLIENT = "select loan from Loan loan where loan.id = :loanId and loan.loanStatus = 300 and loan.client.id = :clientId";

    String FIND_BY_ACCOUNT_NUMBER = "select loan from Loan loan where loan.accountNumber = :accountNumber";

    String FIND_LOAN_ID_AND_EXTERNAL_ID_AND_STATUS = "select new org.apache.fineract.cob.data.LoanIdAndExternalIdAndStatus(loan.id, loan.externalId, loan.loanStatus) from Loan loan where loan.id = :loanId";
    String EXISTS_NON_CLOSED_BY_EXTERNAL_LOAN_ID = "select case when (count (loan) > 0) then 'true' else 'false' end from Loan loan where loan.externalId = :externalLoanId and loan.loanStatus in (100,200,300,303,304)";

    String FIND_ID_BY_EXTERNAL_ID = "SELECT loan.id FROM Loan loan WHERE loan.externalId = :externalId";

    // should follow the logic of `FIND_ALL_NON_CLOSED_LOANS_BY_LAST_CLOSED_BUSINESS_DATE` query
    String FIND_OLDEST_COB_PROCESSED_LOAN = "select loan.id, loan.lastClosedBusinessDate from Loan loan where loan.loanStatus in (100,200,300,303,304) and loan.lastClosedBusinessDate = (select min(l.lastClosedBusinessDate) from Loan l where l"
            + ".loanStatus in (100,200,300,303,304) and l.lastClosedBusinessDate < :cobBusinessDate)";

    String FIND_ALL_NON_CLOSED_LOANS_BEHIND_OR_NULL_BY_LOAN_IDS = "select loan.id, loan.lastClosedBusinessDate from Loan loan where loan.id IN :loanIds and loan.loanStatus in (100,200,300,303,304) and (loan.lastClosedBusinessDate < :cobBusinessDate or "
            + "loan.lastClosedBusinessDate is null)";

    String FIND_ALL_NON_CLOSED_LOANS_BY_LAST_CLOSED_BUSINESS_DATE_AND_MIN_AND_MAX_LOAN_ID = "select loan.id from Loan loan where loan.id BETWEEN :minLoanId and :maxLoanId and loan.loanStatus in (100,200,300,303,304) and (:cobBusinessDate = loan.lastClosedBusinessDate or loan.lastClosedBusinessDate is NULL)";

    String FIND_ALL_NON_CLOSED_LOANS_BY_LAST_CLOSED_BUSINESS_DATE_NOT_NULL_AND_MIN_AND_MAX_LOAN_ID = "select loan.id from Loan loan where loan.id BETWEEN :minLoanId and :maxLoanId and loan.loanStatus in (100,200,300,303,304) and :cobBusinessDate = loan.lastClosedBusinessDate";
    String FIND_ALL_NON_CLOSED_LOANS_BEHIND_BY_LOAN_IDS = "select loan.id, loan.lastClosedBusinessDate from Loan loan where loan.id IN :loanIds and loan.loanStatus in (100,200,300,303,304) and loan.lastClosedBusinessDate < :cobBusinessDate";

    String FIND_ALL_STAYED_LOCKED_BY_COB_BUSINESS_DATE = "select loan.id, loan.externalId, loan.accountNumber from LoanAccountLock lock left join Loan loan on lock.loanId = loan.id where lock.lockPlacedOnCobBusinessDate = :cobBusinessDate";

    String FIND_ALL_LOAN_IDS_BY_STATUS_ID = "SELECT loan.id FROM Loan loan WHERE loan.loanStatus = :statusId";

    String LOANS_FOR_ACCRUAL = "select l from Loan l left join l.loanInterestRecalculationDetails recalcDetails "
            + "where l.loanStatus = 300 and l.isNpa = false and l.chargedOff = false "
            + "and l.loanProduct.accountingRule = :accountingType "
            + "and (recalcDetails.isCompoundingToBePostedAsTransaction is null or recalcDetails.isCompoundingToBePostedAsTransaction = false) "
            + "and (exists (select ls.id from LoanRepaymentScheduleInstallment ls where ls.loan.id = l.id and ls.isDownPayment = false "
            + "and ((coalesce(ls.interestCharged, 0) - coalesce(ls.interestWaived, 0)) <> coalesce(ls.interestAccrued, 0) "
            + "or (coalesce(ls.feeChargesCharged, 0) - coalesce(ls.feeChargesWaived, 0)) <> coalesce(ls.feeAccrued, 0) "
            + "or (coalesce(ls.penaltyCharges, 0) - coalesce(ls.penaltyChargesWaived, 0)) <> coalesce(ls.penaltyAccrued, 0)) ";
    String FIND_LOANS_FOR_PERIODIC_ACCRUAL = LOANS_FOR_ACCRUAL
            + "and (:futureCharges = true or ls.fromDate < :tillDate or (ls.installmentNumber = (select min(lsi.installmentNumber) from LoanRepaymentScheduleInstallment lsi where lsi.loan.id = l.id and lsi.isDownPayment = false) and ls.fromDate = :tillDate))))";
    String FIND_LOANS_FOR_ADD_ACCRUAL = LOANS_FOR_ACCRUAL + "and (:futureCharges = true or ls.dueDate <= :tillDate)))";

    @Query(FIND_GROUP_LOANS_DISBURSED_AFTER)
    List<Loan> getGroupLoansDisbursedAfter(@Param("disbursementDate") LocalDate disbursementDate, @Param("groupId") Long groupId,
            @Param("loanType") Integer loanType);

    @Query(FIND_CLIENT_OR_JLG_LOANS_DISBURSED_AFTER)
    List<Loan> getClientOrJLGLoansDisbursedAfter(@Param("disbursementDate") LocalDate disbursementDate, @Param("clientId") Long clientId);

    @Query(FIND_MAX_GROUP_LOAN_COUNTER_QUERY)
    Integer getMaxGroupLoanCounter(@Param("groupId") Long groupId, @Param("loanType") Integer loanType);

    @Query(FIND_MAX_GROUP_LOAN_PRODUCT_COUNTER_QUERY)
    Integer getMaxGroupLoanProductCounter(@Param("productId") Long productId, @Param("groupId") Long groupId,
            @Param("loanType") Integer loanType);

    @Query(FIND_MAX_CLIENT_OR_JLG_LOAN_COUNTER_QUERY)
    Integer getMaxClientOrJLGLoanCounter(@Param("clientId") Long clientId);

    @Query(FIND_MAX_CLIENT_OR_JLG_LOAN_PRODUCT_COUNTER_QUERY)
    Integer getMaxClientOrJLGLoanProductCounter(@Param("productId") Long productId, @Param("clientId") Long clientId);

    @Query(FIND_GROUP_LOANS_TO_UPDATE)
    List<Loan> getGroupLoansToUpdateLoanCounter(@Param("loanCounter") Integer loanCounter, @Param("groupId") Long groupId,
            @Param("groupLoanType") Integer groupLoanType);

    @Query(FIND_CLIENT_OR_JLG_LOANS_TO_UPDATE)
    List<Loan> getClientOrJLGLoansToUpdateLoanCounter(@Param("loanCounter") Integer loanCounter, @Param("clientId") Long clientId);

    @Query(FIND_GROUP_LOANS_TO_UPDATE_LOANPRODUCT_COUNTER)
    List<Loan> getGroupLoansToUpdateLoanProductCounter(@Param("loanProductCounter") Integer loanProductCounter,
            @Param("groupId") Long groupId, @Param("groupLoanType") Integer groupLoanType);

    @Query(FIND_CLIENT_LOANS_TO_UPDATE_LOANPRODUCT_COUNTER)
    List<Loan> getClientLoansToUpdateLoanProductCounter(@Param("loanProductCounter") Integer loanProductCounter,
            @Param("clientId") Long clientId);

    @Query("select loan from Loan loan where loan.client.id = :clientId and loan.group.id = :groupId")
    List<Loan> findByClientIdAndGroupId(@Param("clientId") Long clientId, @Param("groupId") Long groupId);

    @Query("select loan from Loan loan where loan.client.id = :clientId and loan.group.id = :groupId and loan.loanStatus IN :loanStatuses")
    List<Loan> findByClientIdAndGroupIdAndLoanStatus(@Param("clientId") Long clientId, @Param("groupId") Long groupId,
            @Param("loanStatuses") Collection<Integer> loanStatuses);

    @Query("select loan from Loan loan where loan.client.id = :clientId")
    List<Loan> findLoanByClientId(@Param("clientId") Long clientId);

    @Query("select loan from Loan loan where loan.group.id = :groupId and loan.client.id is null")
    List<Loan> findByGroupId(@Param("groupId") Long groupId);

    @Query("select loan from Loan loan where loan.glim.id = :glimId")
    List<Loan> findByGlimId(@Param("glimId") Long glimId);

    @Query("select loan from Loan loan where loan.id IN :ids and loan.loanStatus IN :loanStatuses and loan.loanType IN :loanTypes")
    List<Loan> findByIdsAndLoanStatusAndLoanType(@Param("ids") Collection<Long> ids,
            @Param("loanStatuses") Collection<Integer> loanStatuses, @Param("loanTypes") Collection<Integer> loanTypes);

    @Query("select loan.id from Loan loan where loan.actualDisbursementDate > :disbursalDate order by loan.actualDisbursementDate")
    List<Long> getLoansDisbursedAfter(@Param("disbursalDate") LocalDate disbursalDate);

    @Query("select loan from Loan loan where loan.client.office.id IN :officeIds and loan.loanStatus IN :loanStatuses")
    List<Loan> findByClientOfficeIdsAndLoanStatus(@Param("officeIds") Collection<Long> officeIds,
            @Param("loanStatuses") Collection<Integer> loanStatuses);

    @Query("select loan from Loan loan where loan.group.office.id IN :officeIds and loan.loanStatus IN :loanStatuses")
    List<Loan> findByGroupOfficeIdsAndLoanStatus(@Param("officeIds") Collection<Long> officeIds,
            @Param("loanStatuses") Collection<Integer> loanStatuses);

    /*** FIXME: Add more appropriate names for the query ***/
    @Query(FIND_ACTIVE_LOANS_PRODUCT_IDS_BY_CLIENT)
    List<Long> findActiveLoansLoanProductIdsByClient(@Param("clientId") Long clientId, @Param("loanStatus") Integer loanStatus);

    @Query(FIND_ACTIVE_LOANS_PRODUCT_IDS_BY_GROUP)
    List<Long> findActiveLoansLoanProductIdsByGroup(@Param("groupId") Long groupId, @Param("loanStatus") Integer loanStatus);

    @Query(DOES_CLIENT_HAVE_NON_CLOSED_LOANS)
    boolean doNonClosedLoanAccountsExistForClient(@Param("clientId") Long clientId);

    @Query(DOES_PRODUCT_HAVE_NON_CLOSED_LOANS)
    boolean doNonClosedLoanAccountsExistForProduct(@Param("productId") Long productId);

    @Query(FIND_NON_CLOSED_BY_ACCOUNT_NUMBER)
    Loan findNonClosedLoanByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query(FIND_NON_CLOSED_LOAN_THAT_BELONGS_TO_CLIENT)
    Loan findNonClosedLoanThatBelongsToClient(@Param("loanId") Long loanId, @Param("clientId") Long clientId);

    @Query(FIND_BY_ACCOUNT_NUMBER)
    Loan findLoanAccountByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query(FIND_LOAN_ID_AND_EXTERNAL_ID_AND_STATUS)
    Optional<LoanIdAndExternalIdAndStatus> findLoanIdAndExternalIdAndStatusByLoanId(@Param("loanId") Long loanId);

    @Query(EXISTS_NON_CLOSED_BY_EXTERNAL_LOAN_ID)
    boolean existsNonClosedLoanByExternalLoanId(@Param("externalLoanId") ExternalId externalLoanId);

    boolean existsByExternalId(@Param("externalId") ExternalId externalId);

    @Query(FIND_ALL_NON_CLOSED)
    List<Long> findAllNonClosedLoanIds();

    @Query(FIND_ID_BY_EXTERNAL_ID)
    Long findIdByExternalId(@Param("externalId") ExternalId externalId);

    @Query(FIND_ALL_NON_CLOSED_LOANS_BEHIND_BY_LOAN_IDS)
    List<LoanIdAndLastClosedBusinessDate> findAllNonClosedLoansBehindByLoanIds(@Param("cobBusinessDate") LocalDate cobBusinessDate,
            @Param("loanIds") List<Long> loanIds);

    @Query(FIND_ALL_NON_CLOSED_LOANS_BEHIND_OR_NULL_BY_LOAN_IDS)
    List<LoanIdAndLastClosedBusinessDate> findAllNonClosedLoansBehindOrNullByLoanIds(@Param("cobBusinessDate") LocalDate cobBusinessDate,
            @Param("loanIds") List<Long> loanIds);

    @Query(FIND_ALL_NON_CLOSED_LOANS_BY_LAST_CLOSED_BUSINESS_DATE_AND_MIN_AND_MAX_LOAN_ID)
    List<Long> findAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(@Param("minLoanId") Long minLoanId,
            @Param("maxLoanId") Long maxLoanId, @Param("cobBusinessDate") LocalDate cobBusinessDate);

    @Query(FIND_ALL_NON_CLOSED_LOANS_BY_LAST_CLOSED_BUSINESS_DATE_NOT_NULL_AND_MIN_AND_MAX_LOAN_ID)
    List<Long> findAllNonClosedLoansByLastClosedBusinessDateNotNullAndMinAndMaxLoanId(@Param("minLoanId") Long minLoanId,
            @Param("maxLoanId") Long maxLoanId, @Param("cobBusinessDate") LocalDate cobBusinessDate);

    @Query(FIND_OLDEST_COB_PROCESSED_LOAN)
    List<LoanIdAndLastClosedBusinessDate> findOldestCOBProcessedLoan(@Param("cobBusinessDate") LocalDate cobBusinessDate);

    @Query(FIND_ALL_STAYED_LOCKED_BY_COB_BUSINESS_DATE)
    List<LoanIdAndExternalIdAndAccountNo> findAllStayedLockedByCobBusinessDate(@Param("cobBusinessDate") LocalDate cobBusinessDate);

    @Query(FIND_ALL_LOAN_IDS_BY_STATUS_ID)
    List<Long> findLoanIdByStatusId(@Param("statusId") Integer statusId);

    @Query(FIND_LOANS_FOR_PERIODIC_ACCRUAL)
    List<Loan> findLoansForPeriodicAccrual(@Param("accountingType") Integer accountingType, @Param("tillDate") LocalDate tillDate,
            @Param("futureCharges") boolean futureCharges);

    @Query(FIND_LOANS_FOR_ADD_ACCRUAL)
    List<Loan> findLoansForAddAccrual(@Param("accountingType") Integer accountingType, @Param("tillDate") LocalDate tillDate,
            @Param("futureCharges") boolean futureCharges);
}
