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
package org.apache.fineract.useradministration.data;

/**
 * Immutable representation of permissions
 */
public class PermissionData {

    @SuppressWarnings("unused")
    private final String grouping;
    @SuppressWarnings("unused")
    private final String code;
    @SuppressWarnings("unused")
    private final String entityName;
    @SuppressWarnings("unused")
    private final String actionName;
    @SuppressWarnings("unused")
    private final Boolean selected;

    public static PermissionData from(final String permissionCode, final boolean isSelected) {
        return new PermissionData(null, permissionCode, null, null, isSelected);
    }

    public static PermissionData instance(final String grouping, final String code, final String entityName, final String actionName,
            final Boolean selected) {
        return new PermissionData(grouping, code, entityName, actionName, selected);
    }

    private PermissionData(final String grouping, final String code, final String entityName, final String actionName,
            final Boolean selected) {
        this.grouping = grouping;
        this.code = code;
        this.entityName = entityName;
        this.actionName = actionName;
        this.selected = selected;
    }
}