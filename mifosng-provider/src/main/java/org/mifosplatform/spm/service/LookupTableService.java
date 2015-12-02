/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.service;

import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.spm.domain.LookupTable;
import org.mifosplatform.spm.domain.Survey;
import org.mifosplatform.spm.repository.LookupTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LookupTableService {

    private final PlatformSecurityContext securityContext;
    private final LookupTableRepository lookupTableRepository;

    @Autowired
    public LookupTableService(final PlatformSecurityContext securityContext,
                              final LookupTableRepository lookupTableRepository) {
        super();
        this.securityContext = securityContext;
        this.lookupTableRepository = lookupTableRepository;
    }

    public LookupTable findById(final Long id) {
        this.securityContext.authenticatedUser();

        return this.lookupTableRepository.findOne(id);
    }

    public List<LookupTable> findBySurvey(final Survey survey) {
        this.securityContext.authenticatedUser();

        return this.lookupTableRepository.findBySurvey(survey);
    }

    public LookupTable createLookupTable(final LookupTable lookupTable) {
        this.securityContext.authenticatedUser();

        return this.lookupTableRepository.save(lookupTable);
    }
}
