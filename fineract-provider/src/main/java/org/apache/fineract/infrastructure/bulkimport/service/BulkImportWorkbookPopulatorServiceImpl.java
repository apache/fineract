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
package org.apache.fineract.infrastructure.bulkimport.service;

import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.populator.*;
import org.apache.fineract.infrastructure.bulkimport.populator.centers.CentersWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.chartofaccounts.ChartOfAccountsWorkbook;
import org.apache.fineract.infrastructure.bulkimport.populator.client.ClientEntityWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.client.ClientPersonWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.fixeddeposits.FixedDepositTransactionWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.fixeddeposits.FixedDepositWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.group.GroupsWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.guarantor.GuarantorWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.journalentry.JournalEntriesWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.loan.LoanWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.loanrepayment.LoanRepaymentWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.office.OfficeWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.recurringdeposit.RecurringDepositTransactionWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.recurringdeposit.RecurringDepositWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.savings.SavingsTransactionsWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.savings.SavingsWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.shareaccount.SharedAccountWorkBookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.staff.StaffWorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.users.UserWorkbookPopulator;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.api.GroupingTypesApiConstants;
import org.apache.fineract.portfolio.group.data.CenterData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.CenterReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.products.data.ProductData;
import org.apache.fineract.portfolio.products.service.ProductReadPlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.*;
import org.apache.fineract.portfolio.savings.service.DepositProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductData;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.service.RoleReadPlatformService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BulkImportWorkbookPopulatorServiceImpl implements BulkImportWorkbookPopulatorService {

  private final PlatformSecurityContext context;
  private final OfficeReadPlatformService officeReadPlatformService;
  private final StaffReadPlatformService staffReadPlatformService;
  private final ClientReadPlatformService clientReadPlatformService;
  private final CenterReadPlatformService centerReadPlatformService;
  private final GroupReadPlatformService groupReadPlatformService;
  private final FundReadPlatformService fundReadPlatformService;
  private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;
  private final LoanProductReadPlatformService loanProductReadPlatformService;
  private final CurrencyReadPlatformService currencyReadPlatformService;
  private final LoanReadPlatformService loanReadPlatformService;
  private final GLAccountReadPlatformService glAccountReadPlatformService;
  private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
  private final CodeValueReadPlatformService codeValueReadPlatformService;
  private final SavingsProductReadPlatformService savingsProductReadPlatformService;
  private final ProductReadPlatformService productReadPlatformService;
  private final ChargeReadPlatformService chargeReadPlatformService;
  private final DepositProductReadPlatformService depositProductReadPlatformService;
  private final RoleReadPlatformService roleReadPlatformService;
  
  @Autowired
  public BulkImportWorkbookPopulatorServiceImpl(final PlatformSecurityContext context,
      final OfficeReadPlatformService officeReadPlatformService,
      final StaffReadPlatformService staffReadPlatformService,
      final ClientReadPlatformService clientReadPlatformService,
      final CenterReadPlatformService centerReadPlatformService,
      final GroupReadPlatformService groupReadPlatformService,
      final FundReadPlatformService fundReadPlatformService,
      final PaymentTypeReadPlatformService paymentTypeReadPlatformService,
      final LoanProductReadPlatformService loanProductReadPlatformService,
      final CurrencyReadPlatformService currencyReadPlatformService,
      final LoanReadPlatformService loanReadPlatformService,
      final GLAccountReadPlatformService glAccountReadPlatformService,
      final SavingsAccountReadPlatformService savingsAccountReadPlatformService,
		final CodeValueReadPlatformService codeValueReadPlatformService,
		final SavingsProductReadPlatformService savingsProductReadPlatformService,
		  final ProductReadPlatformService productReadPlatformService,
		  final ChargeReadPlatformService chargeReadPlatformService,
		  final DepositProductReadPlatformService depositProductReadPlatformService,
		  final RoleReadPlatformService roleReadPlatformService) {
    this.officeReadPlatformService = officeReadPlatformService;
    this.staffReadPlatformService = staffReadPlatformService;
    this.context = context;
    this.clientReadPlatformService=clientReadPlatformService;
    this.centerReadPlatformService=centerReadPlatformService;
    this.groupReadPlatformService=groupReadPlatformService;
    this.fundReadPlatformService=fundReadPlatformService;
    this.paymentTypeReadPlatformService=paymentTypeReadPlatformService;
    this.loanProductReadPlatformService=loanProductReadPlatformService;
    this.currencyReadPlatformService=currencyReadPlatformService;
    this.loanReadPlatformService=loanReadPlatformService;
    this.glAccountReadPlatformService=glAccountReadPlatformService;
    this.savingsAccountReadPlatformService=savingsAccountReadPlatformService;
    this.codeValueReadPlatformService=codeValueReadPlatformService;
    this.savingsProductReadPlatformService=savingsProductReadPlatformService;
    this.productReadPlatformService=productReadPlatformService;
    this.chargeReadPlatformService=chargeReadPlatformService;
    this.depositProductReadPlatformService=depositProductReadPlatformService;
    this.roleReadPlatformService=roleReadPlatformService;
  }

	@Override
	public Response getTemplate(String entityType, Long officeId, Long staffId,final String dateFormat) {
		WorkbookPopulator populator=null;
		final Workbook workbook=new HSSFWorkbook();
		if(entityType!=null){
			if (entityType.trim().equalsIgnoreCase(GlobalEntityType.CLIENTS_PERSON.toString())||
					entityType.trim().equalsIgnoreCase(GlobalEntityType.CLIENTS_ENTTTY.toString())) {
				populator = populateClientWorkbook(entityType,officeId, staffId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.CENTERS.toString())) {
				populator=populateCenterWorkbook(officeId,staffId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.GROUPS.toString())) {
				populator = populateGroupsWorkbook(officeId, staffId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.LOANS.toString())) {
				populator = populateLoanWorkbook(officeId, staffId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.LOAN_TRANSACTIONS.toString())) {
				populator = populateLoanRepaymentWorkbook(officeId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.GL_JOURNAL_ENTRIES.toString())) {
				populator = populateJournalEntriesWorkbook(officeId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.GUARANTORS.toString())) {
				populator = populateGuarantorWorkbook(officeId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.OFFICES.toString())) {
				populator=populateOfficeWorkbook();
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.CHART_OF_ACCOUNTS.toString())) {
				populator=populateChartOfAccountsWorkbook();
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.STAFF.toString())) {
				populator=populateStaffWorkbook(officeId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.SHARE_ACCOUNTS.toString())) {
				populator=populateSharedAcountsWorkbook(officeId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.SAVINGS_ACCOUNT.toString())) {
				populator=populateSavingsAccountWorkbook(officeId,staffId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.SAVINGS_TRANSACTIONS.toString())) {
				populator=populateSavingsTransactionWorkbook(officeId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.RECURRING_DEPOSIT_ACCOUNTS.toString())) {
				populator=populateRecurringDepositWorkbook(officeId,staffId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.RECURRING_DEPOSIT_ACCOUNTS_TRANSACTIONS.toString())) {
				populator=populateRecurringDepositTransactionWorkbook(officeId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.FIXED_DEPOSIT_ACCOUNTS.toString())) {
				populator = populateFixedDepositWorkbook(officeId, staffId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.FIXED_DEPOSIT_TRANSACTIONS.toString())){
				populator=populateFixedDepositTransactionsWorkbook(officeId);
			}else if (entityType.trim().equalsIgnoreCase(GlobalEntityType.USERS.toString())){
				populator=populateUserWorkbook(officeId,staffId);
			}else {
				throw new GeneralPlatformDomainRuleException("error.msg.unable.to.find.resource",
						"Unable to find requested resource");
			}
			populator.populate(workbook,dateFormat);
			return buildResponse(workbook, entityType);
		}else {
			throw new GeneralPlatformDomainRuleException("error.msg.given.entity.type.null",
					"Given Entity type is null");
		}
	}


	private WorkbookPopulator populateClientWorkbook(final String entityType ,final Long officeId, final Long staffId) {
    this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
    this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.STAFF_ENTITY_TYPE);
    List<OfficeData> offices = fetchOffices(officeId);
    List<StaffData> staff = fetchStaff(staffId);
    List<CodeValueData> clientTypeCodeValues =fetchCodeValuesByCodeName("ClientType");
	List<CodeValueData> clientClassification=fetchCodeValuesByCodeName("ClientClassification");
	List<CodeValueData> addressTypesCodeValues=fetchCodeValuesByCodeName("ADDRESS_TYPE");
	List<CodeValueData> stateProvinceCodeValues=fetchCodeValuesByCodeName("STATE");
	List<CodeValueData> countryCodeValues=fetchCodeValuesByCodeName("COUNTRY");
	if(entityType.trim().equalsIgnoreCase(GlobalEntityType.CLIENTS_PERSON.toString())) {
		List<CodeValueData> genderCodeValues = fetchCodeValuesByCodeName("Gender");
		return new ClientPersonWorkbookPopulator(new OfficeSheetPopulator(offices),
				new PersonnelSheetPopulator(staff, offices), clientTypeCodeValues, genderCodeValues, clientClassification
				, addressTypesCodeValues, stateProvinceCodeValues, countryCodeValues);
	}else if(entityType.trim().equalsIgnoreCase(GlobalEntityType.CLIENTS_ENTTTY.toString())){
		List<CodeValueData> constitutionCodeValues=fetchCodeValuesByCodeName("Constitution");
		List<CodeValueData> mainBusinessline=fetchCodeValuesByCodeName("Main Business Line");
		return new ClientEntityWorkbookPopulator(new OfficeSheetPopulator(offices),
				new PersonnelSheetPopulator(staff, offices), clientTypeCodeValues, constitutionCodeValues,mainBusinessline,
				clientClassification, addressTypesCodeValues, stateProvinceCodeValues, countryCodeValues);
	}
	  return null;
  }

  private Response buildResponse(final Workbook workbook, final String entity) {
    String filename = entity + DateUtils.getLocalDateOfTenant().toString() + ".xls";
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      workbook.write(baos);
    } catch (IOException e) {
      e.printStackTrace();
    }

    final ResponseBuilder response = Response.ok(baos.toByteArray());
    response.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    response.header("Content-Type", "application/vnd.ms-excel");
    return response.build();
  }

  @SuppressWarnings("unchecked")
  private List<OfficeData> fetchOffices(final Long officeId) {
    List<OfficeData> offices = null;
    if (officeId == null) {
      Boolean includeAllOffices = Boolean.TRUE;
      offices = (List) this.officeReadPlatformService.retrieveAllOffices(includeAllOffices, null);
    } else {
      offices = new ArrayList<>();
      offices.add(this.officeReadPlatformService.retrieveOffice(officeId));
    }
    return offices;
  }

  @SuppressWarnings("unchecked")
  private List<StaffData> fetchStaff(final Long staffId) {
    List<StaffData> staff = null;
    if (staffId == null){
      staff =
          (List) this.staffReadPlatformService.retrieveAllStaff(null, null, Boolean.FALSE, null);
    }else {
      staff = new ArrayList<>();
      staff.add(this.staffReadPlatformService.retrieveStaff(staffId));
    }
    return staff;
  }
  private List<CodeValueData> fetchCodeValuesByCodeName(String codeName){
  	List<CodeValueData> codeValues=null;
  	if (codeName!=null){
  		codeValues=(List)codeValueReadPlatformService.retrieveCodeValuesByCode(codeName);
	}else {
	 	throw new NullPointerException();
	}
	return codeValues;
  }
  private List<SavingsProductData>fetchSavingsProducts(){
  	List<SavingsProductData> savingsProducts=(List)savingsProductReadPlatformService.retrieveAll();
	return savingsProducts;
  }

private WorkbookPopulator populateCenterWorkbook(Long officeId,Long staffId){
	 this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
	 this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.STAFF_ENTITY_TYPE);
	this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.GROUP_ENTITY_TYPE);
	 List<OfficeData> offices = fetchOffices(officeId);
	List<StaffData> staff = fetchStaff(staffId);
	List<GroupGeneralData> groups = fetchGroups(officeId);
	return new CentersWorkbookPopulator(new OfficeSheetPopulator(offices),
	        new PersonnelSheetPopulator(staff, offices),new GroupSheetPopulator(groups,offices));
}

	private WorkbookPopulator populateGroupsWorkbook(Long officeId, Long staffId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.STAFF_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CENTER_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CLIENT_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(officeId);
		List<StaffData> staff = fetchStaff(staffId);
		List<CenterData> centers = fetchCenters(officeId);
		List<ClientData> clients = fetchClients(officeId);
		return new GroupsWorkbookPopulator(new OfficeSheetPopulator(offices),
				new PersonnelSheetPopulator(staff, offices), new CenterSheetPopulator(centers, offices),
				new ClientSheetPopulator(clients, offices));
	}
	private List<CenterData> fetchCenters(Long officeId) {
		List<CenterData>centers=null;
		if (officeId==null) {
			centers=(List<CenterData>) this.centerReadPlatformService.retrieveAll(null, null);
		} else {
			SearchParameters searchParameters = SearchParameters.from(null, officeId, null, null, null);
			centers = (List<CenterData>)centerReadPlatformService.retrieveAll(searchParameters,null);
		}
		
		return centers;
	}
	private List<ClientData> fetchClients(Long officeId) {
		List<ClientData> clients=null;
		if (officeId==null) {
			Page<ClientData> clientDataPage =this.clientReadPlatformService.retrieveAll(null);
			if (clientDataPage!=null){
				clients=new ArrayList<>();
				for (ClientData client: clientDataPage.getPageItems()) {
					clients.add(client);
				}
			}
		} else {
			SearchParameters searchParameters = SearchParameters.from(null, officeId, null, null, null);
			Page<ClientData> clientDataPage =this.clientReadPlatformService.retrieveAll(searchParameters);
			if (clientDataPage!=null){
				clients=new ArrayList<>();
				for (ClientData client: clientDataPage.getPageItems()) {
					clients.add(client);
				}
			}
		}
		return clients;
	}


	private WorkbookPopulator populateLoanWorkbook(Long officeId, Long staffId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.STAFF_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.GROUP_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CLIENT_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.LOAN_PRODUCT_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.FUNDS_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.PAYMENT_TYPE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CURRENCY_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(officeId);
		List<StaffData> staff = fetchStaff(staffId);
		List<ClientData> clients = fetchClients(officeId);
		List<GroupGeneralData> groups = fetchGroups(officeId);
		List<LoanProductData> loanproducts = fetchLoanProducts();
		List<FundData> funds = fetchFunds();
		List<PaymentTypeData> paymentTypes = fetchPaymentTypes();
		List<CurrencyData> currencies = fetchCurrencies();
		return new LoanWorkbookPopulator(new OfficeSheetPopulator(offices), new ClientSheetPopulator(clients, offices),
				new GroupSheetPopulator(groups, offices), new PersonnelSheetPopulator(staff, offices),
				new LoanProductSheetPopulator(loanproducts), new ExtrasSheetPopulator(funds, paymentTypes, currencies));
	}

	private List<CurrencyData> fetchCurrencies() {
		List<CurrencyData> currencies =(List<CurrencyData>) this.currencyReadPlatformService.
				retrieveAllPlatformCurrencies();
		return currencies;
	}

	private List<PaymentTypeData> fetchPaymentTypes() {
		List<PaymentTypeData> paymentTypeData =(List<PaymentTypeData>) this.paymentTypeReadPlatformService
				.retrieveAllPaymentTypes();
		return paymentTypeData;
	}

	private List<FundData> fetchFunds() {
		List<FundData> funds =(List<FundData>) this.fundReadPlatformService.retrieveAllFunds();
		return funds;
	}

	private List<LoanProductData> fetchLoanProducts() {
		List<LoanProductData>loanproducts =(List<LoanProductData>) this.loanProductReadPlatformService
				.retrieveAllLoanProducts();
		return loanproducts;
	}

	private List<GroupGeneralData> fetchGroups(Long officeId) {
		List<GroupGeneralData> groups = null;
		if (officeId == null) {
			groups = (List<GroupGeneralData>) this.groupReadPlatformService.retrieveAll(null, null);
		} else {
			SearchParameters searchParameters = SearchParameters.from(null, officeId, null, null, null);
			groups = (List<GroupGeneralData>)groupReadPlatformService.retrieveAll(searchParameters,null);
		}

		return groups;
	}

	private WorkbookPopulator populateLoanRepaymentWorkbook(Long officeId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CLIENT_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.FUNDS_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.PAYMENT_TYPE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CURRENCY_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(officeId);
		List<ClientData> clients = fetchClients(officeId);
		List<FundData> funds = fetchFunds();
		List<PaymentTypeData> paymentTypes = fetchPaymentTypes();
		List<CurrencyData> currencies = fetchCurrencies();
		List<LoanAccountData> loans = fetchLoanAccounts(officeId);
		return new LoanRepaymentWorkbookPopulator(loans, new OfficeSheetPopulator(offices),
				new ClientSheetPopulator(clients, offices), new ExtrasSheetPopulator(funds, paymentTypes, currencies));
	}

	private List<LoanAccountData> fetchLoanAccounts(final Long officeId) {
		List<LoanAccountData> loanAccounts = null;
		if(officeId==null){
			loanAccounts= loanReadPlatformService.retrieveAll(null).getPageItems();
		}else {
			SearchParameters searchParameters = SearchParameters.from(null, officeId, null, null, null);
			loanAccounts = loanReadPlatformService.retrieveAll(searchParameters).getPageItems();
		}
		return loanAccounts;
	}

	private WorkbookPopulator populateJournalEntriesWorkbook(Long officeId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.GL_ACCOUNT_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.FUNDS_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.PAYMENT_TYPE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CURRENCY_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(officeId);
		List<GLAccountData> glAccounts = fetchGLAccounts();
		List<FundData> funds = fetchFunds();
		List<PaymentTypeData> paymentTypes = fetchPaymentTypes();
		List<CurrencyData> currencies = fetchCurrencies();
		return new JournalEntriesWorkbookPopulator(new OfficeSheetPopulator(offices),
				new GlAccountSheetPopulator(glAccounts), new ExtrasSheetPopulator(funds, paymentTypes, currencies));
	}

	private List<GLAccountData> fetchGLAccounts() {
		List<GLAccountData> glaccounts = (List<GLAccountData>) this.glAccountReadPlatformService.
				retrieveAllGLAccounts(null, null, null,
					null, null, null);
		return glaccounts;
	}

	private WorkbookPopulator populateGuarantorWorkbook(Long officeId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.STAFF_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(officeId);
		List<ClientData>clients=fetchClients(officeId);
		List<LoanAccountData> loans = fetchLoanAccounts(officeId);
		List<SavingsAccountData> savingsaccounts = fetchSavingsAccounts(officeId);
		List<CodeValueData> guarantorRelationshipTypes=fetchCodeValuesByCodeName("GuarantorRelationship");
		return new GuarantorWorkbookPopulator(new OfficeSheetPopulator(offices),
				new ClientSheetPopulator(clients, offices),loans,savingsaccounts,guarantorRelationshipTypes);
	}

	private List<SavingsAccountData> fetchSavingsAccounts(Long officeId) {
		List<SavingsAccountData> savingsAccounts=null;
		if (officeId!=null) {
			String activeAccounts="sa.status_enum = 300";
			SearchParameters searchParameters = SearchParameters.from(activeAccounts, officeId, null, null, null);
			savingsAccounts = savingsAccountReadPlatformService.retrieveAll(searchParameters).getPageItems();;
		}else {
			savingsAccounts= savingsAccountReadPlatformService.retrieveAll(null).getPageItems();
		}
		return savingsAccounts;
	}


	private WorkbookPopulator populateOfficeWorkbook() {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(null);
		return new OfficeWorkbookPopulator(offices);
	}




	private WorkbookPopulator populateChartOfAccountsWorkbook() {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.GL_ACCOUNT_ENTITY_TYPE);
		List<GLAccountData> glAccounts = fetchGLAccounts();
		return new ChartOfAccountsWorkbook(glAccounts);
	}


	private WorkbookPopulator populateStaffWorkbook(Long officeId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		List<OfficeData> offices=fetchOffices(officeId);
		return new StaffWorkbookPopulator(new OfficeSheetPopulator(offices));
	}



	private WorkbookPopulator populateSharedAcountsWorkbook(Long officeId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CLIENT_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.SHARED_ACCOUNT_ENTITY_TYPE);
		List<ShareProductData> shareProductDataList=fetchSharedProducts();
		List<ChargeData> chargesForShares=fetchChargesForShares();
		List<ClientData> clientDataList=fetchClients(officeId);
		List<OfficeData>officeDataList=fetchOffices(officeId);
		List<SavingsAccountData> savingsAccounts = fetchSavingsAccounts(officeId);
		return new SharedAccountWorkBookPopulator(new SharedProductsSheetPopulator(shareProductDataList,chargesForShares),
				new ClientSheetPopulator(clientDataList,officeDataList),new SavingsAccountSheetPopulator(savingsAccounts));
	}

	private List<ChargeData> fetchChargesForShares() {
		List<ChargeData>chargesForShares=(List<ChargeData>) chargeReadPlatformService.retrieveSharesApplicableCharges();
		return chargesForShares;
	}

	private List<ShareProductData> fetchSharedProducts() {
		List<ProductData> productDataList = productReadPlatformService.retrieveAllProducts(0,50).getPageItems() ;
		List<ShareProductData> sharedProductDataList=new ArrayList<>();
		if(productDataList!=null) {
			for (ProductData data : productDataList) {
				ShareProductData shareProduct = (ShareProductData) data;
				sharedProductDataList.add(shareProduct);
			}
		}
		return sharedProductDataList;
	}


	private WorkbookPopulator populateSavingsAccountWorkbook(Long officeId, Long staffId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.STAFF_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.GROUP_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CLIENT_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.SAVINGS_PRODUCT_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(officeId);
		List<StaffData> staff = fetchStaff(staffId);
		List<ClientData> clients = fetchClients(officeId);
		List<GroupGeneralData> groups = fetchGroups(officeId);
		List<SavingsProductData> savingsProducts=fetchSavingsProducts();
		return new SavingsWorkbookPopulator(new OfficeSheetPopulator(offices), new ClientSheetPopulator(clients, offices),
				new GroupSheetPopulator(groups, offices), new PersonnelSheetPopulator(staff, offices),
				new SavingsProductSheetPopulator(savingsProducts));
	}



	private WorkbookPopulator populateSavingsTransactionWorkbook(Long officeId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CLIENT_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.FUNDS_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.PAYMENT_TYPE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CURRENCY_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(officeId);
		List<ClientData> clients = fetchClients(officeId);
		List<FundData> funds = fetchFunds();
		List<PaymentTypeData> paymentTypes = fetchPaymentTypes();
		List<CurrencyData> currencies = fetchCurrencies();
		List<SavingsAccountData> savingsAccounts=fetchSavingsAccounts(officeId);
		return new SavingsTransactionsWorkbookPopulator(new OfficeSheetPopulator(offices), new ClientSheetPopulator(clients, offices),
				 new ExtrasSheetPopulator(funds, paymentTypes, currencies),savingsAccounts);
	}


	private WorkbookPopulator populateRecurringDepositWorkbook(Long officeId,Long staffId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CLIENT_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.STAFF_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.RECURRING_DEPOSIT_PRODUCT_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(officeId);
		List<ClientData> clients = fetchClients(officeId);
		List<StaffData> staff = fetchStaff(staffId);
		List<RecurringDepositProductData> recurringDepositProducts = fetchRecurringDepositProducts();
		return new RecurringDepositWorkbookPopulator(new OfficeSheetPopulator(offices), new ClientSheetPopulator(clients, offices),
				new PersonnelSheetPopulator(staff,offices),new RecurringDepositProductSheetPopulator(recurringDepositProducts));
	}

	private List<RecurringDepositProductData> fetchRecurringDepositProducts() {
		List<DepositProductData> depositProducts=(List<DepositProductData>)depositProductReadPlatformService
				.retrieveAll(DepositAccountType.RECURRING_DEPOSIT);
		List<RecurringDepositProductData> recurringDepositProducts=new ArrayList<>();
		for (DepositProductData depositproduct: depositProducts) {
			RecurringDepositProductData recurringDepositProduct= (RecurringDepositProductData) depositproduct;
			recurringDepositProducts.add(recurringDepositProduct);
		}
		return recurringDepositProducts;
	}



	private WorkbookPopulator populateRecurringDepositTransactionWorkbook(Long officeId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CLIENT_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.FUNDS_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.PAYMENT_TYPE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CURRENCY_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(officeId);
		List<ClientData> clients = fetchClients(officeId);
		List<FundData> funds = fetchFunds();
		List<PaymentTypeData> paymentTypes = fetchPaymentTypes();
		List<CurrencyData> currencies = fetchCurrencies();
		List<SavingsAccountData> savingsAccounts=fetchSavingsAccounts(officeId);
		return new RecurringDepositTransactionWorkbookPopulator(new OfficeSheetPopulator(offices), new ClientSheetPopulator(clients, offices),
				new ExtrasSheetPopulator(funds, paymentTypes, currencies),savingsAccounts);
	}

	private WorkbookPopulator populateFixedDepositWorkbook(Long officeId, Long staffId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CLIENT_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.STAFF_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.FIXED_DEPOSIT_PRODUCT_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(officeId);
		List<ClientData> clients = fetchClients(officeId);
		List<StaffData> staff = fetchStaff(staffId);
		List<FixedDepositProductData> fixedDepositProducts = fetchFixedDepositProducts();
		return new FixedDepositWorkbookPopulator(new OfficeSheetPopulator(offices), new ClientSheetPopulator(clients, offices),
				new PersonnelSheetPopulator(staff,offices),new FixedDepositProductSheetPopulator(fixedDepositProducts));


	}

	private List<FixedDepositProductData> fetchFixedDepositProducts() {
		List<DepositProductData> depositProducts=(List<DepositProductData>)depositProductReadPlatformService
				.retrieveAll(DepositAccountType.FIXED_DEPOSIT);
		List<FixedDepositProductData> fixedDepositProducts=new ArrayList<>();
		for (DepositProductData depositproduct: depositProducts) {
			FixedDepositProductData fixedDepositProduct= (FixedDepositProductData) depositproduct;
			fixedDepositProducts.add(fixedDepositProduct);
		}
		return fixedDepositProducts;

	}

	private WorkbookPopulator populateUserWorkbook(Long officeId, Long staffId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.USER_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(officeId);
		List<StaffData> staff = fetchStaff(staffId);
		List<RoleData> roles=fetchRoles();
		return new UserWorkbookPopulator(new OfficeSheetPopulator(offices), new PersonnelSheetPopulator(staff,offices),
				new RoleSheetPopulator(roles));
	}

	private List<RoleData> fetchRoles() {
		List<RoleData> rolesList= (List<RoleData>) roleReadPlatformService.retrieveAllActiveRoles();
		return rolesList;
	}

	private WorkbookPopulator populateFixedDepositTransactionsWorkbook(Long officeId) {
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.OFFICE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CLIENT_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.FUNDS_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.PAYMENT_TYPE_ENTITY_TYPE);
		this.context.authenticatedUser().validateHasReadPermission(TemplatePopulateImportConstants.CURRENCY_ENTITY_TYPE);
		List<OfficeData> offices = fetchOffices(officeId);
		List<ClientData> clients = fetchClients(officeId);
		List<FundData> funds = fetchFunds();
		List<PaymentTypeData> paymentTypes = fetchPaymentTypes();
		List<CurrencyData> currencies = fetchCurrencies();
		List<SavingsAccountData> savingsAccounts=fetchSavingsAccounts(officeId);
		return new FixedDepositTransactionWorkbookPopulator(new OfficeSheetPopulator(offices), new ClientSheetPopulator(clients, offices),
				new ExtrasSheetPopulator(funds, paymentTypes, currencies),savingsAccounts);
	}

}