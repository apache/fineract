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
package org.apache.fineract.infrastructure.reportmailingjob.data;

import java.io.File;

/** 
 * Immutable data object representing report mailing job email data. 
 **/
public class ReportMailingJobEmailData {
    private final String to;
    private final String text;
    private final String subject;
    private final File attachment;
    
    public ReportMailingJobEmailData(final String to, final String text, final String subject, final File attachment) {
        this.to = to;
        this.text = text;
        this.subject = subject;
        this.attachment = attachment;
    }

    /**
     * @return the to
     */
    public String getTo() {
        return to;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @return the attachment
     */
    public File getAttachment() {
        return attachment;
    }
}
