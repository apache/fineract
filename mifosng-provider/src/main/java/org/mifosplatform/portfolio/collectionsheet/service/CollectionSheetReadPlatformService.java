/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.service;

import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.portfolio.collectionsheet.data.JLGCollectionSheetData;
import org.mifosplatform.portfolio.collectionsheet.data.JLGCollectionSheetFlatData;

public interface CollectionSheetReadPlatformService {

    JLGCollectionSheetData retriveCollectionSheet(LocalDate localDate, Long groupId);

    JLGCollectionSheetData generateGroupCollectionSheet(final Long groupId, final JsonQuery query);

    JLGCollectionSheetData generateCenterCollectionSheet(final Long groupId, final JsonQuery query);

    Collection<JLGCollectionSheetFlatData> retriveJLGCollectionSheet(String groupHierarchy, String officeHierarchy, LocalDate dueDate);

}
