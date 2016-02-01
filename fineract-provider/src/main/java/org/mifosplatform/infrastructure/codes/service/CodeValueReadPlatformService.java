/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.codes.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;

/**
 * A service for retrieving code value information based on the code itself.
 * 
 * There are two types of code information in the platform:
 * <ol>
 * <li>System defined codes</li>
 * <li>User defined codes</li>
 * </ol>
 * 
 * <p>
 * System defined codes cannot be altered or removed but their code values may
 * be allowed to be added to or removed.
 * </p>
 * 
 * <p>
 * User defined codes can be changed in any way by application users with system
 * permissions.
 * </p>
 */
public interface CodeValueReadPlatformService {

    Collection<CodeValueData> retrieveCodeValuesByCode(final String code);

    Collection<CodeValueData> retrieveAllCodeValues(final Long codeId);

    CodeValueData retrieveCodeValue(final Long codeValueId);
}