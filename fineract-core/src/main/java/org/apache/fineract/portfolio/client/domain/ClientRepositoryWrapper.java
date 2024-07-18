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
package org.apache.fineract.portfolio.client.domain;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Wrapper for {@link ClientRepository} that adds NULL checking and Error handling capabilities
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ClientRepositoryWrapper {

    private final ClientRepository repository;
    private final PlatformSecurityContext context;

    @Transactional(readOnly = true)
    public Client findOneWithNotFoundDetection(final Long id) {
        return this.findOneWithNotFoundDetection(id, false);
    }

    @Transactional(readOnly = true)
    public Client findOneWithNotFoundDetection(final Long clientId, final boolean loadLazyCollections) {
        final Client client = this.repository.findById(clientId).orElseThrow(() -> new ClientNotFoundException(clientId));
        if (loadLazyCollections) {
            client.loadLazyCollections();
        }
        return client;
    }

    public List<Client> findAll(final Collection<Long> clientIds) {
        return this.repository.findAllById(clientIds);
    }

    public void save(final Client client) {
        this.repository.save(client);
    }

    public void saveAndFlush(final Client client) {
        this.repository.saveAndFlush(client);
    }

    public void delete(final Client client) {
        this.repository.delete(client);
    }

    public void flush() {
        this.repository.flush();
    }

    @Transactional(readOnly = true)
    public Client getActiveClientInUserScope(Long clientId) {
        final Client client = this.findOneWithNotFoundDetection(clientId);
        if (client.isNotActive()) {
            throw new ClientNotActiveException(client.getId());
        }
        this.context.validateAccessRights(client.getOffice().getHierarchy());
        return client;
    }

    public Client getClientByAccountNumber(String accountNumber) {
        Client client = this.repository.getClientByAccountNumber(accountNumber);
        if (client == null) {
            throw new ClientNotFoundException(accountNumber, "account.number");
        }
        return client;
    }

    public Client getClientByClientIdAndHierarchy(final Long clientId, final String hierarchySearchString) {
        Client client = this.repository.fetchByClientIdAndHierarchy(clientId, hierarchySearchString, hierarchySearchString);
        if (client == null) {
            throw new ClientNotFoundException(clientId.toString(), "client.id");
        }
        return client;
    }

    public Long findIdByExternalId(final ExternalId externalId) {
        return this.repository.findIdByExternalId(externalId);
    }

}
