package org.mifosng.platform.user.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.mifosng.platform.api.data.PermissionData;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "admin_permission")
public class Permission extends AbstractPersistable<Long> {

    @Column(name = "code", nullable = false, length=100)
    private final String          code;

	@Column(name = "default_name", nullable = false, length=100)
    private final String          defaultName;

	@Column(name = "default_description", nullable = false, length=500)
    private final String          defaultDescription;

	@Column(name = "group_enum", nullable = false)
	@Enumerated(EnumType.ORDINAL)
    private final PermissionGroup permissionGroup;

    public Permission() {
        this.code = null;
        this.defaultDescription = null;
        this.defaultName = null;
        this.permissionGroup = null;
    }

    public Permission(final String code, final String defaultDescription, final String defaultName, final PermissionGroup permissionGroup) {
        this.code = code;
        this.defaultDescription = defaultDescription;
        this.permissionGroup = permissionGroup;
        this.defaultName = defaultName;
    }

	public boolean hasCode(String checkCode) {
		return this.code.equalsIgnoreCase(checkCode);
	}

	public String code() {
		return this.code;
	}

	public PermissionData toData() {
		return new PermissionData(this.getId(), this.defaultName, this.defaultDescription, this.code);
	}
}