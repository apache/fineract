package org.mifosng.platform.noncore;

import java.util.List;
import java.util.Map;

import org.mifosng.platform.api.data.DatatableData;
import org.mifosng.platform.api.data.GenericResultsetData;

public interface ReadWriteNonCoreDataService {

	List<DatatableData> retrieveDatatableNames(String appTable);

	GenericResultsetData retrieveDataTableGenericResultSet(String datatable,
			Long appTableId, String sqlFields, String sqlOrder, Long id);

	String retrieveDataTableJSONObject(String datatable, Long appTableId,
			String sqlFields, String sqlOrder);

	void newDatatableEntry(String datatable, Long appTableId,
			Map<String, String> queryParams);

	void updateDatatableEntryOnetoOne(String datatable, Long appTableId,
			Map<String, String> queryParams);

	void updateDatatableEntryOnetoMany(String datatable, Long appTableId,
			Long datatableId, Map<String, String> queryParams);

	void deleteDatatableEntries(String datatable, Long appTableId);

	void deleteDatatableEntry(String datatable, Long appTableId,
			Long datatableId);

}