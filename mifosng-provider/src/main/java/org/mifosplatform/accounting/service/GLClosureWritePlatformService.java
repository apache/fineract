package org.mifosplatform.accounting.service;

import org.mifosplatform.accounting.api.commands.GLClosureCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface GLClosureWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_GL_CLOSURE')")
    Long createGLClosure(GLClosureCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_GL_CLOSURE')")
    Long updateGLClosure(Long glClosureId, GLClosureCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'DELETE_GL_CLOSURE')")
    Long deleteGLClosure(Long glClosureId);

}
