package org.mifosplatform.infrastructure.dataqueries.service;

import java.util.List;
import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.dataqueries.data.DatatableData;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ReadWriteNonCoreDataService {

    List<DatatableData> retrieveDatatableNames(String appTable);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'REGISTER_DATATABLE')")
    void registerDatatable(String datatable, String appTable);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DEREGISTER_DATATABLE')")
    void deregisterDatatable(String datatable);

    GenericResultsetData retrieveDataTableGenericResultSet(String datatable, Long appTableId, String order, Long id);

    void newDatatableEntry(String datatable, Long appTableId, JsonCommand command);

    Map<String, Object> updateDatatableEntryOneToOne(String datatable, Long appTableId, JsonCommand command);

    Map<String, Object> updateDatatableEntryOneToMany(String datatable, Long appTableId, Long datatableId, JsonCommand command);

    void deleteDatatableEntries(String datatable, Long appTableId);

    void deleteDatatableEntry(String datatable, Long appTableId, Long datatableId);

}