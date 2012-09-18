package org.mifosng.platform.noncore;

import java.util.List;
import java.util.Map;

import org.mifosng.platform.api.data.AdditionalFieldsSetData;
import org.mifosng.platform.api.data.DatatableData;
import org.mifosng.platform.api.data.GenericResultsetData;

public interface ReadWriteNonCoreDataService {

	List<AdditionalFieldsSetData> retrieveExtraDatasetNames(String type);

	GenericResultsetData retrieveExtraData(String type, String set, Long id);

	void updateExtraData(String type, String set, Long id,
			Map<String, String> queryParams);

	/* remove the additional fields functionality above when datatables complete */
	List<DatatableData> retrieveDatatableNames(String appTable);

	GenericResultsetData retrieveDataTableGenericResultSet(String datatable,
			Long appTableId, String sqlFields, String sqlOrder);

	String retrieveDataTableJSONObject(String datatable, Long appTableId,
			String sqlFields, String sqlOrder);

	void newDatatableEntry(String datatable, Long appTableId,
			Map<String, String> queryParams);

	void deleteDatatableEntries(String datatable, Long appTableId);

	void deleteDatatableEntry(String datatable, Long appTableId,
			Long datatableId);

}