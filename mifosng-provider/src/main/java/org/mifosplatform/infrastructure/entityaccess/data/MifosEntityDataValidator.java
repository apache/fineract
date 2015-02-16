/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.entityaccess.api.MifosEntityApiResourceConstants;
import org.mifosplatform.organisation.office.domain.OfficeRepositoryWrapper;
import org.mifosplatform.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.portfolio.savings.domain.SavingsProduct;
import org.mifosplatform.portfolio.savings.domain.SavingsProductRepository;
import org.mifosplatform.portfolio.savings.exception.SavingsProductNotFoundException;
import org.mifosplatform.useradministration.domain.Role;
import org.mifosplatform.useradministration.domain.RoleRepository;
import org.mifosplatform.useradministration.exception.RoleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class MifosEntityDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final LoanProductRepository loanProductRepository;
    private final SavingsProductRepository savingsProductRepository;
    private final ChargeRepositoryWrapper chargeRepositoryWrapper;
    private final RoleRepository roleRepository;

    @Autowired
    public MifosEntityDataValidator(final FromJsonHelper fromApiJsonHelper, final OfficeRepositoryWrapper officeRepositoryWrapper,
            final LoanProductRepository loanProductRepository, final SavingsProductRepository savingsProductRepository,
            final ChargeRepositoryWrapper chargeRepositoryWrapper, final RoleRepository roleRepository) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.officeRepositoryWrapper = officeRepositoryWrapper;
        this.loanProductRepository = loanProductRepository;
        this.savingsProductRepository = savingsProductRepository;
        this.chargeRepositoryWrapper = chargeRepositoryWrapper;
        this.roleRepository = roleRepository;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                MifosEntityApiResourceConstants.CREATE_ENTITY_MAPPING_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(MifosEntityApiResourceConstants.MIFOS_ENTITY_RESOURCE_NAME);

        if (this.fromApiJsonHelper.parameterExists(MifosEntityApiResourceConstants.fromEnityType, element)) {
            final Long fromId = this.fromApiJsonHelper.extractLongNamed(MifosEntityApiResourceConstants.fromEnityType, element);
            baseDataValidator.reset().parameter(MifosEntityApiResourceConstants.fromEnityType).value(fromId).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(MifosEntityApiResourceConstants.toEntityType, element)) {
            final Long toId = this.fromApiJsonHelper.extractLongNamed(MifosEntityApiResourceConstants.toEntityType, element);
            baseDataValidator.reset().parameter(MifosEntityApiResourceConstants.toEntityType).value(toId).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(MifosEntityApiResourceConstants.startDate, element)) {
            final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(MifosEntityApiResourceConstants.startDate, element);
            baseDataValidator.reset().parameter(MifosEntityApiResourceConstants.startDate).value(startDate);
        }

        if (this.fromApiJsonHelper.parameterExists(MifosEntityApiResourceConstants.endDate, element)) {
            final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(MifosEntityApiResourceConstants.endDate, element);
            baseDataValidator.reset().parameter(MifosEntityApiResourceConstants.endDate).value(endDate);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void checkForEntity(String relId, Long fromId, Long toId) {

        switch (relId) {
            case "1":
                checkForOffice(fromId);
                checkForLoanProducts(toId);
            break;
            case "2":
                checkForOffice(fromId);
                checkForSavingsProducts(toId);
            break;
            case "3":
                checkForOffice(fromId);
                checkForCharges(toId);
            break;
            case "4":
                checkForRoles(fromId);
                checkForLoanProducts(toId);
            break;
            case "5":
                checkForRoles(fromId);
                checkForSavingsProducts(toId);
            break;

        }

    }

    public void checkForOffice(Long id) {
        this.officeRepositoryWrapper.findOneWithNotFoundDetection(id);
    }

    public void checkForLoanProducts(final Long id) {
        final LoanProduct loanProduct = this.loanProductRepository.findOne(id);
        if (loanProduct == null) { throw new LoanProductNotFoundException(id); }
    }

    public void checkForSavingsProducts(final Long id) {
        final SavingsProduct savingsProduct = this.savingsProductRepository.findOne(id);
        if (savingsProduct == null) { throw new SavingsProductNotFoundException(id); }
    }

    public void checkForCharges(final Long id) {
        this.chargeRepositoryWrapper.findOneWithNotFoundDetection(id);
    }

    public void checkForRoles(final Long id) {
        final Role role = this.roleRepository.findOne(id);
        if (role == null) { throw new RoleNotFoundException(id); }
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                MifosEntityApiResourceConstants.UPDATE_ENTITY_MAPPING_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(MifosEntityApiResourceConstants.MIFOS_ENTITY_RESOURCE_NAME);

        boolean atLeastOneParameterPassedForUpdate = false;
        if (this.fromApiJsonHelper.parameterExists(MifosEntityApiResourceConstants.fromEnityType, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String fromEnityType = this.fromApiJsonHelper.extractStringNamed(MifosEntityApiResourceConstants.fromEnityType, element);
            baseDataValidator.reset().parameter(MifosEntityApiResourceConstants.fromEnityType).value(fromEnityType);
        }

        if (this.fromApiJsonHelper.parameterExists(MifosEntityApiResourceConstants.fromEnityType, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String toEnityType = this.fromApiJsonHelper.extractStringNamed(MifosEntityApiResourceConstants.toEntityType, element);
            baseDataValidator.reset().parameter(MifosEntityApiResourceConstants.fromEnityType).value(toEnityType);
        }

        if (this.fromApiJsonHelper.parameterExists(MifosEntityApiResourceConstants.toEntityType, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(MifosEntityApiResourceConstants.startDate, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(MifosEntityApiResourceConstants.endDate, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (!atLeastOneParameterPassedForUpdate) {
            final Object forceError = null;
            baseDataValidator.reset().anyOfNotNull(forceError);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

}
