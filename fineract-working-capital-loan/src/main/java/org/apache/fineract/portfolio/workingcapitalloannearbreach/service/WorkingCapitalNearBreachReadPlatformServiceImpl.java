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
package org.apache.fineract.portfolio.workingcapitalloannearbreach.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachData;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.exception.WorkingCapitalNearBreachNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.mapper.WorkingCapitalNearBreachMapper;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.repository.WorkingCapitalNearBreachRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorkingCapitalNearBreachReadPlatformServiceImpl implements WorkingCapitalNearBreachReadPlatformService {

    private final WorkingCapitalNearBreachRepository repository;
    private final WorkingCapitalNearBreachMapper mapper;

    @Override
    public List<WorkingCapitalNearBreachData> retrieveAll() {
        return mapper.toData(repository.findAll());
    }

    @Override
    public WorkingCapitalNearBreachData retrieveOne(final Long nearBreachId) {
        return mapper
                .toData(repository.findById(nearBreachId).orElseThrow(() -> new WorkingCapitalNearBreachNotFoundException(nearBreachId)));
    }
}
