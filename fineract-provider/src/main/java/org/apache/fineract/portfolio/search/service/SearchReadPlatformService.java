/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.search.service;

import java.util.Collection;

import org.mifosplatform.portfolio.search.data.AdHocQuerySearchConditions;
import org.mifosplatform.portfolio.search.data.AdHocSearchQueryData;
import org.mifosplatform.portfolio.search.data.SearchConditions;
import org.mifosplatform.portfolio.search.data.SearchData;

public interface SearchReadPlatformService {

    Collection<SearchData> retriveMatchingData(SearchConditions searchConditions);

    AdHocSearchQueryData retrieveAdHocQueryTemplate();

    Collection<AdHocSearchQueryData> retrieveAdHocQueryMatchingData(AdHocQuerySearchConditions searchConditions);
}
