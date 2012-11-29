package org.mifosplatform.infrastructure.codes.service;

import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosng.platform.exceptions.CodeValueNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.codes.domain.Code;
import org.mifosplatform.infrastructure.codes.domain.CodeRepository;
import org.mifosplatform.infrastructure.codes.exception.SystemDefinedCodeCannotBeChangedException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.domain.Permission;
import org.mifosplatform.infrastructure.user.domain.PermissionRepository;
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
    private final PermissionRepository permissionRepository;

    @Autowired
    public CodeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final CodeRepository codeRepository,
            final PermissionRepository permissionRepository) {
        this.context = context;
        this.codeRepository = codeRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    @Override
    public Long createCode(final CodeCommand command) {

        try {
            context.authenticatedUser();
            command.validateForCreate();

            final Code code = Code.createNew(command.getName());

            this.codeRepository.save(code);

            final Permission thisTask = this.permissionRepository.findOneByCode("CREATE_CODE");
            if (thisTask.hasMakerCheckerEnabled() && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

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

            final Long codeId = command.getId();
            final Code code = this.codeRepository.findOne(codeId);
            if (code == null) { throw new CodeValueNotFoundException(codeId); }
            
            code.update(command);

            this.codeRepository.save(code);

            final Permission thisTask = this.permissionRepository.findOneByCode("UPDATE_CODE");
            if (thisTask.hasMakerCheckerEnabled() && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

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

        final Long codeId = command.getId();
        Code code = retrieveCodeBy(codeId);
        if (code.isSystemDefined()) {
            throw new SystemDefinedCodeCannotBeChangedException();
        }
        
        this.codeRepository.delete(codeId);

        final Permission thisTask = this.permissionRepository.findOneByCode("DELETE_CODE");
        if (thisTask.hasMakerCheckerEnabled() && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

        return new EntityIdentifier(codeId);
    }

    private Code retrieveCodeBy(final Long codeId) {
        final Code code = this.codeRepository.findOne(codeId);
        if (code == null) { throw new CodeValueNotFoundException(codeId); }
        return code;
    }
}