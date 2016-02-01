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
package org.apache.fineract.infrastructure.entityaccess.domain;

import org.apache.fineract.infrastructure.entityaccess.exception.FineractEntityAccessNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link FineractEntityAccessRepository} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class FineractEntityAccessRepositoryWrapper {

    private final FineractEntityAccessRepository repository;

    @Autowired
    public FineractEntityAccessRepositoryWrapper(final FineractEntityAccessRepository repository) {
        this.repository = repository;
    }

    public FineractEntityAccess findOneWithNotFoundDetection(final Long id) {
        final FineractEntityAccess fineractEntityAccess = this.repository.findOne(id);
        if (fineractEntityAccess == null) { throw new FineractEntityAccessNotFoundException(id); }
        return fineractEntityAccess;
    }

    public void save(final FineractEntityAccess fineractEntityAccess) {
        this.repository.save(fineractEntityAccess);
    }

    public void saveAndFlush(final FineractEntityAccess fineractEntityAccess) {
        this.repository.saveAndFlush(fineractEntityAccess);
    }

    public void delete(final FineractEntityAccess fineractEntityAccess) {
        this.repository.delete(fineractEntityAccess);
    }
}