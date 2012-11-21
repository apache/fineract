package org.mifosng.platform.user.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.data.PermissionData;
import org.mifosng.platform.api.data.RoleData;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;

@Entity
@Table(name = "m_role")
public class Role extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name="name", nullable=false, length=100)
    private String          name;

    @Column(name="description", nullable=false, length=500)
    private String          description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "m_role_permission", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions;

    protected Role() {
        this.name = null;
        this.description = null;
        this.permissions = new HashSet<Permission>();
    }

    public Role(final String name, final String description) {
        this.name = name.trim();
        this.description = description.trim();
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
		
		Collection<PermissionData> rolePermissions = new ArrayList<PermissionData>();
		for (Permission permission : this.permissions) {
			PermissionData permissionData = permission.toData();
			rolePermissions.add(permissionData);
		}
		
		return new RoleData(this.getId(), this.name, this.description, rolePermissions);
	}

	public void update(final RoleCommand command) {
		if (command.isNameChanged()) {
			this.name = command.getName();
		}
		
		if (command.isDescriptionChanged()) {
			this.description = command.getDescription();
		}
		
	}

	public String getName() {
		return name;
	}
	
}