package org.mifosng.platform;

import static org.mifosng.platform.Specifications.loansThatMatch;
import static org.mifosng.platform.Specifications.officesThatMatch;
import static org.mifosng.platform.Specifications.productThatMatches;
import static org.mifosng.platform.Specifications.rolesThatMatch;
import static org.mifosng.platform.Specifications.usersThatMatch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.MoneyData;
import org.mifosng.data.ScheduledLoanInstallment;
import org.mifosng.data.command.AdjustLoanTransactionCommand;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.ChangePasswordCommand;
import org.mifosng.data.command.CreateLoanProductCommand;
import org.mifosng.data.command.EnrollClientCommand;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.data.command.NoteCommand;
import org.mifosng.data.command.OfficeCommand;
import org.mifosng.data.command.RoleCommand;
import org.mifosng.data.command.SignupCommand;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.mifosng.data.command.UndoLoanApprovalCommand;
import org.mifosng.data.command.UndoLoanDisbursalCommand;
import org.mifosng.data.command.UpdateLoanProductCommand;
import org.mifosng.data.command.UpdateOrganisationCurrencyCommand;
import org.mifosng.data.command.UpdateUsernamePasswordCommand;
import org.mifosng.data.command.UserCommand;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.client.domain.Note;
import org.mifosng.platform.client.domain.NoteRepository;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.currency.domain.ApplicationCurrencyRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.ApplicationDomainRuleException;
import org.mifosng.platform.exceptions.InvalidSignupException;
import org.mifosng.platform.exceptions.NewDataValidationException;
import org.mifosng.platform.exceptions.NoAuthorizationException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.mifosng.platform.infrastructure.BasicPasswordEncodablePlatformUser;
import org.mifosng.platform.infrastructure.PlatformPasswordEncoder;
import org.mifosng.platform.infrastructure.PlatformUser;
import org.mifosng.platform.infrastructure.UsernameAlreadyExistsException;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.DefaultLoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanBuilder;
import org.mifosng.platform.loan.domain.LoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.LoanProduct;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.LoanProductRepository;
import org.mifosng.platform.loan.domain.LoanRepaymentScheduleInstallment;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.loan.domain.LoanStatus;
import org.mifosng.platform.loan.domain.LoanStatusRepository;
import org.mifosng.platform.loan.domain.LoanTransaction;
import org.mifosng.platform.loan.domain.LoanTransactionRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.OfficeRepository;
import org.mifosng.platform.organisation.domain.Organisation;
import org.mifosng.platform.organisation.domain.OrganisationCurrency;
import org.mifosng.platform.organisation.domain.OrganisationRepository;
import org.mifosng.platform.organisation.service.OfficeCommandValidator;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.mifosng.platform.user.domain.Permission;
import org.mifosng.platform.user.domain.PermissionRepository;
import org.mifosng.platform.user.domain.PlatformUserRepository;
import org.mifosng.platform.user.domain.Role;
import org.mifosng.platform.user.domain.RoleRepository;
import org.mifosng.platform.user.domain.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WritePlatformServiceJpaRepositoryImpl implements
		WritePlatformService {

	private final static Logger logger = LoggerFactory
			.getLogger(WritePlatformServiceJpaRepositoryImpl.class);
	
	private final OrganisationRepository organisationRepository;
	private final OfficeRepository officeRepository;
	private final UserDomainService userDomainService;
	private final PlatformUserRepository platformUserRepository;
	private final PlatformPasswordEncoder platformPasswordEncoder;
	private final ClientRepository clientRepository;
	private final ApplicationCurrencyRepository applicationCurrencyRepository;
	private final LoanProductRepository loanProductRepository;
	private final LoanRepository loanRepository;
	private final LoanStatusRepository loanStatusRepository;
	private final CalculationPlatformService calculationPlatformService;
	private final RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;
	private final AppUserRepository appUserRepository;
	private final NoteRepository noteRepository;
	private final LoanTransactionRepository loanTransactionRepository;

	@Autowired
	public WritePlatformServiceJpaRepositoryImpl(
			final OrganisationRepository organisationRepository,
			final OfficeRepository officeRepository,
			final UserDomainService userDomainService,
			final PlatformUserRepository platformUserRepository,
			final PlatformPasswordEncoder platformPasswordEncoder,
			final ClientRepository clientRepository,
			final NoteRepository noteRepository,
			final ApplicationCurrencyRepository applicationCurrencyRepository,
			final LoanProductRepository loanProductRepository,
			final LoanRepository loanRepository,
			final LoanTransactionRepository loanTransactionRepository,
			final LoanStatusRepository loanStatusRepository,
			final CalculationPlatformService calculationPlatformService,
			final AppUserRepository appUserRepository,
			final RoleRepository roleRepository,
			final PermissionRepository permissionRepository) {
		this.organisationRepository = organisationRepository;
		this.officeRepository = officeRepository;
		this.userDomainService = userDomainService;
		this.platformUserRepository = platformUserRepository;
		this.platformPasswordEncoder = platformPasswordEncoder;
		this.clientRepository = clientRepository;
		this.noteRepository = noteRepository;
		this.applicationCurrencyRepository = applicationCurrencyRepository;
		this.loanProductRepository = loanProductRepository;
		this.loanRepository = loanRepository;
		this.loanTransactionRepository = loanTransactionRepository;
		this.loanStatusRepository = loanStatusRepository;
		this.calculationPlatformService = calculationPlatformService;
		this.appUserRepository = appUserRepository;
		this.roleRepository = roleRepository;
		this.permissionRepository = permissionRepository;
	}

	@Transactional
	@Override
	public Long createUser(final UserCommand command) {
		
		try {
			AppUser currentUser = extractAuthenticatedUser();
			
			UserValidator validator = new UserValidator(command);
			validator.validate();
			
			Set<Role> allRoles = new HashSet<Role>();
			for (String roleId : command.getRoleIds()) {
				Role role = this.roleRepository.findOne(Long.valueOf(roleId));
				allRoles.add(role);
			}

			Office office = this.officeRepository.findOne(command.getOfficeId());

	        AppUser appUser = AppUser.createNew(currentUser.getOrganisation(), office, 
	        		allRoles, command.getUsername(), command.getEmail(), 
	        		command.getFirstname(), command.getLastname(), 
	        		command.getPassword());
			
			this.userDomainService.create(appUser);
			return appUser.getId();
		} catch (DataIntegrityViolationException e) {
			throw new UsernameAlreadyExistsException(e);
		}
	}
	
	@Transactional
	@Override
	public Long updateUser(UserCommand command) {
		try {
			AppUser currentUser = extractAuthenticatedUser();
			
			UserValidator validator = new UserValidator(command);
			validator.validate();
			
			List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
			if (command.getId() == null) {
				ErrorResponse error = new ErrorResponse("validation.msg.user.id.cannot.be.blank", "id");
				dataValidationErrors.add(error);
			}
			
			if (!dataValidationErrors.isEmpty()) {
				throw new NewDataValidationException(dataValidationErrors, "Data validation error exist.");
			}
			
			Set<Role> allRoles = new HashSet<Role>();
			for (String roleId : command.getRoleIds()) {
				Role role = this.roleRepository.findOne(Long.valueOf(roleId));
				allRoles.add(role);
			}

			Office office = this.officeRepository.findOne(command.getOfficeId());

			AppUser userToUpdate = this.appUserRepository.findOne(usersThatMatch(currentUser.getOrganisation(), command.getId()));
			userToUpdate.update(allRoles, office, command.getUsername(), command.getFirstname(), command.getLastname(), command.getEmail());
			
			return userToUpdate.getId();
		} catch (DataIntegrityViolationException e) {
			throw new UsernameAlreadyExistsException(e);
		}
	}
	
	@Transactional
	@Override
	public Long updateCurrentUser(UserCommand command) {
		AppUser currentUser = extractAuthenticatedUser();
		
		UserValidator validator = new UserValidator(command);
		validator.validateAccountSettingDetails();
		
		AppUser userToUpdate = this.appUserRepository.findOne(currentUser.getId());
		
		userToUpdate.update(command.getUsername(), command.getFirstname(), command.getLastname(), command.getEmail());
		
		this.appUserRepository.save(userToUpdate);
		
		return userToUpdate.getId();
	}
	
	@Transactional
	@Override
	public Long updateCurrentUserPassword(ChangePasswordCommand command) {
		AppUser currentUser = extractAuthenticatedUser();
		
		ChangePasswordCommandValidator validator = new ChangePasswordCommandValidator(command);
		validator.validate();
		
		AppUser userToUpdate = this.appUserRepository.findOne(currentUser.getId());
		
		PlatformUser dummyPlatformUser = new BasicPasswordEncodablePlatformUser(
				((AppUser) userToUpdate).getId(),
				userToUpdate.getUsername(), command.getPassword());

		String newPasswordEncoded = this.platformPasswordEncoder
				.encode(dummyPlatformUser);
		
		userToUpdate.updatePasswordOnFirstTimeLogin(newPasswordEncoded);
		
		return userToUpdate.getId();
	}
	
	@Transactional
	@Override
	public void deleteUser(Long userId) {
		this.appUserRepository.delete(userId);
	}
	
	@Transactional
	@Override
	public Long createRole(final RoleCommand command) {
		
		AppUser currentUser = extractAuthenticatedUser();
		
		RoleValidator validator = new RoleValidator(command);
		validator.validateForCreate();

		List<Long> selectedPermissionIds = new ArrayList<Long>();
		for (String selectedId : command.getPermissionIds()) {
			selectedPermissionIds.add(Long.valueOf(selectedId));
		}

		List<Permission> selectedPermissions = new ArrayList<Permission>();
		Collection<Permission> allPermissions = this.permissionRepository
				.findAll();
		for (Permission permission : allPermissions) {
			if (selectedPermissionIds.contains(permission.getId())) {
				selectedPermissions.add(permission);
			}
		}

		Role entity = new Role(currentUser.getOrganisation(), command.getName(), command.getDescription(), selectedPermissions);
				
		this.roleRepository.save(entity);

		return entity.getId();
	}
	
	@Transactional
	@Override
	public Long updateRole(RoleCommand command) {
		
		AppUser currentUser = extractAuthenticatedUser();
		
		RoleValidator validator = new RoleValidator(command);
		validator.validateForUpdate();

		List<Long> selectedPermissionIds = new ArrayList<Long>();
		for (String selectedId : command.getPermissionIds()) {
			selectedPermissionIds.add(Long.valueOf(selectedId));
		}

		List<Permission> selectedPermissions = new ArrayList<Permission>();
		Collection<Permission> allPermissions = this.permissionRepository
				.findAll();
		for (Permission permission : allPermissions) {
			if (selectedPermissionIds.contains(permission.getId())) {
				selectedPermissions.add(permission);
			}
		}
		
		Role role = this.roleRepository.findOne(rolesThatMatch(currentUser.getOrganisation(), command.getId()));
		role.update(command.getName(), command.getDescription(), selectedPermissions);
		
		this.roleRepository.save(role);
		
		return role.getId();
	}

	@Transactional
	@Override
	public Long createOffice(final OfficeCommand command) {
		
		try {
			AppUser currentUser = extractAuthenticatedUser();
			
			OfficeCommandValidator validator = new OfficeCommandValidator(command.getName(), command.getParentId(), command.getOpeningDate(), command.getExternalId());
			validator.validate();
			
			Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, command.getParentId());
	
			Office office = Office.createNew(currentUser.getOrganisation(), parent, command.getName(), command.getOpeningDate(), command.getExternalId());
			
			// pre save to generate id for use in office hierarchy
			this.officeRepository.save(office);
			
			office.generateHierarchy();
			
			this.officeRepository.save(office);
			
			return office.getId();
		} catch (DataIntegrityViolationException dve) {
			 handleOfficeDataIntegrityIssues(command, dve);
			 return Long.valueOf(-1);
		}
	}

	@Transactional
	@Override
	public Long updateOffice(final OfficeCommand command) {

		try {
			AppUser currentUser = extractAuthenticatedUser();
			
			OfficeCommandValidator validator = new OfficeCommandValidator(command.getName(), command.getParentId(), command.getOpeningDate(), command.getExternalId());
			validator.validate();
			
			Office office = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, command.getId());
			
			office.update(command.getName(), command.getExternalId(), command.getOpeningDate());
	
			this.officeRepository.save(office);
	
			return office.getId();
		} catch (DataIntegrityViolationException dve) {
			handleOfficeDataIntegrityIssues(command, dve);
			return Long.valueOf(-1);
		}
	}

	/*
	 * Guaranteed to throw an exception no matter what the data integrity issue is.
	 */
	private void handleOfficeDataIntegrityIssues(final OfficeCommand command, DataIntegrityViolationException dve)  {
		
		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("externalid_org")) {
			throw new PlatformDataIntegrityException("error.msg.office.duplicate.externalId", "Office with externalId {0} already exists", "externalId", command.getExternalId());
		} else if (realCause.getMessage().contains("name_org")) {
			throw new PlatformDataIntegrityException("error.msg.office.duplicate.name", "Office with name {0} already exists", "name", command.getName());
		} 
		
		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
	}

	/*
	 * used to restrict modifying operations to office that are either the users office or lower (child) in the office hierarchy
	 */
	private Office validateUserPriviledgeOnOfficeAndRetrieve(AppUser currentUser, Long officeId) {
		
		Office userOffice = this.officeRepository.findOne(officesThatMatch(currentUser.getOrganisation(), currentUser.getOffice().getId()));
		
		if (userOffice.doesNotHaveAnOfficeInHierarchyWithId(officeId)) {
			ErrorResponse error = new ErrorResponse("error.msg.office.not.authorized", "id", officeId.toString());
			
			throw new ApplicationDomainRuleException(Arrays.asList(error), "Errors exist.");
		}
		
		Office officeToReturn = userOffice;
		if (!userOffice.identifiedBy(officeId)) {
			officeToReturn = this.officeRepository.findOne(officesThatMatch(currentUser.getOrganisation(), officeId));
		}
		
		return officeToReturn;
	}

	@Transactional
	@Override
	public Long signup(final SignupCommand command) {

		try {
			Organisation organisation = new Organisation(
					command.getOrganisationName(), command.getOpeningDate(),
					command.getContactEmail(), command.getContactName(),
					new ArrayList<OrganisationCurrency>());
			this.organisationRepository.save(organisation);

			String name = command.getOrganisationName() + " Head Office";
			String externalId = null;
			Office headOffice = Office.headOffice(organisation, name, command.getOpeningDate(), externalId);
			this.officeRepository.save(headOffice);

			this.userDomainService.createDefaultAdminUser(organisation,
					headOffice);
			return organisation.getId();
		} catch (DataIntegrityViolationException e) {
			throw new InvalidSignupException(e);
		}
	}

	@Transactional
	@Override
	public EntityIdentifier createLoanProduct(final CreateLoanProductCommand command) {

		AppUser currentUser = extractAuthenticatedUser();
		
		LoanProductValidator validator = new LoanProductValidator();
		validator.validateForCreate(command);

		// assemble LoanProduct from data
		InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestMethod());
		InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command.getInterestCalculationPeriodMethod());
		
		AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationMethod());

		PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType
				.fromInt(command.getRepaymentFrequency());
		
		PeriodFrequencyType interestFrequencyType = PeriodFrequencyType
				.fromInt(command.getInterestRateFrequencyMethod());

		MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());

		// apr calculator
		BigDecimal annualInterestRate = BigDecimal.ZERO;
		switch (interestFrequencyType) {
		case DAYS:
			break;
		case WEEKS:
			annualInterestRate = command.getInterestRatePerPeriod().multiply(BigDecimal.valueOf(52));
			break;
		case MONTHS:
			annualInterestRate = command.getInterestRatePerPeriod().multiply(BigDecimal.valueOf(12));
			break;
		case YEARS:
			annualInterestRate = command.getInterestRatePerPeriod().multiply(BigDecimal.valueOf(1));
			break;
		case INVALID:
			break;
		}
		
		LoanProduct loanproduct = new LoanProduct(currentUser.getOrganisation(), command.getName(), command.getDescription(), 
				currency, command.getPrincipal(), 
				command.getInterestRatePerPeriod(), interestFrequencyType, annualInterestRate, interestMethod, interestCalculationPeriodMethod,
				command.getRepaymentEvery(), repaymentFrequencyType, command.getNumberOfRepayments(), amortizationMethod, command.getInArrearsToleranceAmount(),
				command.isFlexibleRepaymentSchedule(), command.isInterestRebateAllowed());
		 
		this.loanProductRepository.save(loanproduct);

		return new EntityIdentifier(loanproduct.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier updateLoanProduct(UpdateLoanProductCommand command) {
		
		AppUser currentUser = extractAuthenticatedUser();
		
		LoanProductValidator validator = new LoanProductValidator();
		validator.validateForUpdate(command);
		
		LoanProduct product = this.loanProductRepository.findOne(productThatMatches(currentUser.getOrganisation(), command.getId()));
		product.update(command);
		
		this.loanProductRepository.save(product);
		
		return new EntityIdentifier(Long.valueOf(product.getId()));
	}
	
	@Transactional
	@Override
	public void updateUsernamePasswordOnFirstTimeLogin(final UpdateUsernamePasswordCommand command) {

		AppUser currentUser = extractAuthenticatedUser();

		try {
			PlatformUser platformUser = ((AppUserRepository) this.platformUserRepository).findOne(usersThatMatch(currentUser.getOrganisation(), command.getOldUsername()));

			PlatformUser dummyPlatformUser = new BasicPasswordEncodablePlatformUser(
					((AppUser) platformUser).getId(),
					platformUser.getUsername(), command.getPassword());

			String encodePassword = this.platformPasswordEncoder.encode(dummyPlatformUser);

			if (command.isUsernameToBeChanged()) {
				platformUser.updateUsernamePasswordOnFirstTimeLogin(
						command.getUsername(), encodePassword);
			} else {
				platformUser.updatePasswordOnFirstTimeLogin(encodePassword);
			}

			((AppUserRepository) this.platformUserRepository).save((AppUser) platformUser);
		} catch (DataIntegrityViolationException e) {
			throw new UsernameAlreadyExistsException(e);
		}
	}

	@Transactional
	@Override
	public Long enrollClient(final EnrollClientCommand command) {

		AppUser currentUser = extractAuthenticatedUser();
		
		EnrollClientCommandValidator validator = new EnrollClientCommandValidator(command);
		validator.validate();

		Office clientOffice = this.officeRepository.findOne(officesThatMatch(
				currentUser.getOrganisation(), command.getOfficeId()));
		
		String firstname = command.getFirstname();
		String lastname = command.getLastname();
		if (StringUtils.isNotBlank(command.getFullname())) {
			lastname = command.getFullname();
			firstname = null;
		}

		Client newClient = Client.newClient(currentUser.getOrganisation(), clientOffice, firstname, lastname, command.getJoiningDate(), command.getExternalId());
				
		this.clientRepository.save(newClient);

		return newClient.getId();
	}
	
	@Transactional
	@Override
	public void updateOrganisationCurrencies(final UpdateOrganisationCurrencyCommand command) {
		
		AppUser currentUser = extractAuthenticatedUser();
		
		if (command.getCodes().isEmpty()) {
			List<ErrorResponse> dataValidationErrors = Arrays.asList(new ErrorResponse("validation.msg.organisation.allowed.currencies.cannot.be.blank", "selectedItems"));
			throw new NewDataValidationException(dataValidationErrors, "Data validation errors exist.");
		}

		Set<OrganisationCurrency> allowedCurrencies = new HashSet<OrganisationCurrency>();

		for (String currencyCode : command.getCodes()) {

			ApplicationCurrency currency = this.applicationCurrencyRepository.findOneByCode(currencyCode);

			OrganisationCurrency allowedCurrency = new OrganisationCurrency(
					currency.getCode(), currency.getName(),
					currency.getDecimalPlaces(), currency.getNameCode(), currency.getDisplaySymbol());

			allowedCurrencies.add(allowedCurrency);
		}

		Organisation org = currentUser.getOrganisation();
		org.setAllowedCurrencies(allowedCurrencies);
		this.organisationRepository.save(org);
	}

	private AppUser extractAuthenticatedUser() {
		AppUser currentUser = null;
		SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			Authentication auth = context.getAuthentication();
			if (auth != null) {
				currentUser = (AppUser) auth.getPrincipal();
			}
		}

		if (currentUser == null) {
			throw new UnAuthenticatedUserException();
		}

		return currentUser;
	}

	@Transactional
	@Override
	public EntityIdentifier submitLoanApplication(final SubmitLoanApplicationCommand command) {

		AppUser currentUser = extractAuthenticatedUser();
		
		SubmitLoanApplicationCommandValidator validator = new SubmitLoanApplicationCommandValidator(command);
		validator.validate();

		LocalDate submittedOn = command.getSubmittedOnDate();
		if (this.isBeforeToday(submittedOn) && currentUser.hasNotPermissionForAnyOf("CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE")) {
			throw new NoAuthorizationException("Cannot add backdated loan.");
		}

		LoanProduct loanProduct = this.loanProductRepository.findOne(command.getProductId());
		Client client = this.clientRepository.findOne(command.getApplicantId());

		MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
		
		final BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(command
				.getInterestRatePerPeriod().doubleValue());
		final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.fromInt(command.getInterestRateFrequencyMethod());
		
		// apr calculator
		BigDecimal defaultAnnualNominalInterestRate = BigDecimal.ZERO;
		switch (interestPeriodFrequencyType) {
		case DAYS:
			break;
		case WEEKS:
			defaultAnnualNominalInterestRate = command
					.getInterestRatePerPeriod()
					.multiply(BigDecimal.valueOf(52));
			break;
		case MONTHS:
			defaultAnnualNominalInterestRate = command
					.getInterestRatePerPeriod()
					.multiply(BigDecimal.valueOf(12));
			break;
		case YEARS:
			defaultAnnualNominalInterestRate = command
					.getInterestRatePerPeriod().multiply(BigDecimal.valueOf(1));
			break;
		case INVALID:
			break;
		}
		
		final InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestMethod());
		final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command.getInterestCalculationPeriodMethod());
		
		final Integer repayEvery = command.getRepaymentEvery();
		final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType
				.fromInt(command.getRepaymentFrequency());
		final Integer defaultNumberOfInstallments = command.getNumberOfRepayments();
		final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationMethod());
		final boolean flexibleRepaymentSchedule = command.isFlexibleRepaymentSchedule();
		final boolean interestRebateAllowed = command.isInterestRebateAllowed();
		
		LoanProductRelatedDetail loanRepaymentScheduleDetail = new LoanProductRelatedDetail(currency,
				command.getPrincipal(), defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType, defaultAnnualNominalInterestRate, 
				interestMethod, interestCalculationPeriodMethod,
				repayEvery, repaymentFrequencyType, defaultNumberOfInstallments, amortizationMethod, command.getInArrearsToleranceAmount(),
				flexibleRepaymentSchedule, interestRebateAllowed);

		LoanSchedule loanSchedule = command.getLoanSchedule();
		List<ScheduledLoanInstallment> loanRepaymentSchedule = loanSchedule.getScheduledLoanInstallments();

		Loan loan = new LoanBuilder()
				.with(currentUser.getOrganisation())
				.with(loanProduct).with(client)
				.with(loanRepaymentScheduleDetail)
				.build();

		for (ScheduledLoanInstallment scheduledLoanInstallment : loanRepaymentSchedule) {

			MoneyData readPrincipalDue = scheduledLoanInstallment
					.getPrincipalDue();

			MoneyData readInterestDue = scheduledLoanInstallment
					.getInterestDue();

			LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(
					loan, scheduledLoanInstallment.getInstallmentNumber(),
					scheduledLoanInstallment.getPeriodStart(),
					scheduledLoanInstallment.getPeriodEnd(), readPrincipalDue.getAmount(),
					readInterestDue.getAmount());
			loan.addRepaymentScheduleInstallment(installment);
		}
		
		loan.submitApplication(submittedOn, command.getExpectedDisbursementDate(), command.getRepaymentsStartingFromDate(), command.getInterestCalculatedFromDate(), defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getSubmittedOnNote())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getSubmittedOnNote());
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}

	private boolean isBeforeToday(final LocalDate date) {
		return date.isBefore(new LocalDate());
	}
	
	private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
		List<LoanStatus> allowedLoanStatuses = this.loanStatusRepository.findAll();
		return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
	}
	
	@Transactional
	@Override
	public EntityIdentifier deleteLoan(Long loanId) {
		
		AppUser currentUser = extractAuthenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), loanId));
		
		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(loanId), "No loan exists with id: " + loanId);
		}
		
		if (loan.isNotSubmittedAndPendingApproval()) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.cannot.delete.loan.in.its.present.state", "eventDate");
			throw new ApplicationDomainRuleException(Arrays.asList(errorResponse), "Loan must be in pending approval state.");
		}
		
		Long clientId = loan.getClient().getId();
		
		List<Note> relatedNotes = this.noteRepository.findByLoanId(loan.getId());
		this.noteRepository.deleteInBatch(relatedNotes);
		
		this.loanRepository.delete(loanId);
		
		return new EntityIdentifier(clientId);
	}

	private List<ErrorResponse> loanIdentifierDoesNotExistError(Long loanId) {
		ErrorResponse errorResponse = new ErrorResponse("error.msg.no.loan.with.identifier.exists", "id", loanId);
		return Arrays.asList(errorResponse);
	}

	@Transactional
	@Override
	public EntityIdentifier approveLoanApplication(final LoanStateTransitionCommand command) {

		AppUser currentUser = extractAuthenticatedUser();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		if (this.isBeforeToday(command.getEventDate()) && currentUser.canNotApproveLoanInPast()) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission.to.approve.loan.in.past", "eventDate");
			throw new ApplicationDomainRuleException(Arrays.asList(errorResponse));
		}

		loan.approve(command.getEventDate(), defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getComment());
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier undoLoanApproval(final UndoLoanApprovalCommand command) {

		AppUser currentUser = extractAuthenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), command.getLoanId()));
		
		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		loan.undoApproval(defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		Note note = Note.loanNote(currentUser.getOrganisation(), loan, "Undo of approval.");
		this.noteRepository.save(note);

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier rejectLoan(final LoanStateTransitionCommand command) {

		AppUser currentUser = (AppUser) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		if (this.isBeforeToday(command.getEventDate()) && currentUser.canNotRejectLoanInPast()) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission.to.reject.loan.in.past", "eventDate");
			throw new ApplicationDomainRuleException(Arrays.asList(errorResponse));
		}
		
		loan.reject(command.getEventDate(), defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getComment());
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier withdrawLoan(final LoanStateTransitionCommand command) {
		AppUser currentUser = extractAuthenticatedUser();

		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		if (this.isBeforeToday(command.getEventDate()) && currentUser.canNotWithdrawByClientLoanInPast()) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission.to.withdraw.loan.in.past", "eventDate");
			throw new ApplicationDomainRuleException(Arrays.asList(errorResponse));
		}
		
		loan.withdraw(command.getEventDate(), defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);

		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getComment());
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier disburseLoan(final LoanStateTransitionCommand command) {

		AppUser currentUser = extractAuthenticatedUser();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		if (this.isBeforeToday(command.getEventDate()) && currentUser.canNotDisburseLoanInPast()) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission.to.disburse.loan.in.past", "eventDate");
			throw new ApplicationDomainRuleException(Arrays.asList(errorResponse));
		}
		
		LocalDate disbursedOn = command.getEventDate();
		String comment = command.getComment();

		LocalDate actualDisbursementDate = new LocalDate(disbursedOn);
		
		if (loan.isRepaymentScheduleRegenerationRequiredForDisbursement(actualDisbursementDate)) {
			
			LocalDate repaymentsStartingFromDate = loan.getExpectedFirstRepaymentOnDate();
			LocalDate interestCalculatedFromDate = loan.getInterestCalculatedFromDate();

			Number principalAsDecimal = loan.getLoanRepaymentScheduleDetail().getPrincipal().getAmount();
			String currencyCode = loan.getLoanRepaymentScheduleDetail().getPrincipal().getCurrencyCode();
			int currencyDigits = loan.getLoanRepaymentScheduleDetail().getPrincipal().getCurrencyDigitsAfterDecimal();
			
			Number interestRatePerYear = loan.getLoanRepaymentScheduleDetail().getAnnualNominalInterestRate();
			Integer numberOfInstallments = loan.getLoanRepaymentScheduleDetail().getNumberOfRepayments();
			
			Integer repaidEvery = loan.getLoanRepaymentScheduleDetail().getRepayEvery();
			Integer selectedRepaymentFrequency = loan.getLoanRepaymentScheduleDetail().getRepaymentPeriodFrequencyType().getValue();
			Integer selectedRepaymentSchedule = loan.getLoanRepaymentScheduleDetail().getAmortizationMethod().getValue();
			boolean flexibleRepaymentSchedule = loan.isFlexibleRepaymentSchedule();
			
			// use annual percentage rate to re-calculate loan schedule for late disbursement
			Number interestRatePerPeriod = interestRatePerYear;
			Integer interestRateFrequencyMethod = PeriodFrequencyType.YEARS.getValue();
			
			Integer interestMethod = loan.getLoanRepaymentScheduleDetail().getInterestMethod().getValue();
			Integer interestCalculationInPeriod = loan.getLoanRepaymentScheduleDetail().getInterestCalculationPeriodMethod().getValue();
			boolean interestRebateAllowed = loan.getLoanRepaymentScheduleDetail().isInterestRebateAllowed();
			
			CalculateLoanScheduleCommand calculateCommand = new CalculateLoanScheduleCommand(currencyCode, currencyDigits, principalAsDecimal, 
					interestRatePerPeriod, interestRateFrequencyMethod, interestMethod, interestCalculationInPeriod,
					repaidEvery, selectedRepaymentFrequency, numberOfInstallments, 
					selectedRepaymentSchedule, flexibleRepaymentSchedule, interestRebateAllowed, actualDisbursementDate, repaymentsStartingFromDate, interestCalculatedFromDate);

			LoanSchedule loanSchedule = this.calculationPlatformService.calculateLoanSchedule(calculateCommand);

			List<LoanRepaymentScheduleInstallment> modifiedLoanRepaymentSchedule = new ArrayList<LoanRepaymentScheduleInstallment>();
			
			for (ScheduledLoanInstallment scheduledLoanInstallment : loanSchedule
					.getScheduledLoanInstallments()) {
				
				final MonetaryCurrency monetaryCurrency = new MonetaryCurrency(
										scheduledLoanInstallment.getPrincipalDue().getCurrencyCode(), 
										scheduledLoanInstallment.getPrincipalDue().getCurrencyDigitsAfterDecimal());

				Money principal = Money.of(monetaryCurrency,
						scheduledLoanInstallment.getPrincipalDue().getAmount());

				Money interest = Money.of(monetaryCurrency,
						scheduledLoanInstallment.getInterestDue().getAmount());

				LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(
						loan, scheduledLoanInstallment.getInstallmentNumber(),
						scheduledLoanInstallment.getPeriodStart(),
						scheduledLoanInstallment.getPeriodEnd(), principal.getAmount(),
						interest.getAmount());
				modifiedLoanRepaymentSchedule.add(installment);
			}
			loan.disburseWithModifiedRepaymentSchedule(disbursedOn, comment, modifiedLoanRepaymentSchedule, defaultLoanLifecycleStateMachine());
		} else {
			loan.disburse(disbursedOn, defaultLoanLifecycleStateMachine());
		}

		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getComment());
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier undloLoanDisbursal(
			final UndoLoanDisbursalCommand command) {

		AppUser currentUser = extractAuthenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		if (loan.isActualDisbursedOnDateEarlierOrLaterThanExpected()) {
			// recalculate loan schedule using original settings.
		}

		loan.undoDisbursal(defaultLoanLifecycleStateMachine());

		this.loanRepository.save(loan);

		// TODO - this may not be wanted.
		Note note = Note.loanNote(currentUser.getOrganisation(), loan, "Undo of disbursal.");
		this.noteRepository.save(note);
		
		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier makeLoanRepayment(final LoanTransactionCommand command) {

		AppUser currentUser = extractAuthenticatedUser();
		
		LoanTransactionValidator validator = new LoanTransactionValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}
		
		if (this.isBeforeToday(command.getPaymentDate()) && currentUser.canNotMakeRepaymentOnLoanInPast()) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission.to.make.repayment.on.loan.in.past", "eventDate");
			throw new ApplicationDomainRuleException(Arrays.asList(errorResponse));
		}

		Money repayment = Money.of(loan.getLoanRepaymentScheduleDetail()
				.getPrincipal().getCurrency(),
				command.getPaymentAmount());

		LoanTransaction loanRepayment = LoanTransaction.repayment(repayment, command.getPaymentDate());
		loan.makeRepayment(loanRepayment, defaultLoanLifecycleStateMachine());
		this.loanTransactionRepository.save(loanRepayment);
		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanTransactionNote(currentUser.getOrganisation(), loan, loanRepayment, command.getComment());
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier adjustLoanTransaction(AdjustLoanTransactionCommand command) {

		AppUser currentUser = extractAuthenticatedUser();

		AdjustLoanTransactionCommandValidator validator = new AdjustLoanTransactionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(
					loanIdentifierDoesNotExistError(command.getLoanId()),
					"No loan exists with id: " + command.getLoanId());
		}

		LoanTransaction transactionToAdjust = this.loanTransactionRepository
				.findOne(command.getRepaymentId());

		Money transactionAmount = Money.of(loan
				.getLoanRepaymentScheduleDetail().getPrincipal()
				.getCurrency(), command.getPaymentAmount());

		// adjustment is only supported for repayments and waivers at present
		LoanTransaction newTransactionDetail = LoanTransaction.repayment(transactionAmount, command.getPaymentDate());
		if (transactionToAdjust.isWaiver()) {
			newTransactionDetail = LoanTransaction.waiver(transactionAmount, command.getPaymentDate());
		}

		loan.adjustExistingTransaction(transactionToAdjust, newTransactionDetail, defaultLoanLifecycleStateMachine());

		this.loanTransactionRepository.save(newTransactionDetail);

		this.loanRepository.save(loan);

		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanTransactionNote(currentUser.getOrganisation(),
					loan, newTransactionDetail, command.getComment());
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier waiveLoanAmount(LoanTransactionCommand command) {
		
		AppUser currentUser = extractAuthenticatedUser();

		LoanTransactionValidator validator = new LoanTransactionValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		Money waived = Money.of(loan.getLoanRepaymentScheduleDetail()
				.getPrincipal().getCurrency(),
				command.getPaymentAmount());

		LoanTransaction waiver = LoanTransaction.waiver(waived, command.getPaymentDate());
		
		loan.waive(waiver, defaultLoanLifecycleStateMachine());
		
		this.loanTransactionRepository.save(waiver);
		
		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanTransactionNote(currentUser.getOrganisation(), loan, waiver, command.getComment());
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier addClientNote(NoteCommand command) {
		
		AppUser currentUser = extractAuthenticatedUser();
		
		Client clientForUpdate = this.clientRepository.findOne(command.getClientId());
		
		Note note = Note.clientNote(currentUser.getOrganisation(), clientForUpdate, command.getNote());
		
		this.noteRepository.save(note);
		
		return new EntityIdentifier(note.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier updateNote(NoteCommand command) {
		
		Note noteForUpdate = this.noteRepository.findOne(command.getId());
		
		noteForUpdate.update(command.getNote());
		
		return new EntityIdentifier(noteForUpdate.getId());
	}
}