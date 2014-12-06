/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mifosplatform.infrastructure.entityaccess.domain;

import org.mifosplatform.infrastructure.entityaccess.exception.MifosEntityAccessNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link MifosEntityAccessRepository} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class MifosEntityAccessRepositoryWrapper {

    private final MifosEntityAccessRepository repository;

    @Autowired
    public MifosEntityAccessRepositoryWrapper(final MifosEntityAccessRepository repository) {
        this.repository = repository;
    }

    public MifosEntityAccess findOneWithNotFoundDetection(final Long id) {
        final MifosEntityAccess mifosEntityAccess = this.repository.findOne(id);
        if (mifosEntityAccess == null) { throw new MifosEntityAccessNotFoundException(id); }
        return mifosEntityAccess;
    }

    public void save(final MifosEntityAccess mifosEntityAccess) {
        this.repository.save(mifosEntityAccess);
    }

    public void saveAndFlush(final MifosEntityAccess mifosEntityAccess) {
        this.repository.saveAndFlush(mifosEntityAccess);
    }

    public void delete(final MifosEntityAccess mifosEntityAccess) {
        this.repository.delete(mifosEntityAccess);
    }
}