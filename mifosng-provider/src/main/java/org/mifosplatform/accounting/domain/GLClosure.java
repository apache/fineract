package org.mifosplatform.accounting.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.api.commands.GLClosureCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "acc_gl_closure", uniqueConstraints = { @UniqueConstraint(columnNames = { "office_id", "closing_date" }, name = "office_id_closing_date") })
public class GLClosure extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @SuppressWarnings("unused")
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = true;

    @Column(name = "closing_date")
    @Temporal(TemporalType.DATE)
    private Date closingDate;

    @SuppressWarnings("unused")
    @Column(name = "comments", nullable = true, length = 500)
    private String comments;

    public static GLClosure createNew(Office office, LocalDate closingDate, String comments) {
        return new GLClosure(office, closingDate, comments);
    }

    protected GLClosure() {
        //
    }

    public GLClosure(Office office, LocalDate closingDate, String comments) {
        this.office = office;
        this.deleted = false;
        this.closingDate = closingDate.toDateMidnight().toDate();
        if (StringUtils.isNotBlank(comments)) {
            this.comments = comments.trim();
        } else {
            this.comments = null;
        }
    }

    public void update(final GLClosureCommand command) {
        if (command.isCommentsChanged()) {
            this.comments = command.getComments().trim();
        }
    }

    public Date getClosingDate() {
        return this.closingDate;
    }

    public Office getOffice() {
        return this.office;
    }

}