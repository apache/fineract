package org.mifosng.platform.documentmanagement.service;

import java.io.InputStream;

import org.mifosng.platform.api.commands.DocumentCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DocumentWritePlatformService {
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_DOCUMENT')")
	Long createDocument(DocumentCommand documentCommand, InputStream inputStream);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_DOCUMENT')")
	EntityIdentifier updateDocument(DocumentCommand documentCommand,
			InputStream inputStream);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_DOCUMENT')")
	EntityIdentifier deleteDocument(DocumentCommand documentCommand);

}