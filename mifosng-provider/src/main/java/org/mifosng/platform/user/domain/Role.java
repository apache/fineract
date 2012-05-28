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

import org.mifosng.data.PermissionData;
import org.mifosng.data.RoleData;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.organisation.domain.Organisation;

@Entity
@Table(name = "admin_role")
public class Role extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name="name", nullable=false, length=100)
    private String          name;

    @Column(name="description", nullable=false, length=500)
    private String          description;

    @ManyToOne
    @JoinColumn(name="org_id", nullable=false)
    private Organisation organisation;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "admin_role_permission", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions;

    protected Role() {
        this.name = null;
        this.description = null;
        this.permissions = new HashSet<Permission>();
        this.organisation = null;
    }

    public Role(final Organisation organisation, final String name, final String description, final List<Permission> rolePermissions) {
        this.organisation = organisation;
        this.name = name.trim();
        this.description = description.trim();
        this.permissions = new HashSet<Permission>(rolePermissions);
    }

    public Collection<Permission> getPermissions() {
        return this.permissions;
    }

    public boolean hasPermissionTo(final String permissionCode) {
        boolean match = false;
        for (Permission permission : this.permissions) {
            if (permission.hasCode(permissionCode)) {
                match = true;
                break;
            }
        }
        return match;
    }

	public RoleData toData() {
		
		RoleData data = new RoleData(this.getId(), this.organisation.getId(), this.name, this.description);
		
		Collection<PermissionData> rolePermissions = new ArrayList<PermissionData>();
		for (Permission permission : this.permissions) {
			PermissionData permissionData = permission.toData();
			rolePermissions.add(permissionData);
		}
		data.setSelectedPermissions(rolePermissions);
		
		return data;
	}

	/**
	 * When updating details, any parameters with null values are ignored.
	 */
	public void update(String name, String description, List<Permission> selectedPermissions) {
		if (name != null) {
			this.name = name;
		}
		if (description != null) {
			this.description = description;
		}
		if (!selectedPermissions.isEmpty()) {
			this.permissions.clear();
			this.permissions = new HashSet<Permission>(selectedPermissions);
		}
	}
}