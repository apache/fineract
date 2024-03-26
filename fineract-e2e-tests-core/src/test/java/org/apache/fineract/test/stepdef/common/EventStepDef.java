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
package org.apache.fineract.test.stepdef.common;

import io.cucumber.java.en.Then;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.Event;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class EventStepDef extends AbstractStepDef {

    @Autowired
    private EventAssertion eventAssertion;

    @Then("{string} event has been raised for the loan")
    public void assertEventRaisedForLoan(String eventType) {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Class<? extends Event> eventTypeClazz = resolveEventType(eventType);
        eventAssertion.assertEventRaised(eventTypeClazz, loanId);
    }

    @Then("No new event with type {string} has been raised for the loan")
    public void assertEventNotRaisedForLoan(String eventType) {
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        Class<? extends Event> eventTypeClazz = resolveEventType(eventType);
        eventAssertion.assertEventNotRaised(eventTypeClazz, loanId);
    }

    private Class<? extends Event> resolveEventType(String eventType) {
        String fullyQualifiedEventClassName = null;
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages("org.apache.fineract.test").scan()) {
            ClassInfoList matchingClasses = scanResult.getAllClasses().filter(i -> i.getSimpleName().equalsIgnoreCase(eventType));
            if (matchingClasses.isEmpty()) {
                throw new IllegalArgumentException("Cannot find event with type name: " + eventType);
            }
            fullyQualifiedEventClassName = matchingClasses.get(0).getName();
        }

        Class<? extends Event> eventTypeClazz = null;
        try {
            eventTypeClazz = (Class<? extends Event>) Class.forName(fullyQualifiedEventClassName);
        } catch (Exception e) {
            throw new RuntimeException("Cannot find event with type name: " + eventType, e);
        }
        return eventTypeClazz;
    }
}
