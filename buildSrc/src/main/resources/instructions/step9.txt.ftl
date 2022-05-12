<#--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements. See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership. The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

-->


[INSTRUCTIONS:START]

Following are the typical things we need to verify before voting on a release candidate. And the release manager should verify them too before calling out a vote.

Make sure release artifacts are hosted at https://dist.apache.org/repos/dist/dev/fineract

* Release candidates should be in format apache-fineract-${project['fineract.release.version']}-binary.tar.gz
* Verify signatures and hashes. You may have to import the public key of the release manager to verify the signatures. (`gpg --recv-key <key id>`)
* Git tag matches the released bits (diff -rf)
* Can compile successfully from source
* Verify DISCLAIMER, NOTICE and LICENSE (year etc)
* All files have correct headers (Rat check should be clean - gradlew rat)
* No jar files in the source artifacts
* Integration tests should work

[INSTRUCTIONS:END]
