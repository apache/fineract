package org.mifosplatform.infrastructure.codes.service;

import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface CodeWritePlatformService {

    Long createCode(CodeCommand command);

    Long updateCode(CodeCommand command);

    EntityIdentifier deleteCode(CodeCommand command);
}