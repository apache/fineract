package org.apache.fineract.commands.data;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

import java.util.Date;

public class FineractEventData {

    private final CommandWrapper request;
    private final CommandProcessingResult response;
    private final String tenantIdentifier;
    private final String timestamp;

    public FineractEventData(CommandWrapper request, CommandProcessingResult response, String tenantIdentifier) {
        this.request = request;
        this.response = response;
        this.tenantIdentifier = tenantIdentifier;
        this.timestamp = new Date().toString();
    }

    public CommandWrapper getRequest() {
        return request;
    }

    public CommandProcessingResult getResponse() {
        return response;
    }

    public String getTenantIdentifier() {
        return tenantIdentifier;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
