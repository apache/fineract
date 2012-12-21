package org.mifosplatform.infrastructure.security.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.StopWatch;

/**
 * Immutable data object representing platform API request used for
 * logging/debugging.
 */
public class PlatformRequestLog {

    @SuppressWarnings("unused")
    private long startTime;
    @SuppressWarnings("unused")
    private long totalTime;
    @SuppressWarnings("unused")
    private final String method;
    @SuppressWarnings("unused")
    private String url;
    @SuppressWarnings("unused")
    private Map<String, String> parameters;

    public static PlatformRequestLog from(final StopWatch task, final HttpServletRequest request) throws IOException {

        final String requestUrl = request.getRequestURL().toString();

        @SuppressWarnings("unchecked")
        Map<String, String> parameters = new HashMap<String, String>(request.getParameterMap());
        parameters.remove("password");
        parameters.remove("_");

        return new PlatformRequestLog(task.getStartTime(), task.getTime(), request.getMethod(), requestUrl, parameters);
    }

    private PlatformRequestLog(final long startTime, final long time, final String method, final String requestUrl,
            final Map<String, String> parameters) {
        this.startTime = startTime;
        this.totalTime = time;
        this.method = method;
        this.url = requestUrl;
        this.parameters = parameters;
    }
}