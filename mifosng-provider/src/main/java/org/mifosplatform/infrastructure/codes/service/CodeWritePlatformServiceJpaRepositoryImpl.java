package org.mifosplatform.infrastructure.codes.service;

import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.codes.domain.Code;
import org.mifosplatform.infrastructure.codes.domain.CodeRepository;
import org.mifosplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.mifosplatform.infrastructure.codes.exception.SystemDefinedCodeCannotBeChangedException;
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

    @Autowired
    public CodeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final CodeRepository codeRepository,
            final ConfigurationDomainService configurationDomainService) {
        this.context = context;
        this.codeRepository = codeRepository;
        this.configurationDomainService = configurationDomainService;
    }

    @Transactional
    @Override
    public Long createCode(final CodeCommand command) {

        try {
            context.authenticatedUser();
            command.validateForCreate();

            final Code code = Code.createNew(command.getName());

            this.codeRepository.save(code);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("CREATE_CODE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
            
            return code.getId();
        } catch (DataIntegrityViolationException dve) {
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public Long updateCode(final CodeCommand command) {

        try {
            context.authenticatedUser();
            command.validateForUpdate();

            final Code code = retrieveCodeBy(command.getName());
            code.update(command);

            this.codeRepository.save(code);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("UPDATE_CODE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
            
            return code.getId();
        } catch (DataIntegrityViolationException dve) {
            handleCodeDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleCodeDataIntegrityIssues(final CodeCommand command, final DataIntegrityViolationException dve) {
        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("code_name_org")) { throw new PlatformDataIntegrityException("error.msg.code.duplicate.name",
                "A code with name '" + command.getName() + "' already exists"); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

    @Transactional
    @Override
    public EntityIdentifier deleteCode(final CodeCommand command) {

        context.authenticatedUser();

        final Code code = retrieveCodeBy(command.getName());
        if (code.isSystemDefined()) {
            throw new SystemDefinedCodeCannotBeChangedException();
        }
        
        this.codeRepository.delete(code);

        if (this.configurationDomainService.isMakerCheckerEnabledForTask("DELETE_CODE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
        
        return new EntityIdentifier(code.getId());
    }

    private Code retrieveCodeBy(final String name) {
        final Code code = this.codeRepository.findOneByName(name);
        if (code == null) { throw new CodeNotFoundException(name); }
        return code;
    }
}