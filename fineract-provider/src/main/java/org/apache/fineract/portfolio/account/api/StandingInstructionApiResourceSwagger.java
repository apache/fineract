/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.account.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.Set;

/**
 * Created by Chirag Gupta on 11/29/17.
 */
final class StandingInstructionApiResourceSwagger {

    private StandingInstructionApiResourceSwagger() {
        // this class is only for Swagger Live Documentation
    }

    @ApiModel(value = "GetStandingInstructionsTemplateResponse")
    public static final class GetStandingInstructionsTemplateResponse {
        private GetStandingInstructionsTemplateResponse() {
        }

        final class GetFromOfficeResponseStandingInstructionSwagger {
            private GetFromOfficeResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "1")
            public Long id;
            @ApiModelProperty(example = "Head Office")
            public String name;
            @ApiModelProperty(example = "Head Office")
            public String decoratedName;
            @ApiModelProperty(example = "1")
            public Integer externalId;
            @ApiModelProperty(example = "[2009, 1, 1]")
            public LocalDate openingDate;
            @ApiModelProperty(example = ".")
            public String hierarchy;
        }

        final class GetFromAccountTypeResponseStandingInstructionSwagger {
            private GetFromAccountTypeResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "2")
            public Long id;
            @ApiModelProperty(example = "accountType.savings")
            public String code;
            @ApiModelProperty(example = "Savings Account")
            public String value;
        }

        final class GetFromOfficeOptionsResponseStandingInstructionSwagger {
            private GetFromOfficeOptionsResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "1")
            public Long id;
            @ApiModelProperty(example = "Head Office")
            public String name;
            @ApiModelProperty(example = "Head Office")
            public String nameDecorated;
        }

        final class GetFromClientOptionsResponseStandingInstructionSwagger {
            private GetFromClientOptionsResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "1")
            public Long id;
            @ApiModelProperty(example = "Client_FirstName_2VRAG Client_LastName_9QCY")
            public String displayName;
            @ApiModelProperty(example = "1")
            public Long officeId;
            @ApiModelProperty(example = "Head Office")
            public String officeName;
        }

        final class GetFromAccountTypeOptionsResponseStandingInstructionSwagger {
            private GetFromAccountTypeOptionsResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "2")
            public Long id;
            @ApiModelProperty(example = "accountType.savings")
            public String code;
            @ApiModelProperty(example = "Savings Account")
            public String value;
        }

        final class GetToOfficeOptionsResponseStandingInstructionSwagger {
            private GetToOfficeOptionsResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "Head Office")
            public String name;
            @ApiModelProperty(example = "Head Office")
            public String nameDecorated;
        }

        final class GetToAccountTypeOptionsResponseStandingInstructionSwagger {
            private GetToAccountTypeOptionsResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "accountType.loan")
            public String code;
            @ApiModelProperty(example = "Loan Account")
            public String value;
        }

        final class GetTransferTypeOptionsResponseStandingInstructionSwagger {
            private GetTransferTypeOptionsResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "accountTransferType.account.transfer")
            public String code;
            @ApiModelProperty(example = "Account Transfer")
            public String value;
        }

        final class GetStatusOptionsResponseStandingInstructionSwagger {
            private GetStatusOptionsResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "standingInstructionStatus.active")
            public String code;
            @ApiModelProperty(example = "Active")
            public String value;
        }

        final class GetInstructionTypeOptionsResponseStandingInstructionSwagger {
            private GetInstructionTypeOptionsResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "standingInstructionType.fixed")
            public String code;
            @ApiModelProperty(example = "Fixed")
            public String value;
        }

        final class GetPriorityOptionsResponseStandingInstructionSwagger {
            private GetPriorityOptionsResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "standingInstructionPriority.urgent")
            public String code;
            @ApiModelProperty(example = "Urgent Priority")
            public String value;
        }

        final class GetRecurrenceTypeOptionsResponseStandingInstructionSwagger {
            private GetRecurrenceTypeOptionsResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "1")
            public Integer id;
            @ApiModelProperty(example = "accountTransferRecurrenceType.periodic")
            public String code;
            @ApiModelProperty(example = "Periodic Recurrence")
            public String value;
        }

        final class GetRecurrenceFrequencyOptionsResponseStandingInstructionSwagger {
            private GetRecurrenceFrequencyOptionsResponseStandingInstructionSwagger() {
            }

            @ApiModelProperty(example = "0")
            public Integer id;
            @ApiModelProperty(example = "frequencyperiodFrequencyType.days")
            public String code;
            @ApiModelProperty(example = "Days")
            public String value;
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

    @ApiModel(value = "PostStandingInstructionsResponse")
    public static final class PostStandingInstructionsResponse {
        private PostStandingInstructionsResponse() {
        }

        @ApiModelProperty(example = "1")
        public Long clientId;
        @ApiModelProperty(example = "65")
        public Integer resourceId;
    }

    @ApiModel(value = "GetStandingInstructionsResponse")
    public static final class GetStandingInstructionsResponse {
        private GetStandingInstructionsResponse() {
        }

        final class GetPageItemsStandingInstructionSwagger {
            final class GetFromOfficeStandingInstructionSwagger {
                private GetFromOfficeStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "1")
                public Long id;
                @ApiModelProperty(example = "Head Office")
                public String name;
            }

            final class GetFromClientStandingInstructionSwagger {
                private GetFromClientStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "1")
                public Long id;
                @ApiModelProperty(example = "Test test")
                public String displayName;
                @ApiModelProperty(example = "1")
                public Long officeId;
                @ApiModelProperty(example = "Head Office")
                public String officeName;
            }

            final class GetFromAccountTypeStandingInstructionSwagger {
                private GetFromAccountTypeStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "accountType.savings")
                public String code;
                @ApiModelProperty(example = "Savings Account")
                public String value;
            }

            final class GetFromAccountStandingInstructionSwagger {
                private GetFromAccountStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "14")
                public Long id;
                @ApiModelProperty(example = "000000014")
                public Long accountNo;
                @ApiModelProperty(example = "1")
                public Long productId;
                @ApiModelProperty(example = "savings old")
                public String productName;
            }

            final class GetToOfficeStandingInstructionSwagger {
                private GetToOfficeStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "1")
                public Long id;
                @ApiModelProperty(example = "Head Office")
                public String name;
            }

            final class GetToClientStandingInstructionSwagger {
                private GetToClientStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "1")
                public Long id;
                @ApiModelProperty(example = "Test test")
                public String displayName;
                @ApiModelProperty(example = "1")
                public Long officeId;
                @ApiModelProperty(example = "Head Office")
                public String officeName;
            }

            final class GetToAccountTypeStandingInstructionSwagger {
                private GetToAccountTypeStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "accountType.savings")
                public String code;
                @ApiModelProperty(example = "Savings Account")
                public String value;
            }

            final class GetToAccountStandingInstructionSwagger {
                private GetToAccountStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "3")
                public Long id;
                @ApiModelProperty(example = "000000003")
                public Long accountNo;
                @ApiModelProperty(example = "4")
                public Long productId;
                @ApiModelProperty(example = "account overdraft")
                public String productName;
            }

            final class GetTransferTypeStandingInstructionSwagger {
                private GetTransferTypeStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "accountTransferType.account.transfer")
                public String code;
                @ApiModelProperty(example = "Account Transfer")
                public String value;
            }

            final class GetPriorityStandingInstructionSwagger {
                private GetPriorityStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "3")
                public Integer id;
                @ApiModelProperty(example = "standingInstructionPriority.medium")
                public String code;
                @ApiModelProperty(example = "Medium Priority")
                public String value;
            }

            final class GetInstructionTypeStandingInstructionSwagger {
                private GetInstructionTypeStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "standingInstructionType.fixed")
                public String code;
                @ApiModelProperty(example = "Fixed")
                public String value;
            }

            final class GetStatusStandingInstructionSwagger {
                private GetStatusStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "standingInstructionStatus.deleted")
                public String code;
                @ApiModelProperty(example = "Deleted")
                public String value;
            }

            final class GetRecurrenceTypeStandingInstructionSwagger {
                private GetRecurrenceTypeStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "1")
                public Integer id;
                @ApiModelProperty(example = "accountTransferRecurrenceType.periodic")
                public String code;
                @ApiModelProperty(example = "Periodic Recurrence")
                public String value;
            }

            final class GetRecurrenceFrequencyStandingInstructionSwagger {
                private GetRecurrenceFrequencyStandingInstructionSwagger() {
                }

                @ApiModelProperty(example = "2")
                public Integer id;
                @ApiModelProperty(example = "frequencyperiodFrequencyType.months")
                public String code;
                @ApiModelProperty(example = "Months")
                public String value;
            }

            @ApiModelProperty(example = "1")
            public Long id;
            @ApiModelProperty(example = "6")
            public Long accountDetailId;
            @ApiModelProperty(example = "test standing")
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
            @ApiModelProperty(example = "150.000000")
            public float amount;
            @ApiModelProperty(example = "[2014, 4, 3]")
            public LocalDate validFrom;
            public GetRecurrenceTypeStandingInstructionSwagger recurrenceType;
            public GetRecurrenceFrequencyStandingInstructionSwagger recurrenceFrequency;
            @ApiModelProperty(example = "1")
            public Integer recurrenceInterval;
            @ApiModelProperty(example = "[4, 3]")
            public LocalDate recurrenceOnMonthDay;
        }

        @ApiModelProperty(example = "2")
        public Integer totalFilteredRecords;
        public Set<GetPageItemsStandingInstructionSwagger> pageItems;
    }

    @ApiModel(value = "PostStandingInstructionsRequest")
    public static final class PostStandingInstructionsRequest {
        private PostStandingInstructionsRequest() {
        }

        @ApiModelProperty(example = "1")
        public Long fromOfficeId;
        @ApiModelProperty(example = "1")
        public Long fromClientId;
        @ApiModelProperty(example = "2")
        public Integer fromAccountType;
        @ApiModelProperty(example = "standing instruction")
        public String name;
        @ApiModelProperty(example = "1")
        public Integer transferType;
        @ApiModelProperty(example = "2")
        public Integer priority;
        @ApiModelProperty(example = "1")
        public Integer status;
        @ApiModelProperty(example = "1")
        public Long fromAccountId;
        @ApiModelProperty(example = "1")
        public Long toOfficeId;
        @ApiModelProperty(example = "1")
        public Long toClientId;
        @ApiModelProperty(example = "2")
        public Integer toAccountType;
        @ApiModelProperty(example = "3")
        public Long toAccountId;
        @ApiModelProperty(example = "1")
        public Integer instructionType;
        @ApiModelProperty(example = "221")
        public Integer amount;
        @ApiModelProperty(example = "08 April 2014")
        public String validFrom;
        @ApiModelProperty(example = "1")
        public Integer recurrenceType;
        @ApiModelProperty(example = "1")
        public Integer recurrenceInterval;
        @ApiModelProperty(example = "2")
        public Integer recurrenceFrequency;
        @ApiModelProperty(value = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "02 April")
        public String recurrenceOnMonthDay;
        @ApiModelProperty(example = "dd MMMM")
        public String monthDayFormat;
    }

    @ApiModel(value = "GetStandingInstructionsStandingInstructionIdResponse")
    public static final class GetStandingInstructionsStandingInstructionIdResponse {
        private GetStandingInstructionsStandingInstructionIdResponse() {
        }

        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "6")
        public Long accountDetailId;
        @ApiModelProperty(example = "test standing")
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
        @ApiModelProperty(example = "150.000000")
        public float amount;
        @ApiModelProperty(example = "[2014, 4, 3]")
        public LocalDate validFrom;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetRecurrenceTypeStandingInstructionSwagger recurrenceType;
        public GetStandingInstructionsResponse.GetPageItemsStandingInstructionSwagger.GetRecurrenceFrequencyStandingInstructionSwagger recurrenceFrequency;
        @ApiModelProperty(example = "1")
        public Integer recurrenceInterval;
        @ApiModelProperty(example = "[4, 3]")
        public LocalDate recurrenceOnMonthDay;
    }

    @ApiModel(value = "PutStandingInstructionsStandingInstructionIdResponse")
    public static final class PutStandingInstructionsStandingInstructionIdResponse {
        private PutStandingInstructionsStandingInstructionIdResponse() {
        }
        final class PutUpdateStandingInstructionChanges{
            @ApiModelProperty(example = "2")
            public Integer recurrenceInterval;
        }
        @ApiModelProperty(example = "20")
        public Integer resourceId;
        public PutUpdateStandingInstructionChanges changes;
    }

    @ApiModel(value = "PutStandingInstructionsStandingInstructionIdRequest")
    public static final class PutStandingInstructionsStandingInstructionIdRequest{
        private PutStandingInstructionsStandingInstructionIdRequest(){}
        @ApiModelProperty(example = "2")
        public Integer recurrenceInterval;
    }
}
