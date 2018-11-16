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
package org.apache.fineract.useradministration.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;

/**
 * Created by sanyam on 23/8/17.
 */
final class RolesApiResourceSwagger {
    private RolesApiResourceSwagger() {

    }

    @ApiModel(value = "GetRolesResponse")
    public static final class GetRolesResponse {
        private GetRolesResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Super Users")
        public String name;
        @ApiModelProperty(example = "This role provides all application permissions.")
        public String description;

    }

    @ApiModel(value = "GetRolesRoleIdResponse")
    public static final class GetRolesRoleIdResponse {
        private GetRolesRoleIdResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Super Users")
        public String name;
        @ApiModelProperty(example = "This role provides all application permissions.")
        public String description;

    }

    @ApiModel(value = "PostRolesRequest")
    public static final class PostRolesRequest {
        private PostRolesRequest() {

        }
        @ApiModelProperty(example = "Another Role Name")
        public String name;
        @ApiModelProperty(example = "A description outlining the purpose of this role in relation to the application.")
        public String description;

    }

    @ApiModel(value = "PostRolesResponse")
    public static final class PostRolesResponse {
        private PostRolesResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;

    }

    @ApiModel(value = "PutRolesRoleIdRequest")
    public static final class PutRolesRoleIdRequest {
        private PutRolesRoleIdRequest() {

        }
        @ApiModelProperty(example = "some description(changed)")
        public String description;

    }

    @ApiModel(value = "PutRolesRoleIdResponse")
    public static final class PutRolesRoleIdResponse {
        private PutRolesRoleIdResponse() {

        }
        final class PutRolesRoleIdResponseChanges {
            private PutRolesRoleIdResponseChanges(){}
            @ApiModelProperty(example = "some description(changed)")
            public String description;
        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
        public PutRolesRoleIdResponseChanges changes;

    }

    @ApiModel(value = "PostRolesRoleIdResponse")
    public static final class PostRolesRoleIdResponse {
        private PostRolesRoleIdResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;

    }

    @ApiModel(value = "GetRolesRoleIdPermissionsResponse")
    public static final class GetRolesRoleIdPermissionsResponse {
        private GetRolesRoleIdPermissionsResponse() {

        }

        final class GetRolesRoleIdPermissionsResponsePermissionData {
            private GetRolesRoleIdPermissionsResponsePermissionData() {

            }
            @ApiModelProperty(example = "authorisation")
            public String grouping;
            @ApiModelProperty(example = "READ_PERMISSION")
            public String code;
            @ApiModelProperty(example = "PERMISSION")
            public String entityName;
            @ApiModelProperty(example = "READ")
            public String actionName;
            @ApiModelProperty(example = "false")
            public Boolean selected;
        }

        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Super Users")
        public String name;
        @ApiModelProperty(example = "This role provides all application permissions.")
        public String description;
        public Collection<GetRolesRoleIdPermissionsResponsePermissionData> permissionUsageData;

    }

    @ApiModel(value = "PutRolesRoleIdPermissionsRequest")
    public static final class PutRolesRoleIdPermissionsRequest {
        private PutRolesRoleIdPermissionsRequest() {

        }

        final class PostRolesRoleIdPermissionsResponsePermissions {
            private PostRolesRoleIdPermissionsResponsePermissions() {

            }
            @ApiModelProperty(example = "true")
            public String ALL_FUNCTIONS_READ;
        }
        public PostRolesRoleIdPermissionsResponsePermissions permissions;

    }

    @ApiModel(value = "PutRolesRoleIdPermissionsResponse")
    public static final class PutRolesRoleIdPermissionsResponse {
        private PutRolesRoleIdPermissionsResponse() {

        }

        final class PostRolesRoleIdPermissionsResponsePermissions {
            private PostRolesRoleIdPermissionsResponsePermissions() {

            }
            @ApiModelProperty(example = "true")
            public String ALL_FUNCTIONS_READ;
        }

        @ApiModelProperty(example = "8")
        public Long resourceId;
        public PostRolesRoleIdPermissionsResponsePermissions permissions;

    }

    @ApiModel(value = "DeleteRolesRoleIdResponse")
    public static final class DeleteRolesRoleIdResponse {
        private DeleteRolesRoleIdResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }
}
