package org.mifosng.platform.makerchecker.service;

import org.mifosng.platform.api.data.EntityIdentifier;

public interface PortfolioCommandSourceWritePlatformService {

	EntityIdentifier logCommandSource(String apiOperation, String resource, Long resourceId, String jsonRequestBody);

	EntityIdentifier approveEntry(Long id);

	Long deleteEntry(Long makerCheckerId);
}