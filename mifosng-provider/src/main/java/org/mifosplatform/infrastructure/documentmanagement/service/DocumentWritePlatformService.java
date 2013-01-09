package org.mifosplatform.infrastructure.documentmanagement.service;

import java.io.InputStream;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.documentmanagement.command.DocumentCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DocumentWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_m_guarantor_external')")
    Long createDocument(DocumentCommand documentCommand, InputStream inputStream);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'UPDATE_m_guarantor_external')")
    CommandProcessingResult updateDocument(DocumentCommand documentCommand, InputStream inputStream);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_m_guarantor_external')")
    CommandProcessingResult deleteDocument(DocumentCommand documentCommand);

}