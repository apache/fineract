/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.service;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.glaccount.domain.GLAccountRepository;
import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.mifosplatform.accounting.provisioning.data.LoanProductProvisioningEntryData;
import org.mifosplatform.accounting.provisioning.data.ProvisioningEntryData;
import org.mifosplatform.accounting.provisioning.domain.LoanProductProvisioningEntry;
import org.mifosplatform.accounting.provisioning.domain.ProvisioningEntry;
import org.mifosplatform.accounting.provisioning.domain.ProvisioningEntryRepository;
import org.mifosplatform.accounting.provisioning.exception.NoProvisioningCriteriaDefinitionFound;
import org.mifosplatform.accounting.provisioning.exception.ProvisioningEntryAlreadyCreatedException;
import org.mifosplatform.accounting.provisioning.exception.ProvisioningEntryNotfoundException;
import org.mifosplatform.accounting.provisioning.exception.ProvisioningJournalEntriesCannotbeCreatedException;
import org.mifosplatform.accounting.provisioning.serialization.ProvisioningEntriesDefinitionJsonDeserializer;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.jobs.annotation.CronTarget;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.monetary.domain.MoneyHelper;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.provisioning.data.ProvisioningCriteriaData;
import org.mifosplatform.organisation.provisioning.domain.ProvisioningCategory;
import org.mifosplatform.organisation.provisioning.domain.ProvisioningCategoryRepository;
import org.mifosplatform.organisation.provisioning.service.ProvisioningCriteriaReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

