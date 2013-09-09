package org.mifosplatform.xbrl.report.service;

import org.mifosplatform.xbrl.report.data.NamespaceData;

public interface ReadNamespaceService {

    NamespaceData retrieveNamespaceById(Long id);

    NamespaceData retrieveNamespaceByPrefix(String prefix);
}
