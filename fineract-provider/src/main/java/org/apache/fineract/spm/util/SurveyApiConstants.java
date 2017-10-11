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
package org.apache.fineract.spm.util;


public class SurveyApiConstants {
    
    public static final String SURVEY_RESOURCE_NAME = "survey";
    public static final String keyParamName = "key";
    public static final String nameParamName = "name";
    public static final String countryCodeParamName = "countrycode";
    public static final String descriptionParamName = "description";
    public static final String sequenceNumberParamName = "sequenceNo";
    public static final String valueParamName = "value";
    public static final String questionParamName = "question";
    public static final String optionsParamName = "options";
    public static final String textParamName = "text";
    public static final String lengthParamName = "length";
    
    //to validate length/max value  
    public static final Integer maxCountryCodeLength = 2;
    public static final Integer maxTextLength = 255;
    public static final Integer maxNameLength = 255;
    public static final Integer maxKeyLength = 32;
    public static final Integer maxOptionsValue = 9999;
    public static final Integer maxDescriptionLength = 4000;
    
}
