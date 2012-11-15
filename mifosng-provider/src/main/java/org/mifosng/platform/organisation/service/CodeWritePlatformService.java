package org.mifosng.platform.organisation.service;

import org.mifosng.platform.api.commands.CodeCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface CodeWritePlatformService {

	@PreAuthorize(value = "hasRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_CODE')")
	Long createCode(final CodeCommand command);


	@PreAuthorize(value = "hasRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_CODE)")

	Long updateCode(final CodeCommand command);

	@PreAuthorize(value = "hasRole('ORGANISATION_ADMINISTRATION_SUPER_USER_ROLE')")
	EntityIdentifier deleteCode(Long codeId);
}