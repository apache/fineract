package org.mifosng.platform.organisation.service;

import java.util.Collection;

import org.mifosng.platform.api.data.CodeValueData;

public interface CodeValueReadPlatformService {

	Collection<CodeValueData> retrieveAllCodeValues(Long codeId);

}