package org.mifosplatform.infrastructure.codes.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.codes.data.CodeData;

public interface CodeReadPlatformService {

    Collection<CodeData> retrieveAllCodes();

    CodeData retrieveCode(Long codeId);
}
