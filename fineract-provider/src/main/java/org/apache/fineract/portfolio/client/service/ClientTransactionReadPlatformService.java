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
package org.apache.fineract.portfolio.client.service;

import java.util.Collection;

import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.portfolio.client.data.ClientTransactionData;
import org.springframework.transaction.annotation.Transactional;

public interface ClientTransactionReadPlatformService {

    @Transactional(readOnly = true)
    public Page<ClientTransactionData> retrieveAllTransactions(Long clientId, SearchParameters parameters);

    @Transactional(readOnly = true)
    public Collection<ClientTransactionData> retrieveAllTransactions(final Long clientId, final Long chargeId);

    @Transactional(readOnly = true)
    public ClientTransactionData retrieveTransaction(Long clientId, Long transactionId);

}
