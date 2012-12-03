package org.mifosplatform.infrastructure.core.serialization;

import java.util.Set;

/**
 * A class to encapsulate settings we allow on API that affect how JSON is to be
 * serialized for the response to api call.
 */
public class ApiRequestJsonSerializationSettings {

    private final boolean prettyPrint;
    private final Set<String> parametersForPartialResponse;

    public ApiRequestJsonSerializationSettings(final boolean prettyPrint, final Set<String> parametersForPartialResponse) {
        this.prettyPrint = prettyPrint;
        this.parametersForPartialResponse = parametersForPartialResponse;
    }

    public static ApiRequestJsonSerializationSettings from(final boolean prettyPrint, final Set<String> parametersForPartialResponse) {

        // FIXME - kw - rather than always creating new objects for this could
        // just send by common ones like, prettyprint=false, empty response
        // parameters
        return new ApiRequestJsonSerializationSettings(prettyPrint, parametersForPartialResponse);
    }

    public boolean isPrettyPrint() {
        return this.prettyPrint;
    }

    public Set<String> getParametersForPartialResponse() {
        return this.parametersForPartialResponse;
    }

    public boolean isPartialResponseRequired() {
        return !this.parametersForPartialResponse.isEmpty();
    }
}