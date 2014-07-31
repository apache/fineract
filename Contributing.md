# Getting started with Mifos X
Instructions for getting started are located at https://mifosforge.jira.com/wiki/display/MIFOSX/Getting+started+-+Contributing+to+MifosX

# Pull Request Checklist
Before sending out a pull request, please ensure that the following pre-requisites are taken care of

An integration test is added/updated to test the new feature developed/bug-fix made

The pull request has a Jira issue associated with it. Details for creating Jira issues are located at https://mifosforge.jira.com/wiki/display/MIFOSX/Getting+started+-+Contributing+to+MifosX#Gettingstarted-ContributingtoMifosX-CreateIssues

The Relevant Jira number (Ex: MIFOSX-1454) should be present as a part of the Commit message. Refer http://comments.gmane.org/gmane.comp.finance.mifos.devel/14664 for details

Ensure newly added code has been formatted and cleaned up as per our preferences at https://github.com/openMF/mifosx/wiki/Eclipse#apply-project-preferences-to-eclipse

For ensuring a cleaner commit history, we request contributors to send their work in a single commit. This can be done using squash or reset, instructions at https://github.com/openMF/mifosx/wiki/Github-&-Git#contributing-back-changes-to-original-repositoryauthors

Rebase your work on top of the latest code from develop branch to make life easier for our Committers

