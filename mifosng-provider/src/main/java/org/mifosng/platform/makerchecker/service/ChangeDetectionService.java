package org.mifosng.platform.makerchecker.service;

public interface ChangeDetectionService {

    String detectChangesOnUpdate(String resourceName, Long resourceId, String commandSerializedAsJson);

}