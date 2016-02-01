/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DepositAccountOnHoldTransactionRepository extends JpaRepository<DepositAccountOnHoldTransaction, Long>,
        JpaSpecificationExecutor<DepositAccountOnHoldTransaction> {

        List<DepositAccountOnHoldTransaction> findBySavingsAccountAndReversedFalseOrderByCreatedDateAsc(SavingsAccount account);
}
