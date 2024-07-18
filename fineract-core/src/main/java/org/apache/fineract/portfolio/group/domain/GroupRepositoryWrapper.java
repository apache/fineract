/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.group.domain;

import java.time.LocalDate;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link GroupRepository} that adds NULL checking and Error handling capabilities
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
        return this.repository.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
    }

    public Group findByOfficeWithNotFoundDetection(final Long id, final Office office) {
        final Group group = findOneWithNotFoundDetection(id);
        if (!group.getOffice().getId().equals(office.getId())) {
            throw new GroupNotFoundException(id);
        }
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

    public void flush() {
        this.repository.flush();
    }

    public LocalDate retrieveSubmittedOndate(final Long groupId) {
        return this.repository.retrieveGroupTypeSubmitteOndDate(groupId);
    }
}
