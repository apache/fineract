package org.mifosng.platform.user.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.infrastructure.PlatformUser;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.Organisation;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Entity
@Table(name = "admin_appuser", uniqueConstraints=@UniqueConstraint(columnNames = {"org_id", "username"}))
public class AppUser extends AbstractAuditableCustom<AppUser, Long> implements PlatformUser {

    @Column(name = "email", nullable=false, length=100)
    private String       email;

    @Column(name = "username", nullable=false, length=100)
    private String       username;
    
    @Column(name = "firstname", nullable=false, length=100)
    private String       firstname;
    
    @Column(name = "lastname", nullable=false, length=100)
    private String       lastname;

    @Column(name = "password", nullable=false)
    private String             password;

    @Column(name = "nonexpired", nullable=false)
    private final boolean      accountNonExpired;

    @Column(name = "nonlocked", nullable=false)
    private final boolean      accountNonLocked;

    @Column(name = "nonexpired_credentials", nullable=false)
    private final boolean      credentialsNonExpired;

    @Column(name = "enabled", nullable=false)
    private final boolean      enabled;

    @Column(name = "firsttime_login_remaining", nullable=false)
    private boolean      firstTimeLoginRemaining;

    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private final Organisation organisation;

    @ManyToOne
    @JoinColumn(name = "office_id")
    private Office office;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "admin_appuser_role", joinColumns = @JoinColumn(name = "appuser_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role>    roles;
    

	public static AppUser createNew(Organisation organisation, Office office,
			Set<Role> allRoles, String username, String email,
			String firstname, String lastname, String password) {
		
		boolean userEnabled = true;
		boolean userAccountNonExpired = true;
		boolean userCredentialsNonExpired = true;
		boolean userAccountNonLocked = true;
		
		Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
		User user = new User(username, password, userEnabled, userAccountNonExpired, userCredentialsNonExpired, userAccountNonLocked, authorities);
		return new AppUser(organisation, office, user, allRoles, email, firstname, lastname);
	}

    protected AppUser() {
        this.organisation = null;
        this.office = null;
        this.email = null;
        this.username = null;
        this.password = null;
        this.accountNonExpired = false;
        this.accountNonLocked = false;
        this.credentialsNonExpired = false;
        this.enabled = false;
        this.roles = new HashSet<Role>();
        this.firstTimeLoginRemaining = true;
    }

    public AppUser(final Organisation organisation, final Office office, final User user, final Set<Role> roles, final String email, final String firstname, final String lastname) {
        this.organisation = organisation;
        this.office = office;
        this.email = email.trim();
        this.username = user.getUsername().trim();
        this.firstname = firstname.trim();
        this.lastname = lastname.trim();
        this.password = user.getPassword().trim();
        this.accountNonExpired = user.isAccountNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.enabled = user.isEnabled();
        this.roles = roles;
		this.firstTimeLoginRemaining = true;
    }

	@Override
	public boolean isFirstTimeLoginRemaining() {
		return this.firstTimeLoginRemaining;
	}

	@Override
	public void updateUsernamePasswordOnFirstTimeLogin(final String newUsername,
			final String newPasswordEncoded) {
		if (this.username.equals(newUsername)) {
			throw new UsernameMustBeDifferentException();
		}
		this.username = newUsername;
		updatePasswordOnFirstTimeLogin(newPasswordEncoded);
		this.firstTimeLoginRemaining = false;
	}

	@Override
	public void updatePasswordOnFirstTimeLogin(final String newPasswordEncoded) {
		if (this.password.equals(newPasswordEncoded)) {
			throw new PasswordMustBeDifferentException();
		}
		this.password = newPasswordEncoded;
		this.firstTimeLoginRemaining = false;
	}

	public void updatePassword(final String encodePassword) {
		this.password = encodePassword;
	}

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return this.populateGrantedAuthorities();
    }

    private List<GrantedAuthority> populateGrantedAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        for (Role role :this.roles) {
            Collection<Permission> permissions = role.getPermissions();
            for (Permission permission : permissions) {
                grantedAuthorities.add(new SimpleGrantedAuthority(permission.code()));
            }
        }
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public String getEmail() {
        return this.email;
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    public String getRoleNames() {
        StringBuilder roleNames = new StringBuilder();

        for (Role role : this.roles) {
            roleNames.append(role.toData().getName()).append(' ');
        }

        return roleNames.toString();
    }

    public void setUserIdAs(final Long id) {
        this.setId(id);
    }

    public Organisation getOrganisation() {
        return this.organisation;
    }

    public Office getOffice() {
        return this.office;
    }

    public boolean isHeadOfficeUser() {
        boolean headOfficeUser = false;
        if (this.office != null) {
            headOfficeUser = this.office.isHeadOffice();
        }
        return headOfficeUser;
    }

    public boolean hasPermissionTo(final String permissionCode) {
        boolean match = false;
        for (Role role : this.roles) {
            if (role.hasPermissionTo(permissionCode)) {
                match = true;
                break;
            }
        }

        return match;
    }
    
    public boolean hasNotPermissionForAnyOf(final String... permissionCodes) {
    	boolean hasNotPermission = true;
    	for (String permissionCode : permissionCodes) {
			boolean checkPermission = this.hasPermissionTo(permissionCode);
			if (checkPermission) {
				hasNotPermission = false;
				break;
			}
		}
    	return hasNotPermission;
    }

    public boolean hasNotPermissionTo(final String permissionCode) {
        return !this.hasPermissionTo(permissionCode);
    }

	public boolean canAccess(Long officeId) {
		return this.office.hasAnOfficeInHierarchyWithId(officeId);
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void update(Set<Role> allRoles, Office office, String username,
			String firstname, String lastname, String email) {
		this.roles.clear();
		this.roles = allRoles;
		this.office = office;
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
	}

	public void update(String username, String firstname, String lastname, String email) {
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
	}

	public boolean canNotApproveLoanInPast() {
		return hasNotPermissionForAnyOf("CAN_APPROVE_LOAN_IN_THE_PAST_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE");
	}

	public boolean canNotRejectLoanInPast() {
		return hasNotPermissionForAnyOf("CAN_REJECT_LOAN_IN_THE_PAST_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE");
	}

	public boolean canNotWithdrawByClientLoanInPast() {
		return hasNotPermissionForAnyOf("CAN_WITHDRAW_LOAN_IN_THE_PAST_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE");
	}

	public boolean canNotDisburseLoanInPast() {
		return hasNotPermissionForAnyOf("CAN_DISBURSE_LOAN_IN_THE_PAST_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE");
	}

	public boolean canNotMakeRepaymentOnLoanInPast() {
		return hasNotPermissionForAnyOf("CAN_MAKE_LOAN_REPAYMENT_IN_THE_PAST_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE");
	}
}