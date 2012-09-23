package org.mifosng.platform.noncore;

import java.util.List;
import java.util.Map;

import org.mifosng.platform.api.data.AdditionalFieldsSetData;
import org.mifosng.platform.api.data.GenericResultsetData;

public interface AdditionalFieldsService {

	// TODO - this service is deprecated and will be replaced by the data tables
	// functionality
	List<AdditionalFieldsSetData> retrieveExtraDatasetNames(String type);

	GenericResultsetData retrieveExtraData(String type, String set, Long id);

	void updateExtraData(String type, String set, Long id,
			Map<String, String> queryParams);

}