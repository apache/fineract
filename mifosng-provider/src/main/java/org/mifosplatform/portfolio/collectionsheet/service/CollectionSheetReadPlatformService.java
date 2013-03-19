package org.mifosplatform.portfolio.collectionsheet.service;

import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.collectionsheet.data.JLGCollectionSheetData;
import org.mifosplatform.portfolio.collectionsheet.data.JLGCollectionSheetFlatData;

public interface CollectionSheetReadPlatformService {

    JLGCollectionSheetData retriveCollectionSheet(LocalDate localDate, Long groupId);

    Collection<JLGCollectionSheetFlatData> retriveJLGCollectionSheet(String groupHierarchy, String officeHierarchy, LocalDate dueDate);

}
