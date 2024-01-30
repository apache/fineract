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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.product.LoanProductCreateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.exception.InvalidCurrencyException;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRate;
import org.apache.fineract.portfolio.floatingrates.domain.FloatingRateRepositoryWrapper;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.exception.FundNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsJsonParser;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationsJsonParser;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductCreditAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductPaymentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductCannotBeModifiedDueToNonClosedLoansException;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductDateException;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.apache.fineract.portfolio.rate.domain.Rate;
import org.apache.fineract.portfolio.rate.domain.RateRepositoryWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class LoanProductWritePlatformServiceJpaRepositoryImpl implements LoanProductWritePlatformService {

    private final PlatformSecurityContext context;
    private final LoanProductDataValidator fromApiJsonDeserializer;
    private final LoanProductRepository loanProductRepository;
    private final AprCalculator aprCalculator;
    private final FundRepository fundRepository;
    private final ChargeRepositoryWrapper chargeRepository;
    private final RateRepositoryWrapper rateRepository;
    private final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService;
    private final FineractEntityAccessUtil fineractEntityAccessUtil;
    private final FloatingRateRepositoryWrapper floatingRateRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final DelinquencyBucketRepository delinquencyBucketRepository;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final AdvancedPaymentAllocationsJsonParser advancedPaymentJsonParser;
    private final CreditAllocationsJsonParser creditAllocationsJsonParser;
    private final LoanProductPaymentAllocationRuleMerger loanProductPaymentAllocationRuleMerger = new LoanProductPaymentAllocationRuleMerger();
    private final LoanProductCreditAllocationRuleMerger loanProductCreditAllocationRuleMerger = new LoanProductCreditAllocationRuleMerger();

    @Transactional
    @Override
    public CommandProcessingResult createLoanProduct(final JsonCommand command) {

        try {

            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command);
            validateInputDates(command);

            final Fund fund = findFundByIdIfProvided(command.longValueOfParameterNamed("fundId"));

            final String loanTransactionProcessingStrategyCode = command.stringValueOfParameterNamed("transactionProcessingStrategyCode");

            final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
            final List<Charge> charges = assembleListOfProductCharges(command, currencyCode);
            final List<Rate> rates = assembleListOfProductRates(command);
            final List<LoanProductPaymentAllocationRule> loanProductPaymentAllocationRules = advancedPaymentJsonParser
                    .assembleLoanProductPaymentAllocationRules(command, loanTransactionProcessingStrategyCode);
            final List<LoanProductCreditAllocationRule> loanProductCreditAllocationRules = creditAllocationsJsonParser
                    .assembleLoanProductCreditAllocationRules(command, loanTransactionProcessingStrategyCode);
            FloatingRate floatingRate = null;
            if (command.parameterExists("floatingRatesId")) {
                floatingRate = this.floatingRateRepository
                        .findOneWithNotFoundDetection(command.longValueOfParameterNamed("floatingRatesId"));
            }
            final LoanProduct loanProduct = LoanProduct.assembleFromJson(fund, loanTransactionProcessingStrategyCode, charges, command,
                    this.aprCalculator, floatingRate, rates, loanProductPaymentAllocationRules, loanProductCreditAllocationRules);
            loanProduct.updateLoanProductInRelatedClasses();
            loanProduct.setTransactionProcessingStrategyName(
                    loanRepaymentScheduleTransactionProcessorFactory.determineProcessor(loanTransactionProcessingStrategyCode).getName());

            if (command.parameterExists("delinquencyBucketId")) {
                loanProduct
                        .setDelinquencyBucket(findDelinquencyBucketIdIfProvided(command.longValueOfParameterNamed("delinquencyBucketId")));
            }

            this.loanProductRepository.saveAndFlush(loanProduct);

            // save accounting mappings
            this.accountMappingWritePlatformService.createLoanProductToGLAccountMapping(loanProduct.getId(), command);
            // check if the office specific products are enabled. If yes, then
            // save this savings product against a specific office
            // i.e. this savings product is specific for this office.
            fineractEntityAccessUtil.checkConfigurationAndAddProductResrictionsForUserOffice(
                    FineractEntityAccessType.OFFICE_ACCESS_TO_LOAN_PRODUCTS, loanProduct.getId());

            businessEventNotifierService.notifyPostBusinessEvent(new LoanProductCreateBusinessEvent(loanProduct));

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(loanProduct.getId()) //
                    .build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }

    }

    private Fund findFundByIdIfProvided(final Long fundId) {
        Fund fund = null;
        if (fundId != null) {
            fund = this.fundRepository.findById(fundId).orElseThrow(() -> new FundNotFoundException(fundId));
        }
        return fund;
    }

    private DelinquencyBucket findDelinquencyBucketIdIfProvided(final Long delinquencyBucketId) {
        DelinquencyBucket delinquencyBucket = null;
        if (delinquencyBucketId != null) {
            delinquencyBucket = delinquencyBucketRepository.findById(delinquencyBucketId)
                    .orElseThrow(() -> new FundNotFoundException(delinquencyBucketId));
        }
        return delinquencyBucket;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateLoanProduct(final Long loanProductId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            final LoanProduct product = this.loanProductRepository.findById(loanProductId)
                    .orElseThrow(() -> new LoanProductNotFoundException(loanProductId));

            this.fromApiJsonDeserializer.validateForUpdate(command, product);
            validateInputDates(command);

            if (anyChangeInCriticalFloatingRateLinkedParams(command, product)
                    && this.loanRepositoryWrapper.doNonClosedLoanAccountsExistForProduct(product.getId())) {
                throw new LoanProductCannotBeModifiedDueToNonClosedLoansException(product.getId());
            }

            FloatingRate floatingRate = null;
            if (command.parameterExists("floatingRatesId")) {
                floatingRate = this.floatingRateRepository
                        .findOneWithNotFoundDetection(command.longValueOfParameterNamed("floatingRatesId"));
            }

            final Map<String, Object> changes = product.update(command, this.aprCalculator, floatingRate);

            if (changes.containsKey("fundId")) {
                final Long fundId = (Long) changes.get("fundId");
                final Fund fund = findFundByIdIfProvided(fundId);
                product.update(fund);
            }

            if (changes.containsKey("delinquencyBucketId")) {
                product.setDelinquencyBucket(findDelinquencyBucketIdIfProvided((Long) changes.get("delinquencyBucketId")));
            }

            if (changes.containsKey("transactionProcessingStrategyCode")) {
                final String transactionProcessingStrategyCode = (String) changes.get("transactionProcessingStrategyCode");
                final String transactionProcessingStrategyName = loanRepaymentScheduleTransactionProcessorFactory
                        .determineProcessor(transactionProcessingStrategyCode).getName();
                product.setTransactionProcessingStrategyCode(transactionProcessingStrategyCode);
                product.setTransactionProcessingStrategyName(transactionProcessingStrategyName);
            }

            if (changes.containsKey("charges")) {
                final List<Charge> productCharges = assembleListOfProductCharges(command, product.getCurrency().getCode());
                final boolean updated = product.update(productCharges);
                if (!updated) {
                    changes.remove("charges");
                }
            }

            if (changes.containsKey("paymentAllocation")) {
                final List<LoanProductPaymentAllocationRule> loanProductPaymentAllocationRules = advancedPaymentJsonParser
                        .assembleLoanProductPaymentAllocationRules(command, product.getTransactionProcessingStrategyCode());
                loanProductPaymentAllocationRules.forEach(lppar -> lppar.setLoanProduct(product));
                final boolean updated = loanProductPaymentAllocationRuleMerger.updateProductPaymentAllocationRules(product,
                        loanProductPaymentAllocationRules);
                if (!updated) {
                    changes.remove("paymentAllocation");
                }
            }

            if (changes.containsKey("creditAllocation")) {
                final List<LoanProductCreditAllocationRule> loanProductCreditAllocationRules = creditAllocationsJsonParser
                        .assembleLoanProductCreditAllocationRules(command, product.getTransactionProcessingStrategyCode());
                loanProductCreditAllocationRules.forEach(lpcar -> lpcar.setLoanProduct(product));
                final boolean updated = loanProductCreditAllocationRuleMerger.updateCreditAllocationRules(product,
                        loanProductCreditAllocationRules);
                if (!updated) {
                    changes.remove("creditAllocation");
                }
            }

            // accounting related changes
            final boolean accountingTypeChanged = changes.containsKey("accountingRule");
            final Map<String, Object> accountingMappingChanges = this.accountMappingWritePlatformService
                    .updateLoanProductToGLAccountMapping(product.getId(), command, accountingTypeChanged, product.getAccountingType());
            changes.putAll(accountingMappingChanges);

            if (changes.containsKey(LoanProductConstants.RATES_PARAM_NAME)) {
                final List<Rate> productRates = assembleListOfProductRates(command);
                final boolean updated = product.updateRates(productRates);
                if (!updated) {
                    changes.remove(LoanProductConstants.RATES_PARAM_NAME);
                }
            }

            if (!changes.isEmpty()) {
                product.validateLoanProductPreSave();
                this.loanProductRepository.saveAndFlush(product);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(loanProductId) //
                    .with(changes) //
                    .build();

        } catch (final DataIntegrityViolationException | JpaSystemException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.resourceResult(-1L);
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }

    }

    private boolean anyChangeInCriticalFloatingRateLinkedParams(JsonCommand command, LoanProduct product) {
        final boolean isChangeFromFloatingToFlatOrViceVersa = command.isChangeInBooleanParameterNamed("isLinkedToFloatingInterestRates",
                product.isLinkedToFloatingInterestRate());
        final boolean isChangeInCriticalFloatingRateParams = product.getFloatingRates() != null
                && (command.isChangeInLongParameterNamed("floatingRatesId", product.getFloatingRates().getFloatingRate().getId())
                        || command.isChangeInBigDecimalParameterNamed("interestRateDifferential",
                                product.getFloatingRates().getInterestRateDifferential()));
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

    private List<Rate> assembleListOfProductRates(final JsonCommand command) {

        final List<Rate> rates = new ArrayList<>();

        if (command.parameterExists("rates")) {
            final JsonArray ratesArray = command.arrayOfParameterNamed("rates");
            if (ratesArray != null) {
                List<Long> idList = new ArrayList<>();
                for (int i = 0; i < ratesArray.size(); i++) {

                    final JsonObject jsonObject = ratesArray.get(i).getAsJsonObject();
                    if (jsonObject.has("id")) {
                        idList.add(jsonObject.get("id").getAsLong());
                    }
                }
                rates.addAll(this.rateRepository.findMultipleWithNotFoundDetection(idList));
            }
        }

        return rates;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("'external_id'")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.product.loan.duplicate.externalId",
                    "Loan Product with externalId `" + externalId + "` already exists", "externalId", externalId, realCause);
        } else if (realCause.getMessage().contains("'unq_name'")) {

            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.product.loan.duplicate.name",
                    "Loan product with name `" + name + "` already exists", "name", name, realCause);
        } else if (realCause.getMessage().contains("'unq_short_name'") || containsDuplicateShortnameErrorForPostgreSQL(realCause)
                || containsDuplicateShortnameErrorForMySQL(realCause)) {

            final String shortName = command.stringValueOfParameterNamed("shortName");
            throw new PlatformDataIntegrityException("error.msg.product.loan.duplicate.short.name",
                    "Loan product with short name `" + shortName + "` already exists", "shortName", shortName, realCause);
        } else if (realCause.getMessage().contains("Duplicate entry")) {
            throw new PlatformDataIntegrityException("error.msg.product.loan.duplicate.charge",
                    "Loan product may only have one charge of each type.`", "charges", realCause);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw ErrorHandler.getMappable(dve, "error.msg.product.loan.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.", null, realCause);
    }

    private static boolean containsDuplicateShortnameErrorForPostgreSQL(Throwable realCause) {
        return realCause.getMessage().contains("m_product_loan_short_name_key");
    }

    private static boolean containsDuplicateShortnameErrorForMySQL(Throwable realCause) {
        return (realCause.getMessage().contains("short_name") && realCause.getMessage().toLowerCase().contains("duplicate"));
    }

    private void validateInputDates(final JsonCommand command) {
        final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
        final LocalDate closeDate = command.localDateValueOfParameterNamed("closeDate");

        if (closeDate != null && DateUtils.isBefore(closeDate, startDate)) {
            throw new LoanProductDateException(startDate.toString(), closeDate.toString());
        }
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("Error occurred.", dve);
    }
}
