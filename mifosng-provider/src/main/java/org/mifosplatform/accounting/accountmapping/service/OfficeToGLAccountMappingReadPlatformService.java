/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.accountmapping.service;


import org.mifosplatform.accounting.accountmapping.data.OfficeToGLAccountMappingData;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.portfolio.group.service.SearchParameters;

public interface OfficeToGLAccountMappingReadPlatformService {

    Page<OfficeToGLAccountMappingData> retrieveAll(final SearchParameters searchParameters);

    OfficeToGLAccountMappingData retrieve(Long mappingId);

    OfficeToGLAccountMappingData retrieveTemplate();

}