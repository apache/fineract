package org.mifosng.platform.organisation.service;

import org.mifosng.platform.api.commands.CodeCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface CodeWritePlatformService {

	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER')")
	Long createCode(final CodeCommand command);
	
	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER')")
	Long updateCode(final CodeCommand command);
}