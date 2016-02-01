/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.closure.service;

import java.util.List;

import org.mifosplatform.accounting.closure.data.GLClosureData;

public interface GLClosureReadPlatformService {

    List<GLClosureData> retrieveAllGLClosures(Long OfficeId);

    GLClosureData retrieveGLClosureById(long glClosureId);

}
