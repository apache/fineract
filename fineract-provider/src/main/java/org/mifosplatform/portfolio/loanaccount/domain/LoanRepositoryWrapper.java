/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        final Loan loan = this.repository.findOne(id);
        if (loan == null) { throw new LoanNotFoundException(id); }
        return loan;
    }

    public Collection<Loan> findActiveLoansByLoanIdAndGroupId(Long clientId, Long groupId) {
        final Collection<Integer> loanStatuses = new ArrayList<>(Arrays.asList(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(),
                LoanStatus.APPROVED.getValue(), LoanStatus.ACTIVE.getValue(), LoanStatus.OVERPAID.getValue()));
        final Collection<Loan> loans = this.repository.findByClientIdAndGroupIdAndLoanStatus(clientId, groupId, loanStatuses);
        return loans;
    }

}