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

package org.apache.fineract.portfolio.self.pockets.domain;

import java.util.Collection;
import java.util.List;
import org.apache.fineract.portfolio.self.pockets.exception.MappingIdNotLinkedToPocketException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PocketAccountMappingRepositoryWrapper {

    private final PocketAccountMappingRepository pocketAccountMappingRepository;

    @Autowired
    public PocketAccountMappingRepositoryWrapper(final PocketAccountMappingRepository pocketAccountMappingRepository) {
        this.pocketAccountMappingRepository = pocketAccountMappingRepository;
    }

    public void save(final PocketAccountMapping pocketAccountMapping) {
        this.pocketAccountMappingRepository.save(pocketAccountMapping);
    }

    public List<PocketAccountMapping> save(final List<PocketAccountMapping> pocketAccountsList) {
        return this.pocketAccountMappingRepository.saveAll(pocketAccountsList);
    }

    public void delete(final List<PocketAccountMapping> pocketAccountsList) {
        this.pocketAccountMappingRepository.deleteAll(pocketAccountsList);
    }

    public PocketAccountMapping findByIdAndPocketIdWithNotFoundException(final Long id, final Long pocketId) {
        PocketAccountMapping pocketAccountMapping = this.pocketAccountMappingRepository.findByIdAndPocketId(id, pocketId);
        if (pocketAccountMapping == null) {
            throw new MappingIdNotLinkedToPocketException(id);
        }
        return pocketAccountMapping;

    }

    public Collection<PocketAccountMapping> findByPocketId(final Long pocketId) {
        return this.pocketAccountMappingRepository.findByPocketId(pocketId);

    }

}
