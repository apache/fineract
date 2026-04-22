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
package org.apache.fineract.commands.domain;

public final class CommandWrapperConstants {

    private CommandWrapperConstants() {}

    // Actions
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_GET = "GET";
    public static final String ACTION_SAVE = "SAVE";
    public static final String ACTION_PERMISSIONS = "PERMISSIONS";
    public static final String ACTION_CHANGEPWD = "CHANGEPWD";
    public static final String ACTION_RECOVERGUARANTEES = "RECOVERGUARANTEES";
    public static final String ACTION_APPROVE = "APPROVE";
    public static final String ACTION_REJECT = "REJECT";
    public static final String ACTION_APPROVALUNDO = "APPROVALUNDO";
    public static final String ACTION_DISBURSE = "DISBURSE";
    public static final String ACTION_DISBURSALUNDO = "DISBURSALUNDO";
    public static final String ACTION_ACTIVATE = "ACTIVATE";
    public static final String ACTION_CLOSE = "CLOSE";
    public static final String ACTION_WITHDRAW = "WITHDRAW";
    public static final String ACTION_REACTIVATE = "REACTIVATE";
    public static final String ACTION_PROPOSETRANSFER = "PROPOSETRANSFER";
    public static final String ACTION_PROPOSEANDACCEPTTRANSFER = "PROPOSEANDACCEPTTRANSFER";
    public static final String ACTION_WITHDRAWTRANSFER = "WITHDRAWTRANSFER";
    public static final String ACTION_ACCEPTTRANSFER = "ACCEPTTRANSFER";
    public static final String ACTION_REJECTTRANSFER = "REJECTTRANSFER";
    public static final String ACTION_UNDOREJECT = "UNDOREJECT";
    public static final String ACTION_UNDOWITHDRAWAL = "UNDOWITHDRAWAL";
    public static final String ACTION_WAIVE = "WAIVE";
    public static final String ACTION_PAY = "PAY";
    public static final String ACTION_ADJUSTMENT = "ADJUSTMENT";
    public static final String ACTION_DEACTIVATEOVERDUE = "DEACTIVATEOVERDUE";
    public static final String ACTION_REPAYMENT = "REPAYMENT";
    public static final String ACTION_MERCHANTISSUEDREFUND = "MERCHANTISSUEDREFUND";
    public static final String ACTION_PAYOUTREFUND = "PAYOUTREFUND";
    public static final String ACTION_GOODWILLCREDIT = "GOODWILLCREDIT";
    public static final String ACTION_INTERESTPAYMENTWAIVER = "INTERESTPAYMENTWAIVER";
    public static final String ACTION_CHARGEREFUND = "CHARGEREFUND";
    public static final String ACTION_RECOVERYPAYMENT = "RECOVERYPAYMENT";
    public static final String ACTION_WAIVEINTERESTPORTION = "WAIVEINTERESTPORTION";
    public static final String ACTION_WRITEOFF = "WRITEOFF";
    public static final String ACTION_UNDOWRITEOFF = "UNDOWRITEOFF";
    public static final String ACTION_CLOSEASRESCHEDULED = "CLOSEASRESCHEDULED";
    public static final String ACTION_ADJUST = "ADJUST";
    public static final String ACTION_REFUNDBYCASH = "REFUNDBYCASH";
    public static final String ACTION_CHARGEBACK = "CHARGEBACK";
    public static final String ACTION_FORECLOSURE = "FORECLOSURE";
    public static final String ACTION_CREDITBALANCEREFUND = "CREDITBALANCEREFUND";
    public static final String ACTION_UNDO = "UNDO";
    public static final String ACTION_BOUNCE = "BOUNCE";
    public static final String ACTION_SETFRAUD = "SETFRAUD";
    public static final String ACTION_UNDODISBURSAL = "UNDODISBURSAL";
    public static final String ACTION_UNDOAPPROVAL = "UNDOAPPROVAL";
    public static final String ACTION_DISBURSETOSAVINGS = "DISBURSETOSAVINGS";
    public static final String ACTION_DISBURSEWITHOUTAUTODOWNPAYMENT = "DISBURSEWITHOUTAUTODOWNPAYMENT";
    public static final String ACTION_DISBURSALLASTUNDO = "DISBURSALLASTUNDO";
    public static final String ACTION_UPDATELOANOFFICER = "UPDATELOANOFFICER";
    public static final String ACTION_REMOVELOANOFFICER = "REMOVELOANOFFICER";
    public static final String ACTION_BULKREASSIGN = "BULKREASSIGN";
    public static final String ACTION_UPDATEDELINQUENCY = "UPDATEDELINQUENCY";
    public static final String ACTION_EXECUTE = "EXECUTE";
    public static final String ACTION_REVERSE = "REVERSE";
    public static final String ACTION_UPDATERUNNINGBALANCE = "UPDATERUNNINGBALANCE";
    public static final String ACTION_DEFINEOPENINGBALANCE = "DEFINEOPENINGBALANCE";
    public static final String ACTION_DEPOSIT = "DEPOSIT";
    public static final String ACTION_WITHDRAWAL = "WITHDRAWAL";
    public static final String ACTION_UNDOTRANSACTION = "UNDOTRANSACTION";
    public static final String ACTION_REVERSETRANSACTION = "REVERSETRANSACTION";
    public static final String ACTION_ADJUSTTRANSACTION = "ADJUSTTRANSACTION";
    public static final String ACTION_CALCULATEINTEREST = "CALCULATEINTEREST";
    public static final String ACTION_POSTINTEREST = "POSTINTEREST";
    public static final String ACTION_APPLYANNUALFEE = "APPLYANNUALFEE";
    public static final String ACTION_INACTIVATE = "INACTIVATE";
    public static final String ACTION_SAVECOLLECTIONSHEET = "SAVECOLLECTIONSHEET";
    public static final String ACTION_ASSOCIATECLIENTS = "ASSOCIATECLIENTS";
    public static final String ACTION_DISASSOCIATECLIENTS = "DISASSOCIATECLIENTS";
    public static final String ACTION_TRANSFERCLIENTS = "TRANSFERCLIENTS";
    public static final String ACTION_UNASSIGNSTAFF = "UNASSIGNSTAFF";
    public static final String ACTION_ASSIGNSTAFF = "ASSIGNSTAFF";
    public static final String ACTION_ASSOCIATEGROUPS = "ASSOCIATEGROUPS";
    public static final String ACTION_DISASSOCIATEGROUPS = "DISASSOCIATEGROUPS";
    public static final String ACTION_ASSIGNROLE = "ASSIGNROLE";
    public static final String ACTION_UNASSIGNROLE = "UNASSIGNROLE";
    public static final String ACTION_UPDATEROLE = "UPDATEROLE";
    public static final String ACTION_UPDATESAVINGSACCOUNT = "UPDATESAVINGSACCOUNT";
    public static final String ACTION_EXECUTEJOB = "EXECUTEJOB";
    public static final String ACTION_PREMATURECLOSE = "PREMATURECLOSE";
    public static final String ACTION_UPDATESAVINGSOFFICER = "UPDATESAVINGSOFFICER";
    public static final String ACTION_REMOVESAVINGSOFFICER = "REMOVESAVINGSOFFICER";
    public static final String ACTION_ALLOCATECASHIER = "ALLOCATECASHIER";
    public static final String ACTION_UPDATECASHIERALLOCATION = "UPDATECASHIERALLOCATION";
    public static final String ACTION_DELETECASHIERALLOCATION = "DELETECASHIERALLOCATION";
    public static final String ACTION_ALLOCATECASHTOCASHIER = "ALLOCATECASHTOCASHIER";
    public static final String ACTION_SETTLECASHFROMCASHIER = "SETTLECASHFROMCASHIER";
    public static final String ACTION_DISABLE = "DISABLE";
    public static final String ACTION_ENABLE = "ENABLE";
    public static final String ACTION_REGISTER = "REGISTER";
    public static final String ACTION_RECREATE = "RECREATE";
    public static final String ACTION_CREATESCHEDULEEXCEPTIONS = "CREATESCHEDULEEXCEPTIONS";
    public static final String ACTION_DELETESCHEDULEEXCEPTIONS = "DELETESCHEDULEEXCEPTIONS";
    public static final String ACTION_UPDATEWITHHOLDTAX = "UPDATEWITHHOLDTAX";
    public static final String ACTION_HOLDAMOUNT = "HOLDAMOUNT";
    public static final String ACTION_RELEASEAMOUNT = "RELEASEAMOUNT";
    public static final String ACTION_BLOCKDEBIT = "BLOCKDEBIT";
    public static final String ACTION_UNBLOCKDEBIT = "UNBLOCKDEBIT";
    public static final String ACTION_BLOCKCREDIT = "BLOCKCREDIT";
    public static final String ACTION_UNBLOCKCREDIT = "UNBLOCKCREDIT";
    public static final String ACTION_BLOCK = "BLOCK";
    public static final String ACTION_UNBLOCK = "UNBLOCK";
    public static final String ACTION_INVALIDATE = "INVALIDATE";
    public static final String ACTION_CHARGEOFF = "CHARGEOFF";
    public static final String ACTION_UNDOCHARGEOFF = "UNDOCHARGEOFF";
    public static final String ACTION_INTERMEDIARYSALE = "INTERMEDIARYSALE";
    public static final String ACTION_SALE = "SALE";
    public static final String ACTION_BUYBACK = "BUYBACK";
    public static final String ACTION_CANCEL = "CANCEL";
    public static final String ACTION_DOWNPAYMENT = "DOWNPAYMENT";
    public static final String ACTION_REAGE = "REAGE";
    public static final String ACTION_UNDO_REAGE = "UNDO_REAGE";
    public static final String ACTION_REAMORTIZE = "REAMORTIZE";
    public static final String ACTION_UNDO_REAMORTIZE = "UNDO_REAMORTIZE";
    public static final String ACTION_CAPITALIZEDINCOME = "CAPITALIZEDINCOME";
    public static final String ACTION_CAPITALIZEDINCOMEADJUSTMENT = "CAPITALIZEDINCOMEADJUSTMENT";
    public static final String ACTION_BUYDOWNFEEADJUSTMENT = "BUYDOWNFEEADJUSTMENT";
    public static final String ACTION_CONTRACT_TERMINATION = "CONTRACT_TERMINATION";
    public static final String ACTION_CONTRACT_TERMINATION_UNDO = "CONTRACT_TERMINATION_UNDO";
    public static final String ACTION_BUYDOWNFEE = "BUYDOWNFEE";
    public static final String ACTION_UPDATE_APPROVED_AMOUNT = "UPDATE_APPROVED_AMOUNT";
    public static final String ACTION_MANUAL_INTEREST_REFUND_TRANSACTION = "MANUAL_INTEREST_REFUND_TRANSACTION";
    public static final String ACTION_ATTACH = "ATTACH";
    public static final String ACTION_DETACH = "DETACH";
    public static final String ACTION_FORCE_WITHDRAWAL = "FORCE_WITHDRAWAL";
    public static final String ACTION_REFUNDBYTRANSFER = "REFUNDBYTRANSFER";
    public static final String ACTION_CREATE_DIVIDEND = "CREATE_DIVIDEND";
    public static final String ACTION_APPROVE_DIVIDEND = "APPROVE_DIVIDEND";
    public static final String ACTION_DELETE_DIVIDEND = "DELETE_DIVIDEND";
    public static final String ACTION_SEND = "send";

    // Entities
    public static final String ENTITY_ORGANISATIONCREDITBUREAU = "ORGANISATIONCREDITBUREAU";
    public static final String ENTITY_CREDITBUREAU_LOANPRODUCT_MAPPING = "CREDITBUREAU_LOANPRODUCT_MAPPING";
    public static final String ENTITY_CREDITREPORT = "CREDITREPORT";
    public static final String ENTITY_CREDITBUREAU_CONFIGURATION = "CREDITBUREAU_CONFIGURATION";
    public static final String ENTITY_ADDRESS = "ADDRESS";
    public static final String ENTITY_FAMILYMEMBERS = "FAMILYMEMBERS";
    public static final String ENTITY_CONFIGURATION = "CONFIGURATION";
    public static final String ENTITY_PERMISSION = "PERMISSION";
    public static final String ENTITY_ROLE = "ROLE";
    public static final String ENTITY_USER = "USER";
    public static final String ENTITY_OFFICE = "OFFICE";
    public static final String ENTITY_OFFICETRANSACTION = "OFFICETRANSACTION";
    public static final String ENTITY_GUARANTOR = "GUARANTOR";
    public static final String ENTITY_LOAN = "LOAN";
    public static final String ENTITY_FUND = "FUND";
    public static final String ENTITY_REPORT = "REPORT";
    public static final String ENTITY_SMS = "SMS";
    public static final String ENTITY_CODE = "CODE";
    public static final String ENTITY_HOOK = "HOOK";
    public static final String ENTITY_CHARGE = "CHARGE";
    public static final String ENTITY_COLLATERAL_PRODUCT = "COLLATERAL_PRODUCT";
    public static final String ENTITY_LOANPRODUCT = "LOANPRODUCT";
    public static final String ENTITY_WORKINGCAPITALLOANPRODUCT = "WORKINGCAPITALLOANPRODUCT";
    public static final String ENTITY_WORKINGCAPITALLOAN = "WORKINGCAPITALLOAN";
    public static final String ENTITY_CLIENTIDENTIFIER = "CLIENTIDENTIFIER";
    public static final String ENTITY_CLIENT = "CLIENT";
    public static final String ENTITY_DATATABLE = "DATATABLE";
    public static final String ENTITY_LOANCHARGE = "LOANCHARGE";
    public static final String ENTITY_REPAYMENT_WITH_POSTDATEDCHECKS = "REPAYMENT_WITH_POSTDATEDCHECKS";
    public static final String ENTITY_DISBURSEMENTDETAIL = "DISBURSEMENTDETAIL";
    public static final String ENTITY_GLIMLOAN = "GLIMLOAN";
    public static final String ENTITY_CODEVALUE = "CODEVALUE";
    public static final String ENTITY_GLCLOSURE = "GLCLOSURE";
    public static final String ENTITY_PERIODICACCRUALACCOUNTING = "PERIODICACCRUALACCOUNTING";
    public static final String ENTITY_GLACCOUNT = "GLACCOUNT";
    public static final String ENTITY_JOURNALENTRY = "JOURNALENTRY";
    public static final String ENTITY_SAVINGSPRODUCT = "SAVINGSPRODUCT";
    public static final String ENTITY_SAVINGSACCOUNT = "SAVINGSACCOUNT";
    public static final String ENTITY_GSIMACCOUNT = "GSIMACCOUNT";
    public static final String ENTITY_ACCOUNTTRANSFER = "ACCOUNTTRANSFER";
    public static final String ENTITY_STANDINGINSTRUCTION = "STANDINGINSTRUCTION";
    public static final String ENTITY_SAVINGSACCOUNTCHARGE = "SAVINGSACCOUNTCHARGE";
    public static final String ENTITY_FIXEDDEPOSITPRODUCT = "FIXEDDEPOSITPRODUCT";
    public static final String ENTITY_RECURRINGDEPOSITPRODUCT = "RECURRINGDEPOSITPRODUCT";
    public static final String ENTITY_CALENDAR = "CALENDAR";
    public static final String ENTITY_GROUP = "GROUP";
    public static final String ENTITY_COLLECTIONSHEET = "COLLECTIONSHEET";
    public static final String ENTITY_COLLATERAL = "COLLATERAL";
    public static final String ENTITY_CLIENT_COLLATERAL_PRODUCT = "CLIENT_COLLATERAL_PRODUCT";
    public static final String ENTITY_LOAN_COLLATERAL_PRODUCT = "LOAN_COLLATERAL_PRODUCT";
    public static final String ENTITY_WAIVECHARGE = "WAIVECHARGE";
    public static final String ENTITY_CENTER = "CENTER";
    public static final String ENTITY_ACCOUNTINGRULE = "ACCOUNTINGRULE";
    public static final String ENTITY_HOLIDAY = "HOLIDAY";
    public static final String ENTITY_TEMPLATE = "TEMPLATE";
    public static final String ENTITY_PRODUCTMIX = "PRODUCTMIX";
    public static final String ENTITY_SCHEDULER = "SCHEDULER";
    public static final String ENTITY_FIXEDDEPOSITACCOUNT = "FIXEDDEPOSITACCOUNT";
    public static final String ENTITY_RECURRINGDEPOSITACCOUNT = "RECURRINGDEPOSITACCOUNT";
    public static final String ENTITY_FINANCIALACTIVITYACCOUNT = "FINANCIALACTIVITYACCOUNT";
    public static final String ENTITY_SURVEY = "SURVEY";
    public static final String ENTITY_LIKELIHOOD = "LIKELIHOOD";
    public static final String ENTITY_TELLER = "TELLER";
    public static final String ENTITY_ENTITYMAPPING = "ENTITYMAPPING";
    public static final String ENTITY_EXTERNALSERVICES = "EXTERNALSERVICES";
    public static final String ENTITY_PROVISIONCATEGORY = "PROVISIONCATEGORY";
    public static final String ENTITY_PROVISIONCRITERIA = "PROVISIONCRITERIA";
    public static final String ENTITY_PROVISIONENTRIES = "PROVISIONENTRIES";
    public static final String ENTITY_PROVISIONJOURNALENTRIES = "PROVISIONJOURNALENTRIES";
    public static final String ENTITY_FLOATINGRATE = "FLOATINGRATE";
    public static final String ENTITY_TAXCOMPONENT = "TAXCOMPONENT";
    public static final String ENTITY_TAXGROUP = "TAXGROUP";
    public static final String ENTITY_ENTITY_DATATABLE_CHECK = "ENTITY_DATATABLE_CHECK";
    public static final String ENTITY_SMSCAMPAIGN = "SMSCAMPAIGN";
    public static final String ENTITY_ADHOC = "ADHOC";
    public static final String ENTITY_EMAIL = "EMAIL";
    public static final String ENTITY_EMAIL_CAMPAIGN = "EMAIL_CAMPAIGN";
    public static final String ENTITY_EMAIL_CONFIGURATION = "EMAIL_CONFIGURATION";
    public static final String ENTITY_TWOFACTOR_ACCESSTOKEN = "TWOFACTOR_ACCESSTOKEN";
    public static final String ENTITY_TWOFACTOR_CONFIGURATION = "TWOFACTOR_CONFIGURATION";
    public static final String ENTITY_RATE = "RATE";
    public static final String ENTITY_DELINQUENCY_RANGE = "DELINQUENCY_RANGE";
    public static final String ENTITY_DELINQUENCY_BUCKET = "DELINQUENCY_BUCKET";
    public static final String ENTITY_BATCH_BUSINESS_STEP = "BATCH_BUSINESS_STEP";
    public static final String ENTITY_INLINE_JOB = "INLINE_JOB";
    public static final String ENTITY_EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE = "EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE";
    public static final String ENTITY_EXTERNAL_ASSET_OWNER = "EXTERNAL_ASSET_OWNER";
    public static final String ENTITY_ASSET_OWNER_TRANSACTION = "ASSET_OWNER_TRANSACTION";
    public static final String ENTITY_DELINQUENCY_ACTION = "DELINQUENCY_ACTION";
    public static final String ENTITY_LOAN_AVAILABLE_DISBURSEMENT_AMOUNT = "LOAN_AVAILABLE_DISBURSEMENT_AMOUNT";
    public static final String ENTITY_LOAN_ORIGINATOR = "LOAN_ORIGINATOR";
    public static final String ENTITY_WORKINGDAYS = "WORKINGDAYS";
    public static final String ENTITY_SHAREPRODUCT = "SHAREPRODUCT";
    public static final String ENTITY_INTEREST_PAUSE = "INTEREST_PAUSE";
    public static final String ENTITY_CACHE = "CACHE";
    public static final String ENTITY_CLIENTNOTE = "CLIENTNOTE";
    public static final String ENTITY_LOANNOTE = "LOANNOTE";
    public static final String ENTITY_LOANTRANSACTIONNOTE = "LOANTRANSACTIONNOTE";
    public static final String ENTITY_SAVINGNOTE = "SAVINGNOTE";
    public static final String ENTITY_GROUPNOTE = "GROUPNOTE";
    public static final String ENTITY_CURRENCY = "CURRENCY";

}
