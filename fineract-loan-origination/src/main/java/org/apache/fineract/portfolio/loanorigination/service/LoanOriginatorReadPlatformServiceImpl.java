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
package org.apache.fineract.portfolio.loanorigination.service;

import java.util.List;
import org.apache.fineract.portfolio.loanorigination.data.LoanOriginatorData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
public class LoanOriginatorReadPlatformServiceImpl implements LoanOriginatorReadPlatformService {

    private static final String NOT_IMPLEMENTED_MESSAGE = "Not implemented yet";

    @Override
    public List<LoanOriginatorData> retrieveAll() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public LoanOriginatorData retrieveById(Long id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public LoanOriginatorData retrieveByExternalId(String externalId) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public Long resolveIdByExternalId(String externalId) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MESSAGE);
    }
}
