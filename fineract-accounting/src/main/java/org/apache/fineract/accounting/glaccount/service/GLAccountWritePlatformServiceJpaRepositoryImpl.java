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
package org.apache.fineract.accounting.glaccount.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.accounting.glaccount.api.GLAccountJsonInputParams;
import org.apache.fineract.accounting.glaccount.command.GLAccountCommand;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.exception.GLAccountDisableException;
import org.apache.fineract.accounting.glaccount.exception.GLAccountDuplicateException;
import org.apache.fineract.accounting.glaccount.exception.GLAccountInvalidDeleteException;
import org.apache.fineract.accounting.glaccount.exception.GLAccountInvalidDeleteException.GlAccountInvalidDeleteReason;
import org.apache.fineract.accounting.glaccount.exception.GLAccountInvalidParentException;
import org.apache.fineract.accounting.glaccount.exception.GLAccountInvalidUpdateException;
import org.apache.fineract.accounting.glaccount.exception.GLAccountInvalidUpdateException.GlAccountInvalidUpdateReason;
import org.apache.fineract.accounting.glaccount.exception.GLAccountNotFoundException;
import org.apache.fineract.accounting.glaccount.exception.InvalidParentGLAccountHeadException;
import org.apache.fineract.accounting.glaccount.serialization.GLAccountCommandFromApiJsonDeserializer;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GLAccountWritePlatformServiceJpaRepositoryImpl implements GLAccountWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(GLAccountWritePlatformServiceJpaRepositoryImpl.class);

    private final GLAccountRepository glAccountRepository;
    private final JournalEntryRepository glJournalEntryRepository;
    private final GLAccountCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final JdbcTemplate jdbcTemplate;

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

            this.glAccountRepository.saveAndFlush(glAccount);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(glAccount.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleGLAccountDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateGLAccount(final Long glAccountId, final JsonCommand command) {
        try {
            final GLAccountCommand accountCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            accountCommand.validateForUpdate();
            if (command.hasParameter(GLAccountJsonInputParams.DISABLED.getValue())
                    && command.booleanPrimitiveValueOfParameterNamed(GLAccountJsonInputParams.DISABLED.getValue())) {
                validateForAttachedProduct(glAccountId);
            }
            final Long parentId = command.longValueOfParameterNamed(GLAccountJsonInputParams.PARENT_ID.getValue());
            if (glAccountId.equals(parentId)) {
                throw new InvalidParentGLAccountHeadException(glAccountId, parentId);
            }
            // is the glAccount valid
            final GLAccount glAccount = this.glAccountRepository.findById(glAccountId)
                    .orElseThrow(() -> new GLAccountNotFoundException(glAccountId));

            final Map<String, Object> changesOnly = glAccount.update(command);

            // is the new parent valid
            if (changesOnly.containsKey(GLAccountJsonInputParams.PARENT_ID.getValue())) {
                final GLAccount parentAccount = validateParentGLAccount(parentId);
                glAccount.setParent(parentAccount);
                glAccount.generateHierarchy();
            }

            if (changesOnly.containsKey(GLAccountJsonInputParams.TAGID.getValue())) {
                final Long tagIdLongValue = command.longValueOfParameterNamed(GLAccountJsonInputParams.TAGID.getValue());
                final GLAccountType accountType = GLAccountType.fromInt(glAccount.getType());
                CodeValue tagID = null;
                if (tagIdLongValue != null) {
                    tagID = retrieveTagId(tagIdLongValue, accountType);
                }
                glAccount.setTagId(tagID);
            }

            /**
             * a detail account cannot be changed to a header account if transactions are already logged against it
             **/
            if (changesOnly.containsKey(GLAccountJsonInputParams.USAGE.getValue())) {
                if (glAccount.isHeaderAccount()) {
                    final boolean journalEntriesForAccountExist = this.glJournalEntryRepository
                            .exists((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("glAccountId"), glAccountId));
                    if (journalEntriesForAccountExist) {
                        throw new GLAccountInvalidUpdateException(GlAccountInvalidUpdateReason.TRANSANCTIONS_LOGGED, glAccountId);
                    }
                }
            }

            if (!changesOnly.isEmpty()) {
                this.glAccountRepository.saveAndFlush(glAccount);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(glAccount.getId()).with(changesOnly)
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleGLAccountDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    private void validateForAttachedProduct(Long glAccountId) {
        String sql = "select count(*) from acc_product_mapping acc where acc.gl_account_id = ?";
        try {
            Integer count = this.jdbcTemplate.queryForObject(sql, Integer.class, glAccountId);
            if (count == null || count > 0) {
                throw new GLAccountDisableException();
            }
        } catch (EmptyResultDataAccessException e) {
            LOG.error("Problem encountered in validateForAttachedProduct()", e);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteGLAccount(final Long glAccountId) {
        final GLAccount glAccount = this.glAccountRepository.findById(glAccountId)
                .orElseThrow(() -> new GLAccountNotFoundException(glAccountId));

        // validate this isn't a header account that has children
        if (glAccount.isHeaderAccount() && glAccount.getChildren().size() > 0) {
            throw new GLAccountInvalidDeleteException(GlAccountInvalidDeleteReason.HAS_CHILDREN, glAccountId);
        }

        // does this account have transactions logged against it
        final boolean journalEntriesForAccountExist = this.glJournalEntryRepository
                .exists((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("glAccountId"), glAccountId));
        if (journalEntriesForAccountExist) {
            throw new GLAccountInvalidDeleteException(GlAccountInvalidDeleteReason.TRANSANCTIONS_LOGGED, glAccountId);
        }
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
            parentGLAccount = this.glAccountRepository.findById(parentAccountId)
                    .orElseThrow(() -> new GLAccountNotFoundException(parentAccountId));
            // ensure parent is not a detail account
            if (parentGLAccount.isDetailAccount()) {
                throw new GLAccountInvalidParentException(parentAccountId);
            }
        }
        return parentGLAccount;
    }

    /**
     * @param command
     * @param dve
     */
    private void handleGLAccountDataIntegrityIssues(final JsonCommand command, final Throwable realCause,
            final NonTransientDataAccessException dve) {
        if (realCause.getMessage().contains("acc_gl_code")) {
            final String glCode = command.stringValueOfParameterNamed(GLAccountJsonInputParams.GL_CODE.getValue());
            throw new GLAccountDuplicateException(glCode);
        }

        LOG.error("Error occured.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.glAccount.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource GL Account: " + realCause.getMessage());
    }

    private CodeValue retrieveTagId(final Long tagId, final GLAccountType accountType) {
        CodeValue glAccountTagType = null;
        if (accountType.isAssetType()) {
            glAccountTagType = this.codeValueRepositoryWrapper
                    .findOneByCodeNameAndIdWithNotFoundDetection(AccountingConstants.ASSESTS_TAG_OPTION_CODE_NAME, tagId);
        } else if (accountType.isLiabilityType()) {
            glAccountTagType = this.codeValueRepositoryWrapper
                    .findOneByCodeNameAndIdWithNotFoundDetection(AccountingConstants.LIABILITIES_TAG_OPTION_CODE_NAME, tagId);
        } else if (accountType.isEquityType()) {
            glAccountTagType = this.codeValueRepositoryWrapper
                    .findOneByCodeNameAndIdWithNotFoundDetection(AccountingConstants.EQUITY_TAG_OPTION_CODE_NAME, tagId);
        } else if (accountType.isIncomeType()) {
            glAccountTagType = this.codeValueRepositoryWrapper
                    .findOneByCodeNameAndIdWithNotFoundDetection(AccountingConstants.INCOME_TAG_OPTION_CODE_NAME, tagId);
        } else if (accountType.isExpenseType()) {
            glAccountTagType = this.codeValueRepositoryWrapper
                    .findOneByCodeNameAndIdWithNotFoundDetection(AccountingConstants.EXPENSES_TAG_OPTION_CODE_NAME, tagId);
        }
        return glAccountTagType;
    }

}
