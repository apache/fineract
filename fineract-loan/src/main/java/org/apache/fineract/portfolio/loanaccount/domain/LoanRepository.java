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
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.cob.data.COBIdAndExternalIdAndAccountNo;
import org.apache.fineract.cob.data.COBIdAndLastClosedBusinessDate;
import org.apache.fineract.cob.data.LoanDataForExternalTransfer;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
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

    String DOES_CLIENT_HAVE_LOANS_WITH_STATUSES = "select case when (count (loan) > 0) then 'true' else 'false' end from Loan loan where loan.client.id = :clientId and loan.loanStatus in :loanStatuses";

    String DOES_PRODUCT_HAVE_LOANS_WITH_STATUSES = "select case when (count (loan) > 0) then 'true' else 'false' end from Loan loan where loan.loanProduct.id = :productId and loan.loanStatus in :loanStatuses";

    String FIND_LOANS_BY_ACCOUNT_NUMBER_AND_STATUSES = "select loan from Loan loan where loan.accountNumber = :accountNumber and loan.loanStatus in :loanStatuses";

    String FIND_ALL_BY_STATUSES = "select loan.id from Loan loan where loan.loanStatus in :loanStatuses";

    String FIND_LOAN_BY_CLIENT_AND_STATUS = "select loan from Loan loan where loan.id = :loanId and loan.loanStatus = :loanStatus and loan.client.id = :clientId";

    String FIND_BY_ACCOUNT_NUMBER = "select loan from Loan loan where loan.accountNumber = :accountNumber";

    String FIND_LOAN_DATA_FOR_EXTERNAL_TRANSFER = "select new org.apache.fineract.cob.data.LoanDataForExternalTransfer(loan.id, loan.externalId, loan.loanStatus, loan.loanProduct.id, loan.loanProduct.shortName) from Loan loan where loan.id = :loanId";
    String EXISTS_BY_EXTERNAL_LOAN_ID_AND_STATUSES = "select case when (count (loan) > 0) then 'true' else 'false' end from Loan loan where loan.externalId = :externalLoanId and loan.loanStatus in :loanStatuses";

    String FIND_ID_BY_EXTERNAL_ID = "SELECT loan.id FROM Loan loan WHERE loan.externalId = :externalId";

    String FIND_IDS_BY_EXTERNAL_IDS = "SELECT loan.id FROM Loan loan WHERE loan.externalId IN :externalIds ORDER BY loan.id ASC";

    // should follow the logic of `FIND_ALL_LOANS_BY_LAST_CLOSED_BUSINESS_DATE` query
    String FIND_OLDEST_COB_PROCESSED_LOAN = "select loan.id, loan.lastClosedBusinessDate from Loan loan where loan.loanStatus in :loanStatuses and loan.lastClosedBusinessDate = (select min(l.lastClosedBusinessDate) from Loan l where l"
            + ".loanStatus in :loanStatuses and l.lastClosedBusinessDate < :cobBusinessDate)";

    String FIND_ALL_LOANS_BEHIND_OR_NULL_BY_LOAN_IDS_AND_STATUSES = "select loan.id, loan.lastClosedBusinessDate from Loan loan where loan.id IN :loanIds and loan.loanStatus in :loanStatuses and (loan.lastClosedBusinessDate < :cobBusinessDate or "
            + "loan.lastClosedBusinessDate is null)";

    String FIND_ALL_LOANS_BY_LAST_CLOSED_BUSINESS_DATE_AND_MIN_AND_MAX_LOAN_ID_AND_STATUSES = "select loan.id from Loan loan where loan.id BETWEEN :minLoanId and :maxLoanId and loan.loanStatus in :loanStatuses and (:cobBusinessDate = loan.lastClosedBusinessDate or loan.lastClosedBusinessDate is NULL)";

    String FIND_ALL_LOANS_BY_LAST_CLOSED_BUSINESS_DATE_NOT_NULL_AND_MIN_AND_MAX_LOAN_ID_AND_STATUSES = "select loan.id from Loan loan where loan.id BETWEEN :minLoanId and :maxLoanId and loan.loanStatus in :loanStatuses and :cobBusinessDate = loan.lastClosedBusinessDate";
    String FIND_ALL_LOANS_BEHIND_BY_LOAN_IDS_AND_STATUSES = "select loan.id, loan.lastClosedBusinessDate from Loan loan where loan.id IN :loanIds and loan.loanStatus in :loanStatuses and loan.lastClosedBusinessDate < :cobBusinessDate";

    String FIND_ALL_STAYED_LOCKED_BY_COB_BUSINESS_DATE = "select loan.id, loan.externalId, loan.accountNumber from LoanAccountLock lock left join Loan loan on lock.loanId = loan.id where lock.lockPlacedOnCobBusinessDate = :cobBusinessDate";

    String FIND_ALL_LOAN_IDS_BY_STATUS = "SELECT loan.id FROM Loan loan WHERE loan.loanStatus = :loanStatus";

    String LOANS_FOR_ACCRUAL = "select l from Loan l left join l.loanInterestRecalculationDetails recalcDetails "
            + "where l.loanStatus = :loanStatus and l.isNpa = false and l.chargedOff = false "
            + "and l.loanProduct.accountingRule = :accountingType "
            + "and (recalcDetails.isCompoundingToBePostedAsTransaction is null or recalcDetails.isCompoundingToBePostedAsTransaction = false) "
            + "and (exists (select ls.id from LoanRepaymentScheduleInstallment ls where ls.loan.id = l.id and ls.isDownPayment = false "
            + "and ((coalesce(ls.interestCharged, 0) - coalesce(ls.interestWaived, 0)) <> coalesce(ls.interestAccrued, 0) "
            + "or (coalesce(ls.feeChargesCharged, 0) - coalesce(ls.feeChargesWaived, 0)) <> coalesce(ls.feeAccrued, 0) "
            + "or (coalesce(ls.penaltyCharges, 0) - coalesce(ls.penaltyChargesWaived, 0)) <> coalesce(ls.penaltyAccrued, 0)) ";
    String FIND_LOANS_FOR_PERIODIC_ACCRUAL = LOANS_FOR_ACCRUAL
            + "and (:futureCharges = true or ls.fromDate < :tillDate or (ls.installmentNumber = (select min(lsi.installmentNumber) from LoanRepaymentScheduleInstallment lsi where lsi.loan.id = l.id and lsi.isDownPayment = false) and ls.fromDate = :tillDate))))";
    String FIND_LOANS_FOR_ADD_ACCRUAL = LOANS_FOR_ACCRUAL + "and (:futureCharges = true or ls.dueDate <= :tillDate)))";

    String FIND_LOAN_BY_EXTERNAL_ID = "SELECT loan FROM Loan loan WHERE loan.externalId = :externalId";

    @Query(FIND_GROUP_LOANS_DISBURSED_AFTER)
    List<Loan> getGroupLoansDisbursedAfter(@Param("disbursementDate") LocalDate disbursementDate, @Param("groupId") Long groupId,
            @Param("loanType") AccountType loanType);

    @Query(FIND_CLIENT_OR_JLG_LOANS_DISBURSED_AFTER)
    List<Loan> getClientOrJLGLoansDisbursedAfter(@Param("disbursementDate") LocalDate disbursementDate, @Param("clientId") Long clientId);

    @Query(FIND_MAX_GROUP_LOAN_COUNTER_QUERY)
    Integer getMaxGroupLoanCounter(@Param("groupId") Long groupId, @Param("loanType") AccountType loanType);

    @Query(FIND_MAX_GROUP_LOAN_PRODUCT_COUNTER_QUERY)
    Integer getMaxGroupLoanProductCounter(@Param("productId") Long productId, @Param("groupId") Long groupId,
            @Param("loanType") AccountType loanType);

    @Query(FIND_MAX_CLIENT_OR_JLG_LOAN_COUNTER_QUERY)
    Integer getMaxClientOrJLGLoanCounter(@Param("clientId") Long clientId);

    @Query(FIND_MAX_CLIENT_OR_JLG_LOAN_PRODUCT_COUNTER_QUERY)
    Integer getMaxClientOrJLGLoanProductCounter(@Param("productId") Long productId, @Param("clientId") Long clientId);

    @Query(FIND_GROUP_LOANS_TO_UPDATE)
    List<Loan> getGroupLoansToUpdateLoanCounter(@Param("loanCounter") Integer loanCounter, @Param("groupId") Long groupId,
            @Param("groupLoanType") AccountType groupLoanType);

    @Query(FIND_CLIENT_OR_JLG_LOANS_TO_UPDATE)
    List<Loan> getClientOrJLGLoansToUpdateLoanCounter(@Param("loanCounter") Integer loanCounter, @Param("clientId") Long clientId);

    @Query(FIND_GROUP_LOANS_TO_UPDATE_LOANPRODUCT_COUNTER)
    List<Loan> getGroupLoansToUpdateLoanProductCounter(@Param("loanProductCounter") Integer loanProductCounter,
            @Param("groupId") Long groupId, @Param("groupLoanType") AccountType groupLoanType);

    @Query(FIND_CLIENT_LOANS_TO_UPDATE_LOANPRODUCT_COUNTER)
    List<Loan> getClientLoansToUpdateLoanProductCounter(@Param("loanProductCounter") Integer loanProductCounter,
            @Param("clientId") Long clientId);

    @Query("select loan from Loan loan where loan.client.id = :clientId and loan.group.id = :groupId")
    List<Loan> findByClientIdAndGroupId(@Param("clientId") Long clientId, @Param("groupId") Long groupId);

    @Query("select loan from Loan loan where loan.client.id = :clientId and loan.group.id = :groupId and loan.loanStatus IN :loanStatuses")
    List<Loan> findByClientIdAndGroupIdAndLoanStatus(@Param("clientId") Long clientId, @Param("groupId") Long groupId,
            @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query("select loan from Loan loan where loan.client.id = :clientId")
    List<Loan> findLoanByClientId(@Param("clientId") Long clientId);

    @Query("select loan from Loan loan where loan.group.id = :groupId and loan.client.id is null")
    List<Loan> findByGroupId(@Param("groupId") Long groupId);

    @Query("select loan from Loan loan where loan.glim.id = :glimId")
    List<Loan> findByGlimId(@Param("glimId") Long glimId);

    @Query("select loan from Loan loan where loan.id IN :ids and loan.loanStatus IN :loanStatuses and loan.loanType IN :loanTypes")
    List<Loan> findByIdsAndLoanStatusAndLoanType(@Param("ids") Collection<Long> ids,
            @Param("loanStatuses") Collection<LoanStatus> loanStatuses, @Param("loanTypes") Collection<AccountType> loanTypes);

    @Query("select loan.id from Loan loan where loan.actualDisbursementDate > :disbursalDate order by loan.actualDisbursementDate")
    List<Long> getLoansDisbursedAfter(@Param("disbursalDate") LocalDate disbursalDate);

    @Query("select loan from Loan loan where loan.client.office.id IN :officeIds and loan.loanStatus IN :loanStatuses")
    List<Loan> findByClientOfficeIdsAndLoanStatus(@Param("officeIds") Collection<Long> officeIds,
            @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query("select loan from Loan loan where loan.group.office.id IN :officeIds and loan.loanStatus IN :loanStatuses")
    List<Loan> findByGroupOfficeIdsAndLoanStatus(@Param("officeIds") Collection<Long> officeIds,
            @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    /*** FIXME: Add more appropriate names for the query ***/
    @Query(FIND_ACTIVE_LOANS_PRODUCT_IDS_BY_CLIENT)
    List<Long> findActiveLoansLoanProductIdsByClient(@Param("clientId") Long clientId, @Param("loanStatus") LoanStatus loanStatus);

    @Query(FIND_ACTIVE_LOANS_PRODUCT_IDS_BY_GROUP)
    List<Long> findActiveLoansLoanProductIdsByGroup(@Param("groupId") Long groupId, @Param("loanStatus") LoanStatus loanStatus);

    @Query(DOES_CLIENT_HAVE_LOANS_WITH_STATUSES)
    boolean doLoanAccountsWithLoansInStatusesExistForClient(@Param("clientId") Long clientId,
            @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query(DOES_PRODUCT_HAVE_LOANS_WITH_STATUSES)
    boolean doLoanAccountsWithLoansInStatusesExistForProduct(@Param("productId") Long productId,
            @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query(FIND_LOANS_BY_ACCOUNT_NUMBER_AND_STATUSES)
    Loan findLoanByAccountNumberAndStatuses(@Param("accountNumber") String accountNumber,
            @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query(FIND_LOAN_BY_CLIENT_AND_STATUS)
    Loan findLoanByClientAndStatus(@Param("loanId") Long loanId, @Param("clientId") Long clientId,
            @Param("loanStatus") LoanStatus loanStatus);

    @Query(FIND_BY_ACCOUNT_NUMBER)
    Loan findLoanAccountByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query(FIND_LOAN_DATA_FOR_EXTERNAL_TRANSFER)
    Optional<LoanDataForExternalTransfer> findLoanDataForExternalTransferByLoanId(@Param("loanId") Long loanId);

    @Query(EXISTS_BY_EXTERNAL_LOAN_ID_AND_STATUSES)
    boolean existsLoanByExternalLoanIdAndStatuses(@Param("externalLoanId") ExternalId externalLoanId,
            @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    boolean existsByExternalId(@Param("externalId") ExternalId externalId);

    @Query(FIND_ALL_BY_STATUSES)
    List<Long> findAllLoanIdsByStatuses(@Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query(FIND_ID_BY_EXTERNAL_ID)
    Long findIdByExternalId(@Param("externalId") ExternalId externalId);

    @Query(FIND_IDS_BY_EXTERNAL_IDS)
    List<Long> findIdsByExternalIds(@Param("externalIds") List<ExternalId> externalIds);

    @Query(FIND_ALL_LOANS_BEHIND_BY_LOAN_IDS_AND_STATUSES)
    List<COBIdAndLastClosedBusinessDate> findAllLoansBehindByLoanIdsAndStatuses(@Param("cobBusinessDate") LocalDate cobBusinessDate,
            @Param("loanIds") List<Long> loanIds, @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query(FIND_ALL_LOANS_BEHIND_OR_NULL_BY_LOAN_IDS_AND_STATUSES)
    List<COBIdAndLastClosedBusinessDate> findAllLoansBehindOrNullByLoanIdsAndStatuses(@Param("cobBusinessDate") LocalDate cobBusinessDate,
            @Param("loanIds") List<Long> loanIds, @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query(FIND_ALL_LOANS_BY_LAST_CLOSED_BUSINESS_DATE_AND_MIN_AND_MAX_LOAN_ID_AND_STATUSES)
    List<Long> findAllLoansByLastClosedBusinessDateAndMinAndMaxLoanIdAndStatuses(@Param("minLoanId") Long minLoanId,
            @Param("maxLoanId") Long maxLoanId, @Param("cobBusinessDate") LocalDate cobBusinessDate,
            @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query(FIND_ALL_LOANS_BY_LAST_CLOSED_BUSINESS_DATE_NOT_NULL_AND_MIN_AND_MAX_LOAN_ID_AND_STATUSES)
    List<Long> findAllLoansByLastClosedBusinessDateNotNullAndMinAndMaxLoanIdAndStatuses(@Param("minLoanId") Long minLoanId,
            @Param("maxLoanId") Long maxLoanId, @Param("cobBusinessDate") LocalDate cobBusinessDate,
            @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query(FIND_OLDEST_COB_PROCESSED_LOAN)
    List<COBIdAndLastClosedBusinessDate> findOldestCOBProcessedLoan(@Param("cobBusinessDate") LocalDate cobBusinessDate,
            @Param("loanStatuses") Collection<LoanStatus> loanStatuses);

    @Query(FIND_ALL_STAYED_LOCKED_BY_COB_BUSINESS_DATE)
    List<COBIdAndExternalIdAndAccountNo> findAllStayedLockedByCobBusinessDate(@Param("cobBusinessDate") LocalDate cobBusinessDate);

    @Query(FIND_ALL_LOAN_IDS_BY_STATUS)
    List<Long> findLoanIdByStatus(@Param("loanStatus") LoanStatus loanStatus);

    @Query(FIND_LOANS_FOR_PERIODIC_ACCRUAL)
    List<Loan> findLoansForPeriodicAccrual(@Param("accountingType") AccountingRuleType accountingType,
            @Param("tillDate") LocalDate tillDate, @Param("futureCharges") boolean futureCharges,
            @Param("loanStatus") LoanStatus loanStatus);

    @Query(FIND_LOANS_FOR_ADD_ACCRUAL)
    List<Loan> findLoansForAddAccrual(@Param("accountingType") AccountingRuleType accountingType, @Param("tillDate") LocalDate tillDate,
            @Param("futureCharges") boolean futureCharges, @Param("loanStatus") LoanStatus loanStatus);

    @Query(FIND_LOAN_BY_EXTERNAL_ID)
    Optional<Loan> findByExternalId(@Param("externalId") ExternalId externalId);

    @Query("select loan.loanRepaymentScheduleDetail.enableIncomeCapitalization from Loan loan where loan.id = :loanId")
    Boolean isEnabledCapitalizedIncome(Long loanId);
}
