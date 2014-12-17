/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.codes.domain;

import org.mifosplatform.infrastructure.codes.exception.CodeValueNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link CodeValueRepository} that is responsible for checking if
 * {@link CodeValue} is returned when using <code>findOne</code> and
 * <code>findByCodeNameAndId</code> repository methods and throwing an
 * appropriate not found exception.
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

    public CodeValue findOneByCodeNameAndIdWithNotFoundDetection(final String codeName, final Long id) {
        final CodeValue codeValue = this.repository.findByCodeNameAndId(codeName, id);
        if (codeValue == null) { throw new CodeValueNotFoundException(codeName, id); }
        return codeValue;
    }
    
    public CodeValue findOneByCodeNameAndLabelWithNotFoundDetection(final String codeName, final String label) {
        final CodeValue codeValue = this.repository.findByCodeNameAndLabel(codeName, label);
        if (codeValue == null) { throw new CodeValueNotFoundException(codeName, label); }
        return codeValue;
    }
    
}