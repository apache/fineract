package org.mifosplatform.accounting.accountmapping.service;

import java.util.HashMap;
import java.util.Map;

import org.mifosplatform.accounting.accountmapping.domain.FinancialActivityAccount;
import org.mifosplatform.accounting.accountmapping.domain.FinancialActivityAccountRepositoryWrapper;
import org.mifosplatform.accounting.accountmapping.exception.DuplicateFinancialActivityAccountFoundException;
import org.mifosplatform.accounting.accountmapping.serialization.FinancialActivityAccountDataValidator;
import org.mifosplatform.accounting.common.AccountingConstants.ORGANIZATION_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.common.AccountingConstants.ORGANIZATION_ACCOUNTS;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.mifosplatform.accounting.glaccount.domain.GLAccountType;
import org.mifosplatform.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingInvalidException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class FinancialActivityAccountWritePlatformServiceImpl implements FinancialActivityAccountWritePlatformService {

    private final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository;
    private final FinancialActivityAccountDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final GLAccountRepositoryWrapper glAccountRepositoryWrapper;

    @Autowired
    public FinancialActivityAccountWritePlatformServiceImpl(
            final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository,
            final FinancialActivityAccountDataValidator fromApiJsonDeserializer, final FromJsonHelper fromApiJsonHelper,
            final GLAccountRepositoryWrapper glAccountRepositoryWrapper) {
        this.financialActivityAccountRepository = financialActivityAccountRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.glAccountRepositoryWrapper = glAccountRepositoryWrapper;
    }

    @Override
    public CommandProcessingResult createGLAccountActivityMapping(JsonCommand command) {

        this.fromApiJsonDeserializer.validateForCreate(command.json());
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        FinancialActivityAccount financialActivityAccount = createLiabilityTrasferSuspenceMapping(element);
        checkForExistingMapping(financialActivityAccount);
        this.financialActivityAccountRepository.save(financialActivityAccount);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(financialActivityAccount.getId()) //
                .build();
    }

    private void checkForExistingMapping(final FinancialActivityAccount financialActivityAccount) {
        FinancialActivityAccount existingMapping = this.financialActivityAccountRepository
                .findByFinancialActivityType(financialActivityAccount.getFinancialActivityType());
        if (existingMapping != null) { throw new DuplicateFinancialActivityAccountFoundException(
                financialActivityAccount.getFinancialActivityType()); }
    }

    @Override
    public CommandProcessingResult updateGLAccountActivityMapping(Long mappingId, JsonCommand command) {
        this.fromApiJsonDeserializer.validateForUpdate(command.json());
        final FinancialActivityAccount financialActivityAccount = this.financialActivityAccountRepository
                .findOneWithNotFoundDetection(mappingId);
        Map<String, Object> changes = findChanges(command, financialActivityAccount);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());

        if (changes.containsKey(ORGANIZATION_ACCOUNTING_PARAMS.LIABILITY_TRANSFER_SUSPENSE.getValue())) {
            final GLAccount glAccount = fetchGLAccount(element, ORGANIZATION_ACCOUNTING_PARAMS.LIABILITY_TRANSFER_SUSPENSE.getValue(),
                    GLAccountType.LIABILITY);
            financialActivityAccount.updateGlAccount(glAccount);
            this.financialActivityAccountRepository.save(financialActivityAccount);
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(mappingId) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult deleteGLAccountActivityMapping(Long mappingId, JsonCommand command) {
        final FinancialActivityAccount officeToGLAccountMapping = this.financialActivityAccountRepository
                .findOneWithNotFoundDetection(mappingId);
        this.financialActivityAccountRepository.delete(officeToGLAccountMapping);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(mappingId) //
                .build();
    }

    private FinancialActivityAccount createLiabilityTrasferSuspenceMapping(final JsonElement element) {
        final GLAccount glAccount = fetchGLAccount(element, ORGANIZATION_ACCOUNTING_PARAMS.LIABILITY_TRANSFER_SUSPENSE.getValue(),
                GLAccountType.LIABILITY);
        final Integer financialAccountType = ORGANIZATION_ACCOUNTS.LIABILITY_TRANSFER_SUSPENSE.getValue();
        return FinancialActivityAccount.createNew(glAccount, financialAccountType);
    }

    private GLAccount fetchGLAccount(final JsonElement element, final String paramName, final GLAccountType expectedAccountType) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);
        final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);
        return glAccount;
    }

    /**
     * Fetches account with a particular Id and throws and Exception it is not
     * of the expected Account Category ('ASSET','liability' etc)
     * 
     * @param paramName
     * @param expectedAccountType
     * @param accountId
     * @return
     */
    public GLAccount getAccountByIdAndType(final String paramName, final GLAccountType expectedAccountType, final Long accountId) {
        final GLAccount glAccount = this.glAccountRepositoryWrapper.findOneWithNotFoundDetection(accountId);
        // validate account is of the expected Type
        if (glAccount.getType().intValue() != expectedAccountType.getValue()) { throw new ProductToGLAccountMappingInvalidException(
                paramName, glAccount.getName(), accountId, GLAccountType.fromInt(glAccount.getType()).toString(),
                expectedAccountType.toString()); }
        return glAccount;
    }

    public Map<String, Object> findChanges(JsonCommand command, FinancialActivityAccount financialActivityAccount) {

        Map<String, Object> changes = new HashMap<String, Object>();

        Long existingGLAccountId = financialActivityAccount.glAccount().getId();
        if (command
                .isChangeInLongParameterNamed(ORGANIZATION_ACCOUNTING_PARAMS.LIABILITY_TRANSFER_SUSPENSE.getValue(), existingGLAccountId)) {
            final Long newValue = command.longValueOfParameterNamed(ORGANIZATION_ACCOUNTING_PARAMS.LIABILITY_TRANSFER_SUSPENSE.getValue());
            changes.put(ORGANIZATION_ACCOUNTING_PARAMS.LIABILITY_TRANSFER_SUSPENSE.getValue(), newValue);
        }
        return changes;
    }

}
