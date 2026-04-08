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
package org.apache.fineract.infrastructure.contentstore.util;

import static java.util.Objects.requireNonNullElse;
import static org.apache.fineract.infrastructure.contentstore.processor.ContentProcessor.BEAN_NAME_EXECUTOR;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.contentstore.exception.ContentProcessorException;
import org.apache.fineract.infrastructure.contentstore.processor.ContentProcessor;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public final class ContentPipe {

    private final ExecutorService executor;
    private final FineractProperties properties;

    public ContentPipe(@Qualifier(BEAN_NAME_EXECUTOR) ExecutorService executor, FineractProperties properties) {
        this.executor = executor;
        this.properties = properties;
    }

    public InputStream pipe(ContentProcessor.OutputStreamConsumer consumer) {
        var pis = new PipedInputStream(requireNonNullElse(properties.getContent().getDefaultBufferSize(), 8192));
        var pos = newPipedOutputStream(pis);

        var future = executor.submit(() -> {
            try (var os = pos) {
                consumer.accept(os);
            } catch (Throwable e) {
                // if an error occurs, the pipe will close
                throw new ContentProcessorException(new IOException(e));
            }
        });

        return new FutureInputStream(pis, future);
    }

    public InputStream pipe(InputStream inputStream, ContentProcessor.InputOutputStreamConsumer consumer) {
        // Java default buffer is 1024 bytes
        var pis = new PipedInputStream(requireNonNullElse(properties.getContent().getDefaultBufferSize(), 8192));
        var pos = newPipedOutputStream(pis);

        // submit the work to the executor
        var future = executor.submit(() -> {
            try (var os = pos; var is = inputStream) {
                // run the user logic (e.g., zipping, resizing)
                consumer.accept(is, os);
                // flushing is handled by try-with-resources close()
            } catch (Throwable e) {
                // if an error occurs, the pipe will close
                throw new ContentProcessorException(new IOException(e));
            }
        });

        // tracks errors
        return new FutureInputStream(pis, future);
    }

    public void write(InputStream is, OutputStream os, byte[] buffer) throws IOException {
        int bytesRead;

        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
    }

    private PipedOutputStream newPipedOutputStream(PipedInputStream pis) {
        try {
            return new PipedOutputStream(pis);
        } catch (IOException e) {
            throw new ContentProcessorException(e);
        }
    }

    private static class FutureInputStream extends InputStream {

        private final PipedInputStream delegate;
        private final Future<?> future;

        FutureInputStream(PipedInputStream delegate, Future<?> future) {
            this.delegate = delegate;
            this.future = future;
        }

        @SuppressWarnings("AvoidHidingCauseException")
        private void checkException() throws IOException {
            // if the task is done, check for exceptions
            if (future.isDone()) {
                try {
                    // throws "ExecutionException" if task failed
                    future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    throw new IOException("Processing interrupted", e);
                } catch (ExecutionException e) {
                    // unwrap cause
                    Throwable cause = e.getCause();

                    if (cause instanceof IOException ioe) {
                        throw ioe;
                    }

                    throw new IOException("Processing failed in worker thread", cause);
                }
            }
        }

        @Override
        public int read() throws IOException {
            checkException();

            int data = delegate.read();

            // check EOF because of error
            checkException();

            return data;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            checkException();

            int count = delegate.read(b, off, len);

            checkException();

            return count;
        }

        @Override
        public void close() throws IOException {
            delegate.close();

            // good practice
            future.cancel(true);
        }
    }
}
