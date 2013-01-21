package org.mifosplatform.accounting.service.impl;

import java.util.List;

import org.mifosplatform.accounting.api.commands.GLAccountCommand;
import org.mifosplatform.accounting.domain.GLAccount;
import org.mifosplatform.accounting.domain.GLAccountRepository;
import org.mifosplatform.accounting.domain.GLJournalEntry;
import org.mifosplatform.accounting.domain.GLJournalEntryRepository;
import org.mifosplatform.accounting.exceptions.GLAccountDuplicateException;
import org.mifosplatform.accounting.exceptions.GLAccountInvalidDeleteException;
import org.mifosplatform.accounting.exceptions.GLAccountInvalidDeleteException.GL_ACCOUNT_INVALID_DELETE_REASON;
import org.mifosplatform.accounting.exceptions.GLAccountInvalidParentException;
import org.mifosplatform.accounting.exceptions.GLAccountInvalidUpdateException;
import org.mifosplatform.accounting.exceptions.GLAccountInvalidUpdateException.GL_ACCOUNT_INVALID_UPDATE_REASON;
import org.mifosplatform.accounting.exceptions.GLAccountNotFoundException;
import org.mifosplatform.accounting.service.GLAccountCommandValidator;
import org.mifosplatform.accounting.service.GLAccountWritePlatformService;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GLAccountWritePlatformServiceJpaRepositoryImpl implements GLAccountWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(GLAccountWritePlatformServiceJpaRepositoryImpl.class);

    private final GLAccountRepository glAccountRepository;
    private final GLJournalEntryRepository glJournalEntryRepository;

    @Autowired
    public GLAccountWritePlatformServiceJpaRepositoryImpl(final GLAccountRepository glAccountRepository,
            final GLJournalEntryRepository glJournalEntryRepository) {
        this.glAccountRepository = glAccountRepository;
        this.glJournalEntryRepository = glJournalEntryRepository;
    }

    @Transactional
    @Override
    public Long createGLAccount(GLAccountCommand command) {
        try {
            GLAccountCommandValidator validator = new GLAccountCommandValidator(command);
            validator.validateForCreate();

            // check parent is valid
            GLAccount parentGLAccount = null;
            if (command.getParentId() != null) {
                parentGLAccount = validateParentGLAccount(command);
            }
            GLAccount glAccount = GLAccount.createNew(parentGLAccount, command.getName(), command.getGlCode(), false,
                    command.getManualEntriesAllowed(), command.getClassification(), command.getUsage(), command.getDescription());

            this.glAccountRepository.saveAndFlush(glAccount);

            return glAccount.getId();
        } catch (DataIntegrityViolationException dve) {
            handleGLAccountDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public Long updateGLAccount(Long glAccountId, GLAccountCommand command) {
        GLAccountCommandValidator validator = new GLAccountCommandValidator(command);
        validator.validateForUpdate();

        // is the glAccount valid
        GLAccount glAccount = glAccountRepository.findOne(glAccountId);
        if (glAccount == null) { throw new GLAccountNotFoundException(glAccountId); }

        // is the new parent valid
        GLAccount parentGLAccount = null;
        if (command.isParentIdChanged()) {
            parentGLAccount = validateParentGLAccount(command);
        }

        /**
         * a detail account cannot be changed to a header account if
         * transactions are already logged against it
         **/
        if (command.isUsageChanged()) {
            if (command.isHeaderAccount()) {
                List<GLJournalEntry> journalEntriesForAccount = glJournalEntryRepository.findFirstJournalEntryForAccount(glAccountId);
                if (journalEntriesForAccount.size() > 0) { throw new GLAccountInvalidUpdateException(
                        GL_ACCOUNT_INVALID_UPDATE_REASON.TRANSANCTIONS_LOGGED, glAccountId); }
            }
        }

        glAccount.update(command, parentGLAccount);

        this.glAccountRepository.saveAndFlush(glAccount);

        return glAccount.getId();
    }

    @Transactional
    @Override
    public Long deleteGLAccount(Long glAccountId) {
        final GLAccount glAccount = this.glAccountRepository.findOne(glAccountId);

        if (glAccount == null) { throw new GLAccountNotFoundException(glAccountId); }

        // validate this isn't a header account that has children
        if (glAccount.isHeaderAccount() && glAccount.getChildren().size() > 0) { throw new GLAccountInvalidDeleteException(
                GL_ACCOUNT_INVALID_DELETE_REASON.HAS_CHILDREN, glAccountId); }

        // does this account have transactions logged against it
        List<GLJournalEntry> journalEntriesForAccount = glJournalEntryRepository.findFirstJournalEntryForAccount(glAccountId);
        if (journalEntriesForAccount.size() > 0) { throw new GLAccountInvalidDeleteException(
                GL_ACCOUNT_INVALID_DELETE_REASON.TRANSANCTIONS_LOGGED, glAccountId); }
        this.glAccountRepository.delete(glAccount);

        return glAccountId;
    }

    /**
     * @param command
     * @return
     */
    private GLAccount validateParentGLAccount(GLAccountCommand command) {
        GLAccount parentGLAccount;
        parentGLAccount = glAccountRepository.findOne(command.getParentId());
        if (parentGLAccount == null) { throw new GLAccountNotFoundException(command.getParentId()); }
        // ensure parent is not a detail account
        if (parentGLAccount.isHeaderAccount()) { throw new GLAccountInvalidParentException(command.getParentId()); }
        return parentGLAccount;
    }

    /**
     * @param command
     * @param dve
     */
    private void handleGLAccountDataIntegrityIssues(final GLAccountCommand command, DataIntegrityViolationException dve) {
        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("acc_gl_code")) {
            String glCode = command.getGlCode();
            throw new GLAccountDuplicateException(glCode);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.glAccount.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource GL Account: " + realCause.getMessage());
    }

}
