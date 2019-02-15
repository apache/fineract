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
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.useradministration.data.RoleData;

import java.util.Collection;

/**
 * Created by sanyam on 23/8/17.
 */
final class UsersApiResourceSwagger {
    private UsersApiResourceSwagger() {

    }

    @ApiModel(value = "GetUsersResponse")
    public static final class GetUsersResponse {
        private GetUsersResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "mifos")
        public String username;
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        @ApiModelProperty(example = "App")
        public String firstname;
        @ApiModelProperty(example = "Administrator")
        public String lastname;
        @ApiModelProperty(example = "demomfi@mifos.org")
        public String email;
        @ApiModelProperty(example = "false")
        public Boolean passwordNeverExpires;
        public StaffData staff;
        public Collection<RoleData> selectedRoles;

    }

    @ApiModel(value = "GetUsersUserIdResponse")
    public static final class GetUsersUserIdResponse {
        private GetUsersUserIdResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "mifos")
        public String username;
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        @ApiModelProperty(example = "App")
        public String firstname;
        @ApiModelProperty(example = "Administrator")
        public String lastname;
        @ApiModelProperty(example = "demomfi@mifos.org")
        public String email;
        @ApiModelProperty(example = "false")
        public Boolean passwordNeverExpires;
        public StaffData staff;
        public Collection<RoleData> availableRoles;;
        public Collection<RoleData> selectedRoles;

    }

    @ApiModel(value = "GetUsersTemplateResponse")
    public static final class GetUsersTemplateResponse {
        private GetUsersTemplateResponse() {

        }
        public Collection<OfficeData> allowedOffices;
        public Collection<RoleData> availableRoles;
    }

    @ApiModel(value = "PostUsersRequest")
    public static final class PostUsersRequest {
        private PostUsersRequest() {

        }
        @ApiModelProperty(example = "newuser")
        public String username;
        @ApiModelProperty(example = "Test")
        public String firstname;
        @ApiModelProperty(example = "User")
        public String lastname;
        @ApiModelProperty(example = "whatever@mifos.org")
        public String email;
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "1")
        public Long staffId;
        @ApiModelProperty(example = "[2,3]")
        public String Roles;
        @ApiModelProperty(example = "true")
        public Boolean sendPasswordToEmail;
        @ApiModelProperty(example = "true")
        public Boolean isSelfServiceUser;
    }

    @ApiModel(value = "PostUsersResponse")
    public static final class PostUsersResponse {
        private PostUsersResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "11")
        public Long resourceId;
    }

    @ApiModel(value = "PutUsersUserIdRequest")
    public static final class PutUsersUserIdRequest {
        private PutUsersUserIdRequest() {

        }
        @ApiModelProperty(example = "Test")
        public String firstname;
        @ApiModelProperty(example = "window75")
        public String password;
        @ApiModelProperty(example = "window75")
        public String repeatPassword;
    }

    @ApiModel(value = "PutUsersUserIdResponse")
    public static final class PutUsersUserIdResponse {
        private PutUsersUserIdResponse() {

        }
        final class PutUsersUserIdResponseChanges {
            private PutUsersUserIdResponseChanges() {

            }
            @ApiModelProperty(example = "Test")
            public String firstname;
            @ApiModelProperty(example = "abc3326b1bb376351c7baeb4175f5e0504e33aadf6a158474a6d71de1befae51")
            public String passwordEncoded;
        }
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "11")
        public Long resourceId;
        public PutUsersUserIdResponseChanges changes;
    }

    @ApiModel(value = "DeleteUsersUserIdResponse")
    public static final class DeleteUsersUserIdResponse {
        private DeleteUsersUserIdResponse() {

        }
        final class DeleteUsersUserIdResponseChanges {
            private DeleteUsersUserIdResponseChanges() {

            }
        }
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "11")
        public Long resourceId;
        public DeleteUsersUserIdResponseChanges changes;
    }

}