@Service
public class ProvisioningEntriesWritePlatformServiceJpaRepositoryImpl implements ProvisioningEntriesWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ProvisioningEntriesWritePlatformServiceJpaRepositoryImpl.class);

    private final ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService;
    private final ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService ;
    private final LoanProductRepository loanProductRepository;
    private final GLAccountRepository glAccountRepository;
    private final OfficeRepository officeRepository;
    private final ProvisioningCategoryRepository provisioningCategoryRepository;
    private final PlatformSecurityContext platformSecurityContext;
    private final ProvisioningEntryRepository provisioningEntryRepository;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final ProvisioningEntriesDefinitionJsonDeserializer fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    
    @Autowired
    public ProvisioningEntriesWritePlatformServiceJpaRepositoryImpl(
            final ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService,
            final ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService,
            final LoanProductRepository loanProductRepository, final GLAccountRepository glAccountRepository,
            final OfficeRepository officeRepository, final ProvisioningCategoryRepository provisioningCategoryRepository,
            final PlatformSecurityContext platformSecurityContext, final ProvisioningEntryRepository provisioningEntryRepository,
            final JournalEntryWritePlatformService journalEntryWritePlatformService,
            final ProvisioningEntriesDefinitionJsonDeserializer fromApiJsonDeserializer, final FromJsonHelper fromApiJsonHelper) {
        this.provisioningEntriesReadPlatformService = provisioningEntriesReadPlatformService;
        this.provisioningCriteriaReadPlatformService = provisioningCriteriaReadPlatformService ;
        this.loanProductRepository = loanProductRepository;
        this.glAccountRepository = glAccountRepository;
        this.officeRepository = officeRepository;
        this.provisioningCategoryRepository = provisioningCategoryRepository;
        this.platformSecurityContext = platformSecurityContext;
        this.provisioningEntryRepository = provisioningEntryRepository;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CommandProcessingResult createProvisioningJournalEntries(Long provisioningEntryId, JsonCommand command) {
        ProvisioningEntry requestedEntry = this.provisioningEntryRepository.findOne(provisioningEntryId);
        if (requestedEntry == null) { throw new ProvisioningEntryNotfoundException(provisioningEntryId); }

        ProvisioningEntryData exisProvisioningEntryData = this.provisioningEntriesReadPlatformService
                .retrieveExistingProvisioningIdDateWithJournals();
        revertAndAddJournalEntries(exisProvisioningEntryData, requestedEntry);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(requestedEntry.getId()).build();
    }

    private void revertAndAddJournalEntries(ProvisioningEntryData existingEntryData, ProvisioningEntry requestedEntry) {
        if (existingEntryData != null) {
            validateForCreateJournalEntry(existingEntryData, requestedEntry);
            this.journalEntryWritePlatformService.revertProvisioningJournalEntries(requestedEntry.getCreatedDate(),
                    existingEntryData.getId(), PortfolioProductType.PROVISIONING.getValue());
        }
        if(requestedEntry.getLoanProductProvisioningEntries() == null || requestedEntry.getLoanProductProvisioningEntries().size() == 0) {
            requestedEntry.setJournalEntryCreated(Boolean.FALSE);    
        }else {
            requestedEntry.setJournalEntryCreated(Boolean.TRUE);
        }
        
        this.provisioningEntryRepository.save(requestedEntry);
        this.journalEntryWritePlatformService.createProvisioningJournalEntries(requestedEntry);
    }

    private void validateForCreateJournalEntry(ProvisioningEntryData existingEntry, ProvisioningEntry requested) {
        Date existingDate = existingEntry.getCreatedDate();
        Date requestedDate = requested.getCreatedDate();
        if (existingDate.after(requestedDate) || existingDate.equals(requestedDate)) { throw new ProvisioningJournalEntriesCannotbeCreatedException(
                existingEntry.getCreatedDate(), requestedDate); }
    }

    private boolean isJournalEntriesRequired(JsonCommand command) {
        boolean bool = false;
        if (this.fromApiJsonHelper.parameterExists("createjournalentries", command.parsedJson())) {
            JsonObject jsonObject = command.parsedJson().getAsJsonObject();
            bool = jsonObject.get("createjournalentries").getAsBoolean();
        }
        return bool;
    }

    private Date parseDate(JsonCommand command) {
        LocalDate localDate = this.fromApiJsonHelper.extractLocalDateNamed("date", command.parsedJson());
        return localDate.toDate();
    }

    @Override
    @CronTarget(jobName = JobName.GENERATE_LOANLOSS_PROVISIONING)
    public void generateLoanLossProvisioningAmount() {
        Date currentDate = new Date();
        boolean addJournalEntries = true;
        try {
            createProvsioningEntry(currentDate, addJournalEntries);
        } catch (ProvisioningEntryAlreadyCreatedException peace) {} catch (DataIntegrityViolationException dive) {}
    }

    @Override
    public CommandProcessingResult createProvisioningEntries(JsonCommand command) {
        this.fromApiJsonDeserializer.validateForCreate(command.json());
        Date createdDate = parseDate(command);
        boolean addJournalEntries = isJournalEntriesRequired(command);
        try {
            Collection<ProvisioningCriteriaData> criteriaCollection = this.provisioningCriteriaReadPlatformService.retrieveAllProvisioningCriterias() ; 
            if(criteriaCollection == null || criteriaCollection.size() == 0){
                throw new NoProvisioningCriteriaDefinitionFound() ;
            }
            ProvisioningEntry requestedEntry = createProvsioningEntry(createdDate, addJournalEntries);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(requestedEntry.getId()).build();
        } catch (DataIntegrityViolationException dve) {
            return CommandProcessingResult.empty();
        }
    }

    private ProvisioningEntry createProvsioningEntry(Date date, boolean addJournalEntries) {
        ProvisioningEntry existingEntry = this.provisioningEntryRepository.findByProvisioningEntryDate(date);
        if (existingEntry != null) { throw new ProvisioningEntryAlreadyCreatedException(existingEntry.getId(),
                existingEntry.getCreatedDate()); }
        AppUser currentUser = this.platformSecurityContext.authenticatedUser();
        AppUser lastModifiedBy = null;
        Date lastModifiedDate = null;
        Collection<LoanProductProvisioningEntry> nullEntries = null;
        ProvisioningEntry requestedEntry = new ProvisioningEntry(currentUser, date, lastModifiedBy, lastModifiedDate, nullEntries);
        Collection<LoanProductProvisioningEntry> entries = generateLoanProvisioningEntry(requestedEntry, date);
        requestedEntry.setProvisioningEntries(entries);
        if (addJournalEntries) {
            ProvisioningEntryData exisProvisioningEntryData = this.provisioningEntriesReadPlatformService
                    .retrieveExistingProvisioningIdDateWithJournals();
            revertAndAddJournalEntries(exisProvisioningEntryData, requestedEntry);
        } else {
            this.provisioningEntryRepository.save(requestedEntry);
        }
        return requestedEntry;
    }

    @Override
    public CommandProcessingResult reCreateProvisioningEntries(Long provisioningEntryId, JsonCommand command) {
        ProvisioningEntry requestedEntry = this.provisioningEntryRepository.findOne(provisioningEntryId);
        if (requestedEntry == null) { throw new ProvisioningEntryNotfoundException(provisioningEntryId); }
        requestedEntry.getLoanProductProvisioningEntries().clear();
        this.provisioningEntryRepository.save(requestedEntry);
        Collection<LoanProductProvisioningEntry> entries = generateLoanProvisioningEntry(requestedEntry, requestedEntry.getCreatedDate());
        requestedEntry.setProvisioningEntries(entries);
        this.provisioningEntryRepository.save(requestedEntry);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(requestedEntry.getId()).build();
    }

    private Collection<LoanProductProvisioningEntry> generateLoanProvisioningEntry(ProvisioningEntry parent, Date date) {
        Collection<LoanProductProvisioningEntryData> entries = this.provisioningEntriesReadPlatformService
                .retrieveLoanProductsProvisioningData(date);
        Map<LoanProductProvisioningEntry, LoanProductProvisioningEntry> provisioningEntries = new HashMap<>();
        for (LoanProductProvisioningEntryData data : entries) {
            LoanProduct loanProduct = this.loanProductRepository.findOne(data.getProductId());
            Office office = this.officeRepository.findOne(data.getOfficeId());
            ProvisioningCategory provisioningCategory = provisioningCategoryRepository.findOne(data.getCategoryId());
            GLAccount liabilityAccount = glAccountRepository.findOne(data.getLiablityAccount());
            GLAccount expenseAccount = glAccountRepository.findOne(data.getExpenseAccount());
            MonetaryCurrency currency = loanProduct.getPrincipalAmount().getCurrency();
            Money money = Money.of(currency, data.getOutstandingBalance());
            Money amountToReserve = money.percentageOf(data.getPercentage(), MoneyHelper.getRoundingMode());
            Long criteraId = data.getCriteriaId();
            LoanProductProvisioningEntry entry = new LoanProductProvisioningEntry(loanProduct, office, data.getCurrencyCode(),
                    provisioningCategory, data.getOverdueInDays(), amountToReserve.getAmount(), liabilityAccount, expenseAccount, criteraId);
            entry.setProvisioningEntry(parent);
            if (!provisioningEntries.containsKey(entry)) {
                provisioningEntries.put(entry, entry);
            } else {
                LoanProductProvisioningEntry entry1 = provisioningEntries.get(entry);
                entry1.addReservedAmount(entry.getReservedAmount());
            }
        }
        return provisioningEntries.values();
    }
}
