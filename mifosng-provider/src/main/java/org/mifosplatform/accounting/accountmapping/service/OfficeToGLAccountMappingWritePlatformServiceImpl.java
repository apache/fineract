package org.mifosplatform.accounting.accountmapping.service;

import java.util.HashMap;
import java.util.Map;

import org.mifosplatform.accounting.accountmapping.domain.OfficeToGLAccountMapping;
import org.mifosplatform.accounting.accountmapping.domain.OfficeToGLAccountMappingRepositoryWrapper;
import org.mifosplatform.accounting.accountmapping.exception.DuplicateOfficeToGLAccountMappingFoundException;
import org.mifosplatform.accounting.accountmapping.exception.MappingUpdateNotSupportedException;
import org.mifosplatform.accounting.accountmapping.serialization.OfficeToGLAccountMappingDataValidator;
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
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class OfficeToGLAccountMappingWritePlatformServiceImpl implements OfficeToGLAccountMappingWritePlatformService {

    private final OfficeToGLAccountMappingRepositoryWrapper accountMappingRepository;
    private final OfficeToGLAccountMappingDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final GLAccountRepositoryWrapper glAccountRepositoryWrapper;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;

    @Autowired
    public OfficeToGLAccountMappingWritePlatformServiceImpl(final OfficeToGLAccountMappingRepositoryWrapper accountMappingRepository,
            final OfficeToGLAccountMappingDataValidator fromApiJsonDeserializer, final FromJsonHelper fromApiJsonHelper,
            final GLAccountRepositoryWrapper glAccountRepositoryWrapper, final OfficeRepositoryWrapper officeRepositoryWrapper) {
        this.accountMappingRepository = accountMappingRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.glAccountRepositoryWrapper = glAccountRepositoryWrapper;
        this.officeRepositoryWrapper = officeRepositoryWrapper;
    }

    @Override
    public CommandProcessingResult createGLAccountMapping(JsonCommand command) {

        this.fromApiJsonDeserializer.validateForCreate(command.json());
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Long officeId = this.fromApiJsonHelper.extractLongNamed("officeId", element);
        final Office office = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
        OfficeToGLAccountMapping accountMapping = createLiabilityTrasferSuspenceMapping(office, element);
        checkForExistingMapping(accountMapping);
        this.accountMappingRepository.save(accountMapping);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(accountMapping.getId()) //
                .build();
    }

    private void checkForExistingMapping(final OfficeToGLAccountMapping accountMapping) {
        OfficeToGLAccountMapping existingMapping = this.accountMappingRepository.findByOfficeAndFinancialAccountType(accountMapping
                .office().getId(), accountMapping.getFinancialAccountType());
        if (existingMapping != null && !existingMapping.getId().equals(accountMapping.getId())) { throw new DuplicateOfficeToGLAccountMappingFoundException(
                accountMapping.office().getId(), accountMapping.getFinancialAccountType()); }
    }

    @Override
    public CommandProcessingResult updateGLAccountMapping(Long mappingId, JsonCommand command) {
        this.fromApiJsonDeserializer.validateForUpdate(command.json());
        final OfficeToGLAccountMapping officeToGLAccountMapping = this.accountMappingRepository.findOneWithNotFoundDetection(mappingId);
        Map<String, Object> changes = findChanges(command, officeToGLAccountMapping);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());

        if (changes.containsKey(ORGANIZATION_ACCOUNTING_PARAMS.LIABILITY_TRANSFER_SUSPENSE.getValue())) {
            final GLAccount glAccount = fetchGLAccount(element, ORGANIZATION_ACCOUNTING_PARAMS.LIABILITY_TRANSFER_SUSPENSE.getValue(),
                    GLAccountType.LIABILITY);
            officeToGLAccountMapping.updateGlAccount(glAccount);
        }

        if (!changes.isEmpty()) {
            checkForExistingMapping(officeToGLAccountMapping);
            this.accountMappingRepository.save(officeToGLAccountMapping);
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(mappingId) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult deleteGLAccountMapping(Long mappingId, JsonCommand command) {
        final OfficeToGLAccountMapping officeToGLAccountMapping = this.accountMappingRepository.findOneWithNotFoundDetection(mappingId);
        this.accountMappingRepository.delete(officeToGLAccountMapping);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(mappingId) //
                .build();
    }

    private OfficeToGLAccountMapping createLiabilityTrasferSuspenceMapping(final Office office, final JsonElement element) {
        final GLAccount glAccount = fetchGLAccount(element, ORGANIZATION_ACCOUNTING_PARAMS.LIABILITY_TRANSFER_SUSPENSE.getValue(),
                GLAccountType.LIABILITY);
        final Integer financialAccountType = ORGANIZATION_ACCOUNTS.LIABILITY_TRANSFER_SUSPENSE.getValue();
        return OfficeToGLAccountMapping.createNew(glAccount, office, financialAccountType);
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

    public Map<String, Object> findChanges(JsonCommand command, OfficeToGLAccountMapping accountMapping) {

        Map<String, Object> changes = new HashMap<String, Object>();

        final String officeParamName = "officeId";
        Long existingOfficeId = accountMapping.office().getId();
        if (command.isChangeInLongParameterNamed(officeParamName, existingOfficeId)) {
            final Long newValue = command.longValueOfParameterNamed(officeParamName);
            String defaultMsg = "Office updating not allowed ";
            throw new MappingUpdateNotSupportedException("officeId", defaultMsg, newValue,existingOfficeId,accountMapping.getId());
        }

        Long existingGLAccountId = accountMapping.glAccount().getId();
        if (command
                .isChangeInLongParameterNamed(ORGANIZATION_ACCOUNTING_PARAMS.LIABILITY_TRANSFER_SUSPENSE.getValue(), existingGLAccountId)) {
            final Long newValue = command.longValueOfParameterNamed(ORGANIZATION_ACCOUNTING_PARAMS.LIABILITY_TRANSFER_SUSPENSE.getValue());
            changes.put(ORGANIZATION_ACCOUNTING_PARAMS.LIABILITY_TRANSFER_SUSPENSE.getValue(), newValue);
        }
        return changes;
    }

}
