package org.mifosng.platform.user.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.mifosng.platform.api.data.PermissionData;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_permission")
public class Permission extends AbstractPersistable<Long> {

    @Column(name = "grouping", nullable = false, length=45)
    private final String          grouping;
    
    @Column(name = "order_in_grouping", nullable = false)
    private final Integer          orderInGrouping;
    
    @Column(name = "code", nullable = false, length=100)
    private final String          code;

	@Column(name = "default_name", nullable = false, length=100)
    private final String          defaultName;

	@Column(name = "default_description", nullable = false, length=500)
    private final String          defaultDescription;

    public Permission() {
        this.grouping = null;
        this.orderInGrouping = null;
        this.code = null;
        this.defaultDescription = null;
        this.defaultName = null;
    }
//TODO - This constructor probably not used in app (JPW)
    public Permission(final String code, final String defaultDescription, final String defaultName) {
        this.grouping = "";
        this.orderInGrouping = 0;
        this.code = code;
        this.defaultDescription = defaultDescription;
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