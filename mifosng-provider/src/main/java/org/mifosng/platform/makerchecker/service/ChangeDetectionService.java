package org.mifosng.platform.makerchecker.service;

public interface ChangeDetectionService {

    String detectChanges(String operation, String resourceName, Long resourceId, String commandSerializedAsJson);

}