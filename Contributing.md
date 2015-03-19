# Getting started with Mifos X
Instructions for getting started are located at https://mifosforge.jira.com/wiki/display/MIFOSX/Getting+started+-+Contributing+to+MifosX

# Pull Request Checklist
Before sending out a pull request, please ensure that the following pre-requisites are taken care of

1. API documentation has been updated to reflect any changes made to the API (new API's, adding new Fields or Options to existing API etc)

1. Any relevant database changes are encapsulated in an SQL file with the next avilable version number at https://github.com/openMF/mifosx/tree/develop/mifosng-provider/src/main/resources/sql/migrations/core_db. These patches are automatically applied by Flyway on server startup. 
For any modifications to the schema which affects data on existing installations (Ex: moving columns to new table etc), ensure that the upgrade scripts are also accompanied by data migration scripts in the same patch file

1. An integration test is added/updated to test the new feature developed/bug-fix being made

1. The pull request has a Jira issue associated with it. Details for creating issues on Jira are located at https://mifosforge.jira.com/wiki/display/MIFOSX/Getting+started+-+Contributing+to+MifosX#Gettingstarted-ContributingtoMifosX-CreateIssues

1. The Relevant Jira number (Ex: MIFOSX-1454) is present as a part of the commit message. Refer http://comments.gmane.org/gmane.comp.finance.mifos.devel/14664 for details

1. Newly added code has been formatted and cleaned up as per our preferences at https://github.com/openMF/mifosx/wiki/Eclipse#apply-project-preferences-to-eclipse

1. Comments are added where relevant to ease peer review

1. Your work is present in a single commit rather than several broken up commits. This helps us maintain a cleaner commit history.  https://github.com/openMF/mifosx/wiki/Github-&-Git#contributing-back-changes-to-original-repositoryauthors has instructions for squashing multiple commits or resetting head and recommiting your work as a single commit

1. Your work is rebased on top of the latest code from develop branch to make life easier for our Committers

1. "gradlew licenseFormatMain licenseFormatTest licenseFormatIntegrationTest" has been run to apply the Mozilla License on all newly added files (your pull requests are verified to ensure compliance with this since https://github.com/openMF/mifosx/pull/1053)
