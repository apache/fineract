package org.mifosng.platform.saving.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "portfolio_product_savings")
public class SavingProduct extends AbstractAuditableCustom<AppUser, Long> {

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	public SavingProduct() {
		this.name = null;
		this.description = null;
	}

	public SavingProduct(final String name, final String description) {
		this.name = name.trim();
		if (StringUtils.isNotBlank(description)) {
			this.description = description.trim();
		} else {
			this.description = null;
		}
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public boolean identifiedBy(final String identifier) {
		return identifier.equalsIgnoreCase(this.name);
	}

	public void update(final SavingProductCommand command) {

		if (command.isNameChanged()) {
			this.name = command.getName();
		}

		if (command.isDescriptionChanged()) {
			this.description = command.getDescription();
		}

	}

}
