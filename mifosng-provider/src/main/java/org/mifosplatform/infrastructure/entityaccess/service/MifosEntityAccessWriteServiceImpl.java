/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;


import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityAccess;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MifosEntityAccessWriteServiceImpl implements
		MifosEntityAccessWriteService {

    private final static Logger logger = LoggerFactory.getLogger(MifosEntityAccessWriteServiceImpl.class);
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final MifosEntityAccessRepository entityAccessRepository;

    @Autowired
    public MifosEntityAccessWriteServiceImpl(
    		final MifosEntityAccessRepository entityAccessRepository,
            final CodeValueRepositoryWrapper codeValueRepositoryWrapper) {
    	this.entityAccessRepository = entityAccessRepository;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
    }

	@Override
	public CommandProcessingResult createEntityAccess(JsonCommand command) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	@Transactional
	public void addNewEntityAccess (final String entityType, final Long entityId,
			final CodeValue accessType, 
			final String secondEntityType, final Long secondEntityId) {
		MifosEntityAccess entityAccess = MifosEntityAccess.createNew(entityType, entityId,
				accessType, secondEntityType, secondEntityId);
		entityAccessRepository.save(entityAccess);
	}
	
	/*
	@Override
	public CommandProcessingResult updateEntityAccess(Long entityAccessId,
			JsonCommand command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandProcessingResult removeEntityAccess(String entityType,
			Long entityId, Long accessType, String secondEntityType,
			Long secondEntityId) {
		// TODO Auto-generated method stub
		return null;
	}
	*/

}