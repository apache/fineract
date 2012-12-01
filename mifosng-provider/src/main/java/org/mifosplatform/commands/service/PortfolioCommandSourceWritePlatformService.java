package org.mifosplatform.commands.service;

import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface PortfolioCommandSourceWritePlatformService {

    EntityIdentifier logCommandSource(String apiOperation, String resource, Long resourceId, String commandSerializedAsJson);

    EntityIdentifier approveEntry(Long id);

    Long deleteEntry(Long makerCheckerId);
}