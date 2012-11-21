package org.mifosng.platform.documentmanagement.service;

import java.io.InputStream;

import org.mifosng.platform.api.commands.DocumentCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DocumentWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_m_guarantor_external')")
	Long createDocument(DocumentCommand documentCommand, InputStream inputStream);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_m_guarantor_external')")
	EntityIdentifier updateDocument(DocumentCommand documentCommand,
			InputStream inputStream);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_m_guarantor_external')")
	EntityIdentifier deleteDocument(DocumentCommand documentCommand);

}