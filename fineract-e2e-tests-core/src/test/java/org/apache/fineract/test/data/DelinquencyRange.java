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
package org.apache.fineract.test.data;

public enum DelinquencyRange {

    NO_DELINQUENCY("NO_DELINQUENCY"), RANGE_1("Delinquency range 1"), RANGE_3("Delinquency range 3"), RANGE_30(
            "Delinquency range 30"), RANGE_60("Delinquency range 60"), RANGE_90("Delinquency range 90"), RANGE_120(
                    "Delinquency range 120"), RANGE_150("Delinquency range 150"), RANGE_180(
                            "Delinquency range 180"), RANGE_210("Delinquency range 210"), RANGE_240("Delinquency range 240");

    public final String value;

    DelinquencyRange(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
