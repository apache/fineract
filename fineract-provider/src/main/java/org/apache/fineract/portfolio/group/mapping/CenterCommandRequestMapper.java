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
package org.apache.fineract.portfolio.group.mapping;

import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.portfolio.group.data.CenterActivateRequest;
import org.apache.fineract.portfolio.group.data.CenterAssociateGroupsRequest;
import org.apache.fineract.portfolio.group.data.CenterCloseRequest;
import org.apache.fineract.portfolio.group.data.CenterCommandRequest;
import org.apache.fineract.portfolio.group.data.CenterDisassociateGroupsRequest;
import org.apache.fineract.portfolio.group.data.CenterSaveCollectionSheetRequest;
import org.mapstruct.Mapper;

@Mapper(config = MapstructMapperConfig.class)
public interface CenterCommandRequestMapper {

    CenterActivateRequest toActivate(CenterCommandRequest source, Long id);

    CenterCloseRequest toClose(CenterCommandRequest source, Long id);

    CenterAssociateGroupsRequest toAssociateGroups(CenterCommandRequest source, Long id);

    CenterDisassociateGroupsRequest toDisassociateGroups(CenterCommandRequest source, Long id);

    CenterSaveCollectionSheetRequest toSaveCollectionSheet(CenterCommandRequest source, Long id);
}
