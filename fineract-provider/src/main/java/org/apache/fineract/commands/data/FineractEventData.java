package org.apache.fineract.commands.data;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FineractEventData {

    private final CommandWrapper request;
    private final CommandProcessingResult response;
    private final String tenantIdentifier;
    private final String timestamp;
    private final UUID contextId;
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public FineractEventData(CommandWrapper request, CommandProcessingResult response, String tenantIdentifier) {

        this.request = request;
        this.response = response;
        this.tenantIdentifier = tenantIdentifier;
        this.timestamp = df.format(new Date());
        this.contextId = UUID.randomUUID();
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

    public UUID getContextId() {
        return contextId;
    }
}
