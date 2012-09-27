package org.mifosng.platform.noncore;

import java.util.List;
import java.util.Map;

import org.mifosng.platform.api.data.DatatableData;
import org.mifosng.platform.api.data.GenericResultsetData;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ReadWriteNonCoreDataService {

	List<DatatableData> retrieveDatatableNames(String appTable);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CAN_REGISTER_DATATABLE')")
	void registerDatatable(String datatable, String appTable);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CAN_DEREGISTER_DATATABLE')")
	void deregisterDatatable(String datatable);

	GenericResultsetData retrieveDataTableGenericResultSet(String datatable,
			Long appTableId, String sqlFields, String sqlOrder, Long id);

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