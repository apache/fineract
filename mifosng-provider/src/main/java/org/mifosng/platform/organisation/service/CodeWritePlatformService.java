package org.mifosng.platform.organisation.service;

import org.mifosng.platform.api.commands.CodeCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface CodeWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_CODE')")
	Long createCode(final CodeCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'UPDATE_CODE')")
	Long updateCode(final CodeCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_CODE')")
	EntityIdentifier deleteCode(Long codeId);
}