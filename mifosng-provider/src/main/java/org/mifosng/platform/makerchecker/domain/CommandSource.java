package org.mifosng.platform.makerchecker.domain;

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
import org.mifosng.platform.user.domain.AppUser;
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

	// should maker and checker be a staff member as opposed to just any application user?
	@SuppressWarnings("unused")
	@ManyToOne
	@JoinColumn(name = "maker_id", nullable = false)
	private AppUser maker;
	
	@SuppressWarnings("unused")
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

	public static CommandSource createdBy(
			final String apiOperation,
			final String resource,
			final Long resourceId, 
			final AppUser maker,
			final LocalDate madeOnDate) {
		return new CommandSource(apiOperation, resource, resourceId, maker, madeOnDate);
	}

	protected CommandSource() {
		//
	}

	private CommandSource(
			final String apiOperation,
			final String resource,
			final Long resourceId,
			final AppUser maker,
			final LocalDate madeOnDate) {
		this.apiOperation = StringUtils.defaultIfEmpty(apiOperation, null);
		this.resource = StringUtils.defaultIfEmpty(resource, null);
		this.resourceId = resourceId;
		this.maker = maker;
		this.madeOnDate = madeOnDate.toDate();
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

	public boolean isClientResource() {
		return this.resource.equalsIgnoreCase("CLIENTS");
	}

	public boolean isCreate() {
		return this.apiOperation.equalsIgnoreCase("CREATE");
	}

	public boolean isUpdate() {
		return this.apiOperation.equalsIgnoreCase("UPDATE") && this.resourceId != null;
	}

	public boolean isDelete() {
		return this.apiOperation.equalsIgnoreCase("DELETE") && this.resourceId != null;
	}
}