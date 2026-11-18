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
package org.apache.fineract.command.jdbc.store.domain;

import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface JdbcCommandRepository extends ListCrudRepository<CommandEntity, Long>, QueryByExampleExecutor<CommandEntity> {

    Optional<CommandEntity> findOneByIdempotencyKeyAndCustomIdempotencyDistinctor(String idempotencyKey,
            String customIdempotencyDistinctor);

    boolean existsByIdempotencyKeyAndCustomIdempotencyDistinctor(String idempotencyKey, String customIdempotencyDistinctor);

    /**
     * Records what the original request was answered with, but only for a command that stored no result of its own.
     *
     * <p>
     * A command that did store a result needs none of this: its repeat is answered by handing that result back through
     * the dispatcher, so the resource and the web layer produce the status and the bytes exactly as they produced them
     * the first time. Recording anything for it would be dead weight, which is why the update excludes it rather than
     * overwriting nothing. What is left is the case with no result to hand back - a failure, whose answer is rendered
     * from an exception, and an answer that is not JSON - and there the status has to be recorded too, because a replay
     * of it is thrown rather than returned and nothing else can tell the mapper what status to use.
     */
    @Modifying
    @Query("""
            UPDATE m_command
            SET response_status_code = :statusCode, response = :body
            WHERE idempotency_key = :idempotencyKey AND custom_idempotency_distinctor = :distinctor AND response IS NULL
            """)
    int updateResponse(@Param("idempotencyKey") String idempotencyKey, @Param("distinctor") String distinctor,
            @Param("statusCode") int statusCode, @Param("body") String body);

    /**
     * Reads the stored response as the exact text it was stored as, for the fallback case where there is no result
     * object to rehydrate. Going through the mapped JSON node would re-render it, and re-rendering changes the bytes: a
     * decimal written as 1.50 comes back as 1.5.
     */
    @Query("SELECT response FROM m_command WHERE idempotency_key = :idempotencyKey AND custom_idempotency_distinctor = :distinctor")
    Optional<String> findResponseTextByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey,
            @Param("distinctor") String distinctor);

    /**
     * Reads the stored request as the exact text it was stored as, for comparing a retry against it. Comparing parsed
     * nodes instead would collapse a decimal to a double, so two amounts differing beyond a double's precision would
     * compare equal and the wrong answer would be replayed.
     */
    @Query("SELECT request FROM m_command WHERE idempotency_key = :idempotencyKey AND custom_idempotency_distinctor = :distinctor")
    Optional<String> findRequestTextByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey,
            @Param("distinctor") String distinctor);
}
