/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.data;

import java.io.Serializable;

public class MifosEntityRelationData implements Serializable {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Integer fromEntityType;
    @SuppressWarnings("unused")
    private final Integer toEntityType;
    @SuppressWarnings("unused")
    private final String mappingTypes;

    public MifosEntityRelationData(final Long id, final Integer fromEntityType, final Integer toEntityType, final String mappingTypes) {
        this.id = id;
        this.fromEntityType = fromEntityType;
        this.toEntityType = toEntityType;
        this.mappingTypes = mappingTypes;
    }

    public static MifosEntityRelationData getMappingTypes(final Long id,final String mappingTypes) {
        Integer fromEntityType = null;
        final Integer toEntityType = null;
        return new MifosEntityRelationData(id, fromEntityType, toEntityType, mappingTypes);
    }
}
