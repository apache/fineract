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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.joda.time.LocalDate;

/**
 * Immutable data object representing a loan transaction.
 */
public class LoanTransactionData {

    private final Long id;
    private final Long officeId;
    private final String officeName;

    private final LoanTransactionEnumData type;

    private final LocalDate date;

    private final CurrencyData currency;
    private final PaymentDetailData paymentDetailData;

    private final BigDecimal amount;
    private final BigDecimal principalPortion;
    private final BigDecimal interestPortion;
    private final BigDecimal feeChargesPortion;
    private final BigDecimal penaltyChargesPortion;
    private final BigDecimal overpaymentPortion;
    private final BigDecimal unrecognizedIncomePortion;
    private final String externalId;
    private final AccountTransferData transfer;
    private final BigDecimal fixedEmiAmount;
    private final BigDecimal outstandingLoanBalance;
    @SuppressWarnings("unused")
    private final LocalDate submittedOnDate;
    private final boolean manuallyReversed;
    @SuppressWarnings("unused")
	private final LocalDate possibleNextRepaymentDate;

    // templates
    final Collection<PaymentTypeData> paymentTypeOptions;
    
    private  Collection<CodeValueData> writeOffReasonOptions = null;

    //import fields
    private transient Integer rowIndex;
    private String dateFormat;
    private String locale;
    private BigDecimal transactionAmount;
    private LocalDate transactionDate;
    private Long paymentTypeId;
    private String accountNumber;
    private Integer checkNumber;
    private Integer routingCode;
    private Integer receiptNumber;
    private Integer bankNumber;
    private transient Integer accountId;
    private transient String transactionType;

    public static LoanTransactionData importInstance(BigDecimal repaymentAmount,LocalDate lastRepaymentDate,
            Long repaymentTypeId,Integer rowIndex,String locale,String dateFormat){
        return new LoanTransactionData(repaymentAmount, lastRepaymentDate, repaymentTypeId, rowIndex,locale,dateFormat);
    }
    private LoanTransactionData(BigDecimal transactionAmount,LocalDate transactionDate,
            Long paymentTypeId,Integer rowIndex,String locale,String dateFormat) {
        this.transactionAmount=transactionAmount;
        this.transactionDate=transactionDate;
        this.paymentTypeId=paymentTypeId;
        this.rowIndex = rowIndex;
        this.dateFormat= dateFormat;
        this.locale= locale;
        this.amount = null;
        this.date = null;
        this.type = null;
        this.id =null;
        this.officeId = null;
        this.officeName = null;
        this.currency = null;
        this.paymentDetailData = null;
        this.principalPortion = null;
        this.interestPortion = null;
        this.feeChargesPortion = null;
        this.penaltyChargesPortion = null;
        this.overpaymentPortion = null;
        this.unrecognizedIncomePortion = null;
        this.externalId = null;
        this.transfer = null;
        this.fixedEmiAmount = null;
        this.outstandingLoanBalance = null;
        this.submittedOnDate = null;
        this.manuallyReversed = false;
        this.possibleNextRepaymentDate = null;
        this.paymentTypeOptions = null;
        this.writeOffReasonOptions = null;
    }
    public static LoanTransactionData importInstance(BigDecimal repaymentAmount,LocalDate repaymentDate,
            Long repaymentTypeId, String accountNumber,Integer checkNumber,Integer routingCode,
            Integer receiptNumber, Integer bankNumber,Integer loanAccountId,String transactionType,
            Integer rowIndex,String locale, String dateFormat){
        return new LoanTransactionData(repaymentAmount, repaymentDate, repaymentTypeId, accountNumber,
                checkNumber, routingCode, receiptNumber, bankNumber, loanAccountId, "",
                rowIndex,locale,dateFormat);
    }

