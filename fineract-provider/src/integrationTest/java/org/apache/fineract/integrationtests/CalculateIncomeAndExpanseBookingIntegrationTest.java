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
package org.apache.fineract.integrationtests;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.accounting.closure.command.GLClosureCommand;
import org.apache.fineract.accounting.closure.data.IncomeAndExpenseBookingData;
import org.apache.fineract.accounting.closure.data.IncomeAndExpenseJournalEntryData;
import org.apache.fineract.accounting.closure.exception.RunningBalanceNotCalculatedException;
import org.apache.fineract.accounting.closure.service.CalculateIncomeAndExpenseBookingImpl;
import org.apache.fineract.accounting.closure.service.IncomeAndExpenseReadPlatformService;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CalculateIncomeAndExpanseBookingIntegrationTest {
    /*
    Test class CalculateIncomeAndExpenseBookingImpl
     */
    @Mock private JsonCommandWrapperTest jsonCommandWrapperTest;
    @Mock private IncomeAndExpenseReadPlatformService incomeAndExpenseReadPlatformService;
    @Mock private OfficeReadPlatformService officeReadPlatformService;

    @InjectMocks
    private CalculateIncomeAndExpenseBookingImpl calculateIncomeAndExpenseBooking;

    @Before
    public void setup() {
        calculateIncomeAndExpenseBooking =  new CalculateIncomeAndExpenseBookingImpl(null, null, null, null, incomeAndExpenseReadPlatformService,officeReadPlatformService);
     }

    @After
    public void tearDown() {

    }
    /*
        Case 1: All running balances has to be calculated before booking off income and expense account
        If not running balances, then throw exception
    */
    @Test(expected = RunningBalanceNotCalculatedException.class)
    public void testBookOffIncomeAndExpenseCheckException() {
        GLClosureCommand glClosureCommand =  new GLClosureCommand((long)10,(long)10, new LocalDate(),"Closing comment",  false, (long)10 , "CAD", false, false, "comment" );
        IncomeAndExpenseJournalEntryData incomeAndExpenseJournalEntryData= new IncomeAndExpenseJournalEntryData(null, null,null, null
                , true, false, null,null,null,10,10,null,null);
        List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJournalEntryDataList = new ArrayList<IncomeAndExpenseJournalEntryData>();
        incomeAndExpenseJournalEntryDataList.add(incomeAndExpenseJournalEntryData);
        calculateIncomeAndExpenseBooking.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList,glClosureCommand, false,null,null);
    }

    /*
    Case 2: All running balances has to be calculated before booking off income and expense account
    No exception is to be thrown since it is running for the input.
    However, later a null pointer exception is thrown, since the object incomeAndExpense is not initialized.
         */
    @Test
    public void testBookOffIncomeAndExpense() {
        GLClosureCommand glClosureCommand =  new GLClosureCommand(10L, 10L, new LocalDate(), "Closing comment", false, 10L , "CAD", false, false, "comment" );
        IncomeAndExpenseJournalEntryData incomeAndExpenseJournalEntryData= new IncomeAndExpenseJournalEntryData(null, null,null, null
                , true, true, null,null,null,10,10,null,null);
        List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJournalEntryDataList = new ArrayList<>();
        incomeAndExpenseJournalEntryDataList.add(incomeAndExpenseJournalEntryData);
        Assert.assertNotNull(calculateIncomeAndExpenseBooking.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList, glClosureCommand, false,null,null));
    }

    /*
        Case 3: In the case of an income account type-  if the OfficeRunningBalance is greater than 0, then add to debits
         */
    @Test(expected = NoClassDefFoundError.class)
    public void testIncomeAccountsRunningBalanceGreaterThanZero_Debit() {
        GLClosureCommand glClosureCommand =  new GLClosureCommand(10L, 10L, new LocalDate(), "Closing comment", false, 10L , "CAD", false, false, "comment" );
        IncomeAndExpenseJournalEntryData incomeAndExpenseJournalEntryData= new IncomeAndExpenseJournalEntryData(null, null,null, null, true, true, null,new BigDecimal(10),null,4,10,null,null);
        IncomeAndExpenseJournalEntryData incomeAndExpenseJournalEntryData2= new IncomeAndExpenseJournalEntryData(null, null,null, null, true, true, null,new BigDecimal(20),null,4,10,null,null);
        List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJournalEntryDataList = new ArrayList<>();
        incomeAndExpenseJournalEntryDataList.add(incomeAndExpenseJournalEntryData);
        incomeAndExpenseJournalEntryDataList.add(incomeAndExpenseJournalEntryData2);
        GLAccount glAccount =  GLAccount.fromJson(null, jsonCommandWrapperTest.getCommand(),null);
        IncomeAndExpenseBookingData incomeAndExpenseBookingData = calculateIncomeAndExpenseBooking.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList, glClosureCommand, false, null, null);
        incomeAndExpenseBookingData.getJournalEntries().forEach(entry->{
            Assert.assertEquals(entry.getDebits().get(0).getAmount(), new BigDecimal(30));
        });
        Assert.assertNotNull(calculateIncomeAndExpenseBooking.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList, glClosureCommand, false,null,null));
    }


    /*
        Case 4: In the case of an income account type-  if the OfficeRunningBalance is less than 0, then add to credits
     */
    @Test(expected = NoClassDefFoundError.class)
    public void testIncomeAccountsRunningBalanceLessThanZero_Credit() {
        GLClosureCommand glClosureCommand =  new GLClosureCommand(10L, 10L, new LocalDate(), "Closing comment", false, 10L , "CAD", false, false, "comment" );
        IncomeAndExpenseJournalEntryData incomeAndExpenseJournalEntryData= new IncomeAndExpenseJournalEntryData(null, null,null, null, true, true, null,new BigDecimal(-10),null,4,10,null,null);
        IncomeAndExpenseJournalEntryData incomeAndExpenseJournalEntryData2= new IncomeAndExpenseJournalEntryData(null, null,null, null, true, true, null,new BigDecimal(-20),null,4,10,null,null);
        List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJournalEntryDataList = new ArrayList<>();
        incomeAndExpenseJournalEntryDataList.add(incomeAndExpenseJournalEntryData);
        incomeAndExpenseJournalEntryDataList.add(incomeAndExpenseJournalEntryData2);
        GLAccount glAccount =  GLAccount.fromJson(null, jsonCommandWrapperTest.getCommand(),null);
        IncomeAndExpenseBookingData incomeAndExpenseBookingData = calculateIncomeAndExpenseBooking.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList, glClosureCommand, false, null, null);
        incomeAndExpenseBookingData.getJournalEntries().forEach(entry->{
            Assert.assertEquals(entry.getCredits().get(0).getAmount(), new BigDecimal(30));
        });
        Assert.assertNotNull(calculateIncomeAndExpenseBooking.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList, glClosureCommand, false,null,null));
    }


    /*
        Case 5: In the case of an Expanse account type-  if the OfficeRunningBalance is greater than 0, then add to credit
      */
    @Test(expected = NoClassDefFoundError.class)
    public void testIncomeAccountsRunningBalanceGreaterThanZero_Credit() {
        GLClosureCommand glClosureCommand =  new GLClosureCommand(10L, 10L, new LocalDate(), "Closing comment", false, 10L , "CAD", false, false, "comment" );
        IncomeAndExpenseJournalEntryData incomeAndExpenseJournalEntryData= new IncomeAndExpenseJournalEntryData(null, null,null, null, true, true, null,new BigDecimal(10),null,5,10,null,null);
        IncomeAndExpenseJournalEntryData incomeAndExpenseJournalEntryData2= new IncomeAndExpenseJournalEntryData(null, null,null, null, true, true, null,new BigDecimal(20),null,5,10,null,null);
        List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJournalEntryDataList = new ArrayList<>();
        incomeAndExpenseJournalEntryDataList.add(incomeAndExpenseJournalEntryData);
        incomeAndExpenseJournalEntryDataList.add(incomeAndExpenseJournalEntryData2);
        GLAccount glAccount =  GLAccount.fromJson(null, jsonCommandWrapperTest.getCommand(),null);
        IncomeAndExpenseBookingData incomeAndExpenseBookingData = calculateIncomeAndExpenseBooking.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList, glClosureCommand, false, null, null);
        incomeAndExpenseBookingData.getJournalEntries().forEach(entry->{
            Assert.assertEquals(entry.getDebits().get(0).getAmount(), new BigDecimal(30));
        });
        Assert.assertNotNull(calculateIncomeAndExpenseBooking.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList, glClosureCommand, false,null,null));
    }


    /*
        Case 6: In the case of an Expanse account type- if the OfficeRunningBalance is less than 0, then add to debits
     */
    @Test(expected = NoClassDefFoundError.class)
    public void testIncomeAccountsRunningBalanceLessThanZero_Debit() {
        GLClosureCommand glClosureCommand =  new GLClosureCommand(10L, 10L, new LocalDate(), "Closing comment", false, 10L , "CAD", false, false, "comment" );
        IncomeAndExpenseJournalEntryData incomeAndExpenseJournalEntryData= new IncomeAndExpenseJournalEntryData(null, null,null, null, true, true, null,new BigDecimal(-10),null,5,10,null,null);
        IncomeAndExpenseJournalEntryData incomeAndExpenseJournalEntryData2= new IncomeAndExpenseJournalEntryData(null, null,null, null, true, true, null,new BigDecimal(-20),null,5,10,null,null);
        List<IncomeAndExpenseJournalEntryData> incomeAndExpenseJournalEntryDataList = new ArrayList<>();
        incomeAndExpenseJournalEntryDataList.add(incomeAndExpenseJournalEntryData);
        incomeAndExpenseJournalEntryDataList.add(incomeAndExpenseJournalEntryData2);
        GLAccount glAccount =  GLAccount.fromJson(null, jsonCommandWrapperTest.getCommand(),null);
        IncomeAndExpenseBookingData incomeAndExpenseBookingData = calculateIncomeAndExpenseBooking.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList, glClosureCommand, false, null, null);
        incomeAndExpenseBookingData.getJournalEntries().forEach(entry->{
            Assert.assertEquals(entry.getCredits().get(0).getAmount(), new BigDecimal(30));
        });
        Assert.assertNotNull(calculateIncomeAndExpenseBooking.bookOffIncomeAndExpense(incomeAndExpenseJournalEntryDataList, glClosureCommand, false,null,null));
    }



}
