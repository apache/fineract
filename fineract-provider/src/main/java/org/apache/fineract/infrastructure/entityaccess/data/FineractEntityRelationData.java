/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.entityaccess.data;

import java.io.Serializable;

public class FineractEntityRelationData implements Serializable {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Integer fromEntityType;
    @SuppressWarnings("unused")
    private final Integer toEntityType;
    @SuppressWarnings("unused")
    private final String mappingTypes;

    public FineractEntityRelationData(final Long id, final Integer fromEntityType, final Integer toEntityType, final String mappingTypes) {
        this.id = id;
        this.fromEntityType = fromEntityType;
        this.toEntityType = toEntityType;
        this.mappingTypes = mappingTypes;
    }

    public static FineractEntityRelationData getMappingTypes(final Long id,final String mappingTypes) {
        Integer fromEntityType = null;
        final Integer toEntityType = null;
        return new FineractEntityRelationData(id, fromEntityType, toEntityType, mappingTypes);
    }
}
