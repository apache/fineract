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
package org.apache.fineract.organisation.office.mapper;

import java.util.Optional;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.domain.Office;
import org.mapstruct.Mapper;

@Mapper(config = MapstructMapperConfig.class)
public interface OfficeDataMapper {

    default OfficeData toOfficeData(Office office) {
        String hierarchy = office.getHierarchy();
        String nameDecorated;
        if (hierarchy == null) {
            nameDecorated = "";
        } else {
            nameDecorated = hierarchy.substring(0, (hierarchy.length() - hierarchy.replace(".", "").length() - 1) * 4)
                    + Optional.ofNullable(office.getName()).orElse("");
        }
        return new OfficeData(office.getId(), office.getName(), nameDecorated, office.getExternalId(), office.getOpeningDate(),
                office.getHierarchy(), office.getParent() != null ? office.getParent().getId() : null,
                office.getParent() != null ? office.getParent().getName() : null, null);
    }
}
