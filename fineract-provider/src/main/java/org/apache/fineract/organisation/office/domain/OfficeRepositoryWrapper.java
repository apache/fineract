/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.office.domain;

import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link OfficeRepository} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class OfficeRepositoryWrapper {

    private final OfficeRepository repository;

    @Autowired
    public OfficeRepositoryWrapper(final OfficeRepository repository) {
        this.repository = repository;
    }

    public Office findOneWithNotFoundDetection(final Long id) {
        final Office office = this.repository.findOne(id);
        if (office == null) { throw new OfficeNotFoundException(id); }
        return office;
    }

    public void save(final Office entity) {
        this.repository.save(entity);
    }

    public void saveAndFlush(final Office entity) {
        this.repository.saveAndFlush(entity);
    }

    public void delete(final Office entity) {
        this.repository.delete(entity);
    }
}