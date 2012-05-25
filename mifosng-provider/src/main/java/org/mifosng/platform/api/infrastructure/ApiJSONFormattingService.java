package org.mifosng.platform.api.infrastructure;

import javax.ws.rs.core.MultivaluedMap;

public interface ApiJSONFormattingService {

	String convertRequest(Object dataObject, String allowedFieldList,
			String selectedFields, MultivaluedMap<String, String> queryParams);

	String convertDataObjectJSON(Object dataObject, String filterType,
			String fields, boolean prettyOutput);

	Boolean isTrue(String param);

	Boolean isPassed(String param);

}
