package org.mifosplatform.accounting.service;

import org.mifosplatform.accounting.api.commands.GLAccountCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface GLAccountWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_GL_ACCOUNT')")
    Long createGLAccount(GLAccountCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'UPDATE_GL_ACCOUNT')")
    Long updateGLAccount(Long glAccountId, GLAccountCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_GL_ACCOUNT')")
    Long deleteGLAccount(Long glAccountId);

}
