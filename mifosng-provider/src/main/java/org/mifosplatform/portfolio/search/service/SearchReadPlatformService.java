package org.mifosplatform.portfolio.search.service;

import java.util.Collection;

import org.mifosplatform.portfolio.search.data.SearchConditions;
import org.mifosplatform.portfolio.search.data.SearchData;

public interface SearchReadPlatformService {

    Collection<SearchData> retriveMatchingData(SearchConditions searchConditions);
}
