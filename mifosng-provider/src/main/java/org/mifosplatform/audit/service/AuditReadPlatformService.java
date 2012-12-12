package org.mifosplatform.audit.service;

import java.util.Collection;

import org.mifosplatform.audit.data.AuditData;
import org.mifosplatform.audit.data.AuditSearchData;

public interface AuditReadPlatformService {

    Collection<AuditData> retrieveAuditEntries(String extraCriteria, boolean includeJson);

    AuditData retrieveAuditEntry(Long auditId);

    AuditSearchData retrieveSearchTemplate();

}