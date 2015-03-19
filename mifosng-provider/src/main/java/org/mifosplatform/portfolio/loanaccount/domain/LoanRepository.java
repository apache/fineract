/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    public static final String FIND_GROUP_LOANS_DISBURSED_AFTER = "from Loan l where l.actualDisbursementDate > :disbursementDate and "
            + "l.group.id = :groupId and l.loanType = :loanType order by l.actualDisbursementDate";

    public static final String FIND_CLIENT_OR_JLG_LOANS_DISBURSED_AFTER = "from Loan l where l.actualDisbursementDate > :disbursementDate and "
            + "l.client.id = :clientId order by l.actualDisbursementDate";

    public static final String FIND_MAX_GROUP_LOAN_COUNTER_QUERY = "Select MAX(l.loanCounter) from Loan l where l.group.id = :groupId "
            + "and l.loanType = :loanType";

    public static final String FIND_MAX_GROUP_LOAN_PRODUCT_COUNTER_QUERY = "Select MAX(l.loanProductCounter) from Loan l where "
            + "l.group.id = :groupId and l.loanType = :loanType and l.loanProduct.id = :productId";

    public static final String FIND_MAX_CLIENT_OR_JLG_LOAN_COUNTER_QUERY = "Select MAX(l.loanCounter) from Loan l where "
            + "l.client.id = :clientId";

    public static final String FIND_MAX_CLIENT_OR_JLG_LOAN_PRODUCT_COUNTER_QUERY = "Select MAX(l.loanProductCounter) from Loan l where "
            + "l.client.id = :clientId and l.loanProduct.id = :productId";

    public static final String FIND_GROUP_LOANS_TO_UPDATE = "from Loan l where l.loanCounter > :loanCounter and "
            + "l.group.id = :groupId and l.loanType = :groupLoanType order by l.loanCounter";

    public static final String FIND_CLIENT_OR_JLG_LOANS_TO_UPDATE = "from Loan l where l.loanCounter > :loanCounter and "
            + "l.client.id = :clientId order by l.loanCounter";

    public static final String FIND_GROUP_LOANS_TO_UPDATE_LOANPRODUCT_COUNTER = "from Loan l where l.loanProductCounter > :loanProductCounter"
            + " and l.group.id = :groupId and l.loanType = :groupLoanType and l.loanCounter is NULL order by l.loanProductCounter";

    public static final String FIND_CLIENT_LOANS_TO_UPDATE_LOANPRODUCT_COUNTER = "from Loan l where l.loanProductCounter > :loanProductCounter"
            + " and l.client.id = :clientId and l.loanCounter is NULL order by l.loanProductCounter";

    public static final String FIND_ACTIVE_LOANS_PRODUCT_IDS_BY_CLIENT = "Select loan.loanProduct.id from Loan loan where "
            + "loan.client.id = :clientId and loan.loanStatus = :loanStatus group by loan.loanProduct.id";

    public static final String FIND_ACTIVE_LOANS_PRODUCT_IDS_BY_GROUP = "Select loan.loanProduct.id from Loan loan where "
            + "loan.group.id = :groupId and loan.loanStatus = :loanStatus and loan.client.id is NULL group by loan.loanProduct.id";

    public static final String DOES_CLIENT_HAVE_NON_CLOSED_LOANS = "select case when (count (loan) > 0) then true else false end from Loan loan where loan.client.id = :clientId and loan.loanStatus in (100,200,300,303,304)";

    @Query(FIND_GROUP_LOANS_DISBURSED_AFTER)
    List<Loan> getGroupLoansDisbursedAfter(@Param("disbursementDate") Date disbursementDate, @Param("groupId") Long groupId,
            @Param("loanType") Integer loanType);

    @Query(FIND_CLIENT_OR_JLG_LOANS_DISBURSED_AFTER)
    List<Loan> getClientOrJLGLoansDisbursedAfter(@Param("disbursementDate") Date disbursementDate, @Param("clientId") Long clientId);

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

    @Query("from Loan loan where loan.client.id = :clientId and loan.group.id = :groupId")
    List<Loan> findByClientIdAndGroupId(@Param("clientId") Long clientId, @Param("groupId") Long groupId);

    @Query("from Loan loan where loan.client.id = :clientId and loan.group.id = :groupId and loan.loanStatus IN :loanStatuses")
    List<Loan> findByClientIdAndGroupIdAndLoanStatus(@Param("clientId") Long clientId, @Param("groupId") Long groupId,
            @Param("loanStatuses") Collection<Integer> loanStatuses);

    @Query("from Loan loan where loan.client.id = :clientId")
    List<Loan> findLoanByClientId(@Param("clientId") Long clientId);

    @Query("from Loan loan where loan.group.id = :groupId and loan.client.id is null")
    List<Loan> findByGroupId(@Param("groupId") Long groupId);

    @Query("from Loan loan where loan.id IN :ids and loan.loanStatus IN :loanStatuses and loan.loanType IN :loanTypes")
    List<Loan> findByIdsAndLoanStatusAndLoanType(@Param("ids") Collection<Long> ids,
            @Param("loanStatuses") Collection<Integer> loanStatuses, @Param("loanTypes") Collection<Integer> loanTypes);

    @Query("select loan.id from Loan loan where loan.actualDisbursementDate > :disbursalDate order by loan.actualDisbursementDate")
    List<Long> getLoansDisbursedAfter(@Param("disbursalDate") Date disbursalDate);

    @Query("from Loan loan where loan.client.office.id IN :officeIds and loan.loanStatus IN :loanStatuses")
    List<Loan> findByClientOfficeIdsAndLoanStatus(@Param("officeIds") Collection<Long> officeIds,
            @Param("loanStatuses") Collection<Integer> loanStatuses);

    @Query("from Loan loan where loan.group.office.id IN :officeIds and loan.loanStatus IN :loanStatuses")
    List<Loan> findByGroupOfficeIdsAndLoanStatus(@Param("officeIds") Collection<Long> officeIds,
            @Param("loanStatuses") Collection<Integer> loanStatuses);

    /*** FIXME: Add more appropriate names for the query ***/
    @Query(FIND_ACTIVE_LOANS_PRODUCT_IDS_BY_CLIENT)
    List<Long> findActiveLoansLoanProductIdsByClient(@Param("clientId") Long clientId, @Param("loanStatus") Integer loanStatus);

    @Query(FIND_ACTIVE_LOANS_PRODUCT_IDS_BY_GROUP)
    List<Long> findActiveLoansLoanProductIdsByGroup(@Param("groupId") Long groupId, @Param("loanStatus") Integer loanStatus);

    @Query(DOES_CLIENT_HAVE_NON_CLOSED_LOANS)
    boolean doNonClosedLoanAccountsExistForClient(@Param("clientId") Long clientId);

}