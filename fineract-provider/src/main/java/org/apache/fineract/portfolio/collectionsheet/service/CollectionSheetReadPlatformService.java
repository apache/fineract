/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.service;

import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.portfolio.collectionsheet.data.IndividualCollectionSheetData;
import org.mifosplatform.portfolio.collectionsheet.data.JLGCollectionSheetData;

public interface CollectionSheetReadPlatformService {

    JLGCollectionSheetData generateGroupCollectionSheet(final Long groupId, final JsonQuery query);

    JLGCollectionSheetData generateCenterCollectionSheet(final Long groupId, final JsonQuery query);

    IndividualCollectionSheetData generateIndividualCollectionSheet(final JsonQuery query);

}
