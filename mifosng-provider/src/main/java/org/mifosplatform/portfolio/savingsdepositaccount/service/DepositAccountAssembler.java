package org.mifosplatform.portfolio.savingsdepositaccount.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccount;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountStatus;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachine;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachineImpl;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.FixedTermDepositInterestCalculator;
import org.mifosplatform.portfolio.savingsdepositaccount.exception.DepositAccounDataValidationtException;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.DepositProduct;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.DepositProductRepository;
import org.mifosplatform.portfolio.savingsdepositproduct.exception.DepositProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * An assembler for turning {@link DepositAccountCommand} into
 * {@link DepositAccount}'s.
 */
@Service
public class DepositAccountAssembler {

    private final ClientRepository clientRepository;
    private final DepositProductRepository depositProductRepository;
    private final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator;

    @Autowired
    public DepositAccountAssembler(final ClientRepository clientRepository, final DepositProductRepository depositProductRepository,
            final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator) {
        this.clientRepository = clientRepository;
        this.depositProductRepository = depositProductRepository;
        this.fixedTermDepositInterestCalculator = fixedTermDepositInterestCalculator;
    }

    public DepositAccount assembleFrom(final DepositAccountCommand command) {

        Client client = this.clientRepository.findOne(command.getClientId());
        if (client == null || client.isDeleted()) { throw new ClientNotFoundException(command.getClientId()); }

        DepositProduct product = this.depositProductRepository.findOne(command.getProductId());
        if (product == null || product.isDeleted()) { throw new DepositProductNotFoundException(command.getProductId()); }

        boolean isInterestWithdrawable = command.isInterestWithdrawable();

        // details inherited from product setting (unless allowed to be
        // overridden through account creation api
        Money deposit = Money.of(product.getCurrency(), command.getDepositAmount());

        Integer tenureInMonths = product.getTenureInMonths();
        if (command.getTenureInMonths() != null) {
            tenureInMonths = command.getTenureInMonths();
        }

        BigDecimal maturityInterestRate = product.getMaturityDefaultInterestRate();
        if (command.getMaturityInterestRate() != null) {
            maturityInterestRate = command.getMaturityInterestRate();
        }

        BigDecimal preClosureInterestRate = product.getPreClosureInterestRate();
        if (command.getPreClosureInterestRate() != null) {
            preClosureInterestRate = command.getPreClosureInterestRate();
        }

        if (product.getMaturityMinInterestRate().compareTo(preClosureInterestRate) == -1) { throw new DepositAccounDataValidationtException(
                preClosureInterestRate, product.getMaturityMinInterestRate()); }

        Integer compoundingInterestEvery = product.getInterestCompoundedEvery();
        if (command.getInterestCompoundedEvery() != null) {
            compoundingInterestEvery = command.getInterestCompoundedEvery();
        }

        PeriodFrequencyType compoundingInterestFrequency = product.getInterestCompoundedEveryPeriodType();
        if (command.getInterestCompoundedEveryPeriodType() != null) {
            compoundingInterestFrequency = PeriodFrequencyType.fromInt(command.getInterestCompoundedEveryPeriodType());
        }

        boolean renewalAllowed = product.isRenewalAllowed();
        if (command.isRenewalAllowedChanged()) {
            renewalAllowed = command.isRenewalAllowed();
        }

        boolean preClosureAllowed = product.isPreClosureAllowed();
        if (command.isPreClosureAllowedChanged()) {
            preClosureAllowed = command.isPreClosureAllowed();
        }

        boolean interestCompoundingAllowed = product.isInterestCompoundingAllowed();
        if (command.isInterestCompoundingAllowedChanged()) {
            interestCompoundingAllowed = command.isInterestCompoundingAllowed();
        }

        boolean isLockinPeriodAllowed = product.isLockinPeriodAllowed();
        if (command.isLockinPeriodChanged()) {
            isLockinPeriodAllowed = command.isLockinPeriodAllowed();
        }

        Integer lockinPeriod = product.getLockinPeriod();
        if (command.getLockinPeriod() != null) {
            lockinPeriod = command.getLockinPeriod();
        }

        PeriodFrequencyType lockinPeriodType = product.getLockinPeriodType();
        if (command.getLockinPeriodType() != null) {
            lockinPeriodType = PeriodFrequencyType.fromInt(command.getLockinPeriodType());
        }
        // end of details allowed to be overriden from product

        DepositAccount account = new DepositAccount().openNew(client, product, command.getExternalId(), deposit, maturityInterestRate,
                preClosureInterestRate, tenureInMonths, compoundingInterestEvery, compoundingInterestFrequency,
                command.getCommencementDate(), renewalAllowed, preClosureAllowed, this.fixedTermDepositInterestCalculator,
                defaultDepositLifecycleStateMachine(), isInterestWithdrawable, interestCompoundingAllowed, isLockinPeriodAllowed,
                lockinPeriod, lockinPeriodType);

        return account;
    }

