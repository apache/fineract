package org.mifosng.platform.organisation.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_code_value", uniqueConstraints = { @UniqueConstraint(columnNames = {
		"code_id", "code_value" }, name = "code_value_duplicate") })
public class CodeValue extends AbstractPersistable<Long> {

	@Column(name = "code_value", length = 100)
	private String label;

	@Column(name = "order_position")
	private int position;

	// Code to which this value belongs
	@SuppressWarnings("unused")
	@ManyToOne
	@JoinColumn(name = "code_id", nullable = false)
	private Code code;

	public static CodeValue createNew(Code code, final String label,
			final int position) {
		return new CodeValue(code, label, position);
	}

	protected CodeValue() {
		//
	}

	private CodeValue(final Code code, final String label, final int position) {
		this.code = code;
		this.label = StringUtils.defaultIfEmpty(label, null);
		this.position = position;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
}