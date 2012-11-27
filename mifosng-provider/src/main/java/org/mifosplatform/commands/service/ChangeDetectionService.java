package org.mifosplatform.commands.service;

public interface ChangeDetectionService {

    String detectChangesOnUpdate(String resourceName, Long resourceId, String commandSerializedAsJson);

}