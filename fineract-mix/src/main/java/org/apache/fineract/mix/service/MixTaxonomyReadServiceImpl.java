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
package org.apache.fineract.mix.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.mix.data.MixTaxonomyData;
import org.apache.fineract.mix.domain.MixTaxonomyRepository;
import org.apache.fineract.mix.mapping.MixTaxonomyMapper;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MixTaxonomyReadServiceImpl implements MixTaxonomyReadService {

    private final MixTaxonomyRepository repository;
    private final MixTaxonomyMapper mapper;

    @Override
    public List<MixTaxonomyData> retrieveAll() {
        return repository.findAllByOrderByIdAsc().stream().map(mapper::map).toList();
    }

    @Override
    public MixTaxonomyData retrieveOne(final Long id) {
        return repository.findById(id).map(mapper::map).orElse(null);
    }
}
