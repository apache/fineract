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

import java.util.Collection;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.api.ChargesApiConstants;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeDeletedException;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeUpdatedException;
import org.apache.fineract.portfolio.charge.exception.ChargeNotFoundException;
import org.apache.fineract.portfolio.charge.serialization.ChargeDefinitionCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChargeWritePlatformServiceJpaRepositoryImpl implements ChargeWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ChargeWritePlatformServiceJpaRepositoryImpl.class);
    private final PlatformSecurityContext context;
    private final ChargeDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final ChargeRepository chargeRepository;
    private final LoanProductRepository loanProductRepository;
    private final FineractEntityAccessUtil fineractEntityAccessUtil;
    private final GLAccountRepositoryWrapper gLAccountRepository;
    private final TaxGroupRepositoryWrapper taxGroupRepository;

    @Autowired
    public ChargeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ChargeDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer, final ChargeRepository chargeRepository,
            final LoanProductRepository loanProductRepository, final RoutingDataSource dataSource,
            final FineractEntityAccessUtil fineractEntityAccessUtil, final GLAccountRepositoryWrapper glAccountRepository,
            final TaxGroupRepositoryWrapper taxGroupRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.chargeRepository = chargeRepository;
        this.loanProductRepository = loanProductRepository;
        this.fineractEntityAccessUtil = fineractEntityAccessUtil;
        this.gLAccountRepository = glAccountRepository;
        this.taxGroupRepository = taxGroupRepository;
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public CommandProcessingResult createCharge(final JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            // Retrieve linked GLAccount for Client charges (if present)
            final Long glAccountId = command.longValueOfParameterNamed(ChargesApiConstants.glAccountIdParamName);

            GLAccount glAccount = null;
            if (glAccountId != null) {
                glAccount = this.gLAccountRepository.findOneWithNotFoundDetection(glAccountId);
            }

            final Long taxGroupId = command.longValueOfParameterNamed(ChargesApiConstants.taxGroupIdParamName);
            TaxGroup taxGroup = null;
            if (taxGroupId != null) {
                taxGroup = this.taxGroupRepository.findOneWithNotFoundDetection(taxGroupId);
            }

            final Charge charge = Charge.fromJson(command, glAccount, taxGroup);
            this.chargeRepository.save(charge);

            // check if the office specific products are enabled. If yes, then
            // save this savings product against a specific office
            // i.e. this savings product is specific for this office.
            fineractEntityAccessUtil.checkConfigurationAndAddProductResrictionsForUserOffice(
                    FineractEntityAccessType.OFFICE_ACCESS_TO_CHARGES, charge.getId());

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(charge.getId()).build();
        }catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch(final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            handleDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public CommandProcessingResult updateCharge(final Long chargeId, final JsonCommand command) {

        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Charge chargeForUpdate = this.chargeRepository.findOne(chargeId);
            if (chargeForUpdate == null) { throw new ChargeNotFoundException(chargeId); }

            final Map<String, Object> changes = chargeForUpdate.update(command);

            this.fromApiJsonDeserializer.validateChargeTimeNCalculationType(chargeForUpdate.getChargeTimeType(),
                    chargeForUpdate.getChargeCalculation());

            // MIFOSX-900: Check if the Charge has been active before and now is
            // deactivated:
            if (changes.containsKey("active")) {
                // IF the key exists then it has changed (otherwise it would
                // have been filtered), so check current state:
                if (!chargeForUpdate.isActive()) {
                    // TODO: Change this function to only check the mappings!!!
                    final Boolean isChargeExistWithLoans = isAnyLoanProductsAssociateWithThisCharge(chargeId);
                    final Boolean isChargeExistWithSavings = isAnySavingsProductsAssociateWithThisCharge(chargeId);

                    if (isChargeExistWithLoans || isChargeExistWithSavings) { throw new ChargeCannotBeUpdatedException(
                            "error.msg.charge.cannot.be.updated.it.is.used.in.loan", "This charge cannot be updated, it is used in loan"); }
                }
            } else if ((changes.containsKey("feeFrequency") || changes.containsKey("feeInterval")) && chargeForUpdate.isLoanCharge()) {
                final Boolean isChargeExistWithLoans = isAnyLoanProductsAssociateWithThisCharge(chargeId);
                if (isChargeExistWithLoans) { throw new ChargeCannotBeUpdatedException(
                        "error.msg.charge.frequency.cannot.be.updated.it.is.used.in.loan",
                        "This charge frequency cannot be updated, it is used in loan"); }
            }

            // Has account Id been changed ?
            if (changes.containsKey(ChargesApiConstants.glAccountIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(ChargesApiConstants.glAccountIdParamName);
                GLAccount newIncomeAccount = null;
                if (newValue != null) {
                    newIncomeAccount = this.gLAccountRepository.findOneWithNotFoundDetection(newValue);
                }
                chargeForUpdate.setAccount(newIncomeAccount);
            }

            if (changes.containsKey(ChargesApiConstants.taxGroupIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(ChargesApiConstants.taxGroupIdParamName);
                TaxGroup taxGroup = null;
                if (newValue != null) {
                    taxGroup = this.taxGroupRepository.findOneWithNotFoundDetection(newValue);
                }
                chargeForUpdate.setTaxGroup(taxGroup);
            }

            if (!changes.isEmpty()) {
                this.chargeRepository.save(chargeForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(chargeId).with(changes).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch(final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            handleDataIntegrityIssues(command, throwable, dve);
         	return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public CommandProcessingResult deleteCharge(final Long chargeId) {

        final Charge chargeForDelete = this.chargeRepository.findOne(chargeId);
        if (chargeForDelete == null || chargeForDelete.isDeleted()) { throw new ChargeNotFoundException(chargeId); }

        final Collection<LoanProduct> loanProducts = this.loanProductRepository.retrieveLoanProductsByChargeId(chargeId);
        final Boolean isChargeExistWithLoans = isAnyLoansAssociateWithThisCharge(chargeId);
        final Boolean isChargeExistWithSavings = isAnySavingsAssociateWithThisCharge(chargeId);

        // TODO: Change error messages around:
        if (!loanProducts.isEmpty() || isChargeExistWithLoans || isChargeExistWithSavings) { throw new ChargeCannotBeDeletedException(
                "error.msg.charge.cannot.be.deleted.it.is.already.used.in.loan",
                "This charge cannot be deleted, it is already used in loan"); }

        chargeForDelete.delete();

        this.chargeRepository.save(chargeForDelete);

        return new CommandProcessingResultBuilder().withEntityId(chargeForDelete.getId()).build();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("name")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.charge.duplicate.name", "Charge with name `" + name + "` already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.charge.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

    private boolean isAnyLoansAssociateWithThisCharge(final Long chargeId) {

        final String sql = "select if((exists (select 1 from m_loan_charge lc where lc.charge_id = ? and lc.is_active = 1)) = 1, 'true', 'false')";
        final String isLoansUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return new Boolean(isLoansUsingCharge);
    }

    private boolean isAnySavingsAssociateWithThisCharge(final Long chargeId) {

        final String sql = "select if((exists (select 1 from m_savings_account_charge sc where sc.charge_id = ? and sc.is_active = 1)) = 1, 'true', 'false')";
        final String isSavingsUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return new Boolean(isSavingsUsingCharge);
    }

    private boolean isAnyLoanProductsAssociateWithThisCharge(final Long chargeId) {

        final String sql = "select if((exists (select 1 from m_product_loan_charge lc where lc.charge_id = ?)) = 1, 'true', 'false')";
        final String isLoansUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return new Boolean(isLoansUsingCharge);
    }

    private boolean isAnySavingsProductsAssociateWithThisCharge(final Long chargeId) {

        final String sql = "select if((exists (select 1 from m_savings_product_charge sc where sc.charge_id = ?)) = 1, 'true', 'false')";
        final String isSavingsUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return new Boolean(isSavingsUsingCharge);
    }
}
