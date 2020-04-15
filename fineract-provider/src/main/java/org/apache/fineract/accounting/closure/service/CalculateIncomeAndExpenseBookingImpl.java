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
package org.apache.fineract.accounting.closure.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.fineract.accounting.closure.command.GLClosureCommand;
import org.apache.fineract.accounting.closure.data.IncomeAndExpenseBookingData;
import org.apache.fineract.accounting.closure.data.IncomeAndExpenseJournalEntryData;
import org.apache.fineract.accounting.closure.data.JournalEntryData;
import org.apache.fineract.accounting.closure.data.SingleDebitOrCreditEntryData;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.closure.domain.GLClosureRepository;
import org.apache.fineract.accounting.closure.exception.GLClosureInvalidException;
import org.apache.fineract.accounting.closure.exception.RunningBalanceNotCalculatedException;
import org.apache.fineract.accounting.closure.serialization.GLClosureCommandFromApiJsonDeserializer;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.exception.GLAccountNotFoundException;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.exception.OfficeNotFoundException;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CalculateIncomeAndExpenseBookingImpl implements CalculateIncomeAndExpenseBooking {

    private final GLClosureCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final OfficeRepositoryWrapper officeRepository;
    private final GLClosureRepository glClosureRepository;
    private final GLAccountRepositoryWrapper glAccountRepository;
    private final IncomeAndExpenseReadPlatformService incomeAndExpenseReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;

    @Autowired
    public CalculateIncomeAndExpenseBookingImpl(final GLClosureCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final OfficeRepositoryWrapper officeRepository,final GLClosureRepository glClosureRepository,
            final GLAccountRepositoryWrapper glAccountRepository,final IncomeAndExpenseReadPlatformService incomeAndExpenseReadPlatformService,
            final OfficeReadPlatformService officeReadPlatformService) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.officeRepository = officeRepository;
        this.glClosureRepository = glClosureRepository;
        this.glAccountRepository = glAccountRepository;
        this.incomeAndExpenseReadPlatformService = incomeAndExpenseReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
    }

    @Override
    public Collection<IncomeAndExpenseBookingData> CalculateIncomeAndExpenseBookings(JsonQuery query) {
        final GLClosureCommand closureCommand = this.fromApiJsonDeserializer.commandFromApiJson(query.json());
        closureCommand.validateForCreate();
        final Long officeId = closureCommand.getOfficeId();
        final Office office = this.officeRepository.findOneWithNotFoundDetection(officeId);
        if (office == null) { throw new OfficeNotFoundException(officeId); }

        final Date todaysDate = new Date();
        final Date closureDate = closureCommand.getClosingDate().toDate();
        if (closureDate.after(todaysDate)) { throw new GLClosureInvalidException(GLClosureInvalidException.GL_CLOSURE_INVALID_REASON.FUTURE_DATE, closureDate); }
        // shouldn't be before an existing accounting closure
        final GLClosure latestGLClosure = this.glClosureRepository.getLatestGLClosureByBranch(officeId);
        if (latestGLClosure != null) {
            if (latestGLClosure.getClosingDate().after(closureDate)) { throw new GLClosureInvalidException(
                    GLClosureInvalidException.GL_CLOSURE_INVALID_REASON.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate()); }
        }
        final LocalDate incomeAndExpenseBookOffDate = LocalDate.fromDateFields(closureDate);

         /*get all offices underneath it valid closure date for all, get Jl for all and make bookings*/
       // final List<Office> childOffices = office.getChildren();
        final Collection<Long> childOfficesByHierarchy = this.officeReadPlatformService.officeByHierarchy(officeId);
        if(closureCommand.getSubBranches() && childOfficesByHierarchy.size() > 0){
            for(final Long childOffice : childOfficesByHierarchy){
                final GLClosure latestChildGlClosure = this.glClosureRepository.getLatestGLClosureByBranch(childOffice);
                if (latestChildGlClosure != null) {
                    if (latestChildGlClosure.getClosingDate().after(closureDate)) { throw new GLClosureInvalidException(
                            GLClosureInvalidException.GL_CLOSURE_INVALID_REASON.ACCOUNTING_CLOSED, latestChildGlClosure.getClosingDate()); }
                }
            }
        }

        Collection<IncomeAndExpenseBookingData> incomeAndExpenseBookingCollection = new ArrayList<>();
        final Long equityGlAccountId = closureCommand.getEquityGlAccountId();

        final GLAccount glAccount= this.glAccountRepository.findOneWithNotFoundDetection(equityGlAccountId);
        if(glAccount == null){throw new GLAccountNotFoundException(equityGlAccountId);}

        if(closureCommand.getSubBranches() && childOfficesByHierarchy.size() > 0){
            for(final Long childOffice : childOfficesByHierarchy){
                final List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJournalEntryDataList = this.incomeAndExpenseReadPlatformService.
                        retrieveAllIncomeAndExpenseJournalEntryData(childOffice, incomeAndExpenseBookOffDate,closureCommand.getCurrencyCode());
                final IncomeAndExpenseBookingData incomeAndExpBookingData = this.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList, closureCommand, true, glAccount,this.officeRepository.findOneWithNotFoundDetection(childOffice));
                if(incomeAndExpBookingData.getJournalEntries().size() > 0){ incomeAndExpenseBookingCollection.add(incomeAndExpBookingData);}
            }
        }else{
            final List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJournalEntryDataList = this.incomeAndExpenseReadPlatformService.retrieveAllIncomeAndExpenseJournalEntryData(officeId,incomeAndExpenseBookOffDate,closureCommand.getCurrencyCode());
            final IncomeAndExpenseBookingData incomeAndExpBookingData= this.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList, closureCommand, true,glAccount,office);
            if(incomeAndExpBookingData.getJournalEntries().size() > 0){ incomeAndExpenseBookingCollection.add(incomeAndExpBookingData);}
        }
        return incomeAndExpenseBookingCollection;
    }

    public IncomeAndExpenseBookingData bookOffIncomeAndExpense(final List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJournalEntryDataList,
                                                                final GLClosureCommand closureData,final boolean preview,final GLAccount glAccount,final Office office){
        /* All running balances has to be calculated before booking off income and expense account */
        boolean isRunningBalanceCalculated = true;
        for(final IncomeAndExpenseJournalEntryData incomeAndExpenseData :incomeAndExpenseJournalEntryDataList ){
            if(!incomeAndExpenseData.isRunningBalanceCalculated()){ throw new RunningBalanceNotCalculatedException(incomeAndExpenseData.getOfficeId());}
        }
        BigDecimal debits = BigDecimal.ZERO;
        BigDecimal credits = BigDecimal.ZERO;

        final List<SingleDebitOrCreditEntryData>  debitsJournalEntry = new ArrayList<>();
        final List<SingleDebitOrCreditEntryData>  creditsJournalEntry = new ArrayList<>();

        for(final IncomeAndExpenseJournalEntryData incomeAndExpense : incomeAndExpenseJournalEntryDataList){
            if(incomeAndExpense.isIncomeAccountType()){
                if(incomeAndExpense.getOfficeRunningBalance().signum() == 1){
                    debits = debits.add(incomeAndExpense.getOfficeRunningBalance());
                    debitsJournalEntry.add(new SingleDebitOrCreditEntryData(incomeAndExpense.getAccountId(),incomeAndExpense.getGlAccountName(),incomeAndExpense.getOfficeRunningBalance(),null));
                }else{
                    credits= credits.add(incomeAndExpense.getOfficeRunningBalance().abs());
                    creditsJournalEntry.add(new SingleDebitOrCreditEntryData(incomeAndExpense.getAccountId(),incomeAndExpense.getGlAccountName(),incomeAndExpense.getOfficeRunningBalance().abs(),null));;
                }
            }
            if(incomeAndExpense.isExpenseAccountType()){
                if(incomeAndExpense.getOfficeRunningBalance().signum() == 1){
                    credits = credits.add(incomeAndExpense.getOfficeRunningBalance());
                    creditsJournalEntry.add(new SingleDebitOrCreditEntryData(incomeAndExpense.getAccountId(),incomeAndExpense.getGlAccountName(),incomeAndExpense.getOfficeRunningBalance().abs(),null));;
                }else{
                    debits= debits.add(incomeAndExpense.getOfficeRunningBalance().abs());
                    debitsJournalEntry.add(new SingleDebitOrCreditEntryData(incomeAndExpense.getAccountId(),incomeAndExpense.getGlAccountName(),incomeAndExpense.getOfficeRunningBalance().abs(),null));
                }
            }
        }
        final LocalDate today = DateUtils.getLocalDateOfTenant();
        final int compare = debits.compareTo(credits);
        BigDecimal difference = BigDecimal.ZERO;
        JournalEntryData journalEntry = null;
        if(compare == 1){
            /* book with target gl id on the credit side */
            difference = debits.subtract(credits);
            SingleDebitOrCreditEntryData targetBooking = new SingleDebitOrCreditEntryData(closureData.getEquityGlAccountId(),glAccount.getName(),difference,null);
            creditsJournalEntry.add(targetBooking);
            journalEntry = new JournalEntryData(office.getId(),today.toString(),closureData.getComments(),creditsJournalEntry,debitsJournalEntry,null,false,closureData.getCurrencyCode(),office.getName());
        }else if(compare == -1){
            /* book with target gl id on the debit side*/
            difference = credits.subtract(debits);
            SingleDebitOrCreditEntryData targetBooking = new SingleDebitOrCreditEntryData(closureData.getEquityGlAccountId(),glAccount.getName(),difference,null);
            debitsJournalEntry.add(targetBooking);
            journalEntry = new JournalEntryData(office.getId(),today.toString(),closureData.getComments(),creditsJournalEntry,debitsJournalEntry,null,false,closureData.getCurrencyCode(),office.getName());
        }
        else if(compare == 0){
            //throw new RunningBalanceZeroException(office.getName());
        }
        final LocalDate localDate = LocalDate.now();
        final List<JournalEntryData> journalEntries = new ArrayList<>();
        if(journalEntry !=null){ journalEntries.add(journalEntry);}

        return new IncomeAndExpenseBookingData(localDate,closureData.getComments(),journalEntries);
    }
}
