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

import org.apache.fineract.spm.data.ComponentData;
import org.apache.fineract.spm.data.QuestionData;
import org.apache.fineract.spm.data.ResponseData;
import org.apache.fineract.spm.data.SurveyData;
import org.apache.fineract.spm.domain.Component;
import org.apache.fineract.spm.domain.Question;
import org.apache.fineract.spm.domain.Response;
import org.apache.fineract.spm.domain.Survey;

import java.util.ArrayList;
import java.util.List;

public class SurveyMapper {

    private SurveyMapper() {
        super();
    }

    public static SurveyData map(final Survey survey) {
        final SurveyData surveyData = new SurveyData(
                survey.getId(), SurveyMapper.mapComponents(survey.getComponents()),
                SurveyMapper.mapQuestions(survey.getQuestions()), survey.getKey(), survey.getName(),
                survey.getDescription(), survey.getCountryCode(), survey.getValidFrom(), survey.getValidTo()
        );
        return surveyData;
    }

    public static Survey map(final SurveyData surveyData, Survey survey) {        
        survey.setComponents(SurveyMapper.mapComponentDatas(surveyData.getComponentDatas(), survey));
        survey.setQuestions(SurveyMapper.mapQuestionDatas(surveyData.getQuestionDatas(), survey));
        survey.setKey(surveyData.getKey());
        survey.setName(surveyData.getName());
        survey.setDescription(surveyData.getDescription());
        survey.setCountryCode(surveyData.getCountryCode());
        return survey;
    }

    private static List<ComponentData> mapComponents(final List<Component> components) {
        final List<ComponentData> componentDatas = new ArrayList<>();
        if (components != null) {
            for (final Component component : components) {
                componentDatas.add(new ComponentData(
                        component.getId(), component.getKey(), component.getText(), component.getDescription(),
                        component.getSequenceNo()
                ));
            }
        }
        return componentDatas;
    }

    private static List<Component> mapComponentDatas(final List<ComponentData> componentDatas, final Survey survey) {
        final List<Component> components = new ArrayList<>();
        if (componentDatas != null) {
            for (final ComponentData componentData : componentDatas) {
                final Component component = new Component();
                component.setSurvey(survey);
                component.setKey(componentData.getKey());
                component.setText(componentData.getText());
                component.setDescription(componentData.getDescription());
                component.setSequenceNo(componentData.getSequenceNo());
                components.add(component);
            }
        }
        return components;
    }

    private static List<QuestionData> mapQuestions(final List<Question> questions) {
        final List<QuestionData> questionDatas = new ArrayList<>();
        if (questions != null) {
            for (final Question question : questions) {
                questionDatas.add(new QuestionData(question.getId(),
                        SurveyMapper.mapResponses(question.getResponses()), question.getComponentKey(), question.getKey(),
                        question.getText(), question.getDescription(), question.getSequenceNo()
                ));
            }
        }
        return questionDatas;
    }

    private static List<Question> mapQuestionDatas(final List<QuestionData> questionDatas, final Survey survey) {
        final List<Question> questions = new ArrayList<>();
        if (questionDatas != null) {
            for (final QuestionData questionData : questionDatas) {
                final Question question = new Question();
                question.setSurvey(survey);
                question.setComponentKey(questionData.getComponentKey());
                question.setResponses(SurveyMapper.mapResponseDatas(questionData.getResponseDatas(), question));
                question.setKey(questionData.getKey());
                question.setText(questionData.getText());
                question.setDescription(question.getDescription());
                question.setSequenceNo(questionData.getSequenceNo());
                questions.add(question);
            }
        }
        return questions;
    }

    private static List<ResponseData> mapResponses(final List<Response> responses) {
        final List<ResponseData> responseDatas = new ArrayList<>();
        if (responses != null) {
            for (final Response response : responses) {
                responseDatas.add(new ResponseData(
                    response.getId(), response.getText(), response.getValue(), response.getSequenceNo()
                ));
            }
        }
        return responseDatas;
    }

    private static List<Response> mapResponseDatas(final List<ResponseData> responseDatas, final Question question) {
        final List<Response> responses = new ArrayList<>();
        if (responseDatas != null) {
            for (final ResponseData responseData : responseDatas) {
                final Response response = new Response();
                response.setQuestion(question);
                response.setText(responseData.getText());
                response.setValue(responseData.getValue());
                response.setSequenceNo(responseData.getSequenceNo());
                responses.add(response);
            }
        }
        return responses;
    }
}
