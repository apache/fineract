/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.service;

import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.common.AccountingConstants;
import org.mifosplatform.accounting.glaccount.api.GLAccountJsonInputParams;
import org.mifosplatform.accounting.glaccount.command.GLAccountCommand;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.glaccount.domain.GLAccountRepository;
import org.mifosplatform.accounting.glaccount.domain.GLAccountType;
import org.mifosplatform.accounting.glaccount.exception.GLAccountDuplicateException;
import org.mifosplatform.accounting.glaccount.exception.GLAccountInvalidDeleteException;
import org.mifosplatform.accounting.glaccount.exception.GLAccountInvalidDeleteException.GL_ACCOUNT_INVALID_DELETE_REASON;
import org.mifosplatform.accounting.glaccount.exception.GLAccountInvalidParentException;
import org.mifosplatform.accounting.glaccount.exception.GLAccountInvalidUpdateException;
import org.mifosplatform.accounting.glaccount.exception.GLAccountInvalidUpdateException.GL_ACCOUNT_INVALID_UPDATE_REASON;
import org.mifosplatform.accounting.glaccount.exception.GLAccountNotFoundException;
import org.mifosplatform.accounting.glaccount.exception.InvalidParentGLAccountHeadException;
import org.mifosplatform.accounting.glaccount.serialization.GLAccountCommandFromApiJsonDeserializer;
import org.mifosplatform.accounting.journalentry.domain.JournalEntry;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryRepository;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
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
    private final JournalEntryRepository glJournalEntryRepository;
    private final GLAccountCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;

    @Autowired
    public GLAccountWritePlatformServiceJpaRepositoryImpl(final GLAccountRepository glAccountRepository,
            final JournalEntryRepository glJournalEntryRepository, final GLAccountCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final CodeValueRepositoryWrapper codeValueRepositoryWrapper) {
        this.glAccountRepository = glAccountRepository;
        this.glJournalEntryRepository = glJournalEntryRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
    }

    @Transactional
    @Override
    public CommandProcessingResult createGLAccount(final JsonCommand command) {
        try {
            final GLAccountCommand accountCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            accountCommand.validateForCreate();

            // check parent is valid
            final Long parentId = command.longValueOfParameterNamed(GLAccountJsonInputParams.PARENT_ID.getValue());
            GLAccount parentGLAccount = null;
            if (parentId != null) {
                parentGLAccount = validateParentGLAccount(parentId);
            }

            CodeValue glAccountTagType = null;
            final Long tagId = command.longValueOfParameterNamed(GLAccountJsonInputParams.TAGID.getValue());
            final Long type = command.longValueOfParameterNamed(GLAccountJsonInputParams.TYPE.getValue());
            final GLAccountType accountType = GLAccountType.fromInt(type.intValue());

            if (tagId != null) {
                glAccountTagType = retrieveTagId(tagId, accountType);
            }

            final GLAccount glAccount = GLAccount.fromJson(parentGLAccount, command, glAccountTagType);

            this.glAccountRepository.saveAndFlush(glAccount);

            glAccount.generateHierarchy();

            this.glAccountRepository.save(glAccount);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(glAccount.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleGLAccountDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateGLAccount(final Long glAccountId, final JsonCommand command) {
        try {
            final GLAccountCommand accountCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            accountCommand.validateForUpdate();

            final Long parentId = command.longValueOfParameterNamed(GLAccountJsonInputParams.PARENT_ID.getValue());
            if (glAccountId.equals(parentId)) { throw new InvalidParentGLAccountHeadException(glAccountId, parentId); }
            // is the glAccount valid
            final GLAccount glAccount = this.glAccountRepository.findOne(glAccountId);
            if (glAccount == null) { throw new GLAccountNotFoundException(glAccountId); }

            final Map<String, Object> changesOnly = glAccount.update(command);

            // is the new parent valid
            if (changesOnly.containsKey(GLAccountJsonInputParams.PARENT_ID.getValue())) {
                final GLAccount parentAccount = validateParentGLAccount(parentId);
                glAccount.updateParentAccount(parentAccount);
            }

            if (changesOnly.containsKey(GLAccountJsonInputParams.TAGID.getValue())) {
                final Long tagIdLongValue = command.longValueOfParameterNamed(GLAccountJsonInputParams.TAGID.getValue());
                final GLAccountType accountType = GLAccountType.fromInt(glAccount.getType());
                CodeValue tagID = null;
                if (tagIdLongValue != null) {
                    tagID = retrieveTagId(tagIdLongValue, accountType);
                }
                glAccount.updateTagId(tagID);
            }

            /**
             * a detail account cannot be changed to a header account if
             * transactions are already logged against it
             **/
            if (changesOnly.containsKey(GLAccountJsonInputParams.USAGE.getValue())) {
                if (glAccount.isHeaderAccount()) {
                    final List<JournalEntry> journalEntriesForAccount = this.glJournalEntryRepository
                            .findFirstJournalEntryForAccount(glAccountId);
                    if (journalEntriesForAccount.size() > 0) { throw new GLAccountInvalidUpdateException(
                            GL_ACCOUNT_INVALID_UPDATE_REASON.TRANSANCTIONS_LOGGED, glAccountId); }
                }
            }

            if (!changesOnly.isEmpty()) {
                this.glAccountRepository.saveAndFlush(glAccount);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(glAccount.getId())
                    .with(changesOnly).build();
        } catch (final DataIntegrityViolationException dve) {
            handleGLAccountDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteGLAccount(final Long glAccountId) {
        final GLAccount glAccount = this.glAccountRepository.findOne(glAccountId);

        if (glAccount == null) { throw new GLAccountNotFoundException(glAccountId); }

        // validate this isn't a header account that has children
        if (glAccount.isHeaderAccount() && glAccount.getChildren().size() > 0) { throw new GLAccountInvalidDeleteException(
                GL_ACCOUNT_INVALID_DELETE_REASON.HAS_CHILDREN, glAccountId); }

        // does this account have transactions logged against it
        final List<JournalEntry> journalEntriesForAccount = this.glJournalEntryRepository.findFirstJournalEntryForAccount(glAccountId);
        if (journalEntriesForAccount.size() > 0) { throw new GLAccountInvalidDeleteException(
                GL_ACCOUNT_INVALID_DELETE_REASON.TRANSANCTIONS_LOGGED, glAccountId); }
        this.glAccountRepository.delete(glAccount);

        return new CommandProcessingResultBuilder().withEntityId(glAccountId).build();
    }

    /**
     * @param command
     * @return
     */
    private GLAccount validateParentGLAccount(final Long parentAccountId) {
        GLAccount parentGLAccount = null;
        if (parentAccountId != null) {
            parentGLAccount = this.glAccountRepository.findOne(parentAccountId);
            if (parentGLAccount == null) { throw new GLAccountNotFoundException(parentAccountId); }
            // ensure parent is not a detail account
            if (parentGLAccount.isDetailAccount()) { throw new GLAccountInvalidParentException(parentAccountId); }
        }
        return parentGLAccount;
    }

    /**
     * @param command
     * @param dve
     */
    private void handleGLAccountDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("acc_gl_code")) {
            final String glCode = command.stringValueOfParameterNamed(GLAccountJsonInputParams.GL_CODE.getValue());
            throw new GLAccountDuplicateException(glCode);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.glAccount.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource GL Account: " + realCause.getMessage());
    }

    private CodeValue retrieveTagId(final Long tagId, final GLAccountType accountType) {
        CodeValue glAccountTagType = null;
        if (accountType.isAssetType()) {
            glAccountTagType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(
                    AccountingConstants.ASSESTS_TAG_OPTION_CODE_NAME, tagId);
        } else if (accountType.isLiabilityType()) {
            glAccountTagType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(
                    AccountingConstants.LIABILITIES_TAG_OPTION_CODE_NAME, tagId);
        } else if (accountType.isEquityType()) {
            glAccountTagType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(
                    AccountingConstants.EQUITY_TAG_OPTION_CODE_NAME, tagId);
        } else if (accountType.isIncomeType()) {
            glAccountTagType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(
                    AccountingConstants.INCOME_TAG_OPTION_CODE_NAME, tagId);
        } else if (accountType.isExpenseType()) {
            glAccountTagType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(
                    AccountingConstants.EXPENSES_TAG_OPTION_CODE_NAME, tagId);
        }
        return glAccountTagType;
    }

}
