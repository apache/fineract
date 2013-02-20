/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.domain;

import org.mifosplatform.portfolio.charge.exception.ChargeIsNotActiveException;
import org.mifosplatform.portfolio.charge.exception.ChargeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link ChargeRepository} that is responsible for checking if
 * {@link Charge} is returned when using <code>findOne</code> repository method
 * and throwing an appropriate not found exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link ChargeRepository} is required.
 * </p>
 */
@Service
public class ChargeRepositoryWrapper {

    private final ChargeRepository repository;

    @Autowired
    public ChargeRepositoryWrapper(final ChargeRepository repository) {
        this.repository = repository;
    }

    public Charge findOneWithNotFoundDetection(final Long id) {

        final Charge chargeDefinition = this.repository.findOne(id);
        if (chargeDefinition == null || chargeDefinition.isDeleted()) { throw new ChargeNotFoundException(id); }
        if (!chargeDefinition.isActive()) { throw new ChargeIsNotActiveException(id, chargeDefinition.getName()); }

        return chargeDefinition;
    }
}
