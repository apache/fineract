package org.mifosplatform.portfolio.group.service;

public class SearchParameters {

    private final String sqlSearch;
    private final Long officeId;
    private final String externalId;
    private final String name;
    private final String hierarchy;
    private final String firstname;
    private final String lastname;

    public static SearchParameters from(final String sqlSearch, final Long officeId, final String externalId, final String name,
            final String hierarchy) {
        return new SearchParameters(sqlSearch, officeId, externalId, name, hierarchy, null, null);
    }

    public static SearchParameters forClients(final String sqlSearch, final Long officeId, final String externalId,
            final String displayName, final String firstname, final String lastname, final String hierarchy) {
        return new SearchParameters(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname);
    }

    private SearchParameters(final String sqlSearch, final Long officeId, final String externalId, final String name,
            final String hierarchy, final String firstname, final String lastname) {
        this.sqlSearch = sqlSearch;
        this.officeId = officeId;
        this.externalId = externalId;
        this.name = name;
        this.hierarchy = hierarchy;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public String getSqlSearch() {
        return this.sqlSearch;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public String getName() {
        return this.name;
    }

    public String getHierarchy() {
        return this.hierarchy;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }
}