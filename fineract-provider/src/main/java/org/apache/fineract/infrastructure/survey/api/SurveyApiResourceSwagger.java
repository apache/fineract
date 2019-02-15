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
package org.apache.fineract.infrastructure.survey.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by sanyam on 13/8/17.
 */
final class SurveyApiResourceSwagger {
    private SurveyApiResourceSwagger(){

    }

    @ApiModel(value = "GetSurveyResponse")
    public static final class GetSurveyResponse {
        private GetSurveyResponse(){

        }
        final class GetSurveyResponseDatatableData {
            private GetSurveyResponseDatatableData() {

            }
            @ApiModelProperty(example = "m_client")
            public String applicationTableName;
            @ApiModelProperty(example = "ppi_kenya_2005")
            public String registeredTableName;
            public List<ResultsetColumnHeaderData> columnHeaderData;
        }
        public GetSurveyResponseDatatableData datatableData;
        @ApiModelProperty(example = "false")
        public boolean enabled;
    }

    @ApiModel(value = "PostSurveySurveyNameApptableIdRequest")
    public static final class PostSurveySurveyNameApptableIdRequest {
        private PostSurveySurveyNameApptableIdRequest() {

        }
        @ApiModelProperty(example = "167")
        public Long ppi_household_members_cd_q1_householdmembers;
        @ApiModelProperty(example = "174")
        public Long ppi_highestschool_cd_q2_highestschool;
        @ApiModelProperty(example = "180")
        public Long ppi_businessoccupation_cd_q3_businessoccupation;
        @ApiModelProperty(example = "184")
        public Long ppi_habitablerooms_cd_q4_habitablerooms;
        @ApiModelProperty(example = "188")
        public Long ppi_floortype_cd_q5_floortype;
        @ApiModelProperty(example = "190")
        public Long ppi_lightingsource_cd_q6_lightingsource;
        @ApiModelProperty(example = "193")
        public Long ppi_irons_cd_q7_irons;
        @ApiModelProperty(example = "195")
        public Long ppi_mosquitonets_cd_q8_mosquitonets;
        @ApiModelProperty(example = "198")
        public Long ppi_towels_cd_q9_towels;
        @ApiModelProperty(example = "201")
        public Long ppi_fryingpans_cd_q10_fryingpans;
        @ApiModelProperty(example = "2014-12-02 20:30:00")
        public DateTime Date;
        @ApiModelProperty(example = "Y-m-d H:i:s")
        public DateTime dateFormat;
        @ApiModelProperty(example = "en_GB")
        public String locale;
    }


    @ApiModel(value = "PostSurveySurveyNameApptableIdResponse")
    public static final class PostSurveySurveyNameApptableIdResponse {
        private PostSurveySurveyNameApptableIdResponse() {

        }
        @ApiModelProperty(example = "2")
        public Long officeId;
        @ApiModelProperty(example = "87")
        public Long clientId;
        @ApiModelProperty(example = "87")
        public Long resourceId;
    }
}
