package org.mifosng.platform.group.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "portfolio_group")
public class Group extends AbstractAuditableCustom<AppUser, Long> {

    @SuppressWarnings("unused")
    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    public Group() {
        this.name = null;
        this.externalId = null;
    }

    public static Group newGroup(String name, String externalId){
        return new Group(name, externalId);
    }
    
    public Group(String name, String externalId) {
        if (StringUtils.isNotBlank(name)) {
            this.name = name.trim();
        } else {
            this.name = null;
        }
        if (StringUtils.isNotBlank(externalId)) {
            this.externalId = externalId.trim();
        } else {
            this.externalId = null;
        }
    }
}
