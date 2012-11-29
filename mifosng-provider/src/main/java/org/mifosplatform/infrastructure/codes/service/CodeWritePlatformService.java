package org.mifosplatform.infrastructure.codes.service;

import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosplatform.infrastructure.codes.command.CodeCommand;

public interface CodeWritePlatformService {

    Long createCode(CodeCommand command);

    Long updateCode(CodeCommand command);

    EntityIdentifier deleteCode(CodeCommand command);
}