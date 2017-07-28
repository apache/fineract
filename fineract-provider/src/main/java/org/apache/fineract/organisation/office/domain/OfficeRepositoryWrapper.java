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
package org.apache.fineract.organisation.office.domain;

import org.apache.fineract.organisation.office.exception.OfficeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly=true)
    public Office findOfficeHierarchy(final Long id) {
        final Office office = this.repository.findOne(id);
        if (office == null) { throw new OfficeNotFoundException(id); }
        office.loadLazyCollections(); 
        return office ;
        
    }
    public Office save(final Office entity) {
        return this.repository.save(entity);
    }

    public Office saveAndFlush(final Office entity) {
        return this.repository.saveAndFlush(entity);
    }

    public void delete(final Office entity) {
        this.repository.delete(entity);
    }
}