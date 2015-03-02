/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.service;

import java.util.Date;
import java.util.Map;

import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.entityaccess.api.MifosEntityApiResourceConstants;
import org.mifosplatform.infrastructure.entityaccess.data.MifosEntityDataValidator;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityAccess;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityAccessRepository;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityRelation;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityRelationRepositoryWrapper;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityToEntityMapping;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityToEntityMappingRepository;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityToEntityMappingRepositoryWrapper;
import org.mifosplatform.infrastructure.entityaccess.exception.MifosEntityToEntityMappingDateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MifosEntityAccessWriteServiceImpl implements MifosEntityAccessWriteService {

    private final static Logger logger = LoggerFactory.getLogger(MifosEntityAccessWriteServiceImpl.class);
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final MifosEntityAccessRepository entityAccessRepository;
    private final MifosEntityRelationRepositoryWrapper mifosEntityRelationRepositoryWrapper;
    private final MifosEntityToEntityMappingRepository mifosEntityToEntityMappingRepository;
    private final MifosEntityToEntityMappingRepositoryWrapper mifosEntityToEntityMappingRepositoryWrapper;
    private final MifosEntityDataValidator fromApiJsonDeserializer;

    @Autowired
    public MifosEntityAccessWriteServiceImpl(final MifosEntityAccessRepository entityAccessRepository,
            final CodeValueRepositoryWrapper codeValueRepositoryWrapper,
            final MifosEntityRelationRepositoryWrapper mifosEntityRelationRepositoryWrapper,
            final MifosEntityToEntityMappingRepository mifosEntityToEntityMappingRepository,
            final MifosEntityToEntityMappingRepositoryWrapper mifosEntityToEntityMappingRepositoryWrapper,
            MifosEntityDataValidator fromApiJsonDeserializer) {
        this.entityAccessRepository = entityAccessRepository;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
        this.mifosEntityToEntityMappingRepository = mifosEntityToEntityMappingRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.mifosEntityRelationRepositoryWrapper = mifosEntityRelationRepositoryWrapper;
        this.mifosEntityToEntityMappingRepositoryWrapper = mifosEntityToEntityMappingRepositoryWrapper;
    }

    @Override
    public CommandProcessingResult createEntityAccess(JsonCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Transactional
    public void addNewEntityAccess(final String entityType, final Long entityId, final CodeValue accessType, final String secondEntityType,
            final Long secondEntityId) {
        MifosEntityAccess entityAccess = MifosEntityAccess.createNew(entityType, entityId, accessType, secondEntityType, secondEntityId);
        entityAccessRepository.save(entityAccess);
    }

    @Override
    @Transactional
    public CommandProcessingResult createEntityToEntityMapping(Long relId, JsonCommand command) {

        try {

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final MifosEntityRelation mapId = this.mifosEntityRelationRepositoryWrapper.findOneWithNotFoundDetection(relId);

            final Long fromId = command.longValueOfParameterNamed(MifosEntityApiResourceConstants.fromEnityType);
            final Long toId = command.longValueOfParameterNamed(MifosEntityApiResourceConstants.toEntityType);
            final Date startDate = command.DateValueOfParameterNamed(MifosEntityApiResourceConstants.startDate);
            final Date endDate = command.DateValueOfParameterNamed(MifosEntityApiResourceConstants.endDate);

            fromApiJsonDeserializer.checkForEntity(relId.toString(), fromId, toId);
            if (startDate != null && endDate != null) {
                if (endDate.before(startDate)) { throw new MifosEntityToEntityMappingDateException(startDate.toString(), endDate.toString()); }
            }

            final MifosEntityToEntityMapping newMap = MifosEntityToEntityMapping.newMap(mapId, fromId, toId, startDate, endDate);

            this.mifosEntityToEntityMappingRepository.save(newMap);

            return new CommandProcessingResultBuilder().withEntityId(newMap.getId()).withCommandId(command.commandId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateEntityToEntityMapping(Long mapId, JsonCommand command) {

        try {

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final MifosEntityToEntityMapping mapForUpdate = this.mifosEntityToEntityMappingRepositoryWrapper
                    .findOneWithNotFoundDetection(mapId);

            String relId = mapForUpdate.getRelationId().getId().toString();
            final Long fromId = command.longValueOfParameterNamed(MifosEntityApiResourceConstants.fromEnityType);
            final Long toId = command.longValueOfParameterNamed(MifosEntityApiResourceConstants.toEntityType);
            fromApiJsonDeserializer.checkForEntity(relId, fromId, toId);

            final Map<String, Object> changes = mapForUpdate.updateMap(command);
            
            if (!changes.isEmpty()) {
                this.mifosEntityToEntityMappingRepository.saveAndFlush(mapForUpdate);
            }
            return new CommandProcessingResultBuilder(). //
                    withEntityId(mapForUpdate.getId()).withCommandId(command.commandId()).build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        } 
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteEntityToEntityMapping(Long mapId) {
        // TODO Auto-generated method stub

        final MifosEntityToEntityMapping deleteMap = this.mifosEntityToEntityMappingRepositoryWrapper.findOneWithNotFoundDetection(mapId);
        this.mifosEntityToEntityMappingRepository.delete(deleteMap);

        return new CommandProcessingResultBuilder(). //
                withEntityId(deleteMap.getId()).build();

    }

    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        realCause.printStackTrace();
        if (realCause.getMessage().contains("rel_id_from_id_to_id")) {
            final String fromId = command.stringValueOfParameterNamed(MifosEntityApiResourceConstants.fromEnityType);
            final String toId = command.stringValueOfParameterNamed(MifosEntityApiResourceConstants.toEntityType);
            throw new PlatformDataIntegrityException("error.msg.duplicate.entity.mapping", "EntityMapping from " + fromId + " to " + toId
                    + " already exist");
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.entity.mapping", "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

    /*
     * @Override public CommandProcessingResult updateEntityAccess(Long
     * entityAccessId, JsonCommand command) { // TODO Auto-generated method stub
     * return null; }
     * 
     * @Override public CommandProcessingResult removeEntityAccess(String
     * entityType, Long entityId, Long accessType, String secondEntityType, Long
     * secondEntityId) { // TODO Auto-generated method stub return null; }
     */

}