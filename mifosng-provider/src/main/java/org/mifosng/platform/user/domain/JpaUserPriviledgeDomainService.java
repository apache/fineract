package org.mifosng.platform.user.domain;

import java.util.Arrays;

import org.mifosng.platform.organisation.domain.Organisation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaUserPriviledgeDomainService implements UserPriviledgeDomainService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    @Override
    public void createAllOrganisationRolesAndPermissions(final Organisation organisation) {

        // 2. create out of the box application permissions and roles
        Permission viewUsersAndRolesPermission = new PermissionBuilder().viewUsersAndRoles().with(organisation).build();
        Permission createUserPermission = new PermissionBuilder().userCreation().with(organisation).build();
        Permission createRolePermission = new PermissionBuilder().roleCreation().with(organisation).build();
        Permission updateApplicationPermissionsPermission = new PermissionBuilder().updateApplicationPermissions().with(organisation).build();

        Permission[] userAdminPermissions = new Permission[] {viewUsersAndRolesPermission, createUserPermission,
                createRolePermission, updateApplicationPermissionsPermission};

        Permission viewOrganisationOfficesStaffAndProductsPermission = new PermissionBuilder().viewOrganisationsOfficesStaffAndProducts().with(organisation).build();
        Permission addOfficePermission = new PermissionBuilder().addOffice().with(organisation).build();
        Permission addStaffPermission = new PermissionBuilder().addStaff().with(organisation).build();
        Permission addLoanProductPermission = new PermissionBuilder().addLoanProduct().with(organisation).build();

        Permission[] organisationAdminPermissions = new Permission[] {viewOrganisationOfficesStaffAndProductsPermission, addOfficePermission
                ,addStaffPermission, addLoanProductPermission};

        Permission viewLoanPortfolioPermission = new PermissionBuilder().viewLoanPortfolio().with(organisation).build();
        Permission addLoanPermission = new PermissionBuilder().addLoan().with(organisation).build();
        Permission addBackdatedLoanPermission = new PermissionBuilder().addBackdatedLoan().with(organisation).build();
        Permission approveLoanPermission = new PermissionBuilder().approveLoan().with(organisation).build();
        Permission approveLoanWithPastDatePermission = new PermissionBuilder().approveLoanInThePast().with(organisation).build();
        Permission rejectLoanPermission = new PermissionBuilder().rejectLoan().with(organisation).build();
        Permission rejectLoanWithPastDatePermission = new PermissionBuilder().rejectLoanInThePast().with(organisation).build();
        Permission withdrawLoanPermission = new PermissionBuilder().withdrawLoan().with(organisation).build();
        Permission withdrawLoanWithPastDatePermission = new PermissionBuilder().withdrawLoanInThePast().with(organisation).build();
        Permission undoLoanApprovalPermission = new PermissionBuilder().undoLoanApproval().with(organisation).build();

        Permission disburseLoanPermission = new PermissionBuilder().disburseLoan().with(organisation).build();
        Permission disburseLoanWithPastDatePermission = new PermissionBuilder().disburseLoanInThePast().with(organisation).build();
        Permission undoLoanDisbursalPermission = new PermissionBuilder().undoLoanDisbursal().with(organisation).build();

        Permission makeLoanRepaymentPermission = new PermissionBuilder().makeLoanRepayment().with(organisation).build();
        Permission makeLoanRepaymentWithPastDatePermission = new PermissionBuilder().makeLoanRepaymentInThePast().with(organisation).build();

        //        Permission writeoffLoanPermission = new PermissionBuilder().writeoffLoan().with(organisation).build();
        //        Permission writeoffLoanWithPastDatePermission = new PermissionBuilder().writeoffLoanInThePast().with(organisation).build();

        //        Permission rescheduleLoanPermission = new PermissionBuilder().rescheduleLoan().with(organisation).build();
        //        Permission rescheduleLoanWithPastDatePermission = new PermissionBuilder().rescheduleInThePast().with(organisation).build();

        // MIFOS-2878 - support recovery of payments for written off loans.

        // support interest reduction for early payments and interest increase for late payments

        //  how many MFIs allow clients to be in multiple groups (more than one)?

        Permission[] fullLoanPortfolioPermissions = new Permission[] {viewLoanPortfolioPermission,
                addLoanPermission,
                addBackdatedLoanPermission,
                approveLoanPermission,
                approveLoanWithPastDatePermission,
                rejectLoanPermission,
                rejectLoanWithPastDatePermission,
                withdrawLoanPermission,
                withdrawLoanWithPastDatePermission,
                undoLoanApprovalPermission,
                disburseLoanPermission,
                disburseLoanWithPastDatePermission,
                undoLoanDisbursalPermission,
                makeLoanRepaymentPermission,
                makeLoanRepaymentWithPastDatePermission,
                //                writeoffLoanPermission,
                //                writeoffLoanWithPastDatePermission,
                //                rescheduleLoanPermission
                //                ,rescheduleLoanWithPastDatePermission
        };

		Permission dataMigrationPermission = new PermissionBuilder()
				.dataMigration().with(organisation).build();

		Permission[] migrationPermissions = new Permission[] { dataMigrationPermission };

        this.permissionRepository.save(Arrays.asList(userAdminPermissions));
        this.permissionRepository.save(Arrays.asList(organisationAdminPermissions));
        this.permissionRepository.save(Arrays.asList(fullLoanPortfolioPermissions));
		this.permissionRepository.save(Arrays.asList(migrationPermissions));

        // 2. roles
        String userAdminRoleDescription = "A user administrator can create new roles, assign and update roles assigned to users and create and deactivate users.";
        
        Role userAdminRole = new Role(organisation, "User Administrator", userAdminRoleDescription, Arrays.asList(userAdminPermissions));
        
        String organisationAdminRoleDescription = "A organisation administrator can view, create and update organisation office hierarchy, staff and products.";
        Role organisatonAdminRole = new Role(organisation, "Organisation Administrator", organisationAdminRoleDescription, Arrays.asList(organisationAdminPermissions));
        
        String fullLoanPortfolioRoleDescription = "A loan portfolio user can view, create and update client, group and loan information.";
        Role loanPortfolioFullRole = new Role(organisation, "Full Loan Portfolio", fullLoanPortfolioRoleDescription, Arrays.asList(fullLoanPortfolioPermissions));

		this.roleRepository.save(Arrays.asList(userAdminRole, organisatonAdminRole, loanPortfolioFullRole));
    }
}