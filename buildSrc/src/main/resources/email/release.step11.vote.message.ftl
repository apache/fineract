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
<#if (project['fineract.vote'].approve.binding?size + project['fineract.vote'].approve.nonBinding?size > project['fineract.vote'].disapprove.binding?size + project['fineract.vote'].disapprove.nonBinding?size)>
Voting is now closed and has passed with the following tally,

Binding +1s: ${project['fineract.vote'].approve.binding?size}
Non binding +1s: ${project['fineract.vote'].approve.nonBinding?size}
<#else>
Voting is now closed and has not passed with the following tally,

Binding +1s: ${project['fineract.vote'].approve.binding?size}
Non binding +1s: ${project['fineract.vote'].approve.nonBinding?size}

Binding -1s: ${project['fineract.vote'].disapprove.binding?size}
Non binding -1s: ${project['fineract.vote'].disapprove.nonBinding?size}
</#if>

Here are the detailed results:

<#list project['fineract.vote'].approve.binding>
Binding +1s:
    <#items as item>
- ${item.name} (${item.email})
    </#items>
</#list>


<#list project['fineract.vote'].approve.nonBinding>
Non binding +1s:
    <#items as item>
- ${item.name} (${item.email})
    </#items>
</#list>


<#list project['fineract.vote'].disapprove.binding>
Binding -1s:
    <#items as item>
- ${item.name} (${item.email})
    </#items>
</#list>

<#list project['fineract.vote'].disapprove.nonBinding>
Non binding -1s:
    <#items as item>
- ${item.name} (${item.email})
    </#items>
</#list>


<#list project['fineract.vote'].noOpinion.binding>
Binding +0s:
    <#items as item>
- ${item.name} (${item.email})
    </#items>
</#list>

<#list project['fineract.vote'].noOpinion.nonBinding>
Non binding +0s:
    <#items as item>
- ${item.name} (${item.email})
    </#items>
</#list>

<#if (project['fineract.vote'].approve.binding?size + project['fineract.vote'].approve.nonBinding?size > project['fineract.vote'].disapprove.binding?size + project['fineract.vote'].disapprove.nonBinding?size)>
Thanks to everyone who voted! I'll now continue with the rest of the release process.
<#else>
Thanks to everyone who voted! Looks like we have to repeat the vote.
</#if>

${project['fineract.config.name']}
