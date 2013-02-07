package org.mifosplatform.infrastructure.codes.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;

/**
 * A service for retrieving code value information based on the code itself.
 * 
 * There are two types of code information in the platform:
 * <ol>
 * 	<li>System defined codes</li>
 *  <li>User defined codes</li>
 * </ol>
 * 
 * <p>System defined codes cannot be altered or removed but their code values may be allowed to be added to or removed.</p>
 * 
 * <p>User defined codes can be changed in any way by application users with system permissions.</p>
 */
public interface CodeValueReadPlatformService {

	// system defined code value data
	Collection<CodeValueData> retrieveCustomIdentifierCodeValues();
	
	Collection<CodeValueData> retrieveAllCodeValues(final Long codeId);
	
	CodeValueData retrieveCodeValue(final Long codeValueId);
}