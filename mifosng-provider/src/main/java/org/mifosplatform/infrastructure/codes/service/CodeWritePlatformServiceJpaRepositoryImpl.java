package org.mifosplatform.infrastructure.codes.service;

import java.util.Map;

import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.codes.domain.Code;
import org.mifosplatform.infrastructure.codes.domain.CodeRepository;
import org.mifosplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.mifosplatform.infrastructure.codes.exception.SystemDefinedCodeCannotBeChangedException;
import org.mifosplatform.infrastructure.codes.serialization.CodeCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.service.ConfigurationDomainService;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeWritePlatformServiceJpaRepositoryImpl implements CodeWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(CodeWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final CodeRepository codeRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final CodeCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public CodeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final CodeRepository codeRepository,
            final ConfigurationDomainService configurationDomainService,
            final CodeCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.codeRepository = codeRepository;
        this.configurationDomainService = configurationDomainService;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public Long createCode(final JsonCommand command) {

        try {
            context.authenticatedUser();
            
            final CodeCommand codeCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            codeCommand.validateForCreate();

            final Code code = Code.fromJson(command);
            this.codeRepository.save(code);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("CREATE_CODE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
            
            return code.getId();
        } catch (DataIntegrityViolationException dve) {
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public EntityIdentifier updateCode(final Long codeId, final JsonCommand command) {

        try {
            context.authenticatedUser();
            final CodeCommand codeCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            codeCommand.validateForUpdate();

            final Code code = retrieveCodeBy(codeId);
            Map<String, Object> changes = code.update(command);

            if (!changes.isEmpty()) {
                this.codeRepository.save(code);
            }

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("UPDATE_CODE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
            
            return EntityIdentifier.withChanges(codeId, changes);
        } catch (DataIntegrityViolationException dve) {
            handleCodeDataIntegrityIssues(command, dve);
            return null;
        }
    }

    @Transactional
    @Override
    public EntityIdentifier deleteCode(final Long codeId, final JsonCommand command) {

        context.authenticatedUser();

        final Code code = retrieveCodeBy(codeId);
        if (code.isSystemDefined()) {
            throw new SystemDefinedCodeCannotBeChangedException();
        }
        
        this.codeRepository.delete(code);

        if (this.configurationDomainService.isMakerCheckerEnabledForTask("DELETE_CODE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
        
        return new EntityIdentifier(code.getId());
    }

    private Code retrieveCodeBy(final Long codeId) {
        final Code code = this.codeRepository.findOne(codeId);
        if (code == null) { throw new CodeNotFoundException(codeId.toString()); }
        return code;
    }
    
    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleCodeDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("code_name_org")) { 
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.code.duplicate.name",
                "A code with name '" + name + "' already exists"); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}