package org.mifosplatform.commands.service;

import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface PortfolioCommandSourceWritePlatformService {

    EntityIdentifier logCommandSource(String taskPermissionName, String apiOperation, String resource, Long resourceId, String subResource,
            Long subRescourceId, String json);

    EntityIdentifier logCommandSource(String taskPermissionName, String apiOperation, String resource, Long resourceId, String json);

    EntityIdentifier approveEntry(Long id);

    Long deleteEntry(Long makerCheckerId);
}