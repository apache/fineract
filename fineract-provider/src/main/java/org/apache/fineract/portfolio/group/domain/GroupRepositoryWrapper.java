/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.domain;

import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link GroupRepository} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class GroupRepositoryWrapper {

    private final GroupRepository repository;

    @Autowired
    public GroupRepositoryWrapper(final GroupRepository repository) {
        this.repository = repository;
    }

    public Group findOneWithNotFoundDetection(final Long id) {
        final Group entity = this.repository.findOne(id);
        if (entity == null) { throw new GroupNotFoundException(id); }
        return entity;
    }

    public Group findByOfficeWithNotFoundDetection(final Long id, final Office office) {
        final Group group = findOneWithNotFoundDetection(id);
        if (group.getOffice().getId() != office.getId()) { throw new GroupNotFoundException(id); }
        return group;
    }

    public void save(final Group entity) {
        this.repository.save(entity);
    }

    public void saveAndFlush(final Group entity) {
        this.repository.saveAndFlush(entity);
    }

    public void delete(final Group entity) {
        this.repository.delete(entity);
    }
}