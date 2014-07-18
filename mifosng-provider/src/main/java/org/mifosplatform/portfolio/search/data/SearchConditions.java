/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.search.data;

import org.mifosplatform.portfolio.search.SearchConstants.SEARCH_SUPPORTED_RESOURCES;

public class SearchConditions {

    private final String searchQuery;
    private final String searchResource;
    private final Boolean clientSearch;
    private final Boolean groupSearch;
    private final Boolean loanSeach;
	private final Boolean savingSeach;
    private final Boolean clientIdentifierSearch;

    public SearchConditions(final String searchQueryParam, final String searchResource) {
        this.searchQuery = searchQueryParam;
        this.searchResource = searchResource;
        this.clientSearch = (null == searchResource || searchResource.toLowerCase().contains(
                SEARCH_SUPPORTED_RESOURCES.CLIENTS.name().toLowerCase())) ? true : false;
        this.groupSearch = (null == searchResource || searchResource.toLowerCase().contains(
                SEARCH_SUPPORTED_RESOURCES.GROUPS.name().toLowerCase())) ? true : false;
        this.loanSeach = (null == searchResource || searchResource.toLowerCase().contains(
                SEARCH_SUPPORTED_RESOURCES.LOANS.name().toLowerCase())) ? true : false;
        this.savingSeach = (null == searchResource || searchResource.toLowerCase().contains(
                SEARCH_SUPPORTED_RESOURCES.SAVINGS.name().toLowerCase())) ? true : false;
  		this.clientIdentifierSearch = (null == searchResource || searchResource.toLowerCase().contains(
                SEARCH_SUPPORTED_RESOURCES.CLIENTIDENTIFIERS.name().toLowerCase())) ? true : false;
    }

    public SearchConditions(final String searchQueryParam, final String searchResource, final Boolean clientSearch,
            final Boolean groupSearch, final Boolean loanSeach, final Boolean savingSeach, final Boolean clientIdentifierSearch) {
        this.searchQuery = searchQueryParam;
        this.searchResource = searchResource;
        this.clientSearch = clientSearch;
        this.groupSearch = groupSearch;
        this.loanSeach = loanSeach;
		this.savingSeach = savingSeach;
        this.clientIdentifierSearch = clientIdentifierSearch;
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public String getSearchResource() {
        return this.searchResource;
    }

    public Boolean isClientSearch() {
        return this.clientSearch;
    }

    public Boolean isGroupSearch() {
        return this.groupSearch;
    }

    public Boolean isLoanSeach() {
        return this.loanSeach;
    }

    public Boolean isSavingSeach() {
        return this.savingSeach;
    }

	public Boolean isClientIdentifierSearch() {
        return this.clientIdentifierSearch;
    }

}
