package org.mifosplatform.audit.api;

import java.util.Collection;

public interface AuditReadPlatformService {

    Collection<AuditData> retrieveAuditEntries(String extraCriteria, boolean includeJson);

}