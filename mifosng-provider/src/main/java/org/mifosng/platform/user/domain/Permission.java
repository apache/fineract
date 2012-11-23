package org.mifosng.platform.user.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_permission")
public class Permission extends AbstractPersistable<Long> {

    @SuppressWarnings("unused")
    @Column(name = "grouping", nullable = false, length = 45)
    private final String grouping;

    @Column(name = "code", nullable = false, length = 100)
    private final String code;

    @SuppressWarnings("unused")
    @Column(name = "entity_name", nullable = true, length = 100)
    private final String entityName;

    @SuppressWarnings("unused")
    @Column(name = "action_name", nullable = true, length = 100)
    private final String actionName;

    @SuppressWarnings("unused")
    @Column(name = "is_maker_checker", nullable = false)
    private Boolean isMakerChecker;

    protected Permission() {
        this.grouping = null;
        this.code = null;
        this.entityName = null;
        this.actionName = null;
    }

    public boolean hasCode(final String checkCode) {
        return this.code.equalsIgnoreCase(checkCode);
    }

    public String code() {
        return this.code;
    }
}