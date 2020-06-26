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
package org.apache.fineract.portfolio.account.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 11/29/17.
 */
final class StandingInstructionApiResourceSwagger {

    private StandingInstructionApiResourceSwagger() {
        // this class is only for Swagger Live Documentation
    }

    @Schema(description = "GetStandingInstructionsTemplateResponse")
    public static final class GetStandingInstructionsTemplateResponse {

        private GetStandingInstructionsTemplateResponse() {}

        static final class GetFromOfficeResponseStandingInstructionSwagger {

            private GetFromOfficeResponseStandingInstructionSwagger() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "Head Office")
            public String name;
            @Schema(example = "Head Office")
            public String decoratedName;
            @Schema(example = "1")
            public Integer externalId;
            @Schema(example = "[2009, 1, 1]")
            public LocalDate openingDate;
            @Schema(example = ".")
            public String hierarchy;
        }

        static final class GetFromAccountTypeResponseStandingInstructionSwagger {

            private GetFromAccountTypeResponseStandingInstructionSwagger() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "accountType.savings")
            public String code;
            @Schema(example = "Savings Account")
            public String description;
        }

        static final class GetFromOfficeOptionsResponseStandingInstructionSwagger {

            private GetFromOfficeOptionsResponseStandingInstructionSwagger() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "Head Office")
            public String name;
            @Schema(example = "Head Office")
            public String nameDecorated;
        }

        static final class GetFromClientOptionsResponseStandingInstructionSwagger {

            private GetFromClientOptionsResponseStandingInstructionSwagger() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "Client_FirstName_2VRAG Client_LastName_9QCY")
            public String displayName;
            @Schema(example = "1")
            public Long officeId;
            @Schema(example = "Head Office")
            public String officeName;
        }

        static final class GetFromAccountTypeOptionsResponseStandingInstructionSwagger {

            private GetFromAccountTypeOptionsResponseStandingInstructionSwagger() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "accountType.savings")
            public String code;
            @Schema(example = "Savings Account")
            public String description;
        }

        static final class GetToOfficeOptionsResponseStandingInstructionSwagger {

            private GetToOfficeOptionsResponseStandingInstructionSwagger() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Head Office")
            public String name;
            @Schema(example = "Head Office")
            public String nameDecorated;
        }

        static final class GetToAccountTypeOptionsResponseStandingInstructionSwagger {

            private GetToAccountTypeOptionsResponseStandingInstructionSwagger() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "accountType.loan")
            public String code;
            @Schema(example = "Loan Account")
            public String description;
        }

        static final class GetTransferTypeOptionsResponseStandingInstructionSwagger {

            private GetTransferTypeOptionsResponseStandingInstructionSwagger() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "accountTransferType.account.transfer")
            public String code;
            @Schema(example = "Account Transfer")
            public String description;
        }

        static final class GetStatusOptionsResponseStandingInstructionSwagger {

            private GetStatusOptionsResponseStandingInstructionSwagger() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "standingInstructionStatus.active")
            public String code;
            @Schema(example = "Active")
            public String description;
        }

        static final class GetInstructionTypeOptionsResponseStandingInstructionSwagger {

            private GetInstructionTypeOptionsResponseStandingInstructionSwagger() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "standingInstructionType.fixed")
            public String code;
            @Schema(example = "Fixed")
            public String description;
        }

        static final class GetPriorityOptionsResponseStandingInstructionSwagger {

            private GetPriorityOptionsResponseStandingInstructionSwagger() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "standingInstructionPriority.urgent")
            public String code;
            @Schema(example = "Urgent Priority")
            public String description;
        }

        static final class GetRecurrenceTypeOptionsResponseStandingInstructionSwagger {

            private GetRecurrenceTypeOptionsResponseStandingInstructionSwagger() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "accountTransferRecurrenceType.periodic")
            public String code;
            @Schema(example = "Periodic Recurrence")
            public String description;
        }

        static final class GetRecurrenceFrequencyOptionsResponseStandingInstructionSwagger {

            private GetRecurrenceFrequencyOptionsResponseStandingInstructionSwagger() {}

            @Schema(example = "0")
            public Integer id;
            @Schema(example = "frequencyperiodFrequencyType.days")
            public String code;
            @Schema(example = "Days")
            public String description;
        }

        public GetFromOfficeResponseStandingInstructionSwagger fromOffice;
        public GetFromAccountTypeResponseStandingInstructionSwagger fromAccountType;
        public Set<GetFromOfficeOptionsResponseStandingInstructionSwagger> fromOfficeOptions;
        public Set<GetFromClientOptionsResponseStandingInstructionSwagger> fromClientOptions;
        public Set<GetFromAccountTypeOptionsResponseStandingInstructionSwagger> fromAccountTypeOptions;
        public Set<GetToOfficeOptionsResponseStandingInstructionSwagger> toOfficeOptions;
        public Set<GetToAccountTypeOptionsResponseStandingInstructionSwagger> toAccountTypeOptions;
        public Set<GetTransferTypeOptionsResponseStandingInstructionSwagger> transferTypeOptions;
        public Set<GetStatusOptionsResponseStandingInstructionSwagger> statusOptions;
        public Set<GetInstructionTypeOptionsResponseStandingInstructionSwagger> instructionTypeOptions;
        public Set<GetPriorityOptionsResponseStandingInstructionSwagger> priorityOptions;
        public Set<GetRecurrenceTypeOptionsResponseStandingInstructionSwagger> recurrenceTypeOptions;
        public Set<GetRecurrenceFrequencyOptionsResponseStandingInstructionSwagger> recurrenceFrequencyOptions;
    }

    @Schema(description = "PostStandingInstructionsResponse")
    public static final class PostStandingInstructionsResponse {

        private PostStandingInstructionsResponse() {}

        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "65")
        public Integer resourceId;
    }

    @Schema(description = "GetStandingInstructionsResponse")
    public static final class GetStandingInstructionsResponse {

        private GetStandingInstructionsResponse() {}

        static final class GetPageItemsStandingInstructionSwagger {

            static final class GetFromOfficeStandingInstructionSwagger {

                private GetFromOfficeStandingInstructionSwagger() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "Head Office")
                public String name;
            }

            static final class GetFromClientStandingInstructionSwagger {

                private GetFromClientStandingInstructionSwagger() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "Test test")
                public String displayName;
                @Schema(example = "1")
                public Long officeId;
                @Schema(example = "Head Office")
                public String officeName;
            }

            static final class GetFromAccountTypeStandingInstructionSwagger {

                private GetFromAccountTypeStandingInstructionSwagger() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "accountType.savings")
                public String code;
                @Schema(example = "Savings Account")
                public String description;
            }

            static final class GetFromAccountStandingInstructionSwagger {

                private GetFromAccountStandingInstructionSwagger() {}

                @Schema(example = "14")
                public Long id;
                @Schema(example = "000000014")
                public Long accountNo;
                @Schema(example = "1")
                public Long productId;
                @Schema(example = "savings old")
                public String productName;
            }

            static final class GetToOfficeStandingInstructionSwagger {

                private GetToOfficeStandingInstructionSwagger() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "Head Office")
                public String name;
            }

            static final class GetToClientStandingInstructionSwagger {

                private GetToClientStandingInstructionSwagger() {}

                @Schema(example = "1")
                public Long id;
                @Schema(example = "Test test")
                public String displayName;
                @Schema(example = "1")
                public Long officeId;
                @Schema(example = "Head Office")
                public String officeName;
            }

            static final class GetToAccountTypeStandingInstructionSwagger {

                private GetToAccountTypeStandingInstructionSwagger() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "accountType.savings")
                public String code;
                @Schema(example = "Savings Account")
                public String description;
            }

            static final class GetToAccountStandingInstructionSwagger {

                private GetToAccountStandingInstructionSwagger() {}

                @Schema(example = "3")
                public Long id;
                @Schema(example = "000000003")
                public Long accountNo;
                @Schema(example = "4")
                public Long productId;
                @Schema(example = "account overdraft")
                public String productName;
            }

            static final class GetTransferTypeStandingInstructionSwagger {

                private GetTransferTypeStandingInstructionSwagger() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "accountTransferType.account.transfer")
                public String code;
                @Schema(example = "Account Transfer")
                public String description;
            }

            static final class GetPriorityStandingInstructionSwagger {

                private GetPriorityStandingInstructionSwagger() {}

                @Schema(example = "3")
                public Integer id;
                @Schema(example = "standingInstructionPriority.medium")
                public String code;
                @Schema(example = "Medium Priority")
                public String description;
            }

            static final class GetInstructionTypeStandingInstructionSwagger {

                private GetInstructionTypeStandingInstructionSwagger() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "standingInstructionType.fixed")
                public String code;
                @Schema(example = "Fixed")
                public String description;
            }

            static final class GetStatusStandingInstructionSwagger {

                private GetStatusStandingInstructionSwagger() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "standingInstructionStatus.deleted")
                public String code;
                @Schema(example = "Deleted")
                public String description;
            }

            static final class GetRecurrenceTypeStandingInstructionSwagger {

                private GetRecurrenceTypeStandingInstructionSwagger() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "accountTransferRecurrenceType.periodic")
                public String code;
                @Schema(example = "Periodic Recurrence")
                public String description;
            }

            static final class GetRecurrenceFrequencyStandingInstructionSwagger {

                private GetRecurrenceFrequencyStandingInstructionSwagger() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "frequencyperiodFrequencyType.months")
                public String code;
                @Schema(example = "Months")
                public String description;
            }

            @Schema(example = "1")
            public Long id;
            @Schema(example = "6")
            public Long accountDetailId;
            @Schema(example = "test standing")
            public String name;
            public GetFromOfficeStandingInstructionSwagger fromOffice;
            public GetFromClientStandingInstructionSwagger fromClient;
            public GetFromAccountTypeStandingInstructionSwagger fromAccountType;
            public GetFromAccountStandingInstructionSwagger fromAccount;
            public GetToOfficeStandingInstructionSwagger toOffice;
            public GetToClientStandingInstructionSwagger toClient;
            public GetToAccountTypeStandingInstructionSwagger toAccountType;
            public GetToAccountStandingInstructionSwagger toAccount;
            public GetTransferTypeStandingInstructionSwagger transferType;
            public GetPriorityStandingInstructionSwagger priority;
            public GetInstructionTypeStandingInstructionSwagger instructionType;
            public GetStatusStandingInstructionSwagger status;
            @Schema(example = "150.000000")
            public float amount;
            @Schema(example = "[2014, 4, 3]")
            public LocalDate validFrom;
            public GetRecurrenceTypeStandingInstructionSwagger recurrenceType;
            public GetRecurrenceFrequencyStandingInstructionSwagger recurrenceFrequency;
            @Schema(example = "1")
            public Integer recurrenceInterval;
            @Schema(example = "[4, 3]")
            public LocalDate recurrenceOnMonthDay;
        }

        @Schema(example = "2")
        public Integer totalFilteredRecords;
        public Set<GetPageItemsStandingInstructionSwagger> pageItems;
    }

    @Schema(description = "PostStandingInstructionsRequest")
    public static final class PostStandingInstructionsRequest {

        private PostStandingInstructionsRequest() {}

        @Schema(example = "1")
        public Long fromOfficeId;
        @Schema(example = "1")
        public Long fromClientId;
        @Schema(example = "2")
        public Integer fromAccountType;
        @Schema(example = "standing instruction")
        public String name;
        @Schema(example = "1")
        public Integer transferType;
        @Schema(example = "2")
        public Integer priority;
        @Schema(example = "1")
        public Integer status;
        @Schema(example = "1")
        public Long fromAccountId;
        @Schema(example = "1")
        public Long toOfficeId;
        @Schema(example = "1")
        public Long toClientId;
        @Schema(example = "2")
        public Integer toAccountType;
        @Schema(example = "3")
        public Long toAccountId;
        @Schema(example = "1")
        public Integer instructionType;
        @Schema(example = "221")
        public Integer amount;
        @Schema(example = "08 April 2014")
        public String validFrom;
        @Schema(example = "1")
        public Integer recurrenceType;
        @Schema(example = "1")
        public Integer recurrenceInterval;
        @Schema(example = "2")
        public Integer recurrenceFrequency;
        @Schema(description = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "02 April")
        public String recurrenceOnMonthDay;
        @Schema(example = "dd MMMM")
        public String monthDayFormat;
    }

    @Schema(description = "GetStandingInstructionsStandingInstructionIdResponse")
    public static final class GetStandingInstructionsStandingInstructionIdResponse {

        private GetStandingInstructionsStandingInstructionIdResponse() {}

        @Schema(example = "1")
        public Long id;
        @Schema(example = "6")
        public Long accountDetailId;
        @Schema(example = "test standing")
        public String name;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetFromOfficeStandingInstructionSwagger fromOffice;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetFromClientStandingInstructionSwagger fromClient;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetFromAccountTypeStandingInstructionSwagger fromAccountType;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetFromAccountStandingInstructionSwagger fromAccount;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetToOfficeStandingInstructionSwagger toOffice;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetToClientStandingInstructionSwagger toClient;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetToAccountTypeStandingInstructionSwagger toAccountType;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetToAccountStandingInstructionSwagger toAccount;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetTransferTypeStandingInstructionSwagger transferType;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetPriorityStandingInstructionSwagger priority;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetInstructionTypeStandingInstructionSwagger instructionType;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetStatusStandingInstructionSwagger status;
        @Schema(example = "150.000000")
        public float amount;
        @Schema(example = "[2014, 4, 3]")
        public LocalDate validFrom;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetRecurrenceTypeStandingInstructionSwagger recurrenceType;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetRecurrenceFrequencyStandingInstructionSwagger recurrenceFrequency;
        @Schema(example = "1")
        public Integer recurrenceInterval;
        @Schema(example = "[4, 3]")
        public LocalDate recurrenceOnMonthDay;
    }

    @Schema(description = "PutStandingInstructionsStandingInstructionIdResponse")
    public static final class PutStandingInstructionsStandingInstructionIdResponse {

        private PutStandingInstructionsStandingInstructionIdResponse() {}

        static final class PutUpdateStandingInstructionChanges {

            @Schema(example = "2")
            public Integer recurrenceInterval;
        }

        @Schema(example = "20")
        public Integer resourceId;
        public PutUpdateStandingInstructionChanges changes;
    }

    @Schema(description = "PutStandingInstructionsStandingInstructionIdRequest")
    public static final class PutStandingInstructionsStandingInstructionIdRequest {

        private PutStandingInstructionsStandingInstructionIdRequest() {}

        @Schema(example = "2")
        public Integer recurrenceInterval;
    }
}
