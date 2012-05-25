package org.mifosng.platform.api.infrastructure;

import java.util.Set;

public interface ApiJSONFormattingService {

	String convertDataObjectJSON(Object dataObject, String fields,
			Set<String> excludeFields, boolean prettyOutput);

	Boolean isTrue(String param);

	Boolean isPassed(String param);

}