    private LoanTransactionData(BigDecimal transactionAmount,LocalDate transactionDate, Long paymentTypeId,
            String accountNumber,Integer checkNumber,Integer routingCode,Integer receiptNumber,
            Integer bankNumber,Integer accountId,String transactionType,Integer rowIndex,String locale,
            String dateFormat) {
        this.transactionAmount = transactionAmount;
        this.transactionDate = transactionDate;
        this.paymentTypeId = paymentTypeId;
        this.accountNumber=accountNumber;
        this.checkNumber=checkNumber;
        this.routingCode=routingCode;
        this.receiptNumber=receiptNumber;
        this.bankNumber=bankNumber;
        this.accountId=accountId;
        this.transactionType=transactionType;
        this.rowIndex=rowIndex;
        this.dateFormat=dateFormat;
        this.locale= locale;
        this.id = null;
        this.officeId = null;
        this.officeName = null;
        this.type = null;
        this.date = null;
        this.currency = null;
        this.paymentDetailData = null;
        this.amount = null;
        this.principalPortion = null;
        this.interestPortion = null;
        this.feeChargesPortion = null;
        this.penaltyChargesPortion = null;
        this.overpaymentPortion = null;
        this.unrecognizedIncomePortion = null;
        this.externalId = null;
        this.transfer = null;
        this.fixedEmiAmount = null;
        this.outstandingLoanBalance = null;
        this.submittedOnDate = null;
        this.manuallyReversed = false;
        this.possibleNextRepaymentDate = null;
        this.paymentTypeOptions = null;
        this.writeOffReasonOptions = null;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public static LoanTransactionData templateOnTop(final LoanTransactionData loanTransactionData,
            final Collection<PaymentTypeData> paymentTypeOptions) {
        return new LoanTransactionData(loanTransactionData.id, loanTransactionData.officeId, loanTransactionData.officeName,
                loanTransactionData.type, loanTransactionData.paymentDetailData, loanTransactionData.currency, loanTransactionData.date,
                loanTransactionData.amount, loanTransactionData.principalPortion, loanTransactionData.interestPortion,
                loanTransactionData.feeChargesPortion, loanTransactionData.penaltyChargesPortion, loanTransactionData.overpaymentPortion,
                loanTransactionData.unrecognizedIncomePortion, paymentTypeOptions, loanTransactionData.externalId,
                loanTransactionData.transfer, loanTransactionData.fixedEmiAmount, loanTransactionData.outstandingLoanBalance,
                loanTransactionData.manuallyReversed);

    }

    public LoanTransactionData(final Long id, final Long officeId, final String officeName, final LoanTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final CurrencyData currency, final LocalDate date, final BigDecimal amount,
            final BigDecimal principalPortion, final BigDecimal interestPortion, final BigDecimal feeChargesPortion,
            final BigDecimal penaltyChargesPortion, final BigDecimal overpaymentPortion, final String externalId,
            final AccountTransferData transfer, BigDecimal fixedEmiAmount, BigDecimal outstandingLoanBalance,
            final BigDecimal unrecognizedIncomePortion,final boolean manuallyReversed) {
        this(id, officeId, officeName, transactionType, paymentDetailData, currency, date, amount, principalPortion, interestPortion,
                feeChargesPortion, penaltyChargesPortion, overpaymentPortion, unrecognizedIncomePortion, null, externalId, transfer,
                fixedEmiAmount, outstandingLoanBalance,manuallyReversed);
    }
 
    public LoanTransactionData(final Long id, final Long officeId, final String officeName, final LoanTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final CurrencyData currency, final LocalDate date, final BigDecimal amount,
            final BigDecimal principalPortion, final BigDecimal interestPortion, final BigDecimal feeChargesPortion,
            final BigDecimal penaltyChargesPortion, final BigDecimal overpaymentPortion, BigDecimal unrecognizedIncomePortion,
            final Collection<PaymentTypeData> paymentTypeOptions, final String externalId, final AccountTransferData transfer,
            final BigDecimal fixedEmiAmount, BigDecimal outstandingLoanBalance,boolean manuallyReversed) {
        this(id, officeId, officeName, transactionType, paymentDetailData, currency, date, amount, principalPortion, interestPortion,
                feeChargesPortion, penaltyChargesPortion, overpaymentPortion, unrecognizedIncomePortion, paymentTypeOptions, externalId,
                transfer, fixedEmiAmount, outstandingLoanBalance, null,manuallyReversed);
    }

    public LoanTransactionData(final Long id, final Long officeId, final String officeName, final LoanTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final CurrencyData currency, final LocalDate date, final BigDecimal amount,
            final BigDecimal principalPortion, final BigDecimal interestPortion, final BigDecimal feeChargesPortion,
            final BigDecimal penaltyChargesPortion, final BigDecimal overpaymentPortion, final BigDecimal unrecognizedIncomePortion,
            final String externalId, final AccountTransferData transfer, BigDecimal fixedEmiAmount, BigDecimal outstandingLoanBalance,
            LocalDate submittedOnDate,final boolean manuallyReversed) {
        this(id, officeId, officeName, transactionType, paymentDetailData, currency, date, amount, principalPortion, interestPortion,
                feeChargesPortion, penaltyChargesPortion, overpaymentPortion, unrecognizedIncomePortion, null, externalId, transfer,
                fixedEmiAmount, outstandingLoanBalance, submittedOnDate,manuallyReversed);
    }

    public LoanTransactionData(final Long id, final Long officeId, final String officeName, final LoanTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final CurrencyData currency, final LocalDate date, final BigDecimal amount,
            final BigDecimal principalPortion, final BigDecimal interestPortion, final BigDecimal feeChargesPortion,
            final BigDecimal penaltyChargesPortion, final BigDecimal overpaymentPortion, final BigDecimal unrecognizedIncomePortion,
            final Collection<PaymentTypeData> paymentTypeOptions, final String externalId, final AccountTransferData transfer,
            final BigDecimal fixedEmiAmount, BigDecimal outstandingLoanBalance, final LocalDate submittedOnDate,final boolean manuallyReversed) {
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.type = transactionType;
        this.paymentDetailData = paymentDetailData;
        this.currency = currency;
        this.date = date;
        this.amount = amount;
        this.principalPortion = principalPortion;
        this.interestPortion = interestPortion;
        this.feeChargesPortion = feeChargesPortion;
        this.penaltyChargesPortion = penaltyChargesPortion;
        this.unrecognizedIncomePortion = unrecognizedIncomePortion;
        this.paymentTypeOptions = paymentTypeOptions;
        this.externalId = externalId;
        this.transfer = transfer;
        this.overpaymentPortion = overpaymentPortion;
        this.fixedEmiAmount = fixedEmiAmount;
        this.outstandingLoanBalance = outstandingLoanBalance;
        this.submittedOnDate = submittedOnDate;
        this.manuallyReversed = manuallyReversed;
        this.possibleNextRepaymentDate = null;
    }

    public LoanTransactionData(Long id, LoanTransactionEnumData transactionType, LocalDate date, BigDecimal totalAmount,
            BigDecimal principalPortion, BigDecimal interestPortion, BigDecimal feeChargesPortion, BigDecimal penaltyChargesPortion,
            BigDecimal overPaymentPortion, BigDecimal unrecognizedIncomePortion, BigDecimal outstandingLoanBalance,final boolean manuallyReversed) {
        this(id, null, null, transactionType, null, null, date, totalAmount, principalPortion, interestPortion, feeChargesPortion,
                penaltyChargesPortion, overPaymentPortion, unrecognizedIncomePortion, null, null, null, null, outstandingLoanBalance, null,
                manuallyReversed);
    }
    
    public static LoanTransactionData LoanTransactionDataForDisbursalTemplate(final LoanTransactionEnumData transactionType, final LocalDate expectedDisbursedOnLocalDateForTemplate, 
			final BigDecimal disburseAmountForTemplate,	final Collection<PaymentTypeData> paymentOptions,
			final BigDecimal retriveLastEmiAmount, final LocalDate possibleNextRepaymentDate) {
		    final Long id = null;
		    final Long officeId = null;
		    final String officeName = null;
		    final PaymentDetailData paymentDetailData = null;
		    final CurrencyData currency = null;
		    final BigDecimal unrecognizedIncomePortion = null;
		    final BigDecimal principalPortion = null;;
		    final BigDecimal interestPortion = null;
		    final BigDecimal feeChargesPortion = null;
		    final BigDecimal penaltyChargesPortion = null;
		    final BigDecimal overpaymentPortion = null;
		    final String externalId = null;
		    final BigDecimal outstandingLoanBalance = null;
		    final AccountTransferData transfer = null;
		    final LocalDate submittedOnDate = null;
		    final boolean manuallyReversed = false;
			return new LoanTransactionData(id, officeId, officeName, transactionType, paymentDetailData, currency, expectedDisbursedOnLocalDateForTemplate,
					disburseAmountForTemplate, principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion, overpaymentPortion,	unrecognizedIncomePortion, 
					paymentOptions, transfer, externalId, retriveLastEmiAmount, outstandingLoanBalance, submittedOnDate, manuallyReversed, possibleNextRepaymentDate);
		
	}

    private LoanTransactionData(Long id , final Long officeId, final String officeName, LoanTransactionEnumData transactionType, final PaymentDetailData paymentDetailData,
    		final CurrencyData currency, final LocalDate date,	BigDecimal amount, final BigDecimal principalPortion, final BigDecimal interestPortion, 
    		final BigDecimal feeChargesPortion, final BigDecimal penaltyChargesPortion, final BigDecimal overpaymentPortion, BigDecimal unrecognizedIncomePortion,	Collection<PaymentTypeData> paymentOptions,
    		final AccountTransferData transfer, final String externalId, final BigDecimal fixedEmiAmount, BigDecimal outstandingLoanBalance, 
    		final LocalDate submittedOnDate, final boolean manuallyReversed, final LocalDate possibleNextRepaymentDate) {
    	 this.id = id;
         this.officeId = officeId;
         this.officeName = officeName;
         this.type = transactionType;
         this.paymentDetailData = paymentDetailData;
         this.currency = currency;
         this.date = date;
         this.amount = amount;
         this.principalPortion = principalPortion;
         this.interestPortion = interestPortion;
         this.feeChargesPortion = feeChargesPortion;
         this.penaltyChargesPortion = penaltyChargesPortion;
         this.unrecognizedIncomePortion = unrecognizedIncomePortion;
         this.paymentTypeOptions = paymentOptions;
         this.externalId = externalId;
         this.transfer = transfer;
         this.overpaymentPortion = overpaymentPortion;
         this.fixedEmiAmount = fixedEmiAmount;
         this.outstandingLoanBalance = outstandingLoanBalance;
         this.submittedOnDate = submittedOnDate;
         this.manuallyReversed = manuallyReversed;
         this.possibleNextRepaymentDate = possibleNextRepaymentDate;
	}

	

	public LocalDate dateOf() {
        return this.date;
    }

    public boolean isNotDisbursement() {
        return Integer.valueOf(1).equals(this.type.id());
    }

    
    public BigDecimal getAmount() {
        return this.amount;
    }

    
    public BigDecimal getUnrecognizedIncomePortion() {
        return this.unrecognizedIncomePortion;
    }

    
    public BigDecimal getInterestPortion() {
        return this.interestPortion;
    }
    
    public void setWriteOffReasonOptions(Collection<CodeValueData> writeOffReasonOptions){
    	this.writeOffReasonOptions =writeOffReasonOptions;
    }
}