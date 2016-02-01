/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.exception.ClientNonPersonNotFoundByClientIdException;
import org.mifosplatform.portfolio.client.exception.ClientNonPersonNotFoundException;
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