    private DepositLifecycleStateMachine defaultDepositLifecycleStateMachine() {
        List<DepositAccountStatus> allowedDepositStatuses = Arrays.asList(DepositAccountStatus.values());
        return new DepositLifecycleStateMachineImpl(allowedDepositStatuses);
    }

    public DepositAccount assembleFrom(DepositAccount account, DepositAccountCommand command) {

        Client client = account.client();

        DepositProduct product = account.product();
        if (command.getProductId() != null) {
            product = this.depositProductRepository.findOne(command.getProductId());
            if (product == null || product.isDeleted()) { throw new DepositProductNotFoundException(command.getProductId()); }
        }

        Money deposit = account.getDeposit();
        if (command.getDepositAmount() != null) {
            deposit = Money.of(account.getDeposit().getCurrency(), command.getDepositAmount());
        }

        Integer tenureInMonths = account.getTenureInMonths();
        if (command.getTenureInMonths() != null) {
            tenureInMonths = command.getTenureInMonths();
        }

        BigDecimal maturityInterestRate = account.getInterestRate();
        if (command.getMaturityInterestRate() != null) {
            maturityInterestRate = command.getMaturityInterestRate();
        }

        BigDecimal preClosureInterestRate = account.getPreClosureInterestRate();
        if (command.getPreClosureInterestRate() != null) {
            preClosureInterestRate = command.getPreClosureInterestRate();
        }

        if (product.getMaturityMinInterestRate().compareTo(preClosureInterestRate) == -1) { throw new DepositAccounDataValidationtException(
                preClosureInterestRate, product.getMaturityMinInterestRate()); }

        Integer compoundingInterestEvery = account.getInterestCompoundedEvery();
        if (command.getInterestCompoundedEvery() != null) {
            compoundingInterestEvery = command.getInterestCompoundedEvery();
        }

        PeriodFrequencyType compoundingInterestFrequency = account.getInterestCompoundedFrequencyType();
        if (command.getInterestCompoundedEveryPeriodType() != null) {
            compoundingInterestFrequency = PeriodFrequencyType.fromInt(command.getInterestCompoundedEveryPeriodType());
        }

        boolean renewalAllowed = account.isRenewalAllowed();
        if (command.isRenewalAllowedChanged()) {
            renewalAllowed = command.isRenewalAllowed();
        }

        boolean preClosureAllowed = account.isPreClosureAllowed();
        if (command.isPreClosureAllowedChanged()) {
            preClosureAllowed = command.isPreClosureAllowed();
        }

        boolean isInterestWithdrawable = account.isInterestWithdrawable();
        if (command.isInterestWithdrawableChanged()) {
            isInterestWithdrawable = command.isInterestWithdrawable();
        }

        boolean isInterestCompoundingAllowed = account.isInterestCompoundingAllowed();
        if (command.isInterestCompoundingAllowedChanged()) {
            isInterestCompoundingAllowed = command.isInterestCompoundingAllowed();
        }

        boolean isLockinPeriodAllowed = account.isLockinPeriodAllowed();
        if (command.isLockinPeriodChanged()) {
            isLockinPeriodAllowed = command.isLockinPeriodAllowed();
        }

        Integer lockinPeriod = account.getLockinPeriod();
        if (command.getLockinPeriod() != null) {
            lockinPeriod = command.getLockinPeriod();
        }

        PeriodFrequencyType lockinPeriodType = account.getLockinPeriodType();
        if (command.getLockinPeriodType() != null) {
            lockinPeriodType = PeriodFrequencyType.fromInt(command.getLockinPeriodType());
        }

        DepositAccount newAccount = new DepositAccount().openNew(client, product, null, deposit, maturityInterestRate,
                preClosureInterestRate, tenureInMonths, compoundingInterestEvery, compoundingInterestFrequency, account.maturesOnDate()
                        .plusDays(1), renewalAllowed, preClosureAllowed, this.fixedTermDepositInterestCalculator,
                defaultDepositLifecycleStateMachine(), isInterestWithdrawable, isInterestCompoundingAllowed, isLockinPeriodAllowed,
                lockinPeriod, lockinPeriodType);

        newAccount.updateAccount(account);

        return newAccount;
    }

    /*
     * public void adjustTotalAmountForPreclosureInterest(DepositAccount
     * account,LocalDate eventDate) {
     * account.adjustTotalAmountForPreclosureInterest
     * (account,this.fixedTermDepositInterestCalculator,eventDate); }
     */

