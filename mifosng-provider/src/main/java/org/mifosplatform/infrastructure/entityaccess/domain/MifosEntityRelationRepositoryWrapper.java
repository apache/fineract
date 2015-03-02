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
public class MifosEntityRelationRepositoryWrapper {

    private final MifosEntityRelationRepository mifosEntityRelationRepository;

    @Autowired
    private MifosEntityRelationRepositoryWrapper(final MifosEntityRelationRepository mifosEntityRelationRepository) {
        this.mifosEntityRelationRepository = mifosEntityRelationRepository;

    }
    
    public MifosEntityRelation findOneWithNotFoundDetection(final Long id) {
        final MifosEntityRelation mifosEntityRelation = this.mifosEntityRelationRepository.findOne(id);
        if (mifosEntityRelation == null) { throw new MifosEntityAccessNotFoundException(id); }
        return mifosEntityRelation;
    }

}
