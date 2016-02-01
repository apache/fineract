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

import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link ClientRepository} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class ClientRepositoryWrapper {

    private final ClientRepository repository;
    private final PlatformSecurityContext context;

    @Autowired
    public ClientRepositoryWrapper(final ClientRepository repository, final PlatformSecurityContext context) {
        this.repository = repository;
        this.context = context;
    }

    public Client findOneWithNotFoundDetection(final Long id) {
        final Client client = this.repository.findOne(id);
        if (client == null) { throw new ClientNotFoundException(id); }
        return client;
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

    public Client getActiveClientInUserScope(Long clientId) {
        final Client client = this.findOneWithNotFoundDetection(clientId);
        if (client.isNotActive()) { throw new ClientNotActiveException(client.getId()); }
        this.context.validateAccessRights(client.getOffice().getHierarchy());
        return client;
    }

}