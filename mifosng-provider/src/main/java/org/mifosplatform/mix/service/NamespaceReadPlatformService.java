package org.mifosplatform.mix.service;

import org.mifosplatform.mix.data.NamespaceData;

public interface NamespaceReadPlatformService {

    NamespaceData retrieveNamespaceById(Long id);

    NamespaceData retrieveNamespaceByPrefix(String prefix);
}
