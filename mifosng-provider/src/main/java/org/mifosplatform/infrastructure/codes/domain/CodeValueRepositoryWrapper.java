package org.mifosplatform.infrastructure.codes.domain;

import org.mifosplatform.infrastructure.codes.exception.CodeValueNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link CodeValueRepository} that is responsible for checking if
 * {@link CodeValue} is returned when using <code>findOne</code> repository
 * method and throwing an appropriate not found exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link CodeValueRepository} is required.
 * </p>
 */
@Service
public class CodeValueRepositoryWrapper {

    private final CodeValueRepository repository;

    @Autowired
    public CodeValueRepositoryWrapper(final CodeValueRepository repository) {
        this.repository = repository;
    }

    public CodeValue findOneWithNotFoundDetection(final Long id) {
        final CodeValue codeValue = this.repository.findOne(id);
        if (codeValue == null) { throw new CodeValueNotFoundException(id); }
        return codeValue;
    }
}