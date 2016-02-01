/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.domain;

import org.mifosplatform.organisation.teller.exception.TellerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TellerRepositoryWrapper {

    private final TellerRepository repository;

    @Autowired
    public TellerRepositoryWrapper(final TellerRepository repository) {
        this.repository = repository;
    }

    public Teller findOneWithNotFoundDetection(final Long id) {
        final Teller teller = this.repository.findOne(id);
        if (teller == null) { throw new TellerNotFoundException(id); }
        return teller;
    }
}
