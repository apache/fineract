package org.mifosng.platform.api.infrastructure;

import javax.ws.rs.core.MultivaluedMap;

public interface ApiJSONFormattingService {

	String convertRequest(Object dataObject, String filterName,
			String allowedFieldList, String selectedFields,
			MultivaluedMap<String, String> queryParams);

	String convertRequest(Object dataObject, String filterName,
			String allowedFieldList, String selectedFields,
			String associationFields, MultivaluedMap<String, String> queryParams);

}
