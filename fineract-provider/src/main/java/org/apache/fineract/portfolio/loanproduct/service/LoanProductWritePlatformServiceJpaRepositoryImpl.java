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
package org.apache.fineract.portfolio.loanproduct.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BUSINESS_ENTITY;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.apache.fineract.portfolio.common.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRateRepositoryWrapper;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.exception.FundNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionProcessingStrategyRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionProcessingStrategyNotFoundException;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BUSINESS_ENTITY;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.apache.fineract.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductCannotBeModifiedDueToNonClosedLoansException;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductDateException;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class LoanProductWritePlatformServiceJpaRepositoryImpl implements LoanProductWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(LoanProductWritePlatformServiceJpaRepositoryImpl.class);
    private final PlatformSecurityContext context;
    private final LoanProductDataValidator fromApiJsonDeserializer;
    private final LoanProductRepository loanProductRepository;
    private final AprCalculator aprCalculator;
    private final FundRepository fundRepository;
    private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;
    private final ChargeRepositoryWrapper chargeRepository;
    private final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService;
    private final FineractEntityAccessUtil fineractEntityAccessUtil;
    private final FloatingRateRepositoryWrapper floatingRateRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Autowired
    public LoanProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final LoanProductDataValidator fromApiJsonDeserializer, final LoanProductRepository loanProductRepository,
            final AprCalculator aprCalculator, final FundRepository fundRepository,
            final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository,
            final ChargeRepositoryWrapper chargeRepository,
            final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService,
            final FineractEntityAccessUtil fineractEntityAccessUtil,
            final FloatingRateRepositoryWrapper floatingRateRepository,
            final LoanRepositoryWrapper loanRepositoryWrapper,
            final BusinessEventNotifierService businessEventNotifierService) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanProductRepository = loanProductRepository;
        this.aprCalculator = aprCalculator;
        this.fundRepository = fundRepository;
        this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
        this.chargeRepository = chargeRepository;
        this.accountMappingWritePlatformService = accountMappingWritePlatformService;
        this.fineractEntityAccessUtil = fineractEntityAccessUtil;
        this.floatingRateRepository = floatingRateRepository;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.businessEventNotifierService = businessEventNotifierService;
    }

    @Transactional
    @Override
    public CommandProcessingResult createLoanProduct(final JsonCommand command) {

        try {

            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());
            validateInputDates(command);

            final Fund fund = findFundByIdIfProvided(command.longValueOfParameterNamed("fundId"));

            final Long transactionProcessingStrategyId = command.longValueOfParameterNamed("transactionProcessingStrategyId");
            final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyByIdIfProvided(transactionProcessingStrategyId);

            final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
            final List<Charge> charges = assembleListOfProductCharges(command, currencyCode);

            FloatingRate floatingRate = null;
            if(command.parameterExists("floatingRatesId")){
            	floatingRate = this.floatingRateRepository
            			.findOneWithNotFoundDetection(command.longValueOfParameterNamed("floatingRatesId"));
            }
            final LoanProduct loanproduct = LoanProduct.assembleFromJson(fund, loanTransactionProcessingStrategy, charges, command,
                    this.aprCalculator, floatingRate);
            loanproduct.updateLoanProductInRelatedClasses();

            this.loanProductRepository.save(loanproduct);

            // save accounting mappings
            this.accountMappingWritePlatformService.createLoanProductToGLAccountMapping(loanproduct.getId(), command);
            // check if the office specific products are enabled. If yes, then save this savings product against a specific office
            // i.e. this savings product is specific for this office.
            fineractEntityAccessUtil.checkConfigurationAndAddProductResrictionsForUserOffice(
            		FineractEntityAccessType.OFFICE_ACCESS_TO_LOAN_PRODUCTS, 
            		loanproduct.getId());

            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_PRODUCT_CREATE,
                    constructEntityMap(BUSINESS_ENTITY.LOAN_PRODUCT, loanproduct));
            
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.LOAN_PRODUCT_CREATE,
            		constructEntityMap(BUSINESS_ENTITY.LOAN_PRODUCT, loanproduct));
            
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(loanproduct.getId()) //
                    .build();

        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch(final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            handleDataIntegrityIssues(command, throwable, dve);
         	return CommandProcessingResult.empty();
        }

    }

    private LoanTransactionProcessingStrategy findStrategyByIdIfProvided(final Long transactionProcessingStrategyId) {
        LoanTransactionProcessingStrategy strategy = null;
        if (transactionProcessingStrategyId != null) {
            strategy = this.loanTransactionProcessingStrategyRepository.findOne(transactionProcessingStrategyId);
            if (strategy == null) { throw new LoanTransactionProcessingStrategyNotFoundException(transactionProcessingStrategyId); }
        }
        return strategy;
    }

    private Fund findFundByIdIfProvided(final Long fundId) {
        Fund fund = null;
        if (fundId != null) {
            fund = this.fundRepository.findOne(fundId);
            if (fund == null) { throw new FundNotFoundException(fundId); }
        }
        return fund;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateLoanProduct(final Long loanProductId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            final LoanProduct product = this.loanProductRepository.findOne(loanProductId);
            if (product == null) { throw new LoanProductNotFoundException(loanProductId); }

            this.fromApiJsonDeserializer.validateForUpdate(command.json(), product);
            validateInputDates(command);

            if(anyChangeInCriticalFloatingRateLinkedParams(command, product) 
            		&& this.loanRepositoryWrapper.doNonClosedLoanAccountsExistForProduct(product.getId())){
            	throw new LoanProductCannotBeModifiedDueToNonClosedLoansException(product.getId());
            }
            
            FloatingRate floatingRate = null;
            if(command.parameterExists("floatingRatesId")){
            	floatingRate = this.floatingRateRepository
            			.findOneWithNotFoundDetection(command.longValueOfParameterNamed("floatingRatesId"));
            }

            final Map<String, Object> changes = product.update(command, this.aprCalculator, floatingRate);

            if (changes.containsKey("fundId")) {
                final Long fundId = (Long) changes.get("fundId");
                final Fund fund = findFundByIdIfProvided(fundId);
                product.update(fund);
            }

            if (changes.containsKey("transactionProcessingStrategyId")) {
                final Long transactionProcessingStrategyId = (Long) changes.get("transactionProcessingStrategyId");
                final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyByIdIfProvided(transactionProcessingStrategyId);
                product.update(loanTransactionProcessingStrategy);
            }

            if (changes.containsKey("charges")) {
                final List<Charge> productCharges = assembleListOfProductCharges(command, product.getCurrency().getCode());
                final boolean updated = product.update(productCharges);
                if (!updated) {
                    changes.remove("charges");
                }
            }

            // accounting related changes
            final boolean accountingTypeChanged = changes.containsKey("accountingRule");
            final Map<String, Object> accountingMappingChanges = this.accountMappingWritePlatformService
                    .updateLoanProductToGLAccountMapping(product.getId(), command, accountingTypeChanged, product.getAccountingType());
            changes.putAll(accountingMappingChanges);

            if (!changes.isEmpty()) {
                this.loanProductRepository.saveAndFlush(product);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(loanProductId) //
                    .with(changes) //
                    .build();

        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }catch(final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            handleDataIntegrityIssues(command, throwable, dve);
         	return CommandProcessingResult.empty();
        }

    }

    private boolean anyChangeInCriticalFloatingRateLinkedParams(JsonCommand command, LoanProduct product) {
        final boolean isChangeFromFloatingToFlatOrViceVersa = command.isChangeInBooleanParameterNamed("isLinkedToFloatingInterestRates", product.isLinkedToFloatingInterestRate());
    	final boolean isChangeInCriticalFloatingRateParams = product.getFloatingRates() != null
    			&& (command.isChangeInLongParameterNamed("floatingRatesId", product.getFloatingRates().getFloatingRate().getId())
    					|| command.isChangeInBigDecimalParameterNamed("interestRateDifferential", product.getFloatingRates().getInterestRateDifferential()));
		return isChangeFromFloatingToFlatOrViceVersa || isChangeInCriticalFloatingRateParams;
	}

	private List<Charge> assembleListOfProductCharges(final JsonCommand command, final String currencyCode) {

        final List<Charge> charges = new ArrayList<>();

        String loanProductCurrencyCode = command.stringValueOfParameterNamed("currencyCode");
        if (loanProductCurrencyCode == null) {
            loanProductCurrencyCode = currencyCode;
        }

        if (command.parameterExists("charges")) {
            final JsonArray chargesArray = command.arrayOfParameterNamed("charges");
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {

                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has("id")) {
                        final Long id = jsonObject.get("id").getAsLong();

                        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);

                        if (!loanProductCurrencyCode.equals(charge.getCurrencyCode())) {
                            final String errorMessage = "Charge and Loan Product must have the same currency.";
                            throw new InvalidCurrencyException("charge", "attach.to.loan.product", errorMessage);
                        }
                        charges.add(charge);
                    }
                }
            }
        }

        return charges;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("'external_id'")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.product.loan.duplicate.externalId", "Loan Product with externalId `"
                    + externalId + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("'unq_name'")) {

            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.product.loan.duplicate.name", "Loan product with name `" + name
                    + "` already exists", "name", name);
        } else if (realCause.getMessage().contains("'unq_short_name'")) {

            final String shortName = command.stringValueOfParameterNamed("shortName");
            throw new PlatformDataIntegrityException("error.msg.product.loan.duplicate.short.name", "Loan product with short name `"
                    + shortName + "` already exists", "shortName", shortName);
        } else if (realCause.getMessage().contains("Duplicate entry")) {
            final Object[] args = null;
            throw new PlatformDataIntegrityException("error.msg.product.loan.duplicate.charge",
                    "Loan product may only have one charge of each type.`", "charges", args);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.product.loan.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void validateInputDates(final JsonCommand command) {
        final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
        final LocalDate closeDate = command.localDateValueOfParameterNamed("closeDate");

        if (startDate != null && closeDate != null) {
            if (closeDate.isBefore(startDate)) { throw new LoanProductDateException(startDate.toString(), closeDate.toString()); }
        }
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        logger.error(dve.getMessage(), dve);
    }

    private Map<BusinessEventNotificationConstants.BUSINESS_ENTITY, Object> constructEntityMap(final BusinessEventNotificationConstants.BUSINESS_ENTITY entityEvent, Object entity) {
        Map<BusinessEventNotificationConstants.BUSINESS_ENTITY, Object> map = new HashMap<>(1);
        map.put(entityEvent, entity);
        return map;
    }
}