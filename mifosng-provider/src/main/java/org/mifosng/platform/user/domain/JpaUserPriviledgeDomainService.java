package org.mifosng.platform.user.domain;

import java.util.Arrays;

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
    public void createAllOrganisationRolesAndPermissions() {

        // 2. create out of the box application permissions and roles
        Permission viewUsersAndRolesPermission = new PermissionBuilder().viewUsersAndRoles().build();
        Permission createUserPermission = new PermissionBuilder().userCreation().build();
        Permission createRolePermission = new PermissionBuilder().roleCreation().build();
        Permission updateApplicationPermissionsPermission = new PermissionBuilder().updateApplicationPermissions().build();

        Permission[] userAdminPermissions = new Permission[] {viewUsersAndRolesPermission, createUserPermission,
                createRolePermission, updateApplicationPermissionsPermission};

        Permission viewOrganisationOfficesStaffAndProductsPermission = new PermissionBuilder().viewOrganisationsOfficesStaffAndProducts().build();
        Permission addOfficePermission = new PermissionBuilder().addOffice().build();
        Permission addStaffPermission = new PermissionBuilder().addStaff().build();
        Permission addLoanProductPermission = new PermissionBuilder().addLoanProduct().build();

        Permission[] organisationAdminPermissions = new Permission[] {viewOrganisationOfficesStaffAndProductsPermission, addOfficePermission
                ,addStaffPermission, addLoanProductPermission};

        Permission viewLoanPortfolioPermission = new PermissionBuilder().viewLoanPortfolio().build();
        Permission addLoanPermission = new PermissionBuilder().addLoan().build();
        Permission addBackdatedLoanPermission = new PermissionBuilder().addBackdatedLoan().build();
        Permission approveLoanPermission = new PermissionBuilder().approveLoan().build();
        Permission approveLoanWithPastDatePermission = new PermissionBuilder().approveLoanInThePast().build();
        Permission rejectLoanPermission = new PermissionBuilder().rejectLoan().build();
        Permission rejectLoanWithPastDatePermission = new PermissionBuilder().rejectLoanInThePast().build();
        Permission withdrawLoanPermission = new PermissionBuilder().withdrawLoan().build();
        Permission withdrawLoanWithPastDatePermission = new PermissionBuilder().withdrawLoanInThePast().build();
        Permission undoLoanApprovalPermission = new PermissionBuilder().undoLoanApproval().build();

        Permission disburseLoanPermission = new PermissionBuilder().disburseLoan().build();
        Permission disburseLoanWithPastDatePermission = new PermissionBuilder().disburseLoanInThePast().build();
        Permission undoLoanDisbursalPermission = new PermissionBuilder().undoLoanDisbursal().build();

        Permission makeLoanRepaymentPermission = new PermissionBuilder().makeLoanRepayment().build();
        Permission makeLoanRepaymentWithPastDatePermission = new PermissionBuilder().makeLoanRepaymentInThePast().build();

        //        Permission writeoffLoanPermission = new PermissionBuilder().writeoffLoan().build();
        //        Permission writeoffLoanWithPastDatePermission = new PermissionBuilder().writeoffLoanInThePast().build();

        //        Permission rescheduleLoanPermission = new PermissionBuilder().rescheduleLoan().build();
        //        Permission rescheduleLoanWithPastDatePermission = new PermissionBuilder().rescheduleInThePast().build();

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
				.dataMigration().build();

		Permission[] migrationPermissions = new Permission[] { dataMigrationPermission };

        this.permissionRepository.save(Arrays.asList(userAdminPermissions));
        this.permissionRepository.save(Arrays.asList(organisationAdminPermissions));
        this.permissionRepository.save(Arrays.asList(fullLoanPortfolioPermissions));
		this.permissionRepository.save(Arrays.asList(migrationPermissions));

        // 2. roles
        String userAdminRoleDescription = "A user administrator can create new roles, assign and update roles assigned to users and create and deactivate users.";
        
        Role userAdminRole = new Role("User Administrator", userAdminRoleDescription, Arrays.asList(userAdminPermissions));
        
        String organisationAdminRoleDescription = "A organisation administrator can view, create and update organisation office hierarchy, staff and products.";
        Role organisatonAdminRole = new Role("Organisation Administrator", organisationAdminRoleDescription, Arrays.asList(organisationAdminPermissions));
        
        String fullLoanPortfolioRoleDescription = "A loan portfolio user can view, create and update client, group and loan information.";
        Role loanPortfolioFullRole = new Role("Full Loan Portfolio", fullLoanPortfolioRoleDescription, Arrays.asList(fullLoanPortfolioPermissions));

		this.roleRepository.save(Arrays.asList(userAdminRole, organisatonAdminRole, loanPortfolioFullRole));
    }
}