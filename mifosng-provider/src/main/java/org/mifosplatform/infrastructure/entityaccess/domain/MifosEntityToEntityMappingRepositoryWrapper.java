/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.domain;

import org.mifosplatform.infrastructure.entityaccess.exception.MifosEntityAccessNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MifosEntityToEntityMappingRepositoryWrapper {

    private final MifosEntityToEntityMappingRepository mifosEntityToEntityMappingRepository;

    @Autowired
    public MifosEntityToEntityMappingRepositoryWrapper(final MifosEntityToEntityMappingRepository mifosEntityToEntityMappingRepository) {
        this.mifosEntityToEntityMappingRepository = mifosEntityToEntityMappingRepository;
    }

    public MifosEntityToEntityMapping findOneWithNotFoundDetection(final Long id) {
        final MifosEntityToEntityMapping mifosEntityToEntityMapping = this.mifosEntityToEntityMappingRepository.findOne(id);
        if (mifosEntityToEntityMapping == null) { throw new MifosEntityAccessNotFoundException(id); }
        return mifosEntityToEntityMapping;
    }

    public void delete(final MifosEntityToEntityMapping mapId) {
        this.mifosEntityToEntityMappingRepository.delete(mapId);
    }

}
