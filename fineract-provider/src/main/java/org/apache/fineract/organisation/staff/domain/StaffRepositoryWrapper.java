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
package org.apache.fineract.organisation.staff.domain;

import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link StaffRepository} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class StaffRepositoryWrapper {

    private final StaffRepository repository;

    @Autowired
    public StaffRepositoryWrapper(final StaffRepository repository) {
        this.repository = repository;
    }

    public Staff findOneWithNotFoundDetection(final Long id) {
        final Staff staff = this.repository.findOne(id);
        if (staff == null) { throw new StaffNotFoundException(id); }
        return staff;
    }

    public Staff findByOfficeWithNotFoundDetection(final Long staffId, final Long officeId) {
        final Staff staff = this.repository.findByOffice(staffId, officeId);
        if (staff == null) { throw new StaffNotFoundException(staffId); }
        return staff;
    }

    public Staff findByOfficeHierarchyWithNotFoundDetection(final Long staffId, final String hierarchy) {
        final Staff staff = this.repository.findOne(staffId);
        if (staff == null) { throw new StaffNotFoundException(staffId); }
        final String staffhierarchy = staff.office().getHierarchy();
        if (!hierarchy.startsWith(staffhierarchy)) { throw new StaffNotFoundException(staffId); }
        return staff;
    }
    public void save(final Staff staff){
        this.repository.save(staff);
    }
}