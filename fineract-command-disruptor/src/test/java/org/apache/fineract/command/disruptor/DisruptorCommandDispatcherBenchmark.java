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
package org.apache.fineract.command.disruptor;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.command.disruptor.implementation.DisruptorCommandDispatcher;
import org.apache.fineract.command.implementation.DefaultCommandHandlerManager;
import org.apache.fineract.command.implementation.DefaultCommandHookManager;
import org.apache.fineract.command.test.sample.command.DummyCommand;
import org.apache.fineract.command.test.sample.data.DummyRequest;
import org.apache.fineract.command.test.sample.data.DummyResponse;
import org.apache.fineract.command.test.sample.handler.DummyCommandHandler;
import org.apache.fineract.command.test.sample.service.DefaultDummyService;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;

@Slf4j
// @BenchmarkMode(Mode.Throughput) // Measures operations per second
// @State(Scope.Benchmark) // Benchmark state for each thread
// @OutputTimeUnit(TimeUnit.SECONDS) // Output results in seconds
@SuppressWarnings({ "rawtypes" })
public class DisruptorCommandDispatcherBenchmark {

    private Disruptor<DisruptorCommandDispatcher.CommandEvent> disruptor;

    private CommandDispatcher dispatcher;

    @Setup(Level.Iteration)
    public void setUp() {
        // create hook manager
        var hookManager = new DefaultCommandHookManager(List.of(), List.of(), List.of());

        // create handler manager
        var handlerManager = new DefaultCommandHandlerManager(List.of(new DummyCommandHandler(new DefaultDummyService())));

        // create the disruptor
        this.disruptor = new Disruptor<>(DisruptorCommandDispatcher.CommandEvent::new, 2048, DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI, new YieldingWaitStrategy());

        disruptor.handleEventsWith(new DisruptorCommandDispatcher.CompleteableCommandEventHandler(hookManager, handlerManager));

        // start the disruptor
        disruptor.start();

        dispatcher = new DisruptorCommandDispatcher(disruptor);
    }

    @TearDown(Level.Iteration)
    @SneakyThrows
    public void tearDown() {
        disruptor.shutdown(1, TimeUnit.SECONDS);
    }

    @Benchmark
    public void processCommand() {
        var command = new DummyCommand();
        command.setPayload(DummyRequest.builder().content("hello").build());

        Supplier<DummyResponse> result = dispatcher.dispatch(command);

        // NOTE: force yield
        result.get();
    }
}
