/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Provides the domain repository for accessing, adding, modifying or deleting cashiers.
 *
 * @author Markus Geiss
 * @see org.mifosplatform.organisation.teller.domain.Cashier
 * @since 2.0.0
 */
public interface CashierRepository extends JpaRepository<Cashier, Long>, JpaSpecificationExecutor<Cashier> {
    // no added behavior
}
