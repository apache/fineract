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
package org.apache.fineract.portfolio.account.data;

/**
 * One partition of the due standing-instruction set, expressed as an inclusive range of source-account keys.
 *
 * <p>
 * Partitions are cut over <em>distinct</em> source accounts, never over instructions, so every instruction debiting a
 * given account falls in exactly one partition and is processed sequentially there. That is what keeps concurrent
 * partitions off the same {@code m_savings_account} row.
 * </p>
 *
 * @param minAccountKey
 *            lowest source-account key in the partition (inclusive)
 * @param maxAccountKey
 *            highest source-account key in the partition (inclusive)
 * @param pageNumber
 *            zero-based ordinal of the partition, used to build its name
 * @param instructionCount
 *            number of due instructions the partition covers, for logging only
 */
public record StandingInstructionPartition(Long minAccountKey, Long maxAccountKey, Long pageNumber, Long instructionCount) {
}
