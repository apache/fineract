package org.mifosplatform.infrastructure.codes.service;

import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosplatform.infrastructure.codes.command.CodeCommand;

public interface CodeWritePlatformService {

    Long createCode(final CodeCommand command);

    Long updateCode(final CodeCommand command);

    EntityIdentifier deleteCode(final CodeCommand command);
}