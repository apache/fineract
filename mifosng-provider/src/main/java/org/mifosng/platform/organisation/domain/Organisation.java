package org.mifosng.platform.organisation.domain;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "org_organisation")
public class Organisation extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name = "name", nullable = false, unique=true, length=100)
    private final String name;

    @Column(name = "opening_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private final Date   openingDate;

    @Column(name = "contact_email", nullable = false, length=100)
    private final String contactEmail;

    @Column(name = "contact_name", nullable = false, length=100)
    private final String contactName;

	@OneToMany(mappedBy = "organisation", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<OrganisationCurrency> allowedCurrencies;

    protected Organisation() {
        this.name = null;
        this.openingDate = null;
        this.contactEmail = null;
        this.contactName = null;
		this.allowedCurrencies = null;
    }

	public Organisation(final String name, final LocalDate openingDate,
			final String contactEmail, final String contactName,
			final List<OrganisationCurrency> currencies
    ) {
        this.name = name.trim();
        this.contactEmail = contactEmail.trim();
        this.contactName = contactName.trim();
        this.openingDate = openingDate.toDateMidnight().toDate();
		this.setAllowedCurrencies(new LinkedHashSet<OrganisationCurrency>(
				currencies));
    }

    public String defaultHeadOfficeName() {
        return this.name + " Head Office";
    }

    public LocalDate openingDate() {
        return new LocalDate(this.openingDate);
    }

    public String getContactEmail() {
        return this.contactEmail;
    }

    public String getContactName() {
        return this.contactName;
    }

    public String getName() {
        return this.name;
    }

	public void addAllowedCurrency(final OrganisationCurrency currency) {
		currency.updateOrganisation(this);
		this.allowedCurrencies.add(currency);
	}

	public void setAllowedCurrencies(
			final Set<OrganisationCurrency> allowedCurrencies) {
		this.allowedCurrencies = allowedCurrencies;
		for (OrganisationCurrency currency : allowedCurrencies) {
			currency.updateOrganisation(this);
		}
	}
}