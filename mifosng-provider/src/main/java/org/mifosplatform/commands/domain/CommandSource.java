package org.mifosplatform.commands.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_portfolio_command_source")
public class CommandSource extends AbstractPersistable<Long> {

    @Column(name = "api_operation", length = 20)
    private String apiOperation;

    @Column(name = "api_resource", length = 20)
    private String resource;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "command_as_json", length = 1000)
    private String commandAsJson;

    @ManyToOne
    @JoinColumn(name = "maker_id", nullable = false)
    private AppUser maker;

    @Column(name = "made_on_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date madeOnDate;

    @SuppressWarnings("unused")
    @ManyToOne
    @JoinColumn(name = "checker_id", nullable = true)
    private AppUser checker;

    @SuppressWarnings("unused")
    @Column(name = "checked_on_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date checkedOnDate;

    public static CommandSource createdBy(final String apiOperation, final String resource, final Long resourceId,
            final String commandSerializedAsJson, final AppUser maker, final LocalDate madeOnDate) {
        return new CommandSource(apiOperation, resource, resourceId, commandSerializedAsJson, maker, madeOnDate);
    }

    protected CommandSource() {
        //
    }

    private CommandSource(final String apiOperation, final String resource, final Long resourceId, final String commandSerializedAsJson, final AppUser maker,
            final LocalDate madeOnDate) {
        this.apiOperation = StringUtils.defaultIfEmpty(apiOperation, null);
        this.resource = StringUtils.defaultIfEmpty(resource, null);
        this.resourceId = resourceId;
        this.commandAsJson = commandSerializedAsJson;
        this.maker = maker;
        this.madeOnDate = madeOnDate.toDate();
    }

    public CommandSource copy() {
        LocalDate madeOnLocalDate = null;
        if (this.madeOnDate != null) {
            madeOnLocalDate = new LocalDate(this.madeOnDate);
        }
        return new CommandSource(this.apiOperation, this.resource, this.resourceId, this.commandAsJson, this.maker, madeOnLocalDate);
    }

    public void markAsChecked(final AppUser checker, final LocalDate checkedOnDate) {
        this.checker = checker;
        this.checkedOnDate = checkedOnDate.toDate();
    }

    public void updateResourceId(final Long resourceId) {
        this.resourceId = resourceId;
    }

    public void updateJsonTo(final String json) {
        this.commandAsJson = json;
    }

    public Long resourceId() {
        return this.resourceId;
    }

    public String json() {
        return this.commandAsJson;
    }

    public String commandName() {
        return this.apiOperation + '-' + this.resource;
    }

    public String resourceName() {
        return this.resource;
    }

    public String operation() {
        return this.apiOperation;
    }

    public boolean isCreate() {
        return this.apiOperation.equalsIgnoreCase("CREATE");
    }

    public boolean isUpdate() {
        // permissions resource has special update which involves no resource.
        return (isPermissionResource() && isUpdateOperation()) || (isCurrencyResource() && isUpdateOperation())
                || (isUpdateOperation() && this.resourceId != null);
    }

    private boolean isUpdateOperation() {
        return this.apiOperation.equalsIgnoreCase("UPDATE");
    }

    public boolean isDelete() {
        return this.apiOperation.equalsIgnoreCase("DELETE") && this.resourceId != null;
    }

    public boolean isUpdateRolePermissions() {
        return this.apiOperation.equalsIgnoreCase("UPDATEPERMISSIONS") && this.resourceId != null;
    }

    public boolean isPermissionResource() {
        return this.resource.equalsIgnoreCase("PERMISSIONS");
    }

    public boolean isRoleResource() {
        return this.resource.equalsIgnoreCase("ROLES");
    }

    public boolean isUserResource() {
        return this.resource.equalsIgnoreCase("USERS");
    }

    public boolean isCurrencyResource() {
        return this.resource.equalsIgnoreCase("CURRENCIES");
    }

    public boolean isCodeResource() {
        return this.resource.equalsIgnoreCase("CODES");
    }

    public boolean isStaffResource() {
        return this.resource.equalsIgnoreCase("STAFF");
    }

    public boolean isFundResource() {
        return this.resource.equalsIgnoreCase("FUNDS");
    }

    public boolean isOfficeResource() {
        return this.resource.equalsIgnoreCase("OFFICES");
    }

    public boolean isOfficeTransactionResource() {
        return this.resource.equalsIgnoreCase("OFFICETRANSACTIONS");
    }

    public boolean isChargeDefinitionResource() {
        return this.resource.equalsIgnoreCase("CHARGES");
    }

    public boolean isClientResource() {
        return this.resource.equalsIgnoreCase("CLIENTS");
    }

}