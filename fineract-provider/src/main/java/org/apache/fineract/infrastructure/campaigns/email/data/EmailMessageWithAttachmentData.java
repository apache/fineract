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
package org.apache.fineract.infrastructure.campaigns.email.data;

import java.io.File;
import java.util.List;
import java.util.Set;

public class EmailMessageWithAttachmentData {

    private final String to;
    private final String text;
    private final String subject;
    private final List<File> attachments;

    private EmailMessageWithAttachmentData(final String to, final String text, final String subject, final List<File> attachments) {
        this.to = to;
        this.text = text;
        this.subject = subject;
        this.attachments = attachments;
    }


    public static EmailMessageWithAttachmentData createNew (final String to, final String text, final String subject, final List<File> attachments){
        return new EmailMessageWithAttachmentData(to,text,subject,attachments);
    }

    public String getTo() {return this.to;}

    public String getText() {return this.text;}

    public String getSubject() {return this.subject;}

    public List<File> getAttachments() {
        return this.attachments;
    }
}
