/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Provides the domain repository for accessing, adding, modifying or deleting teller transactions.
 *
 * @see org.mifosplatform.organisation.teller.domain.TellerTransaction
 * @since 2.0.0
 */
public interface TellerTransactionRepository extends JpaRepository<TellerTransaction, Long>,
        JpaSpecificationExecutor<TellerTransaction> {
    // no added behavior
}
