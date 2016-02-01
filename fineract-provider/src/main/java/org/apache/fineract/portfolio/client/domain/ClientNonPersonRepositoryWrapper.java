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
import org.apache.fineract.portfolio.client.exception.ClientNonPersonNotFoundByClientIdException;
import org.apache.fineract.portfolio.client.exception.ClientNonPersonNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * <p>
 * Wrapper for {@link ClientNonPersonRepository} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class ClientNonPersonRepositoryWrapper {
	
	private final ClientNonPersonRepository repository;
    private final PlatformSecurityContext context;
	
    @Autowired
    public ClientNonPersonRepositoryWrapper(final ClientNonPersonRepository repository, final PlatformSecurityContext context) {
        this.repository = repository;
        this.context = context;
    }

    public ClientNonPerson findOneWithNotFoundDetection(final Long id) {
        final ClientNonPerson clientNonPerson = this.repository.findOne(id);
        if (clientNonPerson == null) { throw new ClientNonPersonNotFoundException(id); }
        return clientNonPerson;
    }
    
    public ClientNonPerson findOneByClientId(final Long clientId) {
    	return this.repository.findByClientId(clientId);
    }
    
    public ClientNonPerson findOneByClientIdWithNotFoundDetection(final Long clientId) {
        final ClientNonPerson clientNonPerson = this.repository.findByClientId(clientId);
        if (clientNonPerson == null) { throw new ClientNonPersonNotFoundByClientIdException(clientId); }
        return clientNonPerson;
    }

    public void save(final ClientNonPerson clientNonPerson) {
        this.repository.save(clientNonPerson);
    }

    public void saveAndFlush(final ClientNonPerson clientNonPerson) {
        this.repository.saveAndFlush(clientNonPerson);
    }

    public void delete(final ClientNonPerson clientNonPerson) {
        this.repository.delete(clientNonPerson);
    }
}