    public void assembleUpdatedDepositAccount(DepositAccount account, DepositAccountCommand command) {

        DepositProduct product = account.product();
        if (command.getProductId() != null) {
            product = this.depositProductRepository.findOne(command.getProductId());
            if (product == null || product.isDeleted()) { throw new DepositProductNotFoundException(command.getProductId()); }
        }

        String externalId = account.getExternalId();
        if (command.isExternalIdChanged()) {
            externalId = command.getExternalId();
        }

        LocalDate commencementDate = account.getProjectedCommencementDate();
        if (command.isCommencementDateChanged()) {
            commencementDate = new LocalDate(command.getCommencementDate());
        }

        Money deposit = account.getDeposit();
        if (command.isDepositAmountChanged()) {
            deposit = Money.of(product.getCurrency(), command.getDepositAmount());
        }

        Integer tenureInMonths = account.getTenureInMonths();
        if (command.isTenureInMonthsChanged()) {
            tenureInMonths = command.getTenureInMonths();
        }

        BigDecimal maturityInterestRate = account.getInterestRate();
        if (command.isMaturityActualInterestRateChanged()) {
            maturityInterestRate = command.getMaturityInterestRate();
        }

        BigDecimal preClosureInterestRate = account.getPreClosureInterestRate();
        if (command.isPreClosureInterestRateChanged()) {
            preClosureInterestRate = command.getPreClosureInterestRate();
        }

        if (product.getMaturityMinInterestRate().compareTo(preClosureInterestRate) == -1) { throw new DepositAccounDataValidationtException(
                preClosureInterestRate, product.getMaturityMinInterestRate()); }

        Integer compoundingInterestEvery = account.getInterestCompoundedEvery();
        if (command.isCompoundingInterestEveryChanged()) {
            compoundingInterestEvery = command.getInterestCompoundedEvery();
        }

        PeriodFrequencyType compoundingInterestFrequency = account.getInterestCompoundedFrequencyType();
        if (command.getInterestCompoundedEveryPeriodType() != null) {
            compoundingInterestFrequency = PeriodFrequencyType.fromInt(command.getInterestCompoundedEveryPeriodType());
        }

        boolean renewalAllowed = account.isRenewalAllowed();
        if (command.isRenewalAllowedChanged()) {
            renewalAllowed = command.isRenewalAllowed();
        }

        boolean preClosureAllowed = account.isPreClosureAllowed();
        if (command.isPreClosureAllowedChanged()) {
            preClosureAllowed = command.isPreClosureAllowed();
        }

        boolean isInterestWithdrawable = account.isInterestWithdrawable();
        if (command.isInterestWithdrawableChanged()) {
            isInterestWithdrawable = command.isInterestWithdrawable();
        }

        boolean isInterestCompoundingAllowed = account.isInterestCompoundingAllowed();
        if (command.isInterestCompoundingAllowedChanged()) {
            isInterestCompoundingAllowed = command.isInterestCompoundingAllowed();
        }

        boolean isLockinPeriodAllowed = account.isLockinPeriodAllowed();
        if (command.isLockinPeriodChanged()) {
            isLockinPeriodAllowed = command.isLockinPeriodAllowed();
        }

        Integer lockinPeriod = account.getLockinPeriod();
        if (command.getLockinPeriod() != null) {
            lockinPeriod = command.getLockinPeriod();
        }

        PeriodFrequencyType lockinPeriodType = account.getLockinPeriodType();
        if (command.getLockinPeriodType() != null) {
            lockinPeriodType = PeriodFrequencyType.fromInt(command.getLockinPeriodType());
        }
        account.update(product, externalId, commencementDate, deposit, tenureInMonths, maturityInterestRate, preClosureInterestRate,
                compoundingInterestEvery, compoundingInterestFrequency, renewalAllowed, preClosureAllowed, isInterestWithdrawable,
                isInterestCompoundingAllowed, this.fixedTermDepositInterestCalculator, isLockinPeriodAllowed, lockinPeriod,
                lockinPeriodType);
    }

    public void updateApprovedDepositAccount(DepositAccount account, DepositAccountCommand command) {

        boolean renewalAllowed = account.isRenewalAllowed();
        if (command.isRenewalAllowedChanged()) {
            renewalAllowed = command.isRenewalAllowed();
        }

        boolean isInterestWithdrawable = account.isInterestWithdrawable();
        if (command.isInterestWithdrawableChanged()) {
            isInterestWithdrawable = command.isInterestWithdrawable();
        }
        account.update(renewalAllowed, isInterestWithdrawable);

    }

    public void postInterest(DepositAccount account) {
        account.postInterestForDepositAccount(account, this.fixedTermDepositInterestCalculator);
    }

}