package org.mifosplatform.portfolio.savingsaccount.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountForLookup;
import org.mifosplatform.portfolio.savingsaccount.data.SavingPermissionData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingSchedulePeriodData;
import org.mifosplatform.portfolio.savingsaccountproduct.data.SavingProductData;
import org.mifosplatform.portfolio.savingsaccountproduct.data.SavingProductLookup;
import org.mifosplatform.portfolio.savingsaccountproduct.service.SavingProductEnumerations;
import org.mifosplatform.portfolio.savingsaccountproduct.service.SavingProductReadPlatformService;
import org.mifosplatform.portfolio.savingsdepositaccount.service.DepositAccountEnumerations;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SavingAccountReadPlatformServiceImpl implements SavingAccountReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final ClientReadPlatformService clientReadPlatformService;
    private final SavingProductReadPlatformService savingProductReadPlatformService; 

    @Autowired
    public SavingAccountReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource, 
    		ClientReadPlatformService clientReadPlatformService, SavingProductReadPlatformService savingProductReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientReadPlatformService = clientReadPlatformService;
        this.savingProductReadPlatformService=savingProductReadPlatformService;
    }

    @Override
    public Collection<SavingAccountData> retrieveAllSavingsAccounts() {

        this.context.authenticatedUser();
        SavingAccountMapper mapper = new SavingAccountMapper();
        String sql = "select " + mapper.schema() + " where sa.is_deleted=0";
        return this.jdbcTemplate.query(sql, mapper, new Object[] {});

    }

    @Override
    public SavingAccountData retrieveSavingsAccount(Long accountId) {

        this.context.authenticatedUser();
        SavingAccountMapper mapper = new SavingAccountMapper();
        String sql = "select " + mapper.schema() + " where sa.id = ? and sa.is_deleted=0";
        SavingAccountData savingAccountData = this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { accountId });
        
        return savingAccountData;
    }

    private static final class SavingAccountMapper implements RowMapper<SavingAccountData> {

        public String schema() {
            return "sa.id AS id, sa.status_enum AS status, sa.external_id AS externalId,"
                    + " sa.client_id AS clientId, sa.product_id AS productId, sa.deposit_amount_per_period AS savingsDepostiAmountPerPeriod,"
                    + " sa.savings_product_type AS savingsProductType, sa.currency_code AS currencyCode, sa.currency_digits AS currencyDigits,"
                    + " sa.total_deposit_amount AS totalDepositAmount, sa.reccuring_nominal_interest_rate AS reccuringInterestRate,"
                    + " sa.regular_saving_nominal_interest_rate AS savingInterestRate, sa.tenure AS tenure, sa.tenure_type AS tenureType,"
                    + " sa.frequency AS savingsFrequencyType, sa.interest_type AS interestType, sa.interest_calculation_method AS interestCalculationMethod,"
                    + " sa.projected_commencement_date AS projectedCommencementDate, sa.actual_commencement_date AS actualCommencementDate,"
                    + " sa.matures_on_date AS maturesOnDate, sa.projected_interest_accrued_on_maturity AS projectedInterestAccuredOnMaturity,"
                    + " sa.actual_interest_accrued AS actualInterestAccured, sa.projected_total_maturity_amount AS projectedTotalMaturityAmount,"
                    + " sa.actual_total_amount AS actualTotalAmount, sa.is_preclosure_allowed AS isPreclosureAllowed, sa.outstanding_amount as outstandingAmount, "
                    + " sa.pre_closure_interest_rate AS preClosureInterestRate, sa.is_lock_in_period_allowed AS isLockinPeriodAllowed,"
                    + " sa.lock_in_period AS lockinPeriod, sa.lock_in_period_type AS lockinPeriodType, sa.withdrawnon_date AS withdrawnonDate,"
                    + " sa.rejectedon_date AS rejectedonDate, sa.closedon_date AS closedonDate, ps.name AS productName, sa.deposit_every as depositEvery, "
                    + " c.firstname AS firstname, c.lastname AS lastname, curr.name AS currencyName, "
                    + " curr.internationalized_name_code AS currencyNameCode, curr.display_symbol AS currencyDisplaySymbol "
                    + " FROM m_saving_account sa " + " JOIN m_client c ON c.id = sa.client_id"
                    + " JOIN m_currency curr ON curr.code = sa.currency_code " + " JOIN m_product_savings ps ON sa.product_id = ps.id ";
        }

        @Override
        public SavingAccountData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            String externalId = rs.getString("externalId");
            Long clientId = rs.getLong("clientId");
            String clientName = rs.getString("firstname") + " " + rs.getString("lastname");

            Long productId = rs.getLong("productId");
            String productName = rs.getString("productName");
            EnumOptionData productType = SavingProductEnumerations.savingProductType(JdbcSupport.getInteger(rs, "savingsProductType"));

            Integer statusId = JdbcSupport.getInteger(rs, "status");
            EnumOptionData status = DepositAccountEnumerations.status(statusId);

            String currencyCode = rs.getString("currencyCode");
            String currencyName = rs.getString("currencyName");
            String currencyNameCode = rs.getString("currencyNameCode");
            String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, currencyDisplaySymbol,
                    currencyNameCode);

            BigDecimal savingsDepostiAmountPerPeriod = rs.getBigDecimal("savingsDepostiAmountPerPeriod");
            EnumOptionData savingsFrequencyType = SavingProductEnumerations.interestFrequencyType(JdbcSupport.getInteger(rs,
                    "savingsFrequencyType"));
            BigDecimal totalDepositAmount = rs.getBigDecimal("totalDepositAmount");

            BigDecimal reccuringInterestRate = rs.getBigDecimal("reccuringInterestRate");
            BigDecimal savingInterestRate = rs.getBigDecimal("savingInterestRate");
            EnumOptionData interestType = SavingProductEnumerations.savingInterestType(JdbcSupport.getInteger(rs, "interestType"));
            EnumOptionData interestCalculationMethod = SavingProductEnumerations.savingInterestCalculationMethod(JdbcSupport.getInteger(rs,
                    "interestCalculationMethod"));

            Integer tenure = JdbcSupport.getInteger(rs, "tenure");
            EnumOptionData tenureType = SavingProductEnumerations.tenureTypeEnum(JdbcSupport.getInteger(rs, "tenureType"));

            LocalDate projectedCommencementDate = JdbcSupport.getLocalDate(rs, "projectedCommencementDate");
            LocalDate actualCommencementDate = JdbcSupport.getLocalDate(rs, "actualCommencementDate");
            LocalDate maturesOnDate = JdbcSupport.getLocalDate(rs, "maturesOnDate");
            BigDecimal projectedInterestAccuredOnMaturity = rs.getBigDecimal("projectedInterestAccuredOnMaturity");
            BigDecimal actualInterestAccured = rs.getBigDecimal("actualInterestAccured");

            BigDecimal projectedMaturityAmount = rs.getBigDecimal("projectedTotalMaturityAmount");
            BigDecimal actualMaturityAmount = rs.getBigDecimal("actualTotalAmount");

            boolean preClosureAllowed = rs.getBoolean("isPreclosureAllowed");
            BigDecimal preClosureInterestRate = rs.getBigDecimal("preClosureInterestRate");
            BigDecimal outstandingAmount = rs.getBigDecimal("outstandingAmount");

            LocalDate withdrawnonDate = JdbcSupport.getLocalDate(rs, "withdrawnonDate");
            LocalDate rejectedonDate = JdbcSupport.getLocalDate(rs, "rejectedonDate");
            LocalDate closedonDate = JdbcSupport.getLocalDate(rs, "closedonDate");

            boolean isLockinPeriodAllowed = rs.getBoolean("isLockinPeriodAllowed");
            Integer lockinPeriod = JdbcSupport.getInteger(rs, "lockinPeriod");
            Integer lockinPeriodTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodType");
            EnumOptionData lockinPeriodType = SavingProductEnumerations.savingsLockinPeriod(lockinPeriodTypeValue);
            Integer depositEvery = JdbcSupport.getInteger(rs, "depositEvery");

            return new SavingAccountData(id, status, externalId, clientId, clientName, productId, productName, productType, currencyData,
                    savingsDepostiAmountPerPeriod, savingsFrequencyType, totalDepositAmount, reccuringInterestRate, savingInterestRate,
                    interestType, interestCalculationMethod, tenure, tenureType, projectedCommencementDate, actualCommencementDate,
                    maturesOnDate, projectedInterestAccuredOnMaturity, actualInterestAccured, projectedMaturityAmount,
                    actualMaturityAmount, preClosureAllowed, preClosureInterestRate, withdrawnonDate, rejectedonDate, closedonDate,
                    isLockinPeriodAllowed, lockinPeriod, lockinPeriodType,depositEvery,outstandingAmount);
        }
    }

	@Override
	public SavingAccountData retrieveNewSavingsAccountDetails(Long clientId, Long productId) {
		
		 context.authenticatedUser();
		 ClientData clientAccount = this.clientReadPlatformService.retrieveIndividualClient(clientId);
		 SavingAccountData accountData = null;
		 Collection<SavingProductLookup> savingProducts = this.savingProductReadPlatformService.retrieveAllSavingProductsForLookup();
		 
		 if (productId != null && productId != -1) {
	            SavingProductData selectedProduct = findSavingProductById(savingProducts, productId);

	            CurrencyData currency = selectedProduct.getCurrency();

	            accountData = new SavingAccountData(clientAccount.id(), clientAccount.displayName(), selectedProduct.getId(),
	                    selectedProduct.getName(), currency, selectedProduct.getInterestRate(), selectedProduct.getSavingsDepositAmount(),
	                    selectedProduct.getSavingProductType(), selectedProduct.getTenureType(), selectedProduct.getTenure(), selectedProduct.getSavingFrequencyType(),
	                    selectedProduct.getInterestType(), selectedProduct.getInterestCalculationMethod(),selectedProduct.getMinimumBalanceForWithdrawal(),
	                    selectedProduct.isPartialDepositAllowed(), selectedProduct.isLockinPeriodAllowed(),selectedProduct.getLockinPeriod(),
	                    selectedProduct.getLockinPeriodType(),selectedProduct.getDepositEvery());
	            
	        } else {
	            accountData = SavingAccountData.createFrom(clientAccount.id(), clientAccount.displayName());
	        }
		return accountData;
	}

	private SavingProductData findSavingProductById( Collection<SavingProductLookup> savingProducts, Long productId) {
		SavingProductData match = this.savingProductReadPlatformService.retrieveNewSavingProductDetails();
	        for (SavingProductLookup savingProductLookup : savingProducts) {
	            if (savingProductLookup.hasId(productId)) {
	                match = this.savingProductReadPlatformService.retrieveSavingProduct(savingProductLookup.getId());
	                break;
	            }
	        }
	        return match;
	    }

	@Override
	public SavingPermissionData retrieveSavingAccountPermissions(SavingAccountData savingAccountData) {
		boolean pendingApproval = (savingAccountData.getStatus().getId().equals(100L));
		boolean undoApprovalAllowed = (savingAccountData.getStatus().getId().equals(300L));
		boolean renewelAllowed = false;
		if (savingAccountData.getMaturesOnDate() != null && savingAccountData.getTenureType().equals(TenureTypeEnum.FIXED_PERIOD)) {
			if (new LocalDate().isAfter(savingAccountData.getMaturesOnDate())) {
				renewelAllowed = true;
			}
		}
		boolean rejectAllowed = pendingApproval;
		boolean withdrawnByApplicantAllowed = pendingApproval;
		boolean isMaturedDepositAccount = (savingAccountData.getStatus().getId().equals(700L));
		return new SavingPermissionData(rejectAllowed, withdrawnByApplicantAllowed, undoApprovalAllowed, pendingApproval, renewelAllowed, isMaturedDepositAccount);
	}
	
	@SuppressWarnings("unused")
	@Override
	public BigDecimal deriveSavingDueAmount(SavingAccountData account) {
		
		BigDecimal dueAmount = BigDecimal.ZERO;
		EnumOptionData status = account.getStatus();
		LocalDate date = account.getActualCommencementDate();
		
		if (account.getStatus() != null && account.getActualCommencementDate() != null) {
			BigDecimal amountPerPeriod = account.getSavingsDepostiAmountPerPeriod();
			Integer tenure = account.getTenure();
			Integer depositEvery = account.getDepositEvery();
			Integer noOfPeriods = tenure/depositEvery ;
			Integer noOfMonths = Months.monthsBetween(date, new LocalDate()).getMonths();
			Integer noOfPeriodsExistedTillDate = noOfMonths/depositEvery;
			BigDecimal totalAmountToBePaidTillDate = BigDecimal.valueOf(noOfPeriodsExistedTillDate.doubleValue()*amountPerPeriod.doubleValue());
			dueAmount = dueAmount.add(BigDecimal.valueOf(totalAmountToBePaidTillDate.doubleValue() - account.getTotalDepositAmount().doubleValue()));
			
		}
		
		return dueAmount;
	}
	
	@Override
	public SavingScheduleData retrieveSavingsAccountSchedule(Long accountId, CurrencyData currency) {
		this.context.authenticatedUser();
		SavingAccountScheduleMapper mapper = new SavingAccountScheduleMapper();
		String sql = "Select " + mapper.savingScheduleSchema() +" where ss.saving_account_id =? order by ss.installment";
		List<SavingSchedulePeriodData> periods = this.jdbcTemplate.query(sql, mapper, new Object[]{accountId}); 
		BigDecimal cumulativeDepositDue = BigDecimal.ZERO;
		for(SavingSchedulePeriodData data : periods){
			cumulativeDepositDue = cumulativeDepositDue.add(data.getDepositDue()).subtract(data.getDepositPaid());
		}
		SavingScheduleData scheduleData = new SavingScheduleData(currency, cumulativeDepositDue, periods);
		return scheduleData;
	}
	
	public static final class SavingAccountScheduleMapper implements RowMapper<SavingSchedulePeriodData> {
		
		public String savingScheduleSchema(){
			return "ss.id as id, ss.saving_account_id as savingAccountId, ss.duedate as dueDate, ss.installment as installment, "
				 + " ss.deposit as deposit, ss.payment_date as paymentDate, ss.deposit_paid as depositPaid, "
				 + " ss.completed_derived as completedDerived FROM m_saving_schedule ss";
		}

		@Override
		public SavingSchedulePeriodData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
			
			LocalDate dueDate = JdbcSupport.getLocalDate(rs, "dueDate");
			Integer installment = JdbcSupport.getInteger(rs, "installment");
			BigDecimal depositDue = rs.getBigDecimal("deposit");
			BigDecimal depositPaid = rs.getBigDecimal("depositPaid");
			
			return new SavingSchedulePeriodData(installment, dueDate, depositDue, depositPaid);
		}
		
	}

	@Override
	public Collection<SavingAccountForLookup> retrieveSavingAccountsForLookUp() {
		SavingAccountLookupMapper mapper = new SavingAccountLookupMapper();
		String sql = "select "+ mapper.schema();
		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class SavingAccountLookupMapper implements RowMapper<SavingAccountForLookup> {
		
		public String schema(){
			return " sa.id as id from m_saving_account sa where sa.is_deleted=0 and sa.status_enum=300";
		}

		@Override
		public SavingAccountForLookup mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
			Long id = rs.getLong("id");
			return new SavingAccountForLookup(id);
		}
		
	}

}