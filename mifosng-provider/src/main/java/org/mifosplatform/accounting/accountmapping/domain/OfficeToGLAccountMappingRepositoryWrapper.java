/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.accountmapping.domain;

import java.util.List;

import org.mifosplatform.accounting.accountmapping.exception.OfficeToGLAccountMappingNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link OfficeToGLAccountMappingRepository} that adds NULL
 * checking and Error handling capabilities
 * </p>
 */
@Service
public class OfficeToGLAccountMappingRepositoryWrapper {

    private final OfficeToGLAccountMappingRepository repository;

    @Autowired
    public OfficeToGLAccountMappingRepositoryWrapper(final OfficeToGLAccountMappingRepository repository) {
        this.repository = repository;
    }

    public OfficeToGLAccountMapping findOneWithNotFoundDetection(final Long id) {
        final OfficeToGLAccountMapping officeToGLAccountMapping = this.repository.findOne(id);
        if (officeToGLAccountMapping == null) { throw new OfficeToGLAccountMappingNotFoundException(id); }
        return officeToGLAccountMapping;
    }

    public OfficeToGLAccountMapping findByOfficeAndFinancialAccountType(final Long officeId, final int financialAccountType) {
        return this.repository.findByOfficeAndFinancialAccountType(officeId, financialAccountType);
    }

    public List<OfficeToGLAccountMapping> findByOffice(final Long officeId) {
        return this.repository.findByOffice(officeId);
    }

    public void save(final OfficeToGLAccountMapping entity) {
        this.repository.save(entity);
    }

    public void saveAndFlush(final OfficeToGLAccountMapping entity) {
        this.repository.saveAndFlush(entity);
    }

    public void delete(final OfficeToGLAccountMapping entity) {
        this.repository.delete(entity);
    }
}