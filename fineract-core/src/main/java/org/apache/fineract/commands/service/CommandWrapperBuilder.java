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
package org.apache.fineract.commands.service;

import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ACCEPTTRANSFER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ACTIVATE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ADJUST;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ADJUSTMENT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ADJUSTTRANSACTION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ALLOCATECASHIER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ALLOCATECASHTOCASHIER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_APPLYANNUALFEE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_APPROVALUNDO;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_APPROVE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_APPROVE_DIVIDEND;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ASSIGNROLE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ASSIGNSTAFF;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ASSOCIATECLIENTS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ASSOCIATEGROUPS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ATTACH;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_BLOCK;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_BLOCKCREDIT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_BLOCKDEBIT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_BOUNCE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_BULKREASSIGN;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_BUYBACK;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_BUYDOWNFEE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_BUYDOWNFEEADJUSTMENT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CALCULATEINTEREST;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CANCEL;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CAPITALIZEDINCOME;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CAPITALIZEDINCOMEADJUSTMENT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CHANGEPWD;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CHARGEBACK;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CHARGEOFF;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CHARGEREFUND;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CLOSE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CLOSEASRESCHEDULED;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CONTRACT_TERMINATION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CONTRACT_TERMINATION_UNDO;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CREATE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CREATESCHEDULEEXCEPTIONS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CREATE_DIVIDEND;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_CREDITBALANCEREFUND;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DEACTIVATEOVERDUE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DEFINEOPENINGBALANCE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DELETE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DELETECASHIERALLOCATION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DELETESCHEDULEEXCEPTIONS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DELETE_DIVIDEND;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DEPOSIT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DETACH;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DISABLE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DISASSOCIATECLIENTS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DISASSOCIATEGROUPS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DISBURSALLASTUNDO;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DISBURSALUNDO;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DISBURSE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DISBURSETOSAVINGS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DISBURSEWITHOUTAUTODOWNPAYMENT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_DOWNPAYMENT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_ENABLE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_EXECUTE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_EXECUTEJOB;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_FORCE_WITHDRAWAL;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_FORECLOSURE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_GET;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_GOODWILLCREDIT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_HOLDAMOUNT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_INACTIVATE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_INTERESTPAYMENTWAIVER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_INTERMEDIARYSALE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_INVALIDATE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_MANUAL_INTEREST_REFUND_TRANSACTION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_MERCHANTISSUEDREFUND;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_PAY;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_PAYOUTREFUND;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_PERMISSIONS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_POSTINTEREST;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_PREMATURECLOSE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_PROPOSEANDACCEPTTRANSFER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_PROPOSETRANSFER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REACTIVATE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REAGE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REAMORTIZE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_RECOVERGUARANTEES;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_RECOVERYPAYMENT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_RECREATE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REFUNDBYCASH;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REFUNDBYTRANSFER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REGISTER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REJECT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REJECTTRANSFER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_RELEASEAMOUNT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REMOVELOANOFFICER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REMOVESAVINGSOFFICER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REPAYMENT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REVERSE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_REVERSETRANSACTION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_SALE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_SAVE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_SAVECOLLECTIONSHEET;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_SETFRAUD;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_SETTLECASHFROMCASHIER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_TRANSFERCLIENTS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNASSIGNROLE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNASSIGNSTAFF;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNBLOCK;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNBLOCKCREDIT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNBLOCKDEBIT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNDO;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNDOAPPROVAL;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNDOCHARGEOFF;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNDODISBURSAL;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNDOREJECT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNDOTRANSACTION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNDOWITHDRAWAL;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNDOWRITEOFF;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNDO_REAGE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UNDO_REAMORTIZE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UPDATE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UPDATECASHIERALLOCATION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UPDATEDELINQUENCY;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UPDATELOANOFFICER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UPDATEROLE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UPDATERUNNINGBALANCE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UPDATESAVINGSACCOUNT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UPDATESAVINGSOFFICER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UPDATEWITHHOLDTAX;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_UPDATE_APPROVED_AMOUNT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_WAIVE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_WAIVEINTERESTPORTION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_WITHDRAW;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_WITHDRAWAL;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_WITHDRAWTRANSFER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ACTION_WRITEOFF;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_ACCOUNTINGRULE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_ACCOUNTTRANSFER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_ADDRESS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_ADHOC;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_ASSET_OWNER_TRANSACTION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_BATCH_BUSINESS_STEP;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_CALENDAR;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_CENTER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_CHARGE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_CLIENT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_CLIENTIDENTIFIER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_CLIENT_COLLATERAL_PRODUCT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_CODE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_CODEVALUE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_COLLATERAL;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_COLLATERAL_PRODUCT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_COLLECTIONSHEET;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_CONFIGURATION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_CREDITBUREAU_CONFIGURATION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_CREDITBUREAU_LOANPRODUCT_MAPPING;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_CREDITREPORT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_DATATABLE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_DELINQUENCY_ACTION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_DELINQUENCY_BUCKET;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_DELINQUENCY_RANGE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_DISBURSEMENTDETAIL;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_EMAIL;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_EMAIL_CAMPAIGN;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_EMAIL_CONFIGURATION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_ENTITYMAPPING;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_ENTITY_DATATABLE_CHECK;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_EXTERNALSERVICES;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_EXTERNAL_ASSET_OWNER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_FAMILYMEMBERS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_FINANCIALACTIVITYACCOUNT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_FIXEDDEPOSITACCOUNT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_FIXEDDEPOSITPRODUCT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_FLOATINGRATE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_FUND;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_GLACCOUNT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_GLCLOSURE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_GLIMLOAN;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_GROUP;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_GSIMACCOUNT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_GUARANTOR;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_HOLIDAY;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_HOOK;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_INLINE_JOB;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_INTEREST_PAUSE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_JOURNALENTRY;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_LIKELIHOOD;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_LOAN;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_LOANCHARGE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_LOANPRODUCT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_LOAN_AVAILABLE_DISBURSEMENT_AMOUNT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_LOAN_COLLATERAL_PRODUCT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_LOAN_ORIGINATOR;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_OFFICE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_OFFICETRANSACTION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_ORGANISATIONCREDITBUREAU;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_PERIODICACCRUALACCOUNTING;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_PERMISSION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_PRODUCTMIX;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_PROVISIONCATEGORY;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_PROVISIONCRITERIA;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_PROVISIONENTRIES;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_PROVISIONJOURNALENTRIES;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_RATE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_RECURRINGDEPOSITACCOUNT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_RECURRINGDEPOSITPRODUCT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_REPAYMENT_WITH_POSTDATEDCHECKS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_REPORT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_ROLE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_SAVINGSACCOUNT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_SAVINGSACCOUNTCHARGE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_SAVINGSPRODUCT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_SCHEDULER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_SHAREPRODUCT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_SMS;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_SMSCAMPAIGN;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_STANDINGINSTRUCTION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_SURVEY;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_TAXCOMPONENT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_TAXGROUP;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_TELLER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_TEMPLATE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_TWOFACTOR_ACCESSTOKEN;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_TWOFACTOR_CONFIGURATION;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_USER;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_WAIVECHARGE;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_WORKINGCAPITALLOAN;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_WORKINGCAPITALLOANPRODUCT;
import static org.apache.fineract.commands.domain.CommandWrapperConstants.ENTITY_WORKINGDAYS;
import static org.apache.fineract.useradministration.service.AppUserConstants.PASSWORD;
import static org.apache.fineract.useradministration.service.AppUserConstants.REPEAT_PASSWORD;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.accountnumberformat.service.AccountNumberFormatConstants;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.useradministration.api.PasswordPreferencesApiConstants;

public class CommandWrapperBuilder {

    private Long groupId;
    private Long clientId;
    private Long loanId;
    private Long savingsId;
    private String actionName;
    private String entityName;
    private Long entityId;
    private Long subentityId;
    private String href;
    private String json = "{}";
    private String transactionId;
    private Long productId;
    private Long organisationCreditBureauId;
    private String jobName;
    private ExternalId loanExternalId;
    private Set<String> sanitizeJsonKeys;

    public CommandWrapper build() {
        return new CommandWrapper(null, this.groupId, this.clientId, this.loanId, this.savingsId, this.actionName, this.entityName,
                this.entityId, this.subentityId, this.href, this.json, this.transactionId, this.productId, null, null,
                this.organisationCreditBureauId, this.jobName, null, this.loanExternalId, this.sanitizeJsonKeys);
    }

    public CommandWrapper build(String idempotencyKey) {
        return new CommandWrapper(null, this.groupId, this.clientId, this.loanId, this.savingsId, this.actionName, this.entityName,
                this.entityId, this.subentityId, this.href, this.json, this.transactionId, this.productId, null, null,
                this.organisationCreditBureauId, this.jobName, idempotencyKey, this.loanExternalId, this.sanitizeJsonKeys);
    }

    public CommandWrapperBuilder updateCreditBureau() {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_ORGANISATIONCREDITBUREAU;
        this.entityId = null;
        this.href = "/creditBureauConfiguration/template";
        return this;
    }

    public CommandWrapperBuilder updateCreditBureauLoanProductMapping() {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_CREDITBUREAU_LOANPRODUCT_MAPPING;
        this.entityId = null;
        this.href = "/creditBureauConfiguration/template";
        return this;
    }

    public CommandWrapperBuilder addOrganisationCreditBureau(final long organisationCreditBureauId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_ORGANISATIONCREDITBUREAU;
        this.entityId = organisationCreditBureauId;
        this.href = "/creditBureauConfiguration/organizationCreditBureau/template";
        this.organisationCreditBureauId = organisationCreditBureauId;
        return this;
    }

    public CommandWrapperBuilder getCreditReport() {
        this.actionName = ACTION_GET;
        this.entityName = ENTITY_CREDITREPORT;
        this.entityId = null;
        this.href = "/getCreditReport/template";
        return this;
    }

    public CommandWrapperBuilder saveCreditReport(final long creditBureauId, final String nationalId) {
        this.actionName = ACTION_SAVE;
        this.entityName = ENTITY_CREDITREPORT;
        this.entityId = creditBureauId;
        this.transactionId = nationalId;
        this.href = "/saveCreditReport/";
        return this;
    }

    public CommandWrapperBuilder deleteCreditReport(final Long creditBureauId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_CREDITREPORT;
        this.entityId = creditBureauId;
        this.href = "/deleteCreditReport/";
        return this;
    }

    public CommandWrapperBuilder createCreditBureauLoanProductMapping(final long organisationCreditBureauId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_CREDITBUREAU_LOANPRODUCT_MAPPING;
        this.entityId = null; // TODO: fix this, was always null
        this.href = "/creditBureauConfiguration/template";
        this.organisationCreditBureauId = organisationCreditBureauId;
        return this;
    }

    public CommandWrapperBuilder addCreditBureauConfiguration(final long creditBureauId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_CREDITBUREAU_CONFIGURATION;
        this.entityId = creditBureauId;
        this.href = "/addCreditBureauConfigurationData/";
        return this;
    }

    public CommandWrapperBuilder updateCreditBureauConfiguration(final long configurationId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_CREDITBUREAU_CONFIGURATION;
        this.entityId = configurationId;
        this.href = "/updateCreditBureauConfigurationData/";
        return this;
    }

    public CommandWrapperBuilder addClientAddress(final long clientId, final long addressTypeId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_ADDRESS;
        this.entityId = addressTypeId;
        this.href = "/clients/" + clientId + "/addresses";
        this.clientId = clientId;
        return this;
    }

    public CommandWrapperBuilder updateClientAddress(final long clientId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_ADDRESS;
        this.href = "/clients/" + clientId + "/addresses";
        this.clientId = clientId;
        return this;
    }

    public CommandWrapperBuilder addFamilyMembers(final long clientId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_FAMILYMEMBERS;
        this.href = "/clients/" + clientId + "/familymembers";
        this.clientId = clientId;
        return this;
    }

    public CommandWrapperBuilder updateFamilyMembers(final long familyMemberId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_FAMILYMEMBERS;
        this.href = "/clients/" + clientId + "/familymembers";
        this.entityId = familyMemberId;
        return this;
    }

    public CommandWrapperBuilder deleteFamilyMembers(final long familyMemberId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_FAMILYMEMBERS;
        this.href = "/clients/" + clientId + "/familymembers";
        this.entityId = familyMemberId;
        return this;
    }

    public CommandWrapperBuilder withLoanId(final Long withLoanId) {
        this.loanId = withLoanId;
        return this;
    }

    public CommandWrapperBuilder withSavingsId(final Long withSavingsId) {
        this.savingsId = withSavingsId;
        return this;
    }

    public CommandWrapperBuilder withClientId(final Long withClientId) {
        this.clientId = withClientId;
        return this;
    }

    public CommandWrapperBuilder withGroupId(final Long withGroupId) {
        this.groupId = withGroupId;
        return this;
    }

    public CommandWrapperBuilder withSubEntityId(final Long withSubEntityId) {
        this.subentityId = withSubEntityId;
        return this;
    }

    public CommandWrapperBuilder withJson(final String withJson) {
        this.json = withJson;
        return this;
    }

    public CommandWrapperBuilder withNoJsonBody() {
        this.json = null;
        return this;
    }

    public CommandWrapperBuilder updateGlobalConfiguration(final Long configId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_CONFIGURATION;
        this.entityId = configId;

        this.href = "/configurations/" + configId;
        return this;
    }

    public CommandWrapperBuilder updatePermissions() {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_PERMISSION;
        this.entityId = null;
        this.href = "/permissions";
        return this;
    }

    public CommandWrapperBuilder createRole() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_ROLE;
        this.href = "/roles/template";
        return this;
    }

    public CommandWrapperBuilder updateRole(final Long roleId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_ROLE;
        this.entityId = roleId;
        this.href = "/roles/" + roleId;
        return this;
    }

    public CommandWrapperBuilder updateRolePermissions(final Long roleId) {
        this.actionName = ACTION_PERMISSIONS;
        this.entityName = ENTITY_ROLE;
        this.entityId = roleId;
        this.href = "/roles/" + roleId + "/permissions";
        return this;
    }

    public CommandWrapperBuilder createUser() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_USER;
        this.entityId = null;
        this.href = "/users/template";
        this.sanitizeJsonKeys = new HashSet<>(Arrays.asList(PASSWORD, REPEAT_PASSWORD));
        return this;
    }

    public CommandWrapperBuilder changeUserPassword(final Long userId) {
        this.actionName = ACTION_CHANGEPWD;
        this.entityName = ENTITY_USER;
        this.entityId = userId;
        this.href = "/users/" + userId + "/pwd";
        this.sanitizeJsonKeys = new HashSet<>(Arrays.asList(PASSWORD, REPEAT_PASSWORD));
        return this;
    }

    public CommandWrapperBuilder updateUser(final Long userId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_USER;
        this.entityId = userId;
        this.href = "/users/" + userId;
        this.sanitizeJsonKeys = new HashSet<>(Arrays.asList(PASSWORD, REPEAT_PASSWORD));
        return this;
    }

    public CommandWrapperBuilder deleteUser(final Long userId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_USER;
        this.entityId = userId;
        this.href = "/users/" + userId;
        return this;
    }

    public CommandWrapperBuilder createOffice() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_OFFICE;
        this.entityId = null;
        this.href = "/offices/template";
        return this;
    }

    public CommandWrapperBuilder updateOffice(final Long officeId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_OFFICE;
        this.entityId = officeId;
        this.href = "/offices/" + officeId;
        return this;
    }

    public CommandWrapperBuilder createOfficeTransaction() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_OFFICETRANSACTION;
        this.href = "/officetransactions/template";
        return this;
    }

    public CommandWrapperBuilder deleteOfficeTransaction(final Long transactionId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_OFFICETRANSACTION;
        this.entityId = transactionId;
        this.href = "/officetransactions/" + transactionId;
        return this;
    }

    public CommandWrapperBuilder createGuarantor(final Long loanId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_GUARANTOR;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/guarantors";
        return this;
    }

    public CommandWrapperBuilder recoverFromGuarantor(final Long loanId) {
        this.actionName = ACTION_RECOVERGUARANTEES;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "?command=recoverGuarantees";
        return this;
    }

    public CommandWrapperBuilder updateGuarantor(final Long loanId, final Long guarantorId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_GUARANTOR;
        this.entityId = guarantorId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/guarantors/" + guarantorId;
        return this;
    }

    public CommandWrapperBuilder deleteGuarantor(final Long loanId, final Long guarantorId, final Long guarantorFundingId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_GUARANTOR;
        this.entityId = guarantorId;
        this.subentityId = guarantorFundingId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/guarantors/" + guarantorId;
        return this;
    }

    public CommandWrapperBuilder createFund() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_FUND;
        this.entityId = null;
        this.href = "/funds/template";
        return this;
    }

    public CommandWrapperBuilder updateFund(final Long fundId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_FUND;
        this.entityId = fundId;
        this.href = "/funds/" + fundId;
        return this;
    }

    public CommandWrapperBuilder createReport() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_REPORT;
        this.entityId = null;
        this.href = "/reports/template";
        return this;
    }

    public CommandWrapperBuilder updateReport(final Long id) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_REPORT;
        this.entityId = id;
        this.href = "/reports/" + id;
        return this;
    }

    public CommandWrapperBuilder deleteReport(final Long id) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_REPORT;
        this.entityId = id;
        this.href = "/reports/" + id;
        return this;
    }

    public CommandWrapperBuilder createSms() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_SMS;
        this.entityId = null;
        this.href = "/sms/template";
        return this;
    }

    public CommandWrapperBuilder updateSms(final Long resourceId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_SMS;
        this.entityId = resourceId;
        this.href = "/sms/" + resourceId;
        return this;
    }

    public CommandWrapperBuilder deleteSms(final Long resourceId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_SMS;
        this.entityId = resourceId;
        this.href = "/sms/" + resourceId;
        return this;
    }

    public CommandWrapperBuilder createCode() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_CODE;
        this.entityId = null;
        this.href = "/codes/template";
        return this;
    }

    public CommandWrapperBuilder updateCode(final Long codeId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_CODE;
        this.entityId = codeId;
        this.href = "/codes/" + codeId;
        return this;
    }

    public CommandWrapperBuilder deleteCode(final Long codeId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_CODE;
        this.entityId = codeId;
        this.href = "/codes/" + codeId;
        return this;
    }

    public CommandWrapperBuilder createHook() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_HOOK;
        this.entityId = null;
        this.href = "/hooks/template";
        return this;
    }

    public CommandWrapperBuilder updateHook(final Long hookId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_HOOK;
        this.entityId = hookId;
        this.href = "/hooks/" + hookId;
        return this;
    }

    public CommandWrapperBuilder deleteHook(final Long hookId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_HOOK;
        this.entityId = hookId;
        this.href = "/hooks/" + hookId;
        return this;
    }

    public CommandWrapperBuilder createCharge() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_CHARGE;
        this.entityId = null;
        this.href = "/charges/template";
        return this;
    }

    public CommandWrapperBuilder createCollateral() {
        this.actionName = ACTION_CREATE;
        this.entityId = null;
        this.entityName = ENTITY_COLLATERAL_PRODUCT;
        this.href = "/collateral-product";
        return this;
    }

    public CommandWrapperBuilder updateCharge(final Long chargeId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_CHARGE;
        this.entityId = chargeId;
        this.href = "/charges/" + chargeId;
        return this;
    }

    public CommandWrapperBuilder deleteCharge(final Long chargeId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_CHARGE;
        this.entityId = chargeId;
        this.href = "/charges/" + chargeId;
        return this;
    }

    public CommandWrapperBuilder createLoanProduct() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_LOANPRODUCT;
        this.entityId = null;
        this.href = "/loanproducts/template";
        return this;
    }

    public CommandWrapperBuilder updateLoanProduct(final Long productId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_LOANPRODUCT;
        this.entityId = productId;
        this.href = "/loanproducts/" + productId;
        return this;
    }

    public CommandWrapperBuilder createWorkingCapitalLoanProduct() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_WORKINGCAPITALLOANPRODUCT;
        this.entityId = null;
        this.href = "/working-capital-loan-products/template";
        return this;
    }

    public CommandWrapperBuilder updateWorkingCapitalLoanProduct(final Long productId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_WORKINGCAPITALLOANPRODUCT;
        this.entityId = productId;
        this.href = "/working-capital-loan-products/" + productId;
        return this;
    }

    public CommandWrapperBuilder deleteWorkingCapitalLoanProduct(final Long productId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_WORKINGCAPITALLOANPRODUCT;
        this.entityId = productId;
        this.href = "/working-capital-loan-products/" + productId;
        return this;
    }

    public CommandWrapperBuilder createWorkingCapitalBreach() {
        this.actionName = "CREATE";
        this.entityName = "WORKINGCAPITALBREACH";
        this.entityId = null;
        this.href = "/working-capital-breach/breaches";
        return this;
    }

    public CommandWrapperBuilder updateWorkingCapitalBreach(final Long breachId) {
        this.actionName = "UPDATE";
        this.entityName = "WORKINGCAPITALBREACH";
        this.entityId = breachId;
        this.href = "/working-capital-breach/breaches/" + breachId;
        return this;
    }

    public CommandWrapperBuilder deleteWorkingCapitalBreach(final Long breachId) {
        this.actionName = "DELETE";
        this.entityName = "WORKINGCAPITALBREACH";
        this.entityId = breachId;
        this.href = "/working-capital-breach/breaches/" + breachId;
        return this;
    }

    public CommandWrapperBuilder createWorkingCapitalLoanApplication() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_WORKINGCAPITALLOAN;
        this.entityId = null;
        this.loanId = null;
        this.href = "/workingcapitalloans";
        return this;
    }

    public CommandWrapperBuilder updateWorkingCapitalLoanApplication() {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_WORKINGCAPITALLOAN;
        this.entityId = loanId;
        this.href = "/workingcapitalloans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder deleteWorkingCapitalLoanApplication() {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_WORKINGCAPITALLOAN;
        this.entityId = loanId;
        this.href = "/workingcapitalloans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder approveWorkingCapitalLoanApplication(final Long loanId) {
        this.actionName = ACTION_APPROVE;
        this.entityName = ENTITY_WORKINGCAPITALLOAN;
        this.entityId = loanId;
        this.href = "/workingcapitalloans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder rejectWorkingCapitalLoanApplication(final Long loanId) {
        this.actionName = ACTION_REJECT;
        this.entityName = ENTITY_WORKINGCAPITALLOAN;
        this.entityId = loanId;
        this.href = "/workingcapitalloans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder undoWorkingCapitalLoanApplicationApproval(final Long loanId) {
        this.actionName = ACTION_APPROVALUNDO;
        this.entityName = ENTITY_WORKINGCAPITALLOAN;
        this.entityId = loanId;
        this.href = "/workingcapitalloans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder disburseWorkingCapitalLoanApplication(final Long loanId) {
        this.actionName = ACTION_DISBURSE;
        this.entityName = ENTITY_WORKINGCAPITALLOAN;
        this.entityId = loanId;
        this.href = "/workingcapitalloans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder undoWorkingCapitalLoanApplicationDisbursal(final Long loanId) {
        this.actionName = ACTION_DISBURSALUNDO;
        this.entityName = ENTITY_WORKINGCAPITALLOAN;
        this.entityId = loanId;
        this.href = "/workingcapitalloans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder createWorkingCapitalLoanDelinquencyAction(final Long workingCapitalLoanId) {
        this.actionName = "CREATE";
        this.entityName = "WC_DELINQUENCY_ACTION";
        this.entityId = workingCapitalLoanId;
        this.loanId = workingCapitalLoanId;
        this.href = "/working-capital-loans/" + workingCapitalLoanId + "/delinquency-actions";
        return this;
    }

    public CommandWrapperBuilder updateDiscountWorkingCapitalLoanApplication(final Long loanId) {
        this.actionName = "UPDATEDISCOUNT";
        this.entityName = "WORKINGCAPITALLOAN";
        this.entityId = loanId;
        this.href = "/workingcapitalloans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder repaymentWorkingCapitalLoanTransaction(final Long loanId) {
        this.actionName = ACTION_REPAYMENT;
        this.entityName = ENTITY_WORKINGCAPITALLOAN;
        this.entityId = loanId;
        this.href = "/working-capital-loans/" + loanId + "/transactions?command=repayment";
        return this;
    }

    public CommandWrapperBuilder creditBalanceRefundWorkingCapitalLoanTransaction(final Long loanId) {
        this.actionName = ACTION_CREDITBALANCEREFUND;
        this.entityName = ENTITY_WORKINGCAPITALLOAN;
        this.entityId = loanId;
        this.href = "/working-capital-loans/" + loanId + "/transactions?command=creditBalanceRefund";
        return this;
    }

    public CommandWrapperBuilder createClientIdentifier(final Long clientId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_CLIENTIDENTIFIER;
        this.entityId = null;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/identifiers/template";
        return this;
    }

    public CommandWrapperBuilder updateClientIdentifier(final Long clientId, final Long clientIdentifierId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_CLIENTIDENTIFIER;
        this.entityId = clientIdentifierId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/identifiers/" + clientIdentifierId;
        return this;
    }

    public CommandWrapperBuilder deleteClientIdentifier(final Long clientId, final Long clientIdentifierId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_CLIENTIDENTIFIER;
        this.entityId = clientIdentifierId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/identifiers/" + clientIdentifierId;
        return this;
    }

    public CommandWrapperBuilder createClient() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_CLIENT;
        this.href = "/clients/template";
        return this;
    }

    public CommandWrapperBuilder activateClient(final Long clientId) {
        this.actionName = ACTION_ACTIVATE;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=activate&template=true";
        return this;
    }

    public CommandWrapperBuilder closeClient(final Long clientId) {
        this.actionName = ACTION_CLOSE;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=close&template=true";
        return this;
    }

    public CommandWrapperBuilder rejectClient(final Long clientId) {
        this.actionName = ACTION_REJECT;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=reject&template=true";
        return this;
    }

    public CommandWrapperBuilder withdrawClient(final Long clientId) {
        this.actionName = ACTION_WITHDRAW;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=withdraw&template=true";
        return this;
    }

    public CommandWrapperBuilder reActivateClient(final Long clientId) {
        this.actionName = ACTION_REACTIVATE;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=reactivate&template=true";
        return this;
    }

    public CommandWrapperBuilder proposeClientTransfer(final Long clientId) {
        this.actionName = ACTION_PROPOSETRANSFER;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clientId/" + clientId + "?command=proposeTransfer";
        return this;
    }

    public CommandWrapperBuilder proposeAndAcceptClientTransfer(final Long clientId) {
        this.actionName = ACTION_PROPOSEANDACCEPTTRANSFER;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clientId/" + clientId + "?command=proposeAndAcceptTransfer";
        return this;
    }

    public CommandWrapperBuilder withdrawClientTransferRequest(final Long clientId) {
        this.actionName = ACTION_WITHDRAWTRANSFER;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clientId/" + clientId + "?command=withdrawTransfer";
        return this;
    }

    public CommandWrapperBuilder acceptClientTransfer(final Long clientId) {
        this.actionName = ACTION_ACCEPTTRANSFER;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clientId/" + clientId + "?command=acceptTransfer";
        return this;
    }

    public CommandWrapperBuilder rejectClientTransfer(final Long clientId) {
        this.actionName = ACTION_REJECTTRANSFER;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clientId/" + clientId + "?command=rejectTransfer";
        return this;
    }

    public CommandWrapperBuilder updateClient(final Long clientId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId;
        return this;
    }

    public CommandWrapperBuilder deleteClient(final Long clientId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId;
        this.json = "{}";
        return this;
    }

    public CommandWrapperBuilder createDBDatatable(final String json) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_DATATABLE;
        this.entityId = null;
        this.href = "/datatables/";
        this.json = json;
        return this;
    }

    public CommandWrapperBuilder updateDBDatatable(final String datatable, final String json) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_DATATABLE;
        this.entityId = null;
        this.href = "/datatables/" + datatable;
        this.json = json;
        return this;
    }

    public CommandWrapperBuilder deleteDBDatatable(final String datatable, final String json) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_DATATABLE;
        this.entityId = null;
        this.href = "/datatables/" + datatable;
        this.json = json;
        return this;
    }

    public CommandWrapperBuilder undoRejection(final Long clientId) {
        this.actionName = ACTION_UNDOREJECT;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=undoRejection";
        return this;
    }

    public CommandWrapperBuilder undoWithdrawal(final Long clientId) {
        this.actionName = ACTION_UNDOWITHDRAWAL;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=undoWithdrawal";
        return this;
    }

    public CommandWrapperBuilder createDatatableEntry(final String datatable, final Long apptableId, final Long datatableId) {
        this.actionName = ACTION_CREATE;
        commonDatatableSettings(datatable, apptableId, datatableId);
        return this;
    }

    public CommandWrapperBuilder updateDatatableEntry(final String datatable, final Long apptableId, final Long datatableId) {
        this.actionName = ACTION_UPDATE;
        commonDatatableSettings(datatable, apptableId, datatableId);
        return this;
    }

    public CommandWrapperBuilder deleteDatatableEntry(final String datatable, final Long apptableId, final Long datatableId) {
        this.actionName = ACTION_DELETE;
        commonDatatableSettings(datatable, apptableId, datatableId);
        return this;
    }

    private void commonDatatableSettings(final String datatable, final Long apptableId, final Long datatableId) {
        this.entityName = datatable;
        this.entityId = apptableId;
        this.subentityId = datatableId;
        if (datatableId == null) {
            this.href = "/datatables/" + datatable + "/" + apptableId;
        } else {
            this.href = "/datatables/" + datatable + "/" + apptableId + "/" + datatableId;
        }
    }

    public CommandWrapperBuilder createLoanCharge(final Long loanId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_LOANCHARGE;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges";
        return this;
    }

    public CommandWrapperBuilder updateLoanCharge(final Long loanId, final Long loanChargeId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_LOANCHARGE;
        this.entityId = loanChargeId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        return this;
    }

    public CommandWrapperBuilder waiveLoanCharge(final Long loanId, final Long loanChargeId) {
        this.actionName = ACTION_WAIVE;
        this.entityName = ENTITY_LOANCHARGE;
        this.entityId = loanChargeId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        return this;
    }

    public CommandWrapperBuilder payLoanCharge(final Long loanId, final Long loanChargeId) {
        this.actionName = ACTION_PAY;
        this.entityName = ENTITY_LOANCHARGE;
        this.entityId = loanChargeId;
        this.loanId = loanId;
        if (loanChargeId == null) {
            this.href = "/loans/" + loanId;
        } else {
            this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        }
        return this;
    }

    public CommandWrapperBuilder adjustmentForLoanCharge(final Long loanId, final Long loanChargeId) {
        this.actionName = ACTION_ADJUSTMENT;
        this.entityName = ENTITY_LOANCHARGE;
        this.entityId = loanChargeId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        return this;
    }

    public CommandWrapperBuilder deactivateOverdueLoanCharges(final Long loanId, final Long loanChargeId) {
        this.actionName = ACTION_DEACTIVATEOVERDUE;
        this.entityName = ENTITY_LOANCHARGE;
        this.entityId = loanChargeId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        return this;
    }

    public CommandWrapperBuilder deleteLoanCharge(final Long loanId, final Long loanChargeId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_LOANCHARGE;
        this.entityId = loanChargeId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/charges/" + loanChargeId;
        return this;
    }

    public CommandWrapperBuilder loanRepaymentTransaction(final Long loanId) {
        this.actionName = ACTION_REPAYMENT;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=repayment";
        return this;
    }

    public CommandWrapperBuilder loanMerchantIssuedRefundTransaction(final Long loanId) {
        this.actionName = ACTION_MERCHANTISSUEDREFUND;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=merchantissuedrefund";
        return this;
    }

    public CommandWrapperBuilder loanPayoutRefundTransaction(final Long loanId) {
        this.actionName = ACTION_PAYOUTREFUND;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=payoutrefund";
        return this;
    }

    public CommandWrapperBuilder loanGoodwillCreditTransaction(final Long loanId) {
        this.actionName = ACTION_GOODWILLCREDIT;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=goodwillcredit";
        return this;
    }

    public CommandWrapperBuilder loanInterestPaymentWaiverTransaction(final Long loanId) {
        this.actionName = ACTION_INTERESTPAYMENTWAIVER;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=interestpaymentwaiver";
        return this;
    }

    public CommandWrapperBuilder refundLoanCharge(final Long loanId) {
        this.actionName = ACTION_CHARGEREFUND;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=chargerefund";
        return this;
    }

    public CommandWrapperBuilder loanRecoveryPaymentTransaction(final Long loanId) {
        this.actionName = ACTION_RECOVERYPAYMENT;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=recoverypayment";
        return this;
    }

    public CommandWrapperBuilder waiveInterestPortionTransaction(final Long loanId) {
        this.actionName = ACTION_WAIVEINTERESTPORTION;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=waiveinterest";
        return this;
    }

    public CommandWrapperBuilder writeOffLoanTransaction(final Long loanId) {
        this.actionName = ACTION_WRITEOFF;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=writeoff";
        return this;
    }

    public CommandWrapperBuilder undoWriteOffLoanTransaction(final Long loanId) {
        this.actionName = ACTION_UNDOWRITEOFF;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=undowriteoff";
        return this;
    }

    public CommandWrapperBuilder closeLoanAsRescheduledTransaction(final Long loanId) {
        this.actionName = ACTION_CLOSEASRESCHEDULED;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=close-rescheduled";
        return this;
    }

    public CommandWrapperBuilder closeLoanTransaction(final Long loanId) {
        this.actionName = ACTION_CLOSE;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=close";
        return this;
    }

    public CommandWrapperBuilder adjustTransaction(final Long loanId, final Long transactionId) {
        this.actionName = ACTION_ADJUST;
        this.entityName = ENTITY_LOAN;
        this.entityId = transactionId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/" + transactionId;
        return this;
    }

    public CommandWrapperBuilder refundLoanTransactionByCash(final Long loanId) {
        this.actionName = ACTION_REFUNDBYCASH;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=refundbycash";
        return this;
    }

    public CommandWrapperBuilder chargebackTransaction(final Long loanId, final Long transactionId) {
        this.actionName = ACTION_CHARGEBACK;
        this.entityName = ENTITY_LOAN;
        this.entityId = transactionId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/" + transactionId;
        return this;
    }

    public CommandWrapperBuilder loanForeclosure(final Long loanId) {
        this.actionName = ACTION_FORECLOSURE;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=foreclosure";
        return this;
    }

    public CommandWrapperBuilder creditBalanceRefund(final Long loanId) {
        this.actionName = ACTION_CREDITBALANCEREFUND;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=creditBalanceRefund";
        return this;
    }

    public CommandWrapperBuilder undoWaiveChargeTransaction(final Long loanId, final Long transactionId) {
        this.actionName = ACTION_UNDO;
        this.entityName = ENTITY_WAIVECHARGE;
        this.entityId = transactionId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=undo";
        return this;
    }

    public CommandWrapperBuilder createLoanApplication() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = null;
        this.href = "/loans";
        return this;
    }

    public CommandWrapperBuilder updatePostDatedCheck(final Long id, final Long loanId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_REPAYMENT_WITH_POSTDATEDCHECKS;
        this.entityId = id;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/repaymentwithpostdatedchecks/" + id;
        return this;
    }

    public CommandWrapperBuilder bouncedCheck(final Long id, final Long loanId) {
        this.actionName = ACTION_BOUNCE;
        this.entityName = ENTITY_REPAYMENT_WITH_POSTDATEDCHECKS;
        this.entityId = id;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/repaymentwithpostdatedchecks/" + id;
        return this;
    }

    public CommandWrapperBuilder deletePostDatedCheck(final Long id, final Long loanId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_REPAYMENT_WITH_POSTDATEDCHECKS;
        this.entityId = id;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/repaymentwithpostdatedchecks/" + id;
        return this;
    }

    public CommandWrapperBuilder updateLoanApplication(final Long loanId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder markAsFraud(final Long loanId) {
        this.actionName = ACTION_SETFRAUD;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder updateDisbusementDate(final Long loanId, final Long disbursementId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_DISBURSEMENTDETAIL;
        this.entityId = disbursementId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/disbursementdetail/" + disbursementId;
        return this;
    }

    public CommandWrapperBuilder addAndDeleteDisbursementDetails(final Long loanId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_DISBURSEMENTDETAIL;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/editdisbursementdetails/";
        return this;
    }

    public CommandWrapperBuilder deleteLoanApplication(final Long loanId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder rejectLoanApplication(final Long loanId) {
        this.actionName = ACTION_REJECT;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder rejectGLIMApplication(final Long glimId) {
        this.actionName = ACTION_REJECT;
        this.entityName = ENTITY_GLIMLOAN;
        this.entityId = glimId;
        this.loanId = glimId;
        this.href = "/loans/" + glimId;
        return this;
    }

    public CommandWrapperBuilder withdrawLoanApplication(final Long loanId) {
        this.actionName = ACTION_WITHDRAW;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder approveLoanApplication(final Long loanId) {
        this.actionName = ACTION_APPROVE;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder approveGLIMLoanApplication(final Long glimId) {
        this.actionName = ACTION_APPROVE;
        this.entityName = ENTITY_GLIMLOAN;
        this.entityId = glimId;
        this.loanId = glimId;
        this.href = "/loans/" + glimId;
        return this;
    }

    public CommandWrapperBuilder disburseGlimLoanApplication(final Long glimId) {
        this.actionName = ACTION_DISBURSE;
        this.entityName = ENTITY_GLIMLOAN;
        this.entityId = glimId;
        this.loanId = glimId;
        this.href = "/loans/" + glimId;
        return this;
    }

    public CommandWrapperBuilder repaymentGlimLoanApplication(final Long glimId) {
        this.actionName = ACTION_REPAYMENT;
        this.entityName = ENTITY_GLIMLOAN;
        this.entityId = glimId;
        this.loanId = glimId;
        this.href = "/loans/" + glimId;
        return this;
    }

    public CommandWrapperBuilder undoGLIMLoanDisbursal(final Long glimId) {
        this.actionName = ACTION_UNDODISBURSAL;
        this.entityName = ENTITY_GLIMLOAN;
        this.entityId = glimId;
        this.loanId = glimId;
        this.href = "/loans/" + glimId;
        return this;
    }

    public CommandWrapperBuilder undoGLIMLoanApproval(final Long glimId) {
        this.actionName = ACTION_UNDOAPPROVAL;
        this.entityName = ENTITY_GLIMLOAN;
        this.entityId = glimId;
        this.loanId = glimId;
        this.href = "/loans/" + glimId;
        return this;
    }

    public CommandWrapperBuilder disburseLoanApplication(final Long loanId) {
        this.actionName = ACTION_DISBURSE;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder disburseLoanToSavingsApplication(final Long loanId) {
        this.actionName = ACTION_DISBURSETOSAVINGS;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder disburseWithoutAutoDownPayment(final Long loanId) {
        this.actionName = ACTION_DISBURSEWITHOUTAUTODOWNPAYMENT;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder undoLoanApplicationApproval(final Long loanId) {
        this.actionName = ACTION_APPROVALUNDO;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder undoLoanApplicationDisbursal(final Long loanId) {
        this.actionName = ACTION_DISBURSALUNDO;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder undoLastDisbursalLoanApplication(final Long loanId) {
        this.actionName = ACTION_DISBURSALLASTUNDO;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder assignLoanOfficer(final Long loanId) {
        this.actionName = ACTION_UPDATELOANOFFICER;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder unassignLoanOfficer(final Long loanId) {
        this.actionName = ACTION_REMOVELOANOFFICER;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder assignLoanOfficersInBulk() {
        this.actionName = ACTION_BULKREASSIGN;
        this.entityName = ENTITY_LOAN;
        this.href = "/loans/loanreassignment";
        return this;
    }

    public CommandWrapperBuilder assignDelinquency(final Long loanId) {
        this.actionName = ACTION_UPDATEDELINQUENCY;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder createCodeValue(final Long codeId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_CODEVALUE;
        this.entityId = codeId;
        this.href = "/codes/" + codeId + "/codevalues/template";
        return this;
    }

    public CommandWrapperBuilder createCodeValue(final Long codeId, String codeName) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_CODEVALUE;
        this.entityId = codeId;
        this.href = "/codes/name/" + codeName + "/codevalues/template";
        return this;
    }

    public CommandWrapperBuilder updateCodeValue(final Long codeId, final Long codeValueId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_CODEVALUE;
        this.subentityId = codeValueId;
        this.entityId = codeId;
        this.href = "/codes/" + codeId + "/codevalues/" + codeValueId;
        return this;
    }

    public CommandWrapperBuilder updateCodeValue(String codeName, Long codeId, Long codeValueId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_CODEVALUE;
        this.entityId = codeId;
        this.subentityId = codeValueId;
        this.href = "/codes/name/" + codeName + "/codevalues/" + codeValueId;
        return this;
    }

    public CommandWrapperBuilder deleteCodeValue(final Long codeId, final Long codeValueId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_CODEVALUE;
        this.subentityId = codeValueId;
        this.entityId = codeId;
        this.href = "/codes/" + codeId + "/codevalues/" + codeValueId;
        return this;
    }

    public CommandWrapperBuilder deleteCodeValue(String codeName, Long codeId, Long codeValueId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_CODEVALUE;
        this.entityId = codeId;
        this.subentityId = codeValueId;
        this.href = "/codes/name/" + codeName + "/codevalues/" + codeValueId;
        return this;
    }

    public CommandWrapperBuilder createGLClosure() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_GLCLOSURE;
        this.entityId = null;
        this.href = "/glclosures/template";
        return this;
    }

    public CommandWrapperBuilder updateGLClosure(final Long glClosureId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_GLCLOSURE;
        this.entityId = glClosureId;
        this.href = "/glclosures/" + glClosureId;
        return this;
    }

    public CommandWrapperBuilder deleteGLClosure(final Long glClosureId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_GLCLOSURE;
        this.entityId = glClosureId;
        this.href = "/glclosures/" + glClosureId;
        return this;
    }

    public CommandWrapperBuilder excuteAccrualAccounting() {
        this.actionName = ACTION_EXECUTE;
        this.entityName = ENTITY_PERIODICACCRUALACCOUNTING;
        this.entityId = null;
        this.href = "/accrualaccounting";
        return this;
    }

    public CommandWrapperBuilder createGLAccount() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_GLACCOUNT;
        this.entityId = null;
        this.href = "/glaccounts/template";
        return this;
    }

    public CommandWrapperBuilder updateGLAccount(final Long glAccountId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_GLACCOUNT;
        this.entityId = glAccountId;
        this.href = "/glaccounts/" + glAccountId;
        return this;
    }

    public CommandWrapperBuilder deleteGLAccount(final Long glAccountId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_GLACCOUNT;
        this.entityId = glAccountId;
        this.href = "/glaccounts/" + glAccountId;
        return this;
    }

    public CommandWrapperBuilder createJournalEntry() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_JOURNALENTRY;
        this.entityId = null;
        this.href = "/journalentries/template";
        return this;
    }

    public CommandWrapperBuilder reverseJournalEntry(final String transactionId) {
        this.actionName = ACTION_REVERSE;
        this.entityName = ENTITY_JOURNALENTRY;
        this.entityId = null;
        this.transactionId = transactionId;
        this.href = "/journalentries/" + transactionId;
        return this;
    }

    public CommandWrapperBuilder updateRunningBalanceForJournalEntry() {
        this.actionName = ACTION_UPDATERUNNINGBALANCE;
        this.entityName = ENTITY_JOURNALENTRY;
        this.entityId = null;
        this.href = "/journalentries/update";
        return this;
    }

    public CommandWrapperBuilder defineOpeningBalanceForJournalEntry() {
        this.actionName = ACTION_DEFINEOPENINGBALANCE;
        this.entityName = ENTITY_JOURNALENTRY;
        this.entityId = null;
        this.href = "/journalentries/update";
        return this;
    }

    public CommandWrapperBuilder createSavingProduct() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_SAVINGSPRODUCT;
        this.entityId = null;
        this.href = "/savingsproducts/template";
        return this;
    }

    public CommandWrapperBuilder updateSavingProduct(final Long productId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_SAVINGSPRODUCT;
        this.entityId = productId;
        this.href = "/savingsproducts/" + productId;
        return this;
    }

    public CommandWrapperBuilder deleteSavingProduct(final Long productId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_SAVINGSPRODUCT;
        this.entityId = productId;
        this.href = "/savingsproducts/" + productId;
        return this;
    }

    public CommandWrapperBuilder createSavingsAccount() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = null;
        this.href = "/savingsaccounts/template";
        return this;
    }

    public CommandWrapperBuilder createGSIMAccount() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_GSIMACCOUNT;
        this.entityId = null;
        this.href = "/gsimaccounts/template";
        return this;
    }

    public CommandWrapperBuilder updateSavingsAccount(final Long accountId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId;
        return this;
    }

    public CommandWrapperBuilder updateGSIMAccount(final Long accountId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_GSIMACCOUNT;
        this.entityId = accountId;
        this.href = "/gsimaccounts/" + accountId;
        return this;
    }

    public CommandWrapperBuilder deleteSavingsAccount(final Long accountId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId;
        return this;
    }

    public CommandWrapperBuilder rejectSavingsAccountApplication(final Long accountId) {
        this.actionName = ACTION_REJECT;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=reject";
        return this;
    }

    public CommandWrapperBuilder rejectGSIMAccountApplication(final Long accountId) {
        this.actionName = ACTION_REJECT;
        this.entityName = ENTITY_GSIMACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=reject";
        return this;
    }

    public CommandWrapperBuilder withdrawSavingsAccountApplication(final Long accountId) {
        this.actionName = ACTION_WITHDRAW;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=withdrawnByApplicant";
        return this;
    }

    public CommandWrapperBuilder approveSavingsAccountApplication(final Long accountId) {
        this.actionName = ACTION_APPROVE;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=approve";
        return this;
    }

    public CommandWrapperBuilder approveGSIMAccountApplication(final Long accountId) {
        this.actionName = ACTION_APPROVE;
        this.entityName = ENTITY_GSIMACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/gsimsaccounts/" + accountId + "?command=approve";
        return this;
    }

    public CommandWrapperBuilder undoSavingsAccountApplication(final Long accountId) {
        this.actionName = ACTION_APPROVALUNDO;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=undoapproval";
        return this;
    }

    public CommandWrapperBuilder undoGSIMApplicationApproval(final Long accountId) {
        this.actionName = ACTION_APPROVALUNDO;
        this.entityName = ENTITY_GSIMACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=undoapproval";
        return this;
    }

    public CommandWrapperBuilder savingsAccountActivation(final Long accountId) {
        this.actionName = ACTION_ACTIVATE;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=activate";
        return this;
    }

    public CommandWrapperBuilder gsimAccountActivation(final Long accountId) {
        this.actionName = ACTION_ACTIVATE;
        this.entityName = ENTITY_GSIMACCOUNT;
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=activate";
        return this;
    }

    public CommandWrapperBuilder closeSavingsAccountApplication(final Long accountId) {
        this.actionName = ACTION_CLOSE;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=close";
        return this;
    }

    public CommandWrapperBuilder closeGSIMApplication(final Long accountId) {
        this.actionName = ACTION_CLOSE;
        this.entityName = ENTITY_GSIMACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=close";
        return this;
    }

    public CommandWrapperBuilder createAccountTransfer() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_ACCOUNTTRANSFER;
        this.entityId = null;
        this.href = "/accounttransfers";
        return this;
    }

    public CommandWrapperBuilder createStandingInstruction() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_STANDINGINSTRUCTION;
        this.entityId = null;
        this.href = "/standinginstructions";
        return this;
    }

    public CommandWrapperBuilder updateStandingInstruction(final Long standingInstructionId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_STANDINGINSTRUCTION;
        this.entityId = standingInstructionId;
        this.href = "/standinginstructions";
        return this;
    }

    public CommandWrapperBuilder deleteStandingInstruction(final Long standingInstructionId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_STANDINGINSTRUCTION;
        this.entityId = standingInstructionId;
        this.href = "/standinginstructions";
        return this;
    }

    public CommandWrapperBuilder savingsAccountDeposit(final Long accountId) {
        this.actionName = ACTION_DEPOSIT;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "/transactions";
        return this;
    }

    public CommandWrapperBuilder gsimSavingsAccountDeposit(final Long accountId) {
        this.actionName = ACTION_DEPOSIT;
        this.entityName = ENTITY_GSIMACCOUNT;
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "/transactions";
        return this;
    }

    public CommandWrapperBuilder savingsAccountWithdrawal(final Long accountId) {
        this.actionName = ACTION_WITHDRAWAL;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "/transactions";
        return this;
    }

    public CommandWrapperBuilder undoSavingsAccountTransaction(final Long accountId, final Long transactionId) {
        this.actionName = ACTION_UNDOTRANSACTION;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.subentityId = transactionId;
        this.transactionId = transactionId.toString();
        this.href = "/savingsaccounts/" + accountId + "/transactions/" + transactionId + "?command=undo";
        return this;
    }

    public CommandWrapperBuilder reverseSavingsAccountTransaction(final Long accountId, final Long transactionId) {
        this.actionName = ACTION_REVERSETRANSACTION;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.subentityId = transactionId;
        this.transactionId = transactionId.toString();
        this.href = "/savingsaccounts/" + accountId + "/transactions/" + transactionId + "?command=reverse";
        return this;
    }

    public CommandWrapperBuilder adjustSavingsAccountTransaction(final Long accountId, final Long transactionId) {
        this.actionName = ACTION_ADJUSTTRANSACTION;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.subentityId = transactionId;
        this.transactionId = transactionId.toString();
        this.href = "/savingsaccounts/" + accountId + "/transactions/" + transactionId + "?command=modify";
        return this;
    }

    public CommandWrapperBuilder savingsAccountInterestCalculation(final Long accountId) {
        this.actionName = ACTION_CALCULATEINTEREST;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=calculateInterest";
        return this;
    }

    public CommandWrapperBuilder savingsAccountInterestPosting(final Long accountId) {
        this.actionName = ACTION_POSTINTEREST;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=postInterest";
        return this;
    }

    public CommandWrapperBuilder savingsAccountApplyAnnualFees(final Long accountId) {
        this.actionName = ACTION_APPLYANNUALFEE;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=applyAnnualFees";
        return this;
    }

    public CommandWrapperBuilder createSavingsAccountCharge(final Long savingsAccountId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_SAVINGSACCOUNTCHARGE;
        this.savingsId = savingsAccountId;
        this.href = "/savingsaccounts/" + savingsAccountId + "/charges";
        return this;
    }

    public CommandWrapperBuilder updateSavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_SAVINGSACCOUNTCHARGE;
        this.entityId = savingsAccountChargeId;
        this.savingsId = savingsAccountId;
        this.href = "/savingsaccounts/" + savingsAccountId + "/charges/" + savingsAccountChargeId;
        return this;
    }

    public CommandWrapperBuilder waiveSavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId) {
        this.actionName = ACTION_WAIVE;
        this.entityName = ENTITY_SAVINGSACCOUNTCHARGE;
        this.entityId = savingsAccountChargeId;
        this.savingsId = savingsAccountId;
        this.href = "/savingsaccounts/" + savingsAccountId + "/charges/" + savingsAccountChargeId;
        return this;

    }

    public CommandWrapperBuilder paySavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId) {
        this.actionName = ACTION_PAY;
        this.entityName = ENTITY_SAVINGSACCOUNTCHARGE;
        this.entityId = savingsAccountChargeId;
        this.savingsId = savingsAccountId;
        this.href = "/savingsaccounts/" + savingsAccountId + "/charges/" + savingsAccountChargeId;
        return this;

    }

    public CommandWrapperBuilder inactivateSavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId) {
        this.actionName = ACTION_INACTIVATE;
        this.entityName = ENTITY_SAVINGSACCOUNTCHARGE;
        this.entityId = savingsAccountChargeId;
        this.savingsId = savingsAccountId;
        this.href = "/savingsaccounts/" + savingsAccountId + "/charges/" + savingsAccountChargeId;
        return this;

    }

    public CommandWrapperBuilder deleteSavingsAccountCharge(final Long savingsAccountId, final Long savingsAccountChargeId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_SAVINGSACCOUNTCHARGE;
        this.entityId = savingsAccountChargeId;
        this.savingsId = savingsAccountId;
        this.href = "/savingsaccounts/" + savingsAccountId + "/charges/" + savingsAccountChargeId;
        return this;
    }

    public CommandWrapperBuilder createFixedDepositProduct() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_FIXEDDEPOSITPRODUCT;
        this.entityId = null;
        this.href = "/fixeddepositproducts/template";
        return this;
    }

    public CommandWrapperBuilder updateFixedDepositProduct(final Long productId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_FIXEDDEPOSITPRODUCT;
        this.entityId = productId;
        this.href = "/fixeddepositproducts/" + productId;
        return this;
    }

    public CommandWrapperBuilder deleteFixedDepositProduct(final Long productId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_FIXEDDEPOSITPRODUCT;
        this.entityId = productId;
        this.href = "/fixeddepositproducts/" + productId;
        return this;
    }

    public CommandWrapperBuilder createRecurringDepositProduct() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_RECURRINGDEPOSITPRODUCT;
        this.entityId = null;
        this.href = "/recurringdepositproducts/template";
        return this;
    }

    public CommandWrapperBuilder updateRecurringDepositProduct(final Long productId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_RECURRINGDEPOSITPRODUCT;
        this.entityId = productId;
        this.href = "/recurringdepositproducts/" + productId;
        return this;
    }

    public CommandWrapperBuilder deleteRecurringDepositProduct(final Long productId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_RECURRINGDEPOSITPRODUCT;
        this.entityId = productId;
        this.href = "/recurringdepositproducts/" + productId;
        return this;
    }

    public CommandWrapperBuilder createCalendar(final CommandWrapper resourceDetails, final String supportedEntityType,
            final Long supportedEntityId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_CALENDAR;
        this.clientId = resourceDetails.getClientId();
        this.loanId = resourceDetails.getLoanId();
        this.groupId = resourceDetails.getGroupId();
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/calendars/template";
        return this;
    }

    public CommandWrapperBuilder updateCalendar(final String supportedEntityType, final Long supportedEntityId, final Long calendarId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_CALENDAR;
        this.entityId = calendarId;
        this.groupId = supportedEntityId;
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/calendars/" + calendarId;
        return this;
    }

    public CommandWrapperBuilder deleteCalendar(final String supportedEntityType, final Long supportedEntityId, final Long calendarId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_CALENDAR;
        this.entityId = calendarId;
        this.href = "/" + supportedEntityType + "/" + supportedEntityId + "/calendars/" + calendarId;
        return this;
    }

    public CommandWrapperBuilder createGroup() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_GROUP;
        this.href = "/groups/template";
        return this;
    }

    public CommandWrapperBuilder updateGroup(final Long groupId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_GROUP;
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId;
        return this;
    }

    public CommandWrapperBuilder activateGroup(final Long groupId) {
        this.actionName = ACTION_ACTIVATE;
        this.entityName = ENTITY_GROUP;
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=activate";
        return this;
    }

    public CommandWrapperBuilder saveGroupCollectionSheet(final Long groupId) {
        this.actionName = ACTION_SAVECOLLECTIONSHEET;
        this.entityName = ENTITY_GROUP;
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=saveCollectionSheet";
        return this;
    }

    public CommandWrapperBuilder saveIndividualCollectionSheet() {
        this.actionName = ACTION_SAVE;
        this.entityName = ENTITY_COLLECTIONSHEET;
        this.href = "/collectionsheet?command=saveCollectionSheet";
        return this;
    }

    public CommandWrapperBuilder deleteGroup(final Long groupId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_GROUP;
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId;
        return this;
    }

    public CommandWrapperBuilder associateClientsToGroup(final Long groupId) {
        this.actionName = ACTION_ASSOCIATECLIENTS;
        this.entityName = ENTITY_GROUP;
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=associateClients";
        return this;
    }

    public CommandWrapperBuilder disassociateClientsFromGroup(final Long groupId) {
        this.actionName = ACTION_DISASSOCIATECLIENTS;
        this.entityName = ENTITY_GROUP;
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=disassociateClients";
        return this;
    }

    public CommandWrapperBuilder transferClientsBetweenGroups(final Long sourceGroupId) {
        this.actionName = ACTION_TRANSFERCLIENTS;
        this.entityName = ENTITY_GROUP;
        this.entityId = sourceGroupId;
        this.groupId = sourceGroupId;
        this.href = "/groups/" + sourceGroupId + "?command=transferClients";
        return this;
    }

    public CommandWrapperBuilder unassignGroupStaff(final Long groupId) {
        this.actionName = ACTION_UNASSIGNSTAFF;
        this.entityName = ENTITY_GROUP;
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId;
        return this;
    }

    public CommandWrapperBuilder assignGroupStaff(final Long groupId) {
        this.actionName = ACTION_ASSIGNSTAFF;
        this.entityName = ENTITY_GROUP;
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=assignStaff";
        return this;
    }

    public CommandWrapperBuilder closeGroup(final Long groupId) {
        this.actionName = ACTION_CLOSE;
        this.entityName = ENTITY_GROUP;
        this.entityId = groupId;
        this.groupId = groupId;
        this.href = "/groups/" + groupId + "?command=close";
        return this;
    }

    public CommandWrapperBuilder createCollateral(final Long loanId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_COLLATERAL;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/collaterals/template";
        return this;
    }

    public CommandWrapperBuilder updateCollateral(final Long loanId, final Long collateralId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_COLLATERAL;
        this.entityId = collateralId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/collaterals/" + collateralId;
        return this;
    }

    public CommandWrapperBuilder updateCollateralProduct(final Long collateralId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_COLLATERAL_PRODUCT;
        this.entityId = collateralId;
        this.href = "/collateral-management/" + collateralId;
        return this;
    }

    public CommandWrapperBuilder updateClientCollateralProduct(final Long clientId, final Long collateralId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_CLIENT_COLLATERAL_PRODUCT;
        this.entityId = collateralId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/collateral/" + collateralId;
        return this;
    }

    public CommandWrapperBuilder deleteLoanCollateral(final Long loanId, final Long collateralId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_LOAN_COLLATERAL_PRODUCT;
        this.entityId = collateralId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/collateral/" + collateralId;
        return this;
    }

    public CommandWrapperBuilder deleteCollateral(final Long loanId, final Long collateralId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_COLLATERAL;
        this.entityId = collateralId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/collaterals/" + collateralId;
        return this;
    }

    public CommandWrapperBuilder deleteCollateralProduct(final Long collateralId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_COLLATERAL_PRODUCT;
        this.entityId = collateralId;
        this.href = "/collateral-management/" + collateralId;
        return this;
    }

    public CommandWrapperBuilder deleteClientCollateralProduct(final Long collateralId, final Long clientId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_CLIENT_COLLATERAL_PRODUCT;
        this.entityId = collateralId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/collateral-management/" + collateralId;
        return this;
    }

    public CommandWrapperBuilder addClientCollateralProduct(final Long clientId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_CLIENT_COLLATERAL_PRODUCT;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/collateral-management";
        return this;
    }

    public CommandWrapperBuilder createCenter() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_CENTER;
        this.href = "/centers/template";
        return this;
    }

    public CommandWrapperBuilder updateCenter(final Long centerId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_CENTER;
        this.entityId = centerId;
        this.href = "/centers/" + centerId;
        return this;
    }

    public CommandWrapperBuilder deleteCenter(final Long centerId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_CENTER;
        this.entityId = centerId;
        this.href = "/centers/" + centerId;
        return this;
    }

    public CommandWrapperBuilder activateCenter(final Long centerId) {
        this.actionName = ACTION_ACTIVATE;
        this.entityName = ENTITY_CENTER;
        this.entityId = centerId;
        this.groupId = centerId;
        this.href = "/centers/" + centerId + "?command=activate";
        return this;
    }

    public CommandWrapperBuilder saveCenterCollectionSheet(final Long centerId) {
        this.actionName = ACTION_SAVECOLLECTIONSHEET;
        this.entityName = ENTITY_CENTER;
        this.entityId = centerId;
        this.groupId = centerId;
        this.href = "/centers/" + centerId + "?command=saveCollectionSheet";
        return this;
    }

    public CommandWrapperBuilder closeCenter(final Long centerId) {
        this.actionName = ACTION_CLOSE;
        this.entityName = ENTITY_CENTER;
        this.entityId = centerId;
        this.groupId = centerId;
        this.href = "/centers/" + centerId + "?command=close";
        return this;
    }

    public CommandWrapperBuilder associateGroupsToCenter(final Long centerId) {
        this.actionName = ACTION_ASSOCIATEGROUPS;
        this.entityName = ENTITY_CENTER;
        this.entityId = centerId;
        this.groupId = centerId;
        this.href = "/groups/" + centerId + "?command=associateGroups";
        return this;
    }

    public CommandWrapperBuilder disassociateGroupsFromCenter(final Long centerId) {
        this.actionName = ACTION_DISASSOCIATEGROUPS;
        this.entityName = ENTITY_CENTER;
        this.entityId = centerId;
        this.groupId = centerId;
        this.href = "/groups/" + centerId + "?command=disassociateGroups";
        return this;
    }

    public CommandWrapperBuilder createAccountingRule() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_ACCOUNTINGRULE;
        this.entityId = null;
        this.href = "/accountingrules/template";
        return this;
    }

    public CommandWrapperBuilder updateAccountingRule(final Long accountingRuleId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_ACCOUNTINGRULE;
        this.entityId = accountingRuleId;
        this.href = "/accountingrules/" + accountingRuleId;
        return this;
    }

    public CommandWrapperBuilder deleteAccountingRule(final Long accountingRuleId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_ACCOUNTINGRULE;
        this.entityId = accountingRuleId;
        this.href = "/accountingrules/" + accountingRuleId;
        return this;
    }

    public CommandWrapperBuilder createHoliday() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_HOLIDAY;
        this.entityId = null;
        this.href = "/holidays/template";
        return this;
    }

    public CommandWrapperBuilder activateHoliday(final Long holidayId) {
        this.actionName = ACTION_ACTIVATE;
        this.entityName = ENTITY_HOLIDAY;
        this.entityId = holidayId;
        this.href = "/holidays/" + holidayId + "command=activate";
        return this;
    }

    public CommandWrapperBuilder updateHoliday(final Long holidayId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_HOLIDAY;
        this.entityId = holidayId;
        this.href = "/holidays/" + holidayId;
        return this;
    }

    public CommandWrapperBuilder deleteHoliday(final Long holidayId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_HOLIDAY;
        this.entityId = holidayId;
        this.href = "/holidays/" + holidayId + "command=delete";
        return this;
    }

    public CommandWrapperBuilder assignRole(final Long groupId) {
        this.actionName = ACTION_ASSIGNROLE;
        this.entityName = ENTITY_GROUP;
        this.groupId = groupId;
        this.entityId = null;
        this.href = "/groups/" + groupId + "?command=assignRole";
        return this;
    }

    public CommandWrapperBuilder unassignRole(final Long groupId, final Long roleId) {
        this.actionName = ACTION_UNASSIGNROLE;
        this.entityName = ENTITY_GROUP;
        this.groupId = groupId;
        this.entityId = roleId;
        this.href = "/groups/" + groupId + "?command=unassignRole";
        return this;
    }

    public CommandWrapperBuilder updateRole(final Long groupId, final Long roleId) {
        this.actionName = ACTION_UPDATEROLE;
        this.entityName = ENTITY_GROUP;
        this.groupId = groupId;
        this.entityId = roleId;
        this.href = "/groups/" + groupId + "?command=updateRole";
        return this;
    }

    public CommandWrapperBuilder unassignClientStaff(final Long clientId) {
        this.actionName = ACTION_UNASSIGNSTAFF;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=unassignStaff";
        return this;
    }

    public CommandWrapperBuilder createTemplate() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_TEMPLATE;
        this.entityId = null;
        this.href = "/templates";
        return this;
    }

    public CommandWrapperBuilder updateTemplate(final Long templateId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_TEMPLATE;
        this.entityId = templateId;
        this.href = "/templates/" + templateId;
        return this;
    }

    public CommandWrapperBuilder deleteTemplate(final Long templateId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_TEMPLATE;
        this.entityId = templateId;
        this.href = "/templates/" + templateId;
        return this;
    }

    public CommandWrapperBuilder assignClientStaff(final Long clientId) {
        this.actionName = ACTION_ASSIGNSTAFF;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=assignStaff";
        return this;
    }

    public CommandWrapperBuilder updateClientSavingsAccount(final Long clientId) {
        this.actionName = ACTION_UPDATESAVINGSACCOUNT;
        this.entityName = ENTITY_CLIENT;
        this.entityId = clientId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "?command=updateSavingsAccount";
        return this;
    }

    public CommandWrapperBuilder createProductMix(final Long productId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_PRODUCTMIX;
        this.entityId = null;
        this.productId = productId;
        this.href = "/loanproducts/" + productId + "/productmix";
        return this;
    }

    public CommandWrapperBuilder updateProductMix(final Long productId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_PRODUCTMIX;
        this.entityId = null;
        this.productId = productId;
        this.href = "/loanproducts/" + productId + "/productmix";
        return this;
    }

    public CommandWrapperBuilder deleteProductMix(final Long productId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_PRODUCTMIX;
        this.entityId = null;
        this.productId = productId;
        this.href = "/loanproducts/" + productId + "/productmix";
        return this;
    }

    public CommandWrapperBuilder withProduct(final Long productId) {
        this.productId = productId;
        return this;
    }

    public CommandWrapperBuilder updateJobDetail(final Long jobId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_SCHEDULER;
        this.entityId = jobId;
        this.href = "/updateJobDetail/" + jobId + "/updateJobDetail";
        return this;
    }

    public CommandWrapperBuilder executeSchedulerJob(final Long jobId) {
        this.actionName = ACTION_EXECUTEJOB;
        this.entityName = ENTITY_SCHEDULER;
        this.entityId = jobId;
        this.href = "/jobs/" + jobId + "?command=executeJob";
        return this;
    }

    /**
     * Deposit account mappings
     */

    public CommandWrapperBuilder createFixedDepositAccount() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.entityId = null;
        this.href = "/fixeddepositaccounts/template";
        return this;
    }

    public CommandWrapperBuilder updateFixedDepositAccount(final Long accountId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId;
        return this;
    }

    public CommandWrapperBuilder deleteFixedDepositAccount(final Long accountId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId;
        return this;
    }

    public CommandWrapperBuilder rejectFixedDepositAccountApplication(final Long accountId) {
        this.actionName = ACTION_REJECT;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=reject";
        return this;
    }

    public CommandWrapperBuilder withdrawFixedDepositAccountApplication(final Long accountId) {
        this.actionName = ACTION_WITHDRAW;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=withdrawnByApplicant";
        return this;
    }

    public CommandWrapperBuilder approveFixedDepositAccountApplication(final Long accountId) {
        this.actionName = ACTION_APPROVE;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=approve";
        return this;
    }

    public CommandWrapperBuilder undoFixedDepositAccountApplication(final Long accountId) {
        this.actionName = ACTION_APPROVALUNDO;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=undoapproval";
        return this;
    }

    public CommandWrapperBuilder undoFixedDepositAccountTransaction(final Long accountId, final Long transactionId) {
        this.actionName = "UNDOTRANSACTION";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.subentityId = transactionId;
        this.transactionId = transactionId.toString();
        this.href = "/fixeddepositaccounts/" + accountId + "/transactions/" + transactionId + "?command=undo";
        return this;
    }

    public CommandWrapperBuilder adjustFixedDepositAccountTransaction(final Long accountId, final Long transactionId) {
        this.actionName = "ADJUSTTRANSACTION";
        this.entityName = "FIXEDDEPOSITACCOUNT";
        this.savingsId = accountId;
        this.entityId = accountId;
        this.subentityId = transactionId;
        this.transactionId = transactionId.toString();
        this.href = "/fixeddepositaccounts/" + accountId + "/transactions/" + transactionId + "?command=modify";
        return this;
    }

    public CommandWrapperBuilder fixedDepositAccountActivation(final Long accountId) {
        this.actionName = ACTION_ACTIVATE;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=activate";
        return this;
    }

    public CommandWrapperBuilder closeFixedDepositAccount(final Long accountId) {
        this.actionName = ACTION_CLOSE;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=close";
        return this;
    }

    public CommandWrapperBuilder prematureCloseFixedDepositAccount(final Long accountId) {
        this.actionName = ACTION_PREMATURECLOSE;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=prematureClose";
        return this;
    }

    public CommandWrapperBuilder fixedDepositAccountInterestCalculation(final Long accountId) {
        this.actionName = ACTION_CALCULATEINTEREST;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=calculateInterest";
        return this;
    }

    public CommandWrapperBuilder fixedDepositAccountInterestPosting(final Long accountId) {
        this.actionName = ACTION_POSTINTEREST;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "?command=postInterest";
        return this;
    }

    public CommandWrapperBuilder fixedDepositAccountDeposit(final Long accountId) {
        this.actionName = ACTION_DEPOSIT;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "/transactions?command=deposit";
        return this;
    }

    public CommandWrapperBuilder fixedDepositAccountWithdrawal(final Long accountId) {
        this.actionName = ACTION_WITHDRAWAL;
        this.entityName = ENTITY_FIXEDDEPOSITACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/fixeddepositaccounts/" + accountId + "/transactions?command=withdrawal";
        return this;
    }

    public CommandWrapperBuilder createRecurringDepositAccount() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.entityId = null;
        this.href = "/recurringdepositaccounts/template";
        return this;
    }

    public CommandWrapperBuilder updateRecurringDepositAccount(final Long accountId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId;
        return this;
    }

    public CommandWrapperBuilder recurringAccountDeposit(final Long accountId) {
        this.actionName = ACTION_DEPOSIT;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "/transactions?command=deposit";
        return this;
    }

    public CommandWrapperBuilder recurringAccountWithdrawal(final Long accountId) {
        this.actionName = ACTION_WITHDRAWAL;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "/transactions?command=withdrawal";
        return this;
    }

    public CommandWrapperBuilder adjustRecurringAccountTransaction(final Long accountId, final Long transactionId) {
        this.actionName = ACTION_ADJUSTTRANSACTION;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.subentityId = transactionId;
        this.transactionId = transactionId.toString();
        this.href = "/recurringdepositaccounts/" + accountId + "/transactions/" + transactionId + "?command=modify";
        return this;
    }

    public CommandWrapperBuilder undoRecurringAccountTransaction(final Long accountId, final Long transactionId) {
        this.actionName = ACTION_UNDOTRANSACTION;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.subentityId = transactionId;
        this.transactionId = transactionId.toString();
        this.href = "/recurringdepositaccounts/" + accountId + "/transactions/" + transactionId + "?command=undo";
        return this;
    }

    public CommandWrapperBuilder deleteRecurringDepositAccount(final Long accountId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId;
        return this;
    }

    public CommandWrapperBuilder rejectRecurringDepositAccountApplication(final Long accountId) {
        this.actionName = ACTION_REJECT;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=reject";
        return this;
    }

    public CommandWrapperBuilder withdrawRecurringDepositAccountApplication(final Long accountId) {
        this.actionName = ACTION_WITHDRAW;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=withdrawnByApplicant";
        return this;
    }

    public CommandWrapperBuilder approveRecurringDepositAccountApplication(final Long accountId) {
        this.actionName = ACTION_APPROVE;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=approve";
        return this;
    }

    public CommandWrapperBuilder undoRecurringDepositAccountApplication(final Long accountId) {
        this.actionName = ACTION_APPROVALUNDO;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=undoapproval";
        return this;
    }

    public CommandWrapperBuilder recurringDepositAccountActivation(final Long accountId) {
        this.actionName = ACTION_ACTIVATE;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=activate";
        return this;
    }

    public CommandWrapperBuilder closeRecurringDepositAccount(final Long accountId) {
        this.actionName = ACTION_CLOSE;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=close";
        return this;
    }

    public CommandWrapperBuilder updateDepositAmountForRecurringDepositAccount(final Long accountId) {
        this.actionName = DepositsApiConstants.UPDATE_DEPOSIT_AMOUNT.toUpperCase(Locale.ROOT);
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=" + DepositsApiConstants.UPDATE_DEPOSIT_AMOUNT;
        return this;
    }

    public CommandWrapperBuilder prematureCloseRecurringDepositAccount(final Long accountId) {
        this.actionName = ACTION_PREMATURECLOSE;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=prematureClose";
        return this;
    }

    public CommandWrapperBuilder recurringDepositAccountInterestCalculation(final Long accountId) {
        this.actionName = ACTION_CALCULATEINTEREST;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=calculateInterest";
        return this;
    }

    public CommandWrapperBuilder recurringDepositAccountInterestPosting(final Long accountId) {
        this.actionName = ACTION_POSTINTEREST;
        this.entityName = ENTITY_RECURRINGDEPOSITACCOUNT;
        this.savingsId = accountId;
        this.entityId = accountId;
        this.href = "/recurringdepositaccounts/" + accountId + "?command=postInterest";
        return this;
    }

    public CommandWrapperBuilder createOfficeToGLAccountMapping() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_FINANCIALACTIVITYACCOUNT;
        this.entityId = null;
        this.href = "/organizationglaccounts/template";
        return this;
    }

    public CommandWrapperBuilder updateOfficeToGLAccountMapping(final Long mappingId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_FINANCIALACTIVITYACCOUNT;
        this.entityId = mappingId;
        this.href = "/organizationglaccounts/" + mappingId;
        return this;
    }

    public CommandWrapperBuilder deleteOfficeToGLAccountMapping(final Long mappingId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_FINANCIALACTIVITYACCOUNT;
        this.entityId = mappingId;
        this.href = "/organizationglaccounts/" + mappingId;
        return this;
    }

    public CommandWrapperBuilder registerDBDatatable(final String datatable, final String apptable) {
        this.actionName = ACTION_REGISTER;
        this.entityName = ENTITY_DATATABLE;
        this.entityId = null;
        this.href = "/datatables/register/" + datatable + "/" + apptable;
        return this;
    }

    public CommandWrapperBuilder registerSurvey(final String datatable, final String apptable) {
        this.actionName = ACTION_REGISTER;
        this.entityName = ENTITY_SURVEY;
        this.entityId = null;
        this.href = "/survey/register/" + datatable + "/" + apptable;
        return this;
    }

    public CommandWrapperBuilder fullFilSurvey(final String datatable, final Long apptableId) {
        this.entityName = datatable;
        this.entityId = apptableId;
        this.actionName = ACTION_CREATE;
        this.href = "/survey/" + datatable + "/" + apptableId;
        return this;
    }

    public CommandWrapperBuilder updateLikelihood(final Long entityId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_LIKELIHOOD;
        this.href = "/likelihood/" + entityId;
        this.entityId = entityId;
        return this;
    }

    public CommandWrapperBuilder assignSavingsOfficer(final Long accountId) {
        this.actionName = ACTION_UPDATESAVINGSOFFICER;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?command=assignSavingsOfficer";
        return this;
    }

    public CommandWrapperBuilder unassignSavingsOfficer(final Long accountId) {
        this.actionName = ACTION_REMOVESAVINGSOFFICER;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?commad=unassignSavingsOfficer";
        return this;
    }

    public CommandWrapperBuilder createLoanRescheduleRequest(final String entityName) {
        this.actionName = ACTION_CREATE;
        this.entityName = entityName;
        this.entityId = null;
        this.href = "/rescheduleloans";
        return this;
    }

    public CommandWrapperBuilder approveLoanRescheduleRequest(final String entityName, final Long requestId) {
        this.actionName = ACTION_APPROVE;
        this.entityName = entityName;
        this.entityId = requestId;
        this.href = "/rescheduleloans/" + requestId + "?command=approve";
        return this;
    }

    public CommandWrapperBuilder rejectLoanRescheduleRequest(final String entityName, final Long requestId) {
        this.actionName = ACTION_REJECT;
        this.entityName = entityName;
        this.entityId = requestId;
        this.href = "/rescheduleloans/" + requestId + "?command=reject";
        return this;
    }

    public CommandWrapperBuilder createAccountNumberFormat() {
        this.actionName = ACTION_CREATE;
        this.entityName = AccountNumberFormatConstants.ENTITY_NAME.toUpperCase(Locale.ROOT);
        this.href = AccountNumberFormatConstants.resourceRelativeURL;
        return this;
    }

    public CommandWrapperBuilder updateAccountNumberFormat(final Long accountNumberFormatId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = AccountNumberFormatConstants.ENTITY_NAME.toUpperCase(Locale.ROOT);
        this.entityId = accountNumberFormatId;
        this.href = AccountNumberFormatConstants.resourceRelativeURL + "/" + accountNumberFormatId;
        return this;
    }

    public CommandWrapperBuilder deleteAccountNumberFormat(final Long accountNumberFormatId) {
        this.actionName = ACTION_DELETE;
        this.entityName = AccountNumberFormatConstants.ENTITY_NAME.toUpperCase(Locale.ROOT);
        this.entityId = accountNumberFormatId;
        this.href = "AccountNumberFormatConstants.resourceRelativeURL" + "/" + accountNumberFormatId;
        this.json = "{}";
        return this;
    }

    public CommandWrapperBuilder refundByTransfer() {
        this.actionName = ACTION_REFUNDBYTRANSFER;
        this.entityName = ENTITY_ACCOUNTTRANSFER;
        this.entityId = null;
        this.href = "/refundByTransfer";
        return this;
    }

    public CommandWrapperBuilder createTeller() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_TELLER;
        this.entityId = null;
        this.href = "/tellers/templates";
        return this;
    }

    public CommandWrapperBuilder updateTeller(final Long tellerId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_TELLER;
        this.entityId = tellerId;
        this.href = "/tellers/" + tellerId;
        return this;
    }

    public CommandWrapperBuilder deleteTeller(final Long tellerId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_TELLER;
        this.entityId = tellerId;
        this.href = "/tellers/" + tellerId;
        return this;
    }

    public CommandWrapperBuilder allocateTeller(final long tellerId) {
        this.actionName = ACTION_ALLOCATECASHIER;
        this.entityName = ENTITY_TELLER;
        this.entityId = tellerId;
        this.href = "/tellers/" + tellerId + "/cashiers/templates";
        return this;
    }

    public CommandWrapperBuilder updateAllocationTeller(final Long tellerId, final Long cashierId) {
        this.actionName = ACTION_UPDATECASHIERALLOCATION;
        this.entityName = ENTITY_TELLER;
        this.entityId = tellerId;
        this.subentityId = cashierId;
        this.href = "/tellers/" + tellerId + "/cashiers/" + cashierId;
        return this;
    }

    public CommandWrapperBuilder deleteAllocationTeller(final Long tellerId, final Long cashierId) {
        this.actionName = ACTION_DELETECASHIERALLOCATION;
        this.entityName = ENTITY_TELLER;
        this.entityId = tellerId;
        this.subentityId = cashierId;
        this.href = "/tellers/" + tellerId + "/cashiers/" + cashierId;
        return this;
    }

    public CommandWrapperBuilder allocateCashToCashier(final Long tellerId, final Long cashierId) {
        this.actionName = ACTION_ALLOCATECASHTOCASHIER;
        this.entityName = ENTITY_TELLER;
        this.entityId = tellerId;
        this.subentityId = cashierId;
        this.href = "/tellers/" + tellerId + "/cashiers/" + cashierId + "/allocate";
        return this;
    }

    public CommandWrapperBuilder settleCashFromCashier(final Long tellerId, final Long cashierId) {
        this.actionName = ACTION_SETTLECASHFROMCASHIER;
        this.entityName = ENTITY_TELLER;
        this.entityId = tellerId;
        this.subentityId = cashierId;
        this.href = "/tellers/" + tellerId + "/cashiers/" + cashierId + "/settle";
        return this;
    }

    public CommandWrapperBuilder deleteRole(Long roleId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_ROLE;
        this.entityId = roleId;
        this.href = "/roles/" + roleId;
        this.json = "{}";
        return this;
    }

    public CommandWrapperBuilder disableRole(Long roleId) {
        this.actionName = ACTION_DISABLE;
        this.entityName = ENTITY_ROLE;
        this.entityId = roleId;
        this.href = "/roles/" + roleId + "/disbales";
        this.json = "{}";
        return this;
    }

    public CommandWrapperBuilder enableRole(Long roleId) {
        this.actionName = ACTION_ENABLE;
        this.entityName = ENTITY_ROLE;
        this.entityId = roleId;
        this.href = "/roles/" + roleId + "/enable";
        this.json = "{}";
        return this;
    }

    public CommandWrapperBuilder createMap(Long relId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_ENTITYMAPPING;
        this.entityId = relId;
        this.href = "/entitytoentitymapping/" + relId;
        return this;
    }

    public CommandWrapperBuilder updateMap(Long mapId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_ENTITYMAPPING;
        this.entityId = mapId;
        this.href = "/entitytoentitymapping" + mapId;
        return this;
    }

    public CommandWrapperBuilder deleteMap(final Long mapId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_ENTITYMAPPING;
        this.entityId = mapId;
        this.href = "/entitytoentitymapping/" + mapId;
        return this;
    }

    public CommandWrapperBuilder updateWorkingDays() {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_WORKINGDAYS;
        this.href = "/workingdays/";
        return this;
    }

    public CommandWrapperBuilder updatePasswordPreferences() {
        this.actionName = ACTION_UPDATE;
        this.entityName = PasswordPreferencesApiConstants.ENTITY_NAME;
        this.href = "/" + PasswordPreferencesApiConstants.RESOURCE_NAME;
        return this;
    }

    public CommandWrapperBuilder updateExternalServiceProperties(final String externalServiceName) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_EXTERNALSERVICES;
        this.transactionId = externalServiceName;
        this.href = "/externalservices/" + externalServiceName;
        return this;
    }

    public CommandWrapperBuilder createClientCharge(final Long clientId) {
        this.actionName = ClientApiConstants.CLIENT_CHARGE_ACTION_CREATE;
        this.entityName = ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/charges";
        return this;
    }

    public CommandWrapperBuilder deleteClientCharge(final Long clientId, final Long chargeId) {
        this.actionName = ClientApiConstants.CLIENT_CHARGE_ACTION_DELETE;
        this.entityName = ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME;
        this.clientId = clientId;
        this.entityId = chargeId;
        this.href = "/clients/" + clientId + "/charges/" + chargeId;
        return this;
    }

    public CommandWrapperBuilder waiveClientCharge(final Long clientId, final Long chargeId) {
        this.actionName = ClientApiConstants.CLIENT_CHARGE_ACTION_WAIVE;
        this.entityName = ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME;
        this.entityId = chargeId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/charges/" + chargeId + "?command=waive";
        return this;
    }

    public CommandWrapperBuilder payClientCharge(final Long clientId, final Long chargeId) {
        this.actionName = ClientApiConstants.CLIENT_CHARGE_ACTION_PAY;
        this.entityName = ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME;
        this.entityId = chargeId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/charges/" + chargeId + "?command=paycharge";
        return this;
    }

    public CommandWrapperBuilder undoClientTransaction(final Long clientId, final Long transactionId) {
        this.actionName = ClientApiConstants.CLIENT_TRANSACTION_ACTION_UNDO;
        this.entityName = ClientApiConstants.CLIENT_RESOURCE_NAME;
        this.entityId = transactionId;
        this.clientId = clientId;
        this.href = "/clients/" + clientId + "/transactions/" + transactionId + "?command=undo";
        return this;
    }

    public CommandWrapperBuilder createProvisioningCategory() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_PROVISIONCATEGORY;
        this.entityId = null;
        this.href = "/provisioningcategory";
        return this;
    }

    public CommandWrapperBuilder updateProvisioningCategory(final Long cateoryId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_PROVISIONCATEGORY;
        this.entityId = cateoryId;
        this.href = "/provisioningcategory/" + cateoryId;
        return this;
    }

    public CommandWrapperBuilder deleteProvisioningCategory(final Long categoryId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_PROVISIONCATEGORY;
        this.entityId = categoryId;
        this.href = "/provisioningcategory/" + categoryId;
        return this;
    }

    public CommandWrapperBuilder createProvisioningCriteria() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_PROVISIONCRITERIA;
        this.entityId = null;
        this.href = "/provisioningcriteria";
        return this;
    }

    public CommandWrapperBuilder updateProvisioningCriteria(final Long criteriaId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_PROVISIONCRITERIA;
        this.entityId = criteriaId;
        this.href = "/provisioningcriteria/" + criteriaId;
        return this;
    }

    public CommandWrapperBuilder deleteProvisioningCriteria(final Long criteriaId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_PROVISIONCRITERIA;
        this.entityId = criteriaId;
        this.href = "/provisioningcriteria/" + criteriaId;
        return this;
    }

    public CommandWrapperBuilder createProvisioningEntries() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_PROVISIONENTRIES;
        this.entityId = null;
        this.href = "/provisioningentries";
        return this;
    }

    public CommandWrapperBuilder createProvisioningJournalEntries(final Long entryId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_PROVISIONJOURNALENTRIES;
        this.entityId = entryId;
        this.href = "/provisioningentries/" + entryId;
        return this;
    }

    public CommandWrapperBuilder reCreateProvisioningEntries(final Long entryId) {
        this.actionName = ACTION_RECREATE;
        this.entityName = ENTITY_PROVISIONENTRIES;
        this.entityId = entryId;
        this.href = "/provisioningentries/" + entryId;
        return this;
    }

    public CommandWrapperBuilder createFloatingRate() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_FLOATINGRATE;
        this.entityId = null;
        this.href = "/floatingrates";
        return this;
    }

    public CommandWrapperBuilder updateFloatingRate(final Long floatingRateId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_FLOATINGRATE;
        this.entityId = floatingRateId;
        this.href = "/floatingrates/" + floatingRateId;
        return this;
    }

    public CommandWrapperBuilder createScheduleExceptions(final Long loanId) {
        this.actionName = ACTION_CREATESCHEDULEEXCEPTIONS;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/schedule";
        return this;
    }

    public CommandWrapperBuilder deleteScheduleExceptions(final Long loanId) {
        this.actionName = ACTION_DELETESCHEDULEEXCEPTIONS;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/schedule";
        return this;
    }

    public CommandWrapperBuilder createProduct(String productType) {
        this.entityName = productType.toUpperCase(Locale.ROOT) + "PRODUCT"; // To Support
        // different
        // type of
        // products
        this.actionName = ACTION_CREATE;
        this.entityId = null;
        this.href = "/products/" + productType;
        return this;
    }

    public CommandWrapperBuilder updateProduct(String productType, final Long productId) {
        this.entityName = productType.toUpperCase(Locale.ROOT) + "PRODUCT";
        this.actionName = ACTION_UPDATE;
        this.entityId = productId;
        this.href = "/products/" + productType + "/" + productId;
        return this;
    }

    public CommandWrapperBuilder createAccount(String accountType) {
        this.entityName = accountType.toUpperCase(Locale.ROOT) + "ACCOUNT"; // To Support
        // different
        // type of
        // Accounts
        this.actionName = ACTION_CREATE;
        this.entityId = null;
        this.href = "/accounts/" + accountType;
        return this;
    }

    public CommandWrapperBuilder updateAccount(String accountType, final Long accountId) {
        this.entityName = accountType.toUpperCase(Locale.ROOT) + "ACCOUNT";
        this.actionName = ACTION_UPDATE;
        this.entityId = accountId;
        this.href = "/accounts/" + accountType + "/" + accountId;
        return this;
    }

    public CommandWrapperBuilder createProductCommand(String productType, String command, final Long productId) {
        this.entityName = productType.toUpperCase(Locale.ROOT) + "PRODUCT";
        this.actionName = ACTION_CREATE + "_" + command.toUpperCase(Locale.ROOT);
        this.entityId = productId;
        this.href = "/products/" + productType + "/" + productId + "?command=" + command;
        return this;
    }

    public CommandWrapperBuilder createShareProductDividendPayoutCommand(final Long productId) {
        this.entityName = ENTITY_SHAREPRODUCT;
        this.actionName = ACTION_CREATE_DIVIDEND;
        this.entityId = productId;
        this.href = "/shareproduct/" + productId + "/dividend";
        return this;
    }

    public CommandWrapperBuilder approveShareProductDividendPayoutCommand(final Long productId, final Long dividendId) {
        this.entityName = ENTITY_SHAREPRODUCT;
        this.actionName = ACTION_APPROVE_DIVIDEND;
        this.entityId = dividendId;
        this.href = "/shareproduct/" + productId + "/dividend/" + dividendId;
        return this;
    }

    public CommandWrapperBuilder deleteShareProductDividendPayoutCommand(final Long productId, final Long dividendId) {
        this.entityName = ENTITY_SHAREPRODUCT;
        this.actionName = ACTION_DELETE_DIVIDEND;
        this.entityId = dividendId;
        this.href = "/shareproduct/" + productId + "/dividend/" + dividendId;
        return this;
    }

    public CommandWrapperBuilder createAccountCommand(String accountType, final Long accountId, String command) {
        this.entityName = accountType.toUpperCase(Locale.ROOT) + "ACCOUNT";
        this.actionName = command.toUpperCase(Locale.ROOT);
        this.entityId = accountId;
        this.href = "/accounts/" + accountType + "/" + accountId + "?command=" + command;
        return this;
    }

    public CommandWrapperBuilder createTaxComponent() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_TAXCOMPONENT;
        this.entityId = null;
        this.href = "/taxes/component";
        return this;
    }

    public CommandWrapperBuilder updateTaxComponent(final Long taxComponentId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_TAXCOMPONENT;
        this.entityId = taxComponentId;
        this.href = "/taxes/component/" + taxComponentId;
        return this;
    }

    public CommandWrapperBuilder createTaxGroup() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_TAXGROUP;
        this.entityId = null;
        this.href = "/taxes/group";
        return this;
    }

    public CommandWrapperBuilder updateTaxGroup(final Long taxGroupId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_TAXGROUP;
        this.entityId = taxGroupId;
        this.href = "/taxes/group/" + taxGroupId;
        return this;
    }

    public CommandWrapperBuilder updateWithHoldTax(final Long accountId) {
        this.actionName = ACTION_UPDATEWITHHOLDTAX;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = accountId;
        this.href = "/savingsaccounts/" + accountId + "?commad=updateTaxWithHoldTax";
        return this;
    }

    public CommandWrapperBuilder createEntityDatatableChecks(final String json) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_ENTITY_DATATABLE_CHECK;
        this.entityId = null;
        this.href = "/entityDatatableChecks/";
        this.json = json;
        return this;
    }

    public CommandWrapperBuilder deleteEntityDatatableChecks(final long entityDatatableCheckId, final String json) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_ENTITY_DATATABLE_CHECK;
        this.entityId = entityDatatableCheckId;
        this.href = "/entityDatatableChecks/" + entityDatatableCheckId;
        this.json = json;
        return this;
    }

    public CommandWrapperBuilder createReportMailingJob(final String entityName) {
        this.actionName = ACTION_CREATE;
        this.entityName = entityName;
        this.entityId = null;
        this.href = "/reportmailingjobs";
        return this;
    }

    public CommandWrapperBuilder updateReportMailingJob(final String entityName, final Long entityId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = entityName;
        this.entityId = entityId;
        this.href = "/reportmailingjobs/" + entityId;
        return this;
    }

    public CommandWrapperBuilder deleteReportMailingJob(final String entityName, final Long entityId) {
        this.actionName = ACTION_DELETE;
        this.entityName = entityName;
        this.entityId = entityId;
        this.href = "/reportmailingjobs/" + entityId;
        return this;
    }

    public CommandWrapperBuilder createSmsCampaign() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_SMSCAMPAIGN;
        this.entityId = null;
        this.href = "/smscampaigns";
        return this;
    }

    public CommandWrapperBuilder updateSmsCampaign(final Long resourceId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_SMSCAMPAIGN;
        this.entityId = resourceId;
        this.href = "/smscampaigns/" + resourceId;
        return this;
    }

    public CommandWrapperBuilder activateSmsCampaign(final Long resourceId) {
        this.actionName = ACTION_ACTIVATE;
        this.entityName = ENTITY_SMSCAMPAIGN;
        this.entityId = resourceId;
        this.href = "/smscampaigns/" + resourceId + "?command=activate";
        return this;
    }

    public CommandWrapperBuilder closeSmsCampaign(final Long resourceId) {
        this.actionName = ACTION_CLOSE;
        this.entityName = ENTITY_SMSCAMPAIGN;
        this.entityId = resourceId;
        this.href = "/smscampaigns/" + resourceId + "?command=close";
        return this;
    }

    public CommandWrapperBuilder reactivateSmsCampaign(final Long resourceId) {
        this.actionName = ACTION_REACTIVATE;
        this.entityName = ENTITY_SMSCAMPAIGN;
        this.entityId = resourceId;
        this.href = "/smscampaigns/" + resourceId + "?command=reactivate";
        return this;
    }

    public CommandWrapperBuilder deleteSmsCampaign(final Long resourceId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_SMSCAMPAIGN;
        this.entityId = resourceId;
        this.href = "/smscampaigns/" + resourceId;
        return this;
    }

    public CommandWrapperBuilder holdAmount(final Long accountId) {
        this.actionName = ACTION_HOLDAMOUNT;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "/transactions?command=holdAmount";
        return this;
    }

    public CommandWrapperBuilder releaseAmount(final Long accountId, final Long transactionId) {
        this.actionName = ACTION_RELEASEAMOUNT;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = null;
        this.savingsId = accountId;
        this.transactionId = transactionId.toString();
        this.href = "/savingsaccounts/" + accountId + "/transactions/" + transactionId + "?command=releaseAmount";
        return this;
    }

    public CommandWrapperBuilder blockDebitsFromSavingsAccount(final Long accountId) {
        this.actionName = ACTION_BLOCKDEBIT;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=blockDebit";
        return this;
    }

    public CommandWrapperBuilder unblockDebitsFromSavingsAccount(final Long accountId) {
        this.actionName = ACTION_UNBLOCKDEBIT;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=unblockDebit";
        return this;
    }

    public CommandWrapperBuilder blockCreditsToSavingsAccount(final Long accountId) {
        this.actionName = ACTION_BLOCKCREDIT;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=blockCredit";
        return this;
    }

    public CommandWrapperBuilder unblockCreditsToSavingsAccount(final Long accountId) {
        this.actionName = ACTION_UNBLOCKCREDIT;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=unblockCredit";
        return this;
    }

    public CommandWrapperBuilder blockSavingsAccount(final Long accountId) {
        this.actionName = ACTION_BLOCK;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=block";
        return this;
    }

    public CommandWrapperBuilder unblockSavingsAccount(final Long accountId) {
        this.actionName = ACTION_UNBLOCK;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.savingsId = accountId;
        this.entityId = null;
        this.href = "/savingsaccounts/" + accountId + "?command=unblock";
        return this;
    }

    public CommandWrapperBuilder createAdHoc() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_ADHOC;
        this.href = "/adhocquery/template";
        return this;
    }

    public CommandWrapperBuilder updateAdHoc(final Long adHocId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_ADHOC;
        this.entityId = adHocId;
        this.href = "/adhocquery/" + adHocId;
        return this;
    }

    public CommandWrapperBuilder deleteAdHoc(Long adHocId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_ADHOC;
        this.entityId = adHocId;
        this.href = "/adhocquery/" + adHocId;
        this.json = "{}";
        return this;
    }

    public CommandWrapperBuilder createEmail() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_EMAIL;
        this.entityId = null;
        this.href = "/emailcampaigns/template";
        return this;
    }

    public CommandWrapperBuilder updateEmail(final Long resourceId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_EMAIL;
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId;
        return this;
    }

    public CommandWrapperBuilder deleteEmail(final Long resourceId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_EMAIL;
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId;
        return this;
    }

    public CommandWrapperBuilder createEmailCampaign() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_EMAIL_CAMPAIGN;
        this.entityId = null;
        this.href = "/emailcampaigns/campaign";
        return this;
    }

    public CommandWrapperBuilder updateEmailCampaign(final Long resourceId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_EMAIL_CAMPAIGN;
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId;
        return this;
    }

    public CommandWrapperBuilder deleteEmailCampaign(final Long resourceId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_EMAIL_CAMPAIGN;
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId;
        return this;
    }

    public CommandWrapperBuilder activateEmailCampaign(final Long resourceId) {
        this.actionName = ACTION_ACTIVATE;
        this.entityName = ENTITY_EMAIL_CAMPAIGN;
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId + "?command=activate";
        return this;
    }

    public CommandWrapperBuilder closeEmailCampaign(final Long resourceId) {
        this.actionName = ACTION_CLOSE;
        this.entityName = ENTITY_EMAIL_CAMPAIGN;
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId + "?command=close";
        return this;
    }

    public CommandWrapperBuilder reactivateEmailCampaign(final Long resourceId) {
        this.actionName = ACTION_REACTIVATE;
        this.entityName = ENTITY_EMAIL_CAMPAIGN;
        this.entityId = resourceId;
        this.href = "/emailcampaigns/" + resourceId + "?command=reactivate";
        return this;
    }

    public CommandWrapperBuilder updateEmailConfiguration() {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_EMAIL_CONFIGURATION;
        this.href = "/emailcampaigns/configuration/";
        return this;
    }

    public CommandWrapperBuilder invalidateTwoFactorAccessToken() {
        this.actionName = ACTION_INVALIDATE;
        this.entityName = ENTITY_TWOFACTOR_ACCESSTOKEN;
        this.href = "/twofactor/invalidate";
        return this;
    }

    public CommandWrapperBuilder updateTwoFactorConfiguration() {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_TWOFACTOR_CONFIGURATION;
        this.href = "/twofactor/configure";
        return this;
    }

    public CommandWrapperBuilder createRate() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_RATE;
        this.entityId = null;
        this.href = "/rates/template";
        return this;
    }

    public CommandWrapperBuilder updateRate(final Long rateId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_RATE;
        this.entityId = rateId;
        this.href = "/rates/" + rateId;
        return this;
    }

    public CommandWrapperBuilder createDelinquencyRange() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_DELINQUENCY_RANGE;
        this.href = "/delinquency/range";
        return this;
    }

    public CommandWrapperBuilder updateDelinquencyRange(final Long delinquencyRangeId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_DELINQUENCY_RANGE;
        this.entityId = delinquencyRangeId;
        this.href = "/delinquency/range";
        return this;
    }

    public CommandWrapperBuilder deleteDelinquencyRange(final Long delinquencyRangeId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_DELINQUENCY_RANGE;
        this.entityId = delinquencyRangeId;
        this.href = "/delinquency/range";
        return this;
    }

    public CommandWrapperBuilder createDelinquencyBucket() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_DELINQUENCY_BUCKET;
        this.href = "/delinquency/bucket";
        return this;
    }

    public CommandWrapperBuilder updateDelinquencyBucket(final Long delinquencyBucketId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_DELINQUENCY_BUCKET;
        this.entityId = delinquencyBucketId;
        this.href = "/delinquency/bucket";
        return this;
    }

    public CommandWrapperBuilder deleteDelinquencyBucket(final Long delinquencyBucketId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_DELINQUENCY_BUCKET;
        this.entityId = delinquencyBucketId;
        this.href = "/delinquency/bucket";
        return this;
    }

    public CommandWrapperBuilder updateBusinessStepConfig(String jobName) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_BATCH_BUSINESS_STEP;
        this.href = "/jobs/" + jobName + "/steps";
        this.jobName = jobName;
        return this;
    }

    public CommandWrapperBuilder executeInlineJob(String jobName) {
        this.actionName = ACTION_EXECUTE;
        this.entityName = ENTITY_INLINE_JOB;
        this.href = "/jobs/" + jobName + "/inline";
        this.jobName = jobName;
        return this;
    }

    public CommandWrapperBuilder chargeOff(final Long loanId) {
        this.actionName = ACTION_CHARGEOFF;
        this.entityName = ENTITY_LOAN;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=charge-off";
        return this;
    }

    public CommandWrapperBuilder undoChargeOff(final Long loanId) {
        this.actionName = ACTION_UNDOCHARGEOFF;
        this.entityName = ENTITY_LOAN;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=undo-charge-off";
        return this;
    }

    public CommandWrapperBuilder createExternalAssetOwnerLoanProductAttribute(final Long loanProductId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE;
        this.productId = loanProductId;
        this.href = "/external-asset-owners/loan-product/" + loanProductId + "/attributes";
        return this;
    }

    public CommandWrapperBuilder updateExternalAssetOwnerLoanProductAttribute(final Long loanProductId, final Long attributeId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE;
        this.productId = loanProductId;
        this.entityId = attributeId;
        this.href = "/external-asset-owners/loan-product/" + loanProductId + "/attributes/" + attributeId;
        return this;
    }

    public CommandWrapperBuilder intermediarySaleLoanToExternalAssetOwner(final Long loanId) {
        this.actionName = ACTION_INTERMEDIARYSALE;
        this.entityName = ENTITY_LOAN;
        this.loanId = loanId;
        this.href = "/external-asset-owners/transfers/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder saleLoanToExternalAssetOwner(final Long loanId) {
        this.actionName = ACTION_SALE;
        this.entityName = ENTITY_LOAN;
        this.loanId = loanId;
        this.href = "/external-asset-owners/transfers/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder createExternalAssetOwner() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_EXTERNAL_ASSET_OWNER;
        this.href = "/external-asset-owners";
        return this;
    }

    public CommandWrapperBuilder buybackLoanToExternalAssetOwner(final Long loanId) {
        this.actionName = ACTION_BUYBACK;
        this.entityName = ENTITY_LOAN;
        this.loanId = loanId;
        this.href = "/external-asset-owners/transfers/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder cancelTransactionByIdToExternalAssetOwner(final Long id) {
        this.actionName = ACTION_CANCEL;
        this.entityName = ENTITY_ASSET_OWNER_TRANSACTION;
        this.entityId = id;
        this.href = "/external-asset-owners/transfers/" + id;
        return this;
    }

    public CommandWrapperBuilder downPayment(final Long loanId) {
        this.actionName = ACTION_DOWNPAYMENT;
        this.entityName = ENTITY_LOAN;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=downPayment";
        return this;
    }

    public CommandWrapperBuilder reAge(final Long loanId) {
        this.actionName = ACTION_REAGE;
        this.entityName = ENTITY_LOAN;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=reAge";
        return this;
    }

    public CommandWrapperBuilder undoReAge(final Long loanId) {
        this.actionName = ACTION_UNDO_REAGE;
        this.entityName = ENTITY_LOAN;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=undoReAge";
        return this;
    }

    public CommandWrapperBuilder reAmortize(final Long loanId) {
        this.actionName = ACTION_REAMORTIZE;
        this.entityName = ENTITY_LOAN;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=reAmortize";
        return this;
    }

    public CommandWrapperBuilder undoReAmortize(final Long loanId) {
        this.actionName = ACTION_UNDO_REAMORTIZE;
        this.entityName = ENTITY_LOAN;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions?command=undoReAmortize";
        return this;
    }

    public CommandWrapperBuilder createDelinquencyAction(final Long loanId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_DELINQUENCY_ACTION;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/delinquency-action";
        return this;
    }

    public CommandWrapperBuilder createInterestPause(final long loanId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_INTEREST_PAUSE;
        this.loanId = loanId;
        this.href = "/v1/loans/" + loanId + "/interest-pauses";
        return this;
    }

    public CommandWrapperBuilder createInterestPauseByExternalId(final String loanExternalId) {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_INTEREST_PAUSE;
        this.loanExternalId = new ExternalId(loanExternalId);
        this.href = "/v1/loans/external-id/" + loanExternalId + "/interest-pauses";
        return this;
    }

    public CommandWrapperBuilder deleteInterestPause(final long loanId, final long variationId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_INTEREST_PAUSE;
        this.loanId = loanId;
        this.entityId = variationId;
        this.href = "/v1/loans/" + loanId + "/interest-pauses/" + variationId;
        return this;
    }

    public CommandWrapperBuilder deleteInterestPause(final String loanExternalId, final long variationId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_INTEREST_PAUSE;
        this.loanExternalId = new ExternalId(loanExternalId);
        this.entityId = variationId;
        this.href = "/v1/loans/external-id/" + loanExternalId + "/interest-pauses/" + variationId;
        return this;
    }

    public CommandWrapperBuilder updateInterestPause(final long loanId, final long variationId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_INTEREST_PAUSE;
        this.loanId = loanId;
        this.entityId = variationId;
        this.href = "/v1/loans/" + loanId + "/interest-pauses/" + variationId;
        return this;
    }

    public CommandWrapperBuilder updateInterestPause(final String loanExternalId, final long variationId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_INTEREST_PAUSE;
        this.loanExternalId = new ExternalId(loanExternalId);
        this.entityId = variationId;
        this.href = "/v1/loans/external-id/" + loanExternalId + "/interest-pauses/" + variationId;
        return this;
    }

    public CommandWrapperBuilder addCapitalizedIncome(final Long loanId) {
        this.actionName = ACTION_CAPITALIZEDINCOME;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder capitalizedIncomeAdjustment(final Long loanId, final Long transactionId) {
        this.actionName = ACTION_CAPITALIZEDINCOMEADJUSTMENT;
        this.entityName = ENTITY_LOAN;
        this.entityId = transactionId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/" + transactionId;
        return this;
    }

    public CommandWrapperBuilder buyDownFeeAdjustment(final Long loanId, final Long transactionId) {
        this.actionName = ACTION_BUYDOWNFEEADJUSTMENT;
        this.entityName = ENTITY_LOAN;
        this.entityId = transactionId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/" + transactionId;
        return this;
    }

    public CommandWrapperBuilder applyContractTermination(final Long loanId) {
        this.actionName = ACTION_CONTRACT_TERMINATION;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder undoContractTermination(final Long loanId) {
        this.actionName = ACTION_CONTRACT_TERMINATION_UNDO;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder makeLoanBuyDownFee(final Long loanId) {
        this.actionName = ACTION_BUYDOWNFEE;
        this.entityName = ENTITY_LOAN;
        this.entityId = null;
        this.loanId = loanId;
        this.href = "/loans/" + loanId + "/transactions/template?command=buyDownFee";
        return this;
    }

    public CommandWrapperBuilder updateLoanApprovedAmount(final Long loanId) {
        this.actionName = ACTION_UPDATE_APPROVED_AMOUNT;
        this.entityName = ENTITY_LOAN;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder manualInterestRefund(final Long loanId, final Long transactionId) {
        this.actionName = ACTION_MANUAL_INTEREST_REFUND_TRANSACTION;
        this.entityName = ENTITY_LOAN;
        this.loanId = loanId;
        this.entityId = transactionId;
        this.href = "/loans/" + loanId + "/transactions/" + transactionId + "?command=interest-refund";
        return this;
    }

    public CommandWrapperBuilder updateLoanAvailableDisbursementAmount(final Long loanId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_LOAN_AVAILABLE_DISBURSEMENT_AMOUNT;
        this.entityId = loanId;
        this.loanId = loanId;
        this.href = "/loans/" + loanId;
        return this;
    }

    public CommandWrapperBuilder createLoanOriginator() {
        this.actionName = ACTION_CREATE;
        this.entityName = ENTITY_LOAN_ORIGINATOR;
        this.href = "/loan-originators";
        return this;
    }

    public CommandWrapperBuilder updateLoanOriginator(final Long originatorId) {
        this.actionName = ACTION_UPDATE;
        this.entityName = ENTITY_LOAN_ORIGINATOR;
        this.entityId = originatorId;
        this.href = "/loan-originators/" + originatorId;
        return this;
    }

    public CommandWrapperBuilder deleteLoanOriginator(final Long originatorId) {
        this.actionName = ACTION_DELETE;
        this.entityName = ENTITY_LOAN_ORIGINATOR;
        this.entityId = originatorId;
        this.href = "/loan-originators/" + originatorId;
        return this;
    }

    public CommandWrapperBuilder attachLoanOriginator(final Long loanId, final Long originatorId) {
        this.actionName = ACTION_ATTACH;
        this.entityName = ENTITY_LOAN_ORIGINATOR;
        this.entityId = loanId;
        this.loanId = loanId;
        this.subentityId = originatorId;
        this.href = "/loans/" + loanId + "/originators/" + originatorId;
        return this;
    }

    public CommandWrapperBuilder detachLoanOriginator(final Long loanId, final Long originatorId) {
        this.actionName = ACTION_DETACH;
        this.entityName = ENTITY_LOAN_ORIGINATOR;
        this.entityId = loanId;
        this.loanId = loanId;
        this.subentityId = originatorId;
        this.href = "/loans/" + loanId + "/originators/" + originatorId;
        return this;
    }

    public CommandWrapperBuilder savingsAccountForceWithdrawal(final Long accountId) {
        this.actionName = ACTION_FORCE_WITHDRAWAL;
        this.entityName = ENTITY_SAVINGSACCOUNT;
        this.entityId = accountId;
        this.savingsId = accountId;
        this.href = "/savingsaccounts/" + accountId;
        return this;
    }
}
