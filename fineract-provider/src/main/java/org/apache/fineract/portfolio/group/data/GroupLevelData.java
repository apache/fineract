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
package org.apache.fineract.portfolio.group.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Immutable data object representing groups.
 */
@Getter
@RequiredArgsConstructor
public class GroupLevelData {

    private final Long levelId;
    private final String levelName;
    private final Long parentLevelId;
    private final String parentLevelName;
    private final Long childLevelId;
    private final String childLevelName;
    private final boolean superParent;
    private final boolean recursable;
    private final boolean canHaveClients;

}
