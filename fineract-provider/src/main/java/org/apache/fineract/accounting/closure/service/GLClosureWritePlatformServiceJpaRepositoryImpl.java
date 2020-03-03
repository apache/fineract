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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.closure.api.GLClosureJsonInputParams;
import org.apache.fineract.accounting.closure.command.GLClosureCommand;
import org.apache.fineract.accounting.closure.data.IncomeAndExpenseJournalEntryData;
import org.apache.fineract.accounting.closure.domain.GLClosure;
import org.apache.fineract.accounting.closure.domain.GLClosureRepository;
import org.apache.fineract.accounting.closure.domain.IncomeAndExpenseBooking;
import org.apache.fineract.accounting.closure.domain.IncomeAndExpenseBookingRepository;
import org.apache.fineract.accounting.closure.exception.GLClosureDuplicateException;
import org.apache.fineract.accounting.closure.exception.GLClosureInvalidDeleteException;
import org.apache.fineract.accounting.closure.exception.GLClosureInvalidException;
import org.apache.fineract.accounting.closure.exception.GLClosureInvalidException.GL_CLOSURE_INVALID_REASON;
import org.apache.fineract.accounting.closure.exception.GLClosureNotFoundException;
import org.apache.fineract.accounting.closure.exception.RunningBalanceNotCalculatedException;
import org.apache.fineract.accounting.closure.serialization.GLClosureCommandFromApiJsonDeserializer;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.exception.GLAccountNotFoundException;
import org.apache.fineract.accounting.journalentry.command.JournalEntryCommand;
import org.apache.fineract.accounting.journalentry.command.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class GLClosureWritePlatformServiceJpaRepositoryImpl implements GLClosureWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(GLClosureWritePlatformServiceJpaRepositoryImpl.class);

    private final GLClosureRepository glClosureRepository;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final GLClosureCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final GLAccountRepository glAccountRepository;
    private final GLClosureReadPlatformService glClosureReadPlatformService;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final IncomeAndExpenseBookingRepository incomeAndExpenseBookingRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final IncomeAndExpenseReadPlatformService incomeAndExpenseReadPlatformService;

    @Autowired
    public GLClosureWritePlatformServiceJpaRepositoryImpl(final GLClosureRepository glClosureRepository,
            final OfficeRepositoryWrapper officeRepositoryWrapper, final GLClosureCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final GLAccountRepository glAccountRepository,final GLClosureReadPlatformService glClosureReadPlatformService,
            final JournalEntryWritePlatformService journalEntryWritePlatformService, final IncomeAndExpenseBookingRepository incomeAndExpenseBookingRepository,
            final FromJsonHelper fromApiJsonHelper,final IncomeAndExpenseReadPlatformService incomeAndExpenseReadPlatformService,
            final OfficeReadPlatformService officeReadPlatformService) {
        this.glClosureRepository = glClosureRepository;
        this.officeRepositoryWrapper = officeRepositoryWrapper;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.glAccountRepository  = glAccountRepository;
        this.glClosureReadPlatformService = glClosureReadPlatformService;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.incomeAndExpenseBookingRepository = incomeAndExpenseBookingRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.incomeAndExpenseReadPlatformService = incomeAndExpenseReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult createGLClosure(final JsonCommand command) {
        try {
            final GLClosureCommand closureCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            closureCommand.validateForCreate();

            // check office is valid
            final Long officeId = command.longValueOfParameterNamed(GLClosureJsonInputParams.OFFICE_ID.getValue());
            final Office office = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
            // TODO: Get Tenant specific date
            // ensure closure date is not in the future
            final Date todaysDate = new Date();
            final Date closureDate = command.DateValueOfParameterNamed(GLClosureJsonInputParams.CLOSING_DATE.getValue());
            if (closureDate.after(todaysDate)) { throw new GLClosureInvalidException(GL_CLOSURE_INVALID_REASON.FUTURE_DATE, closureDate); }
            // shouldn't be before an existing accounting closure
            final GLClosure latestGLClosure = this.glClosureRepository.getLatestGLClosureByBranch(officeId);
            if (latestGLClosure != null) {
                if (latestGLClosure.getClosingDate().after(closureDate)) { throw new GLClosureInvalidException(
                        GL_CLOSURE_INVALID_REASON.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate()); }
            }
            final GLClosure glClosure = GLClosure.fromJson(office, command);
            final Collection<Long> childOfficesByHierarchy = this.officeReadPlatformService.officeByHierarchy(officeId);
            if(closureCommand.getSubBranches() && childOfficesByHierarchy.size() > 0){
                for(final Long childOffice : childOfficesByHierarchy){
                    final GLClosure latestChildGlClosure = this.glClosureRepository.getLatestGLClosureByBranch(childOffice);
                    if (latestChildGlClosure != null) {
                        if (latestChildGlClosure.getClosingDate().after(closureDate)) { throw new GLClosureInvalidException(
                                GL_CLOSURE_INVALID_REASON.ACCOUNTING_CLOSED, latestChildGlClosure.getClosingDate()); }
                    }
                }
            }
            final boolean bookOffIncomeAndExpense = command.booleanPrimitiveValueOfParameterNamed(GLClosureJsonInputParams.BOOK_OFF_INCOME_AND_EXPENSE.getValue());
            final LocalDate incomeAndExpenseBookOffDate = LocalDate.fromDateFields(closureDate);
            if(bookOffIncomeAndExpense){
                final Long equityGlAccountId = command.longValueOfParameterNamed(GLClosureJsonInputParams.EQUITY_GL_ACCOUNT_ID.getValue());

                final GLAccount glAccount= this.glAccountRepository.findByParent(equityGlAccountId);

                if(glAccount == null){throw new GLAccountNotFoundException(equityGlAccountId);}

                if(closureCommand.getSubBranches() && childOfficesByHierarchy.size() > 0){
                    final List<GLClosure> subOfficesGlClosure = new ArrayList<>();
                    final HashMap<Long,GLClosure> officeGlClosure = new HashMap<>();

                    final HashMap<Long,List<IncomeAndExpenseJournalEntryData>> allIncomeAndExpenseJLWithSubBranches = new HashMap<>();
                    for(final Long childOffice : childOfficesByHierarchy){
                        final GLClosure gl = GLClosure.fromJson(this.officeRepositoryWrapper.findOneWithNotFoundDetection(childOffice), command); subOfficesGlClosure.add(gl);
                        final List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJournalEntryDataList = this.incomeAndExpenseReadPlatformService.retrieveAllIncomeAndExpenseJournalEntryData(childOffice, incomeAndExpenseBookOffDate,closureCommand.getCurrencyCode());
                        officeGlClosure.put(childOffice,gl);
                        allIncomeAndExpenseJLWithSubBranches.put(childOffice,incomeAndExpenseJournalEntryDataList);
                    }
                    this.handleRunningBalanceNotCalculated(allIncomeAndExpenseJLWithSubBranches);
                    /*  add main office to the subBranches list*/
                    subOfficesGlClosure.add(glClosure); officeGlClosure.put(officeId,glClosure);


                    /* create journal entry for each office */
                    for(final Long childOffice : childOfficesByHierarchy){
                        final List<IncomeAndExpenseJournalEntryData> incomeAndExpJL = allIncomeAndExpenseJLWithSubBranches.get(childOffice);
                        final Office hierarchyOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(childOffice);
                        final String transactionId = this.bookOffIncomeAndExpense(incomeAndExpJL,closureCommand,glAccount,hierarchyOffice);
                        IncomeAndExpenseBooking incomeAndExpenseBooking = null;
                        if(transactionId !=null){incomeAndExpenseBooking = IncomeAndExpenseBooking.createNew(officeGlClosure.get(childOffice),transactionId,hierarchyOffice,false);}
                        this.glClosureRepository.saveAndFlush(officeGlClosure.get(childOffice));
                        if(incomeAndExpenseBooking != null){ this.incomeAndExpenseBookingRepository.saveAndFlush(incomeAndExpenseBooking);}
                    }
                }else{
                    final List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJournalEntryDataList = this.incomeAndExpenseReadPlatformService.retrieveAllIncomeAndExpenseJournalEntryData(officeId, incomeAndExpenseBookOffDate,closureCommand.getCurrencyCode());
                    String transactionId = this.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList,closureCommand,glAccount,office);
                    this.glClosureRepository.saveAndFlush(glClosure);
                    if(transactionId != null){
                        final IncomeAndExpenseBooking incomeAndExpenseBooking = IncomeAndExpenseBooking.createNew(glClosure,transactionId,office,false);
                        this.incomeAndExpenseBookingRepository.saveAndFlush(incomeAndExpenseBooking);
                    }

                }

            }else{
                this.glClosureRepository.saveAndFlush(glClosure);
                /* save subBranches closure if parameter is passed and is true*/
                if(closureCommand.getSubBranches() && childOfficesByHierarchy.size() > 0){
                    for(final Long childOffice : childOfficesByHierarchy){
                        final GLClosure subBranchesGlClosure = GLClosure.fromJson(this.officeRepositoryWrapper.findOneWithNotFoundDetection(childOffice), command);
                        this.glClosureRepository.saveAndFlush(subBranchesGlClosure);
                    }
                }
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(officeId)
                    .withEntityId(glClosure.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleGLClosureIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateGLClosure(final Long glClosureId, final JsonCommand command) {
        final GLClosureCommand closureCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        closureCommand.validateForUpdate();

        // is the glClosure valid
        final GLClosure glClosure = this.glClosureRepository.findById(glClosureId).orElseThrow(() -> new GLClosureNotFoundException(glClosureId));

        final Map<String, Object> changesOnly = glClosure.update(command);

        if (!changesOnly.isEmpty()) {
            this.glClosureRepository.saveAndFlush(glClosure);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(glClosure.getOffice().getId())
                .withEntityId(glClosure.getId()).with(changesOnly).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteGLClosure(final Long glClosureId, final JsonCommand command) {
        final GLClosure glClosure = this.glClosureRepository.findById(glClosureId)
                .orElseThrow(() -> new GLClosureNotFoundException(glClosureId));


        /**
         * check if any closures are present for this branch at a later date
         * than this closure date
         **/
        final Date closureDate = glClosure.getClosingDate();
        final GLClosure latestGLClosure = this.glClosureRepository.getLatestGLClosureByBranch(glClosure.getOffice().getId());
        if (latestGLClosure.getClosingDate().after(closureDate)) { throw new GLClosureInvalidDeleteException(latestGLClosure.getOffice()
                .getId(), latestGLClosure.getOffice().getName(), latestGLClosure.getClosingDate()); }
                if(fromApiJsonHelper.parameterExists(GLClosureJsonInputParams.REVERSE_INCOME_AND_EXPENSE_BOOKING.getValue(),command.parsedJson())){
            final Boolean reverseBookOffIncomeAndExpense = command.booleanObjectValueOfParameterNamed(GLClosureJsonInputParams.REVERSE_INCOME_AND_EXPENSE_BOOKING.getValue());
            if(reverseBookOffIncomeAndExpense !=null && reverseBookOffIncomeAndExpense){
                final IncomeAndExpenseBooking incomeAndExpenseBooking = this.incomeAndExpenseBookingRepository.findByGlClosureAndReversedIsFalse(glClosure);
                if(incomeAndExpenseBooking !=null){
                    this.journalEntryWritePlatformService.revertJournalEntry(incomeAndExpenseBooking.getTransactionId());
                    incomeAndExpenseBooking.updateReversed(true);
                }
            }
        }

        glClosure.updateDeleted(true);
        return new CommandProcessingResultBuilder().withOfficeId(glClosure.getOffice().getId()).withEntityId(glClosure.getId()).build();
    }

    /**
     * @param command
     * @param dve
     */
    private void handleGLClosureIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("office_id_closing_date")) { throw new GLClosureDuplicateException(
                command.longValueOfParameterNamed(GLClosureJsonInputParams.OFFICE_ID.getValue()), new LocalDate(
                        command.DateValueOfParameterNamed(GLClosureJsonInputParams.CLOSING_DATE.getValue()))); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.glClosure.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource GL Closure: " + realCause.getMessage());
    }
    private void handleRunningBalanceNotCalculated(final HashMap<Long,List<IncomeAndExpenseJournalEntryData>> allIncomeAndExpenseJLWithSubBranches){
        final Iterator<Map.Entry<Long,List<IncomeAndExpenseJournalEntryData>>> iterator = allIncomeAndExpenseJLWithSubBranches.entrySet().iterator();
        while(iterator.hasNext()){
            final List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJL = iterator.next().getValue();
            for(final IncomeAndExpenseJournalEntryData incomeAndExpenseData :incomeAndExpenseJL ){
                if(!incomeAndExpenseData.isRunningBalanceCalculated()){ throw new RunningBalanceNotCalculatedException(incomeAndExpenseData.getOfficeId());}
            }
        }
    }
    private String bookOffIncomeAndExpense(final List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJournalEntryDataList,
            final GLClosureCommand closureData,final GLAccount glAccount, final Office office){
        /* All running balances has to be calculated before booking off income and expense account */
        for(final IncomeAndExpenseJournalEntryData incomeAndExpenseData :incomeAndExpenseJournalEntryDataList ){
            if(!incomeAndExpenseData.isRunningBalanceCalculated()){ throw new RunningBalanceNotCalculatedException(incomeAndExpenseData.getOfficeId());}
        }
        BigDecimal debits = BigDecimal.ZERO;
        BigDecimal credits = BigDecimal.ZERO;

        int i = 0;
        int j = 0;
        for(final IncomeAndExpenseJournalEntryData incomeAndExpense : incomeAndExpenseJournalEntryDataList){
            if(incomeAndExpense.isIncomeAccountType()){
                if(incomeAndExpense.getOfficeRunningBalance().signum() == 1){debits = debits.add(incomeAndExpense.getOfficeRunningBalance());i++;}
                else{ credits = credits.add(incomeAndExpense.getOfficeRunningBalance().abs());j++;}
            }
            if(incomeAndExpense.isExpenseAccountType()){
                if(incomeAndExpense.getOfficeRunningBalance().signum() == 1){credits = credits.add(incomeAndExpense.getOfficeRunningBalance()); j++;}
                else{debits= debits.add(incomeAndExpense.getOfficeRunningBalance().abs());i++;}
            }
        }
        final int compare = debits.compareTo(credits);
        BigDecimal difference = BigDecimal.ZERO;
        JournalEntryCommand journalEntryCommand = null;
        if(compare ==1){ j++;}else{ i++;}

        SingleDebitOrCreditEntryCommand[]  debitsJournalEntry  = new SingleDebitOrCreditEntryCommand[i];
        SingleDebitOrCreditEntryCommand[]  creditsJournalEntry  = new SingleDebitOrCreditEntryCommand[j];
        int m=0; int n=0;
        for(final IncomeAndExpenseJournalEntryData incomeAndExpense : incomeAndExpenseJournalEntryDataList){
            if(incomeAndExpense.isIncomeAccountType()){
                if(incomeAndExpense.getOfficeRunningBalance().signum() == 1){
                    debitsJournalEntry[m] = new SingleDebitOrCreditEntryCommand(null,incomeAndExpense.getAccountId(),incomeAndExpense.getOfficeRunningBalance().abs(),null);m++;
                }else{
                    creditsJournalEntry[n]= new SingleDebitOrCreditEntryCommand(null,incomeAndExpense.getAccountId(),incomeAndExpense.getOfficeRunningBalance().abs(),null);n++;
                }
            }
            if(incomeAndExpense.isExpenseAccountType()){
                if(incomeAndExpense.getOfficeRunningBalance().signum() == 1){
                    creditsJournalEntry[n]= new SingleDebitOrCreditEntryCommand(null,incomeAndExpense.getAccountId(),incomeAndExpense.getOfficeRunningBalance().abs(),null);n++;
                }else{
                    debitsJournalEntry[m]= new SingleDebitOrCreditEntryCommand(null,incomeAndExpense.getAccountId(),incomeAndExpense.getOfficeRunningBalance().abs(),null);m++;
                }
            }
        }
        String transactionId = null;
        if(compare == 1){
            /* book with target gl id on the credit side */
            difference = debits.subtract(credits);
            final SingleDebitOrCreditEntryCommand targetBooking = new SingleDebitOrCreditEntryCommand(null,closureData.getEquityGlAccountId(),difference,null);
            creditsJournalEntry[n] = targetBooking;
            journalEntryCommand = new JournalEntryCommand(office.getId(),closureData.getCurrencyCode(),closureData.getClosingDate(),closureData.getComments(),creditsJournalEntry,debitsJournalEntry,closureData.getIncomeAndExpenseComments(),
                    null,null,null,null,null,null,null,null);
            transactionId = this.journalEntryWritePlatformService.createJournalEntryForIncomeAndExpenseBookOff(journalEntryCommand);

        }else if(compare == -1){
            /* book with target gl id on the debit side*/
            difference = credits.subtract(debits);
            final SingleDebitOrCreditEntryCommand targetBooking = new SingleDebitOrCreditEntryCommand(null,closureData.getEquityGlAccountId(),difference,null);
            debitsJournalEntry[m]= targetBooking;
            journalEntryCommand = new JournalEntryCommand(office.getId(),closureData.getCurrencyCode(),closureData.getClosingDate(),closureData.getComments(),creditsJournalEntry,debitsJournalEntry,closureData.getIncomeAndExpenseComments(),
                    null,null,null,null,null,null,null,null);
            transactionId = this.journalEntryWritePlatformService.createJournalEntryForIncomeAndExpenseBookOff(journalEntryCommand);
        }else if(compare == 0){
            //throw new RunningBalanceZeroException(office.getName());
            return null;
        }
        return transactionId;

    }

}
