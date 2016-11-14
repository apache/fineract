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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Wrapper for {@link LoanRepository} that adds NULL checking and Error handling
 * capabilities
 * </p>
 */
@Service
public class LoanRepositoryWrapper {

    private final LoanRepository repository;

    @Autowired
    public LoanRepositoryWrapper(final LoanRepository repository) {
        this.repository = repository;
    }

    public Loan findOneWithNotFoundDetection(final Long id) {
        return this.findOneWithNotFoundDetection(id, false) ;
    }

    @Transactional(readOnly=true)
    public Loan findOneWithNotFoundDetection(final Long id, boolean loadLazyCollections) {
        final Loan loan = this.repository.findOne(id);
        if (loan == null) { throw new LoanNotFoundException(id); }
        if(loadLazyCollections) {
            loan.initializeLazyCollections();
        }
        return loan;
    }
    
    //Root Entities are enough
    public Collection<Loan> findActiveLoansByLoanIdAndGroupId(Long clientId, Long groupId) {
        final Collection<Integer> loanStatuses = new ArrayList<>(Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(),
                LoanStatus.APPROVED.getValue(), LoanStatus.ACTIVE.getValue(), LoanStatus.OVERPAID.getValue()));
        final Collection<Loan> loans = this.repository.findByClientIdAndGroupIdAndLoanStatus(clientId, groupId, loanStatuses);
        return loans;
    }

    public Loan saveAndFlush(final Loan loan) {
        return this.repository.saveAndFlush(loan) ;
    }
    
    @Transactional
    public Loan save(final Loan loan) {
        return this.repository.save(loan) ;
    }
    
    public List<Loan> save(List<Loan> loans) {
        return this.repository.save(loans) ;
    }
    public void flush() {
        this.repository.flush(); 
    }
    
    public void delete(final Long loanId) {
        this.repository.delete(loanId);
    }
    //Only root entities is enough
    public List<Loan> getGroupLoansDisbursedAfter(@Param("disbursementDate") Date disbursementDate, @Param("groupId") Long groupId,
            @Param("loanType") Integer loanType) {
        return this.repository.getGroupLoansDisbursedAfter(disbursementDate, groupId, loanType) ;
    }
    //Only root entities enough
    public List<Loan> getClientOrJLGLoansDisbursedAfter(@Param("disbursementDate") Date disbursementDate, @Param("clientId") Long clientId) {
        return this.repository.getClientOrJLGLoansDisbursedAfter(disbursementDate, clientId) ;
    }

    public Integer getMaxGroupLoanCounter(@Param("groupId") Long groupId, @Param("loanType") Integer loanType) {
        return this.repository.getMaxGroupLoanCounter(groupId, loanType) ;
    }

    public Integer getMaxGroupLoanProductCounter(@Param("productId") Long productId, @Param("groupId") Long groupId,
            @Param("loanType") Integer loanType) {
        return this.repository.getMaxGroupLoanProductCounter(productId, groupId, loanType) ;
    }

    public Integer getMaxClientOrJLGLoanCounter(@Param("clientId") Long clientId) {
        return this.repository.getMaxClientOrJLGLoanCounter(clientId) ;
    }

    public Integer getMaxClientOrJLGLoanProductCounter(@Param("productId") Long productId, @Param("clientId") Long clientId) {
        return this.repository.getMaxClientOrJLGLoanProductCounter(productId, clientId) ;
    }

    //Only root entities are enough
    public List<Loan> getGroupLoansToUpdateLoanCounter(@Param("loanCounter") Integer loanCounter, @Param("groupId") Long groupId,
            @Param("groupLoanType") Integer groupLoanType) {
        return this.repository.getGroupLoansToUpdateLoanCounter(loanCounter, groupId, groupLoanType) ;
    }

    //Only root entities are enough
    public List<Loan> getClientOrJLGLoansToUpdateLoanCounter(@Param("loanCounter") Integer loanCounter, @Param("clientId") Long clientId) {
        return this.repository.getClientOrJLGLoansToUpdateLoanCounter(loanCounter, clientId) ;
    }

    //Only root entities are enough
    public List<Loan> getGroupLoansToUpdateLoanProductCounter(@Param("loanProductCounter") Integer loanProductCounter,
            @Param("groupId") Long groupId, @Param("groupLoanType") Integer groupLoanType) {
        return this.repository.getGroupLoansToUpdateLoanProductCounter(loanProductCounter, groupId, groupLoanType) ;
    }
    
    //Only root entities are enough
    public List<Loan> getClientLoansToUpdateLoanProductCounter(@Param("loanProductCounter") Integer loanProductCounter,
            @Param("clientId") Long clientId) {
        return this.repository.getClientLoansToUpdateLoanProductCounter(loanProductCounter, clientId) ;
    }

    //Need loanOfficerHistory. Check whether FETCH JOIN is good enough here
    @Transactional(readOnly=true)
    public List<Loan> findByClientIdAndGroupId(@Param("clientId") Long clientId, @Param("groupId") Long groupId) {
        List<Loan> loans = this.repository.findByClientIdAndGroupId(clientId, groupId) ;
        if(loans != null && loans.size() > 0) {
            for(Loan loan: loans) {
                loan.initializeLoanOfficerHistory();
            }
        }
        return loans ;
    }

    //need loanTransactions, loanOfficerHistory
    @Transactional(readOnly=true)
    public List<Loan> findLoanByClientId(@Param("clientId") Long clientId) {
        List<Loan> loans = this.repository.findLoanByClientId(clientId) ;
        if(loans != null && loans.size() > 0) {
            for(Loan loan: loans) {
                loan.initilizeTransactions();
                loan.initializeLoanOfficerHistory();
            }
        }
        return loans ;
    }

    //Root entities are enough
    public List<Loan> findByGroupId(@Param("groupId") Long groupId) {
        return this.repository.findByGroupId(groupId) ;
    }

    //Looks like we need complete Data
    public List<Loan> findByIdsAndLoanStatusAndLoanType(@Param("ids") Collection<Long> ids,
            @Param("loanStatuses") Collection<Integer> loanStatuses, @Param("loanTypes") Collection<Integer> loanTypes) {
        List<Loan> loans =this.repository.findByIdsAndLoanStatusAndLoanType(ids, loanStatuses, loanTypes) ;
        if(loans != null && loans.size() > 0) {
            for(Loan loan: loans) {
                loan.initializeLazyCollections();
            }
        }
        return loans ;
    }

    //This method is not used
    public List<Long> getLoansDisbursedAfter(@Param("disbursalDate") Date disbursalDate) {
        return this.repository.getLoansDisbursedAfter(disbursalDate) ;
    }

    //Repayments Schedule
    public List<Loan> findByClientOfficeIdsAndLoanStatus(@Param("officeIds") Collection<Long> officeIds,
            @Param("loanStatuses") Collection<Integer> loanStatuses) {
        List<Loan> loans = this.repository.findByClientOfficeIdsAndLoanStatus(officeIds, loanStatuses) ;
        if(loans != null && loans.size() >0) {
            for(Loan loan: loans) {
                loan.initializeRepaymentSchedule();
            }
        }
        return loans ; 
    }

  //Repayments Schedule
    public List<Loan> findByGroupOfficeIdsAndLoanStatus(@Param("officeIds") Collection<Long> officeIds,
            @Param("loanStatuses") Collection<Integer> loanStatuses) {
        List<Loan> loans = this.repository.findByGroupOfficeIdsAndLoanStatus(officeIds, loanStatuses) ;
        if(loans != null && loans.size() >0) {
            for(Loan loan: loans) {
                loan.initializeRepaymentSchedule();
            }
        }
        return loans ; 
    }

    public List<Long> findActiveLoansLoanProductIdsByClient(@Param("clientId") Long clientId, @Param("loanStatus") Integer loanStatus) {
        return this.repository.findActiveLoansLoanProductIdsByClient(clientId, loanStatus) ;
    }

    public List<Long> findActiveLoansLoanProductIdsByGroup(@Param("groupId") Long groupId, @Param("loanStatus") Integer loanStatus) {
        return this.repository.findActiveLoansLoanProductIdsByGroup(groupId, loanStatus) ;
    }

    public boolean doNonClosedLoanAccountsExistForClient(@Param("clientId") Long clientId) {
        return this.repository.doNonClosedLoanAccountsExistForClient(clientId) ;
    }
    
    public boolean doNonClosedLoanAccountsExistForProduct(@Param("productId") Long productId) {
        return this.repository.doNonClosedLoanAccountsExistForProduct(productId) ;
    }
    
    public Loan findNonClosedLoanByAccountNumber(@Param("accountNumber") String accountNumber) {
        return this.repository.findNonClosedLoanByAccountNumber(accountNumber) ;
    }

    //Looks like we need complete entity
    @Transactional(readOnly=true)
    public Loan findNonClosedLoanThatBelongsToClient(@Param("loanId") Long loanId, @Param("clientId") Long clientId) {
        Loan loan = this.repository.findNonClosedLoanThatBelongsToClient(loanId, clientId) ;
        if(loan != null) loan.initilizeTransactions();
        return loan ;
    }
}