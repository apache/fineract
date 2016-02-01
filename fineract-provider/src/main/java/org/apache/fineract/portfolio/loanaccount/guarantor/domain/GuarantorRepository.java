/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.domain;

import java.util.List;

import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GuarantorRepository extends JpaRepository<Guarantor, Long>, JpaSpecificationExecutor<Guarantor> {

    Guarantor findByLoanAndId(Loan loan, Long id);

    List<Guarantor> findByLoan(Loan loan);
}