package org.mifosng.platform.organisation.service;

import java.util.Collection;

import org.mifosng.platform.api.data.CodeData;

public interface CodeReadPlatformService {

	Collection<CodeData> retrieveAllCodes();

	CodeData retrieveCode(Long codeId);
}
