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

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.useradministration.data.RoleData;

/**
 * Created by sanyam on 23/8/17.
 */
final class UsersApiResourceSwagger {

    private UsersApiResourceSwagger() {

    }

    @Schema(description = "GetUsersResponse")
    public static final class GetUsersResponse {

        private GetUsersResponse() {

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "mifos")
        public String username;
        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "Head Office")
        public String officeName;
        @Schema(example = "App")
        public String firstname;
        @Schema(example = "Administrator")
        public String lastname;
        @Schema(example = "demomfi@mifos.org")
        public String email;
        @Schema(example = "false")
        public Boolean passwordNeverExpires;
        public StaffData staff;
        public Collection<RoleData> selectedRoles;

    }

    @Schema(description = "GetUsersUserIdResponse")
    public static final class GetUsersUserIdResponse {

        private GetUsersUserIdResponse() {

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "mifos")
        public String username;
        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "Head Office")
        public String officeName;
        @Schema(example = "App")
        public String firstname;
        @Schema(example = "Administrator")
        public String lastname;
        @Schema(example = "demomfi@mifos.org")
        public String email;
        @Schema(example = "false")
        public Boolean passwordNeverExpires;
        public StaffData staff;
        public Collection<RoleData> availableRoles;
        public Collection<RoleData> selectedRoles;

    }

    @Schema(description = "GetUsersTemplateResponse")
    public static final class GetUsersTemplateResponse {

        private GetUsersTemplateResponse() {

        }

        public Collection<OfficeData> allowedOffices;
        public Collection<RoleData> availableRoles;
        public Collection<RoleData> selfServiceRoles;
    }

    @Schema(description = "PostUsersRequest")
    public static final class PostUsersRequest {

        private PostUsersRequest() {

        }

        @Schema(example = "newuser")
        public String username;
        @Schema(example = "password")
        public String password;
        @Schema(example = "repeatPassword")
        public String repeatPassword;
        @Schema(example = "Test")
        public String firstname;
        @Schema(example = "User")
        public String lastname;
        @Schema(example = "whatever@mifos.org")
        public String email;
        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long staffId;
        @Schema(example = "[2,3]")
        public List<Long> roles;
        @Schema(example = "[2,3]")
        public List<Long> clients;
        @Schema(example = "true")
        public Boolean sendPasswordToEmail;
        @Schema(example = "true")
        public Boolean passwordNeverExpires;
        @Schema(example = "true")
        public Boolean isSelfServiceUser;
    }

    @Schema(description = "PostUsersResponse")
    public static final class PostUsersResponse {

        private PostUsersResponse() {

        }

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "11")
        public Long resourceId;
    }

    @Schema(description = "PutUsersUserIdRequest")
    public static final class PutUsersUserIdRequest {

        private PutUsersUserIdRequest() {

        }

        @Schema(example = "Test")
        public String firstname;
        @Schema(example = "User")
        public String lastname;
        @Schema(example = "whatever@mifos.org")
        public String email;
        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long staffId;
        @Schema(example = "[2,3]")
        public List<Long> roles;
        @Schema(example = "[2,3]")
        public List<Long> clients;
        @Schema(example = "password")
        public String password;
        @Schema(example = "repeatPassword")
        public String repeatPassword;
        @Schema(example = "true")
        public Boolean sendPasswordToEmail;
        @Schema(example = "true")
        public Boolean isSelfServiceUser;
    }

    @Schema(description = "PutUsersUserIdResponse")
    public static final class PutUsersUserIdResponse {

        private PutUsersUserIdResponse() {

        }

        static final class PutUsersUserIdResponseChanges {

            private PutUsersUserIdResponseChanges() {

            }

            @Schema(example = "Test")
            public String firstname;
            @Schema(example = "abc3326b1bb376351c7baeb4175f5e0504e33aadf6a158474a6d71de1befae51")
            public String passwordEncoded;
        }

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "11")
        public Long resourceId;
        public PutUsersUserIdResponseChanges changes;
    }

    @Schema(description = "DeleteUsersUserIdResponse")
    public static final class DeleteUsersUserIdResponse {

        private DeleteUsersUserIdResponse() {

        }

        static final class DeleteUsersUserIdResponseChanges {

            private DeleteUsersUserIdResponseChanges() {

            }
        }

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "11")
        public Long resourceId;
        public DeleteUsersUserIdResponseChanges changes;
    }

}
