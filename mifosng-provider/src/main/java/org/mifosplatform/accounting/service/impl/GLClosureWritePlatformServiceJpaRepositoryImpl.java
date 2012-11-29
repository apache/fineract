package org.mifosplatform.accounting.service.impl;

import org.mifosng.platform.exceptions.OfficeNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosplatform.accounting.api.commands.GLClosureCommand;
import org.mifosplatform.accounting.domain.GLClosure;
import org.mifosplatform.accounting.domain.GLClosureRepository;
import org.mifosplatform.accounting.exceptions.GLClosureDuplicateException;
import org.mifosplatform.accounting.exceptions.GLClosureNotFoundException;
import org.mifosplatform.accounting.service.GLClosureCommandValidator;
import org.mifosplatform.accounting.service.GLClosureWritePlatformService;
import org.mifosplatform.infrastructure.office.domain.Office;
import org.mifosplatform.infrastructure.office.domain.OfficeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GLClosureWritePlatformServiceJpaRepositoryImpl implements GLClosureWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(GLClosureWritePlatformServiceJpaRepositoryImpl.class);

    private final GLClosureRepository glClosureRepository;
    private final OfficeRepository officeRepository;

    @Autowired
    public GLClosureWritePlatformServiceJpaRepositoryImpl(final GLClosureRepository glClosureRepository,
            final OfficeRepository officeRepository) {
        this.glClosureRepository = glClosureRepository;
        this.officeRepository = officeRepository;
    }

    @Transactional
    @Override
    public Long createGLClosure(GLClosureCommand command) {
        try {
            GLClosureCommandValidator validator = new GLClosureCommandValidator(command);
            validator.validateForCreate();

            // check office is valid
            final Office office = this.officeRepository.findOne(command.getOfficeId());
            if (office == null) { throw new OfficeNotFoundException(command.getOfficeId()); }

            GLClosure glClosure = GLClosure.createNew(office, command.getClosingDate(), command.getComments());

            this.glClosureRepository.saveAndFlush(glClosure);

            return glClosure.getId();
        } catch (DataIntegrityViolationException dve) {
            handleGLClosureIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public Long updateGLClosure(Long glClosureId, GLClosureCommand command) {
        GLClosureCommandValidator validator = new GLClosureCommandValidator(command);
        validator.validateForUpdate();

        // is the glClosure valid
        GLClosure glClosure = glClosureRepository.findOne(glClosureId);
        if (glClosure == null) { throw new GLClosureNotFoundException(glClosureId); }

        glClosure.update(command);

        this.glClosureRepository.saveAndFlush(glClosure);

        return glClosure.getId();
    }

    @Transactional
    @Override
    public Long deleteGLClosure(Long glClosureId) {
        final GLClosure glClosure = this.glClosureRepository.findOne(glClosureId);

        if (glClosure == null) { throw new GLClosureNotFoundException(glClosureId); }

        // TODO: check if any closures are present for this branch at a later
        // date than this closure

        this.glClosureRepository.delete(glClosure);

        return glClosureId;
    }

    /**
     * @param command
     * @param dve
     */
    private void handleGLClosureIntegrityIssues(final GLClosureCommand command, DataIntegrityViolationException dve) {
        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("office_id_closing_date")) { throw new GLClosureDuplicateException(command.getOfficeId(),
                command.getClosingDate()); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.glClosure.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource GL Closure: " + realCause.getMessage());
    }
}
