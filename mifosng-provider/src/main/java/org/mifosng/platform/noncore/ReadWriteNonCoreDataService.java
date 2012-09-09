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

	List<DatatableData> retrieveDatatableNames(String appTable);

	GenericResultsetData retrieveDataTableGenericResultSet(String datatable,
			Long id, String sqlFields, String sqlOrder);

	String retrieveDataTableJSONObject(String datatable, Long id,
			String sqlFields, String sqlOrder);

}