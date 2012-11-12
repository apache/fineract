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
@Table(name = "m_maker_checker")
public class MakerChecker extends AbstractPersistable<Long> {

	@Column(name = "task_name", length = 100)
	private String taskName;

	@Column(name = "task_json", length = 1000)
	private String taskJson;

	// should maker and checker be a staff member as opposed to just any application user?
	@ManyToOne
	@JoinColumn(name = "maker_id", nullable = false)
	private AppUser maker;
	
	@Column(name = "made_on_date", nullable = false)
    @Temporal(TemporalType.DATE)
	private Date madeOnDate;
	
	@ManyToOne
	@JoinColumn(name = "checker_id", nullable = true)
	private AppUser checker;
	
	@Column(name = "checked_on_date", nullable = false)
    @Temporal(TemporalType.DATE)
	private Date checkedOnDate;

	public static MakerChecker makerEntry(
			final String taskName,
			final String jsonRequestBody,
			final AppUser maker,
			final LocalDate madeOnDate) {
		return new MakerChecker(taskName, jsonRequestBody, maker, madeOnDate);
	}

	protected MakerChecker() {
		//
	}

	private MakerChecker(
			final String taskName,
			final String taskJson,
			final AppUser maker,
			final LocalDate madeOnDate) {
		this.taskName = StringUtils.defaultIfEmpty(taskName, null);
		this.taskJson = StringUtils.defaultIfEmpty(taskJson, null);
		this.maker = maker;
		this.madeOnDate = madeOnDate.toDate();
	}
}