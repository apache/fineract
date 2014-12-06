/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.service;

import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityAccessType;
import org.mifosplatform.infrastructure.entityaccess.domain.MifosEntityType;
import org.mifosplatform.infrastructure.entityaccess.service.MifosEntityAccessUtil;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepository;
import org.mifosplatform.portfolio.charge.exception.ChargeCannotBeDeletedException;
import org.mifosplatform.portfolio.charge.exception.ChargeCannotBeUpdatedException;
import org.mifosplatform.portfolio.charge.exception.ChargeNotFoundException;
import org.mifosplatform.portfolio.charge.serialization.ChargeDefinitionCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
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
    private final MifosEntityAccessUtil mifosEntityAccessUtil;

    @Autowired
    public ChargeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
    		final ChargeDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final ChargeRepository chargeRepository, final LoanProductRepository loanProductRepository, final RoutingDataSource dataSource,
            final MifosEntityAccessUtil mifosEntityAccessUtil
            ) {
    	this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.chargeRepository = chargeRepository;
        this.loanProductRepository = loanProductRepository;
        this.mifosEntityAccessUtil = mifosEntityAccessUtil;
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public CommandProcessingResult createCharge(final JsonCommand command) {
        try {
        	this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Charge charge = Charge.fromJson(command);
            this.chargeRepository.save(charge);

            // check if the office specific products are enabled. If yes, then save this savings product against a specific office
            // i.e. this savings product is specific for this office.
            mifosEntityAccessUtil.checkConfigurationAndAddProductResrictionsForUserOffice(
            		MifosEntityAccessType.OFFICE_ACCESS_TO_CHARGES, 
            		MifosEntityType.CHARGE, 
            		charge.getId());

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(charge.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public CommandProcessingResult updateCharge(final Long chargeId, final JsonCommand command) {

        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Charge chargeForUpdate = this.chargeRepository.findOne(chargeId);
            if (chargeForUpdate == null) { throw new ChargeNotFoundException(chargeId); }

            final Map<String, Object> changes = chargeForUpdate.update(command);

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
            }else if((changes.containsKey("feeFrequency") || changes.containsKey("feeInterval")) && chargeForUpdate.isLoanCharge()){
                final Boolean isChargeExistWithLoans = isAnyLoanProductsAssociateWithThisCharge(chargeId);
                if (isChargeExistWithLoans) { throw new ChargeCannotBeUpdatedException(
                        "error.msg.charge.frequency.cannot.be.updated.it.is.used.in.loan", "This charge frequency cannot be updated, it is used in loan"); }
            }

            if (!changes.isEmpty()) {

                this.chargeRepository.save(chargeForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(chargeId).with(changes).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "charges", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
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
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
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

        final String sql = "select if((exists (select 1 from m_loan_charge lc where lc.charge_id = ?)) = 1, 'true', 'false')";
        final String isLoansUsingCharge = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { chargeId });
        return new Boolean(isLoansUsingCharge);
    }

    private boolean isAnySavingsAssociateWithThisCharge(final Long chargeId) {

        final String sql = "select if((exists (select 1 from m_savings_account_charge sc where sc.charge_id = ?)) = 1, 'true', 'false')";
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
