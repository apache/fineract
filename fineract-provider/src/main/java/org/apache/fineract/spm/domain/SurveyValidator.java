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
package org.apache.fineract.spm.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.spm.util.SurveyApiConstants;
import org.springframework.stereotype.Component;

@Component
public class SurveyValidator {

    public void validate(final Survey survey) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SurveyApiConstants.SURVEY_RESOURCE_NAME);

        baseDataValidator.reset().parameter(SurveyApiConstants.keyParamName).value(survey.getKey()).notNull().notBlank()
                .notExceedingLengthOf(SurveyApiConstants.maxKeyLength);

        baseDataValidator.reset().parameter(SurveyApiConstants.nameParamName).value(survey.getName()).notNull().notBlank()
                .notExceedingLengthOf(SurveyApiConstants.maxNameLength);

        baseDataValidator.reset().parameter(SurveyApiConstants.countryCodeParamName).value(survey.getCountryCode()).notNull().notBlank()
                .notExceedingLengthOf(SurveyApiConstants.maxCountryCodeLength);
        baseDataValidator.reset().parameter(SurveyApiConstants.descriptionParamName).value(survey.getDescription()).ignoreIfNull()
                .notExceedingLengthOf(SurveyApiConstants.maxDescriptionLength);
        List<Question> questions = survey.getQuestions();
        baseDataValidator.reset().parameter(SurveyApiConstants.questionParamName).value(questions).notNull();
        validateQuestions(baseDataValidator, questions);
        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private void validateQuestions(final DataValidatorBuilder baseDataValidator, List<Question> questions) {
        if (questions != null) {
            baseDataValidator.reset().parameter(SurveyApiConstants.questionParamName + "." + SurveyApiConstants.lengthParamName)
                    .value(questions.toArray()).arrayNotEmpty();
            for (Question question : questions) {
                baseDataValidator.reset().parameter(SurveyApiConstants.questionParamName + "." + SurveyApiConstants.keyParamName)
                        .value(question.getKey()).notNull().notExceedingLengthOf(SurveyApiConstants.maxKeyLength);
                baseDataValidator.reset().parameter(SurveyApiConstants.questionParamName + "." + SurveyApiConstants.textParamName)
                        .value(question.getText()).notNull().notExceedingLengthOf(SurveyApiConstants.maxTextLength);
                baseDataValidator.reset().parameter(SurveyApiConstants.questionParamName + "." + SurveyApiConstants.descriptionParamName)
                        .value(question.getDescription()).ignoreIfNull().notExceedingLengthOf(SurveyApiConstants.maxDescriptionLength);
                validateOptions(baseDataValidator, question);

            }
        }
    }

    private void validateOptions(final DataValidatorBuilder baseDataValidator, Question question) {
        List<Response> responses = question.getResponses();
        baseDataValidator.reset().parameter(SurveyApiConstants.questionParamName + "." + SurveyApiConstants.optionsParamName)
                .value(responses).notNull();
        if (responses != null) {
            baseDataValidator.reset().parameter(SurveyApiConstants.questionParamName + "." + SurveyApiConstants.optionsParamName)
                    .value(responses.toArray()).arrayNotEmpty();
            for (Response response : responses) {
                baseDataValidator.reset().parameter(SurveyApiConstants.optionsParamName + "." + SurveyApiConstants.textParamName)
                        .value(response.getText()).notNull().notExceedingLengthOf(SurveyApiConstants.maxTextLength);
                baseDataValidator.reset().parameter(SurveyApiConstants.optionsParamName + "." + SurveyApiConstants.valueParamName)
                        .value(response.getValue()).notNull().notGreaterThanMax(SurveyApiConstants.maxOptionsValue);
            }
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
