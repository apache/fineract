package org.mifosng.platform.organisation.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_code", uniqueConstraints = { @UniqueConstraint(columnNames = { "code_name" }, name = "code_name") })
public class Code extends AbstractPersistable<Long> {

	@SuppressWarnings("unused")
	@Column(name = "code_name", length = 100)
	private String codeName;

	public static Code createNew(final String codeName) {
		return new Code(codeName);
	}

	protected Code() {
		//
	}

	private Code(final String codeName) {
		this.codeName = codeName;
	}

}