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

    public List<LookupTable> findByKey(final String key) {
        this.securityContext.authenticatedUser();

        return this.lookupTableRepository.findByKey(key);
    }

    public List<LookupTable> findBySurvey(final Survey survey) {
        this.securityContext.authenticatedUser();

        return this.lookupTableRepository.findBySurvey(survey);
    }

    public List<LookupTable> createLookupTable(final List<LookupTable> lookupTable) {
        this.securityContext.authenticatedUser();

        return this.lookupTableRepository.save(lookupTable);
    }
}
