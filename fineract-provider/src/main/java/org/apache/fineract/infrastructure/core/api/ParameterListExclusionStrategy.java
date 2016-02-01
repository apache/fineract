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
package org.apache.fineract.infrastructure.core.api;

import java.util.Set;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

// FIXME - KW - General thing to fix about REST API is if use partial response approach of fields=id,name,selectedPermissions
//        It will return just id,name parameters of RoleData and ignore description, however as PermissionData used in selectedPermissions collection
//        also has a field called description it gets ignored also. This is because of the implementation of ParameterListExclusionStrategy which doesnt take
//        into account the Object its looking at.
public class ParameterListExclusionStrategy implements ExclusionStrategy {

    private final Set<String> parameterNamesToSkip;

    public ParameterListExclusionStrategy(final Set<String> parameterNamesToSkip) {
        this.parameterNamesToSkip = parameterNamesToSkip;
    }

    @Override
    public boolean shouldSkipField(final FieldAttributes f) {
        return this.parameterNamesToSkip.contains(f.getName());
    }

    @SuppressWarnings("unused")
    @Override
    public boolean shouldSkipClass(final Class<?> clazz) {
        return false;
    }
}