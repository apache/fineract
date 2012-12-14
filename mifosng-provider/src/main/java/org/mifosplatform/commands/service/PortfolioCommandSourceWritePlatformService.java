package org.mifosplatform.commands.service;

import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface PortfolioCommandSourceWritePlatformService {

    EntityIdentifier logCommandSource(String actionName, String entityName, String apiOperation, String resource, Long resourceId, String subResource,
            Long subRescourceId, String json);

    EntityIdentifier logCommandSource(String actionName, String entityName, String apiOperation, String resource, Long resourceId, String json);

    EntityIdentifier approveEntry(Long id);

    Long deleteEntry(Long makerCheckerId);
}