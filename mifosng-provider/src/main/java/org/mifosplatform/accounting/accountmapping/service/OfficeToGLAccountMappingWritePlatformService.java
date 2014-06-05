/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.accountmapping.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface OfficeToGLAccountMappingWritePlatformService {

    CommandProcessingResult createGLAccountMapping(JsonCommand command);

    CommandProcessingResult updateGLAccountMapping(Long mappingId, JsonCommand command);

    CommandProcessingResult deleteGLAccountMapping(Long mappingId, JsonCommand command);

}