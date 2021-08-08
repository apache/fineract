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

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import java.util.List;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;

/**
 * Created by sanyam on 13/8/17.
 */
@SuppressWarnings({ "MemberName" })
final class SurveyApiResourceSwagger {

    private SurveyApiResourceSwagger() {

    }

    @Schema(description = "GetSurveyResponse")
    public static final class GetSurveyResponse {

        private GetSurveyResponse() {

        }

        static final class GetSurveyResponseDatatableData {

            private GetSurveyResponseDatatableData() {

            }

            @Schema(example = "m_client")
            public String applicationTableName;
            @Schema(example = "ppi_kenya_2005")
            public String registeredTableName;
            public List<ResultsetColumnHeaderData> columnHeaderData;
        }

        public GetSurveyResponseDatatableData datatableData;
        @Schema(example = "false")
        public boolean enabled;
    }

    @Schema(description = "PostSurveySurveyNameApptableIdRequest")
    public static final class PostSurveySurveyNameApptableIdRequest {

        private PostSurveySurveyNameApptableIdRequest() {

        }

        @Schema(example = "167")
        public Long ppi_household_members_cd_q1_householdmembers;
        @Schema(example = "174")
        public Long ppi_highestschool_cd_q2_highestschool;
        @Schema(example = "180")
        public Long ppi_businessoccupation_cd_q3_businessoccupation;
        @Schema(example = "184")
        public Long ppi_habitablerooms_cd_q4_habitablerooms;
        @Schema(example = "188")
        public Long ppi_floortype_cd_q5_floortype;
        @Schema(example = "190")
        public Long ppi_lightingsource_cd_q6_lightingsource;
        @Schema(example = "193")
        public Long ppi_irons_cd_q7_irons;
        @Schema(example = "195")
        public Long ppi_mosquitonets_cd_q8_mosquitonets;
        @Schema(example = "198")
        public Long ppi_towels_cd_q9_towels;
        @Schema(example = "201")
        public Long ppi_fryingpans_cd_q10_fryingpans;
        @Schema(example = "2014-12-02 20:30:00")
        public ZonedDateTime Date;
        @Schema(example = "Y-m-d H:i:s")
        public ZonedDateTime dateFormat;
        @Schema(example = "en_GB")
        public String locale;
    }

    @Schema(description = "PostSurveySurveyNameApptableIdResponse")
    public static final class PostSurveySurveyNameApptableIdResponse {

        private PostSurveySurveyNameApptableIdResponse() {

        }

        @Schema(example = "2")
        public Long officeId;
        @Schema(example = "87")
        public Long clientId;
        @Schema(example = "87")
        public Long resourceId;
    }
}
