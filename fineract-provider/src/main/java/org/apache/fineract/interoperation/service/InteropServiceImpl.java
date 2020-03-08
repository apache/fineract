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
package org.apache.fineract.interoperation.service;

import static org.apache.fineract.interoperation.util.InteropUtil.DEFAULT_LOCALE;
import static org.apache.fineract.interoperation.util.InteropUtil.DEFAULT_ROUTING_CODE;
import static org.springframework.data.jpa.domain.Specification.where;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.interoperation.data.InteropAccountData;
import org.apache.fineract.interoperation.data.InteropIdentifierAccountResponseData;
import org.apache.fineract.interoperation.data.InteropIdentifierRequestData;
import org.apache.fineract.interoperation.data.InteropIdentifiersResponseData;
import org.apache.fineract.interoperation.data.InteropQuoteRequestData;
import org.apache.fineract.interoperation.data.InteropQuoteResponseData;
import org.apache.fineract.interoperation.data.InteropRequestData;
import org.apache.fineract.interoperation.data.InteropTransactionRequestData;
import org.apache.fineract.interoperation.data.InteropTransactionRequestResponseData;
import org.apache.fineract.interoperation.data.InteropTransactionsData;
import org.apache.fineract.interoperation.data.InteropTransferRequestData;
import org.apache.fineract.interoperation.data.InteropTransferResponseData;
import org.apache.fineract.interoperation.data.MoneyData;
import org.apache.fineract.interoperation.domain.InteropActionState;
import org.apache.fineract.interoperation.domain.InteropIdentifier;
import org.apache.fineract.interoperation.domain.InteropIdentifierRepository;
import org.apache.fineract.interoperation.domain.InteropIdentifierType;
import org.apache.fineract.interoperation.serialization.InteropDataValidator;
import org.apache.fineract.interoperation.util.MathUtil;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountDomainService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionSummaryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteropServiceImpl implements InteropService {

    private final static Logger LOG = LoggerFactory.getLogger(InteropServiceImpl.class);

    private final PlatformSecurityContext securityContext;
    private final InteropDataValidator dataValidator;

    private final SavingsAccountRepository savingsAccountRepository;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final ApplicationCurrencyRepository currencyRepository;
    private final NoteRepository noteRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final InteropIdentifierRepository identifierRepository;

    private final SavingsHelper savingsHelper;
    private final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;

    private final SavingsAccountDomainService savingsAccountService;
    @Autowired
    public InteropServiceImpl(PlatformSecurityContext securityContext,
            InteropDataValidator interopDataValidator,
            SavingsAccountRepository savingsAccountRepository,
            SavingsAccountTransactionRepository savingsAccountTransactionRepository,
            ApplicationCurrencyRepository applicationCurrencyRepository,
            NoteRepository noteRepository,
            PaymentTypeRepository paymentTypeRepository,
            InteropIdentifierRepository identifierRepository,
            SavingsHelper savingsHelper,
            SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper,
            SavingsAccountDomainService savingsAccountService,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService) {
        this.securityContext = securityContext;
        this.dataValidator = interopDataValidator;
        this.savingsAccountRepository = savingsAccountRepository;
        this.savingsAccountTransactionRepository = savingsAccountTransactionRepository;
        this.currencyRepository = applicationCurrencyRepository;
        this.noteRepository = noteRepository;
        this.paymentTypeRepository = paymentTypeRepository;
        this.identifierRepository = identifierRepository;
        this.savingsHelper = savingsHelper;
        this.savingsAccountTransactionSummaryWrapper = savingsAccountTransactionSummaryWrapper;
        this.savingsAccountService = savingsAccountService;
    }

    @NotNull
    @Override
    @Transactional
    public InteropAccountData getAccountDetails(@NotNull String accountId) {
        SavingsAccount savingsAccount = validateAndGetSavingAccount(accountId);
        return InteropAccountData.build(savingsAccount);
    }

    @NotNull
    @Override
    @Transactional
    public InteropTransactionsData getAccountTransactions(@NotNull String accountId, boolean debit, boolean credit, java.time.LocalDateTime transactionsFrom, java.time.LocalDateTime transactionsTo) {
        SavingsAccount savingsAccount = validateAndGetSavingAccount(accountId);
        ZoneId zoneId = ZoneId.of(ThreadLocalContextUtil.getTenant().getTimezoneId());
        Predicate<SavingsAccountTransaction> transFilter = t -> {
            SavingsAccountTransactionType transactionType = SavingsAccountTransactionType.fromInt(t.getTypeOf());
            if (debit != transactionType.isDebit() && credit != transactionType.isCredit()) {
                return false;
            }

            if (transactionsFrom == null && transactionsTo == null) {
                return true;
            }

            java.time.LocalDateTime transactionDate = t.getTransactionLocalDate().toDateTimeAtStartOfDay().toDate().toInstant().atZone(zoneId).toLocalDateTime();
            return (transactionsTo == null || transactionsTo.compareTo(transactionDate) > 0)
                    && (transactionsFrom == null || transactionsFrom.compareTo(transactionDate.withHour(23).withMinute(59).withSecond(59)) <= 0);
        };
        return InteropTransactionsData.build(savingsAccount, transFilter);
    }

    @NotNull
    @Override
    @Transactional
    public InteropIdentifiersResponseData getAccountIdentifiers(@NotNull String accountId) {
        SavingsAccount savingsAccount = validateAndGetSavingAccount(accountId);
        return InteropIdentifiersResponseData.build(savingsAccount);
    }

    @NotNull
    @Override
    @Transactional
    public InteropIdentifierAccountResponseData getAccountByIdentifier(@NotNull InteropIdentifierType idType, @NotNull String idValue, String subIdOrType) {
        InteropIdentifier identifier = findIdentifier(idType, idValue, subIdOrType);
        return InteropIdentifierAccountResponseData.build(identifier.getAccount().getExternalId());
    }

    @NotNull
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public InteropIdentifierAccountResponseData registerAccountIdentifier(@NotNull InteropIdentifierType idType, @NotNull String idValue,
            String subIdOrType, @NotNull JsonCommand command) {
        InteropIdentifierRequestData request = dataValidator.validateAndParseCreateIdentifier(idType, idValue, subIdOrType, command);
        //TODO: error handling
        SavingsAccount savingsAccount = validateAndGetSavingAccount(request.getAccountId());

        AppUser createdBy = getLoginUser();

        InteropIdentifier identifier = new InteropIdentifier(savingsAccount, request.getIdType(), request.getIdValue(),
                request.getSubIdOrType(), createdBy.getUsername(), DateUtils.getDateOfTenant());

        identifierRepository.save(identifier);

        return InteropIdentifierAccountResponseData.build(savingsAccount.getExternalId());
    }

    @Override
    @NotNull
    @Transactional(propagation = Propagation.MANDATORY)
    public InteropIdentifierAccountResponseData deleteAccountIdentifier(@NotNull InteropIdentifierType idType, @NotNull String idValue,
            String subIdOrType) {
        InteropIdentifier identifier = findIdentifier(idType, idValue, subIdOrType);

        String accountId = identifier.getAccount().getExternalId();

        identifierRepository.delete(identifier);

        return InteropIdentifierAccountResponseData.build(accountId);
    }

    @Override
    public InteropTransactionRequestResponseData getTransactionRequest(@NotNull String transactionCode, @NotNull String requestCode) {
        // always REJECTED until request info is stored
        return InteropTransactionRequestResponseData.build(transactionCode, InteropActionState.REJECTED, requestCode);
    }

    @Override
    @NotNull
    @Transactional(propagation = Propagation.MANDATORY)
    public InteropTransactionRequestResponseData createTransactionRequest(@NotNull JsonCommand command) {
        // only when Payee request transaction from Payer, so here role must be always Payer
        InteropTransactionRequestData request = dataValidator.validateAndParseCreateRequest(command);

        validateAndGetSavingAccount(request);

        return InteropTransactionRequestResponseData.build(command.commandId(), request.getTransactionCode(), InteropActionState.ACCEPTED,
                request.getExpiration(), request.getExtensionList(), request.getRequestCode());
    }

    @Override
    public InteropQuoteResponseData getQuote(@NotNull String transactionCode, @NotNull String quoteCode) {
        return null;
    }

    @Override
    @NotNull
    @Transactional(propagation = Propagation.MANDATORY)
    public InteropQuoteResponseData createQuote(@NotNull JsonCommand command) {
        InteropQuoteRequestData request = dataValidator.validateAndParseCreateQuote(command);

        //TODO: error handling
        SavingsAccount savingsAccount = validateAndGetSavingAccount(request);
        SavingsAccountTransactionType transactionType = request.getTransactionRole().getTransactionType();

        BigDecimal fee = transactionType.isDebit() ? savingsAccount.calculateWithdrawalFee(request.getAmount().getAmount()) : BigDecimal.ZERO;

        return InteropQuoteResponseData.build(command.commandId(), request.getTransactionCode(), InteropActionState.ACCEPTED,
                request.getExpiration(), request.getExtensionList(), request.getQuoteCode(), MoneyData.build(fee, savingsAccount.getCurrency().getCode()),
                null);
    }

    @Override
    public InteropTransferResponseData getTransfer(@NotNull String transactionCode, @NotNull String transferCode) {
        return null;
    }

    @Override
    @NotNull
    @Transactional(propagation = Propagation.MANDATORY)
    public InteropTransferResponseData prepareTransfer(@NotNull JsonCommand command) {
        InteropTransferRequestData request = dataValidator.validateAndParsePrepareTransfer(command);

        //TODO: error handling
        //TODO: REVERSE
        SavingsAccount savingsAccount = validateAndGetSavingAccount(request);

        BigDecimal total = validateTransfer(request, savingsAccount);

        String transferCode = request.getTransferCode();
        LocalDateTime transactionDate = DateUtils.getLocalDateTimeOfTenant();
        if (MathUtil.isGreaterThanZero(total)) {
            if (MathUtil.isLessThan(savingsAccount.getWithdrawableBalance(), total)) {
                throw new UnsupportedOperationException();
            }
            if (findTransaction(savingsAccount, transferCode, SavingsAccountTransactionType.AMOUNT_HOLD) != null) {
                throw new UnsupportedOperationException("Transfer amount was already put on hold " + transferCode);
            }

            PaymentDetail paymentDetail = PaymentDetail.instance(findPaymentType(), savingsAccount.getExternalId(), null, getRoutingCode(), transferCode, null);
            AppUser appUser = getLoginUser();
            SavingsAccountTransaction transaction = SavingsAccountTransaction.holdAmount(savingsAccount, savingsAccount.office(),
                    paymentDetail, transactionDate.toLocalDate(), Money.of(savingsAccount.getCurrency(), total), new Date(),
                    appUser);

            savingsAccount.holdAmount(total);
            savingsAccount.addTransaction(transaction);

            savingsAccountRepository.save(savingsAccount);
        }

        return InteropTransferResponseData.build(command.commandId(), request.getTransactionCode(), InteropActionState.ACCEPTED, request.getExpiration(),
                request.getExtensionList(), transferCode, transactionDate);
    }

    @Override
    @NotNull
    @Transactional(propagation = Propagation.MANDATORY)
    public InteropTransferResponseData commitTransfer(@NotNull JsonCommand command) {
        InteropTransferRequestData request = dataValidator.validateAndParseCreateTransfer(command);
        SavingsAccountTransactionType transactionType = request.getTransactionRole().getTransactionType();
        boolean debit = transactionType.isDebit();

        //TODO: error handling
        //TODO: REVERSE
        SavingsAccount savingsAccount = validateAndGetSavingAccount(request);

        validateTransfer(request, savingsAccount);

        String transferCode = request.getTransferCode();
        if (findTransaction(savingsAccount, transferCode, debit ? SavingsAccountTransactionType.WITHDRAWAL : SavingsAccountTransactionType.DEPOSIT) != null) {
            throw new UnsupportedOperationException("Transfer was already committed " + transferCode);
        }

        PaymentDetail paymentDetail = PaymentDetail.instance(findPaymentType(), savingsAccount.getExternalId(), null, getRoutingCode(), transferCode, null);

        LocalDateTime transactionDateTime = DateUtils.getLocalDateTimeOfTenant();
        LocalDate transactionDate = transactionDateTime.toLocalDate();
        Date createdDate = new Date();

        SavingsAccountTransaction holdTransaction = findTransaction(savingsAccount, transferCode, SavingsAccountTransactionType.AMOUNT_HOLD);
        if (holdTransaction != null && holdTransaction.getReleaseIdOfHoldAmountTransaction() == null) {
            AppUser appUser = getLoginUser();

            SavingsAccountTransaction releaseTransaction = SavingsAccountTransaction.releaseAmount(holdTransaction, transactionDate, createdDate, appUser);
            releaseTransaction = savingsAccountTransactionRepository.saveAndFlush(releaseTransaction);
            holdTransaction.updateReleaseId(releaseTransaction.getId());

            savingsAccount.releaseAmount(holdTransaction.getAmount());
            savingsAccount.addTransaction(releaseTransaction);

            savingsAccountRepository.save(savingsAccount);
        }

        BigDecimal amount = request.getAmount().getAmount();
        DateTimeFormatter fmt = getDateTimeFormatter(command);

        SavingsAccountTransaction transaction;
        if (debit) {
            SavingsTransactionBooleanValues transactionValues = new SavingsTransactionBooleanValues(false, true, true, false, false);
            transaction = savingsAccountService.handleWithdrawal(savingsAccount, fmt, transactionDate, amount,
                    paymentDetail, transactionValues);
        }
        else {
            transaction = savingsAccountService.handleDeposit(savingsAccount, fmt, transactionDate, amount,
                    paymentDetail, false, true);
        }

        String note = request.getNote();
        if (!StringUtils.isBlank(note)) {
            noteRepository.save(Note.savingsTransactionNote(savingsAccount, transaction, note));
        }

        return InteropTransferResponseData.build(command.commandId(), request.getTransactionCode(), InteropActionState.ACCEPTED,
                request.getExpiration(), request.getExtensionList(), request.getTransferCode(), transactionDateTime);
    }

    // Util

    private SavingsAccount validateAndGetSavingAccount(String accountId) {
        SavingsAccount savingsAccount = savingsAccountRepository.findByExternalId(accountId);
        if (savingsAccount == null) {
            throw new SavingsAccountNotFoundException(accountId);
        }
        return savingsAccount;
    }

    private SavingsAccount validateAndGetSavingAccount(@NotNull InteropRequestData request) {
        //TODO: error handling
        SavingsAccount savingsAccount = validateAndGetSavingAccount(request.getAccountId());
        savingsAccount.setHelpers(savingsAccountTransactionSummaryWrapper, savingsHelper);

        ApplicationCurrency currency = currencyRepository.findOneByCode(request.getAmount().getCurrency());
        if (!savingsAccount.getCurrency().getCode().equals(currency.getCode())) {
            throw new UnsupportedOperationException();
        }

        SavingsAccountTransactionType transactionType = request.getTransactionRole().getTransactionType();
        if (!savingsAccount.isTransactionAllowed(transactionType, request.getExpirationLocalDate())) {
            throw new UnsupportedOperationException();
        }

        request.normalizeAmounts(savingsAccount.getCurrency());
        if (transactionType.isDebit() && MathUtil.isLessThan(savingsAccount.getWithdrawableBalance(), request.getAmount().getAmount())) {
            throw new UnsupportedOperationException();
        }

        return savingsAccount;
    }

    private BigDecimal validateTransfer(@NotNull InteropTransferRequestData request, @NotNull SavingsAccount savingsAccount) {
        BigDecimal amount = request.getAmount().getAmount();
        SavingsAccountTransactionType transactionType = request.getTransactionRole().getTransactionType();

        BigDecimal total = transactionType.isDebit() ? amount : MathUtil.negate(amount);
        MoneyData fspFee = request.getFspFee();
        if (fspFee != null) {
            if (!savingsAccount.getCurrency().getCode().equals(fspFee.getCurrency())) {
                throw new UnsupportedOperationException();
            }
            //TODO: compare with calculated quote fee
            total = MathUtil.add(total, fspFee.getAmount());
        }
        MoneyData fspCommission = request.getFspCommission();
        if (fspCommission != null) {
            if (!savingsAccount.getCurrency().getCode().equals(fspCommission.getCurrency())) {
                throw new UnsupportedOperationException();
            }
            //TODO: compare with calculated quote commission
            total = MathUtil.subtractToZero(total, fspCommission.getAmount());
        }
        return total;
    }

    private DateTimeFormatter getDateTimeFormatter(@NotNull JsonCommand command) {
        Locale locale = command.extractLocale();
        if (locale == null) {
            locale = DEFAULT_LOCALE;
        }
        String dateFormat = command.dateFormat();
        if (StringUtils.isEmpty(dateFormat)) {
            dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
        }

        return DateTimeFormat.forPattern(dateFormat).withLocale(locale);
    }

    PaymentType findPaymentType() {
        List<PaymentType> paymentTypes = paymentTypeRepository.findAll();
        for (PaymentType paymentType : paymentTypes) {
            if (!paymentType.isCashPayment()) {
                return paymentType;
                //TODO: for now first not cash is retured:
                // 1. must be added as initial setup,
                // 2. if more than one non-cashe type added then update this code
            }
        }
        return null;
    }

    SavingsAccountTransaction findTransaction(@NotNull SavingsAccount savingsAccount, @NotNull String transactionCode, SavingsAccountTransactionType transactionType) {
        String routingCode = getRoutingCode();
        for (SavingsAccountTransaction transaction : savingsAccount.getTransactions()) {
            if (transactionType != null && !transactionType.getValue().equals(transaction.getTypeOf())) {
                continue;
            }

            PaymentDetail detail = transaction.getPaymentDetail();
            if (detail != null && routingCode.equals(detail.getRoutingCode()) && transactionCode.equals(detail.getReceiptNumber())) {
                return transaction;
            }
        }
        return null;
    }

    public InteropIdentifier findIdentifier(@NotNull InteropIdentifierType idType, @NotNull String idValue, String subIdOrType) {
        return identifierRepository.findOne(where(idTypeEqual(idType)).and(idValueEqual(idValue)).and(subIdOrTypeEqual(subIdOrType)))
                .orElseThrow(() -> new UnsupportedOperationException(
                        "Account not found for identifier " + idType + "/" + idValue + (subIdOrType == null ? "" : ("/" + subIdOrType))));
    }

    public static Specification<InteropIdentifier> idTypeEqual(@NotNull InteropIdentifierType idType) {
        return (Root<InteropIdentifier> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.and(cb.equal(root.get("type"), idType));
    }

    public static Specification<InteropIdentifier> idValueEqual(@NotNull String idValue) {
        return (Root<InteropIdentifier> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.and(cb.equal(root.get("value"), idValue));
    }

    public static Specification<InteropIdentifier> subIdOrTypeEqual(String subIdOrType) {
        return (Root<InteropIdentifier> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Path<Object> path = root.get("subValueOrType");
            return cb.and(subIdOrType == null ? cb.isNull(path) : cb.equal(path, subIdOrType));
        };
    }

    private AppUser getLoginUser() {
        return securityContext.getAuthenticatedUserIfPresent();
    }

    @NotNull
    String getRoutingCode() {
        return DEFAULT_ROUTING_CODE;
    }
}
