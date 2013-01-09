package org.mifosplatform.portfolio.loanproduct.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.accounting.service.ProductToGLAccountMappingWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepository;
import org.mifosplatform.portfolio.charge.exception.ChargeIsNotActiveException;
import org.mifosplatform.portfolio.charge.exception.ChargeNotFoundException;
import org.mifosplatform.portfolio.fund.domain.Fund;
import org.mifosplatform.portfolio.fund.domain.FundRepository;
import org.mifosplatform.portfolio.fund.exception.FundNotFoundException;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionProcessingStrategyRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTransactionProcessingStrategyNotFoundException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.mifosplatform.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.portfolio.loanproduct.serialization.LoanProductCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
public class LoanProductWritePlatformServiceJpaRepositoryImpl implements LoanProductWritePlatformService {

    private final PlatformSecurityContext context;
    private final LoanProductCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final LoanProductRepository loanProductRepository;
    private final AprCalculator aprCalculator;
    private final FundRepository fundRepository;
    private final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository;
    private final ChargeRepository chargeRepository;
    private final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService;

    @Autowired
    public LoanProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final LoanProductCommandFromApiJsonDeserializer fromApiJsonDeserializer, final LoanProductRepository loanProductRepository,
            final AprCalculator aprCalculator, final FundRepository fundRepository,
            final LoanTransactionProcessingStrategyRepository loanTransactionProcessingStrategyRepository,
            final ChargeRepository chargeRepository, final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanProductRepository = loanProductRepository;
        this.aprCalculator = aprCalculator;
        this.fundRepository = fundRepository;
        this.loanTransactionProcessingStrategyRepository = loanTransactionProcessingStrategyRepository;
        this.chargeRepository = chargeRepository;
        this.accountMappingWritePlatformService = accountMappingWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult createLoanProduct(final JsonCommand command) {

        this.context.authenticatedUser();

        final LoanProductCommand loanProductCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        loanProductCommand.validateForCreate();

        // associating fund with loan product at creation is optional for now.
        final Fund fund = findFundByIdIfProvided(command.longValueOfParameterNamed("fundId"));
        final LoanTransactionProcessingStrategy loanTransactionProcessingStrategy = findStrategyByIdIfProvided(command
                .longValueOfParameterNamed("transactionProcessingStrategyId"));
        final Set<Charge> charges = this.assembleSetOfCharges(command, null);

        final LoanProduct loanproduct = LoanProduct.assembleFromJson(fund, loanTransactionProcessingStrategy, charges, command,
                this.aprCalculator);

        this.loanProductRepository.save(loanproduct);
        
        //save accounting mappings
        accountMappingWritePlatformService.createLoanProductToGLAccountMapping(loanproduct.getId(), loanProductCommand);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanproduct.getId()).build();
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

        this.context.authenticatedUser();
        
        final LoanProductCommand loanProductCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        loanProductCommand.validateForUpdate();

        final LoanProduct product = this.loanProductRepository.findOne(loanProductId);
        if (product == null) { throw new LoanProductNotFoundException(loanProductId); }
        
        final Map<String, Object> changes = product.update(command, this.aprCalculator);

        // associating fund with loan product at creation is optional for now.
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
            final Set<Charge> charges = this.assembleSetOfCharges(command, product.getCurrency().getCode());
            product.update(charges);
        }
        
        // accounting related changes
        if (loanProductCommand.getAccountingType() != null) {
            boolean accountingTypeChanged = changes.containsKey("accountingType");
            accountMappingWritePlatformService.updateLoanProductToGLAccountMapping(product.getId(), loanProductCommand, accountingTypeChanged);
        }

        if (!changes.isEmpty()) {
            this.loanProductRepository.save(product);
        }
        
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanProductId).with(changes).build();
    }

    private Set<Charge> assembleSetOfCharges(final JsonCommand command, final String currencyCode) {

        final Set<Charge> charges = new HashSet<Charge>();
        final String[] chargesArray = command.arrayValueOfParameterNamed("charges");

        String loanProductCurrencyCode = command.stringValueOfParameterNamed("currencyCode");
        if (loanProductCurrencyCode == null) {
            loanProductCurrencyCode = currencyCode;
        }

        if (!ObjectUtils.isEmpty(chargesArray)) {
            for (final String chargeId : chargesArray) {

                final Long id = Long.valueOf(chargeId);
                final Charge charge = this.chargeRepository.findOne(id);
                if (charge == null || charge.isDeleted()) { throw new ChargeNotFoundException(id); }
                if (!charge.isActive()) { throw new ChargeIsNotActiveException(id, charge.getName()); }

                if (!loanProductCurrencyCode.equals(charge.getCurrencyCode())) {
                    String errorMessage = "Charge and Loan Product must have the same currency.";
                    throw new InvalidCurrencyException("charge", "attach.to.loan.product", errorMessage);
                }
                charges.add(charge);
            }
        }

        return charges;
    }
    
}