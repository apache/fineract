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
package org.apache.fineract.portfolio.charge.service;

import jakarta.persistence.PersistenceException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.portfolio.charge.data.ChargeChangeDto;
import org.apache.fineract.portfolio.charge.data.CreateChargeRequest;
import org.apache.fineract.portfolio.charge.data.CreateChargeResponse;
import org.apache.fineract.portfolio.charge.data.DeleteChargeResponse;
import org.apache.fineract.portfolio.charge.data.UpdateChargeRequest;
import org.apache.fineract.portfolio.charge.data.UpdateChargeResponse;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeDeletedException;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeUpdatedException;
import org.apache.fineract.portfolio.charge.exception.ChargeNotFoundException;
import org.apache.fineract.portfolio.charge.mapper.ChargeMapper;
import org.apache.fineract.portfolio.charge.util.ChargeChangeEntityUtil;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class ChargeWritePlatformServiceJpaRepositoryImpl implements ChargeWritePlatformService {

    private final ChargeRepository chargeRepository;
    private final LoanProductRepository loanProductRepository;
    private final JdbcTemplate jdbcTemplate;
    private final FineractEntityAccessUtil fineractEntityAccessUtil;
    private final GLAccountRepositoryWrapper glAccountRepository;
    private final TaxGroupRepositoryWrapper taxGroupRepository;
    private final PaymentTypeRepositoryWrapper paymentTyperepositoryWrapper;
    private final ChargeMapper chargeMapper;

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public CreateChargeResponse createCharge(CreateChargeRequest request) {
        try {
            final Charge charge = chargeMapper.map(request);
            this.chargeRepository.saveAndFlush(charge);

            // check if the office specific products are enabled. If yes, then
            // save this savings product against a specific office
            // i.e. this savings product is specific for this office.
            fineractEntityAccessUtil.checkConfigurationAndAddProductResrictionsForUserOffice(
                    FineractEntityAccessType.OFFICE_ACCESS_TO_CHARGES, charge.getId());

            return new CreateChargeResponse(charge.getId());
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(request.getName(), dve.getMostSpecificCause(), dve);
            return new CreateChargeResponse();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(request.getName(), throwable, dve);
            return new CreateChargeResponse();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public UpdateChargeResponse updateCharge(UpdateChargeRequest request) {
        final Long chargeId = request.getId();
        try {

            final Charge chargeForUpdate = this.chargeRepository.findById(chargeId)
                    .orElseThrow(() -> new ChargeNotFoundException(chargeId));

            final ChargeChangeDto chargeChangeDto = ChargeChangeEntityUtil.prepareToUpdateEntity(chargeForUpdate, request);

            // MIFOSX-900: Check if the Charge has been active before and now is
            // deactivated:

            if (!chargeForUpdate.isActive()) {
                // TODO: Change this function to only check the mappings!!!
                final Boolean isChargeExistWithLoans = isAnyLoanProductsAssociateWithThisCharge(chargeId);
                final Boolean isChargeExistWithSavings = isAnySavingsProductsAssociateWithThisCharge(chargeId);

                if (isChargeExistWithLoans || isChargeExistWithSavings) {
                    throw new ChargeCannotBeUpdatedException("error.msg.charge.cannot.be.updated.it.is.used.in.loan",
                            "This charge cannot be updated, it is used in loan");
                }
            } else if (chargeForUpdate.isLoanCharge() && isAnyLoanProductsAssociateWithThisCharge(chargeId)) {
                throw new ChargeCannotBeUpdatedException("error.msg.charge.frequency.cannot.be.updated.it.is.used.in.loan",
                        "This charge frequency cannot be updated, it is used in loan");
            }

            // Has account Id been changed ?
            if (request.getIncomeAccountId() != null) {
                GLAccount newIncomeAccount = this.glAccountRepository.findOneWithNotFoundDetection(request.getIncomeAccountId());
                chargeForUpdate.setAccount(newIncomeAccount);
            }

            final Long paymentTypeId = request.getPaymentTypeId();
            if (paymentTypeId != null) {
                final PaymentType paymentType = this.paymentTyperepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
                chargeForUpdate.setPaymentType(paymentType);
            }

            Long taxGroupId = request.getTaxGroupId();
            if (taxGroupId != null) {
                final TaxGroup taxGroup = this.taxGroupRepository.findOneWithNotFoundDetection(taxGroupId);
                chargeForUpdate.setTaxGroup(taxGroup);
            }

            this.chargeRepository.save(chargeForUpdate);

            return UpdateChargeResponse.builder().resourceId(chargeId).changes(chargeChangeDto).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(request.getName(), dve.getMostSpecificCause(), dve);
            return new UpdateChargeResponse();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(request.getName(), throwable, dve);
            return new UpdateChargeResponse();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public DeleteChargeResponse deleteCharge(final Long chargeId) {
        final Charge chargeForDelete = this.chargeRepository.findById(chargeId).orElseThrow(() -> new ChargeNotFoundException(chargeId));
        if (chargeForDelete.isDeleted()) {
            throw new ChargeNotFoundException(chargeId);
        }

        final Collection<LoanProduct> loanProducts = this.loanProductRepository.retrieveLoanProductsByChargeId(chargeId);
        final boolean isChargeExistWithLoans = isAnyLoansAssociateWithThisCharge(chargeId);
        final boolean isChargeExistWithSavings = isAnySavingsAssociateWithThisCharge(chargeId);

        // TODO: Change error messages around:
        if (!loanProducts.isEmpty() || isChargeExistWithLoans || isChargeExistWithSavings) {
            throw new ChargeCannotBeDeletedException("error.msg.charge.cannot.be.deleted.it.is.already.used.in.loan",
                    "This charge cannot be deleted, it is already used in loan");
        }

        chargeForDelete.delete();

        this.chargeRepository.save(chargeForDelete);

        return new DeleteChargeResponse(chargeForDelete.getId());
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(String name, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("name")) {
            throw new PlatformDataIntegrityException("error.msg.charge.duplicate.name", "Charge with name `" + name + "` already exists",
                    "name", name);
        }

        log.error("Error occured.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.charge.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

    private boolean isAnyLoansAssociateWithThisCharge(final Long chargeId) {
        final String sql = "select (CASE WHEN exists (select 1 from m_loan_charge lc where lc.charge_id = ? and lc.is_active = true) THEN 'true' ELSE 'false' END)";
        final String isLoansUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return Boolean.parseBoolean(isLoansUsingCharge);
    }

    private boolean isAnySavingsAssociateWithThisCharge(final Long chargeId) {
        final String sql = "select (CASE WHEN exists (select 1 from m_savings_account_charge sc where sc.charge_id = ? and sc.is_active = true) THEN 'true' ELSE 'false' END)";
        final String isSavingsUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return Boolean.parseBoolean(isSavingsUsingCharge);
    }

    private boolean isAnyLoanProductsAssociateWithThisCharge(final Long chargeId) {
        final String sql = "select (CASE WHEN exists (select 1 from m_product_loan_charge lc where lc.charge_id = ?) THEN 'true' ELSE 'false' END)";
        final String isLoansUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return Boolean.parseBoolean(isLoansUsingCharge);
    }

    private boolean isAnySavingsProductsAssociateWithThisCharge(final Long chargeId) {
        final String sql = "select (CASE WHEN (exists (select 1 from m_savings_product_charge sc where sc.charge_id = ?)) = 1 THEN 'true' ELSE 'false' END)";
        final String isSavingsUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return Boolean.parseBoolean(isSavingsUsingCharge);
    }
}
