Changelog
=====

See VERSIONING (https://github.com/openMF/mifosx/blob/master/VERSIONING.md) for information on what updates to the version number implies for the platform.

Releases
===============

1. 30 Apr 2014 - 1.22.0.RELEASE
1. 30 Mar 2014 - 1.21.0.RELEASE
1. 18 Mar 2014 - 1.20.1.RELEASE
1. 16 Mar 2014 - 1.20.0.RELEASE
1. 04 Mar 2014 - 1.19.0.RELEASE
1. 23 Feb 2014 - 1.18.0.RELEASE
1. 21 Feb 2014 - 1.17.1.RELEASE
1. 17 Jan 2014 - 1.17.0.RELEASE
1. 29 Dec 2013 - 1.16.1.RELEASE
1. 15 Dec 2013 - 1.16.0.RELEASE
1. 12 Dec 2013 - 1.15.2.RELEASE
1. 04 Dec 2013 - 1.15.1.RELEASE
1. 01 Dec 2013 - 1.15.0.RELEASE
1. 21 Nov 2013 - 1.14.0.RELEASE
1. 03 Nov 2013 - 1.13.4.RELEASE
1. 30 Oct 2013 - 1.13.3.RELEASE
1. 28 Oct 2013 - 1.13.2.RELEASE
1. 22 Oct 2013 - 1.13.1.RELEASE
1. 19 Oct 2013 - 1.13.0.RELEASE
1. 16 Oct 2013 - 1.12.0.RELEASE
1. 7 Oct 2013 - 1.11.1.RELEASE
1. 5 Oct 2013 - 1.11.0.RELEASE
1. 4 Oct 2013 - 1.10.3.RELEASE
1. 22 Sep 2013 - 1.10.2.RELEASE
1. 20 Sep 2013 - 1.10.1.RELEASE
1. 19 Sep 2013 - 1.10.0.RELEASE
1. 18 Sep 2013 - 1.9.2.RELEASE
1. 10 Sep 2013 - 1.9.1.RELEASE
1. 8 Sep 2013 - 1.9.0.RELEASE
1. 21 Aug 2013 - 1.8.1.RELEASE
1. 20 Aug 2013 - 1.8.0.RELEASE
1. 14 Aug 2013 - 1.7.2.RELEASE
1. 12 Aug 2013 - 1.7.1.RELEASE
1. 28 July 2013 - 1.7.0.RELEASE
1. 28 July 2013 - 1.6.1.RELEASE
1. 20 July 2013 - 1.6.0.RELEASE
1. 14 July 2013 - 1.5.0.RELEASE
1. 25 Jun 2013 - 1.4.1.RELEASE
1. 20 Jun 2013 - 1.4.0.RELEASE
1. 10 Jun 2013 - 1.3.0.RELEASE
1. 06 Jun 2013 - 1.2.1.RELEASE
1. 27 May 2013 - 1.2.0.RELEASE
1. 24 May 2013 - 1.1.4.RELEASE
1. 22 May 2013 - 1.1.3.RELEASE
1. 19 May 2013 - 1.1.2.RELEASE
1. 12 May 2013 - 1.1.1.RELEASE
1. 09 May 2013 - 1.1.0.RELEASE
1. 08 May 2013 - 1.0.1.RELEASE
1. 07 May 2013 - 1.0.0.RELEASE
1. 30 April 2013 - 0.12.1.beta
2. 30 April 2013 - 0.11.4.beta
1. 22 April 2013 - 0.12.0.beta
1. 17 April 2013 - 0.11.3.beta
1. 10 April 2013 - 0.11.2.beta
1. 05 April 2013 - 0.11.1.beta
1. 05 April 2013 - 0.11.0.beta


1.22.0.RELEASE
=============
This feature release contains work around Fixed deposits with interest slabs , recurring deposits, accrual accounting (upfront and periodic accrual) for loan products including the ability to categorize loans as Non Performing Assets, standing instructions, ability to disburse loans to linked savings accounts and upgrading the community app to Bootstrap 3.

Important: Table `m_savings_account_transfer` has been dropped as a part of the work around Standing instructions. The database changes may be viewed at https://github.com/openMF/mifosx/blob/master/mifosng-db/migrations/core_db/V160__standing_instruction_changes.sql.

Platform & API

New Features & Improvements
 - [MIFOSX-1111] - Add Support for Term Deposits or Fixed Deposits or Time Deposits
 - [MIFOSX-1112] - Add Support for Recurring Deposits
 - [MIFOSX-1049] - Ship sample Data with every stable release
 - [MIFOSX-1065] - In saving product, need quarterly, biannually, yearly option for interest compounding.
 - [MIFOSX-1081] - Ability to Categorize Loans as Non Performing Assets (NPA)
 - [MIFOSX-865] - Accrual Accounting support for Loan Products
 - [MIFOSX-1055] - Add Standing Instructions for account to account transfer
 - [MIFOSX-1146] - add support for disbursing loan amount to savings account
 - [MIFOSX-1094] - Passing Tenant Database host, port and credentials to Pentaho reports
 - [MIFOSX-1008] - No Interest posting on saving account when Interest is set @ 0%
 - [MIFOSX-1075] - Add overdue penalty batch job in DB and handle exceptions in batch job
 - [MIFOSX-1035] - JUnit Integration Test Cases for Charges
 - [MIFOSX-1046] - JUnit test cases for code and code values API's
 - [MIFOSX-1047] - JUnit test cases for Scheduler Jobs API's


Bugs
 - [MIFOSX-886] - In Modify Loan application adding charge on specified due date/Overdue fees results in Unknown data integrity issue
 - [MIFOSX-998] - Should not be allowed to activate Group/Center and associate clients under group with out permissions.
 - [MIFOSX-1021] - Not able to view particular charge details attached for savings account
 - [MIFOSX-1027] - Potential issues with maker-Checker functionality
 - [MIFOSX-1036] - Not able to generate Pentaho report added for client's Loan and saving transactions page
 - [MIFOSX-1056] - Improper error message is displayed after clicking on "Repayment info" button while submitting the tranche loan application for a client with insufficient input
 - [MIFOSX-1076] - In Modify Loan application if the Loan amount is modified, the % Charge on disbusement is not modifying accordingly.
 - [MIFOSX-1077] - In Client loan page after submitting the Loan application if the %amount charge is edited then it is not getting calculated properly
 - [MIFOSX-1053] - Not able to pay overdue savings charges through Schedular Job.


Community App
 - [MIFOSX-962] - Upgrade to Twitter Bootstrap 3
 - [MIFOSX-1048] - Gmail Like Loading Progress Bar during application load
 - [MIFOSX-965] - Community app should "respond" to fill entire screen
 - [MIFOSX-1064] - Remove Gray "frame" around page data so as to match with the background.
 - [MIFOSX-1072] - Ability to view error log details of individual run in View history page of a particular Schedular Job page
 - [MIFOSX-1057] - New Collection Sheet UI changes
 - [MIFOSX-1022] - Improvements In savings account for a client
 - [MIFOSX-1058] - Not able to create holiday because of issue with the checkbox while selecting the office
 - [MIFOSX-1073] - Issue with respect to View a particular Charge details in Loan and savings accounts
 - [MIFOSX-1042] - UI related issues with respect to Datatables
 - [MIFOSX-1041] - Issue related to Collection sheets
 - [MIFOSX-1090] - Added overdue penalties is not displaying in loan repayment schedule
 - [MIFOSX-1095] - Not able to assign Moratorium for Interest in new loan application page for a client
 - [MIFOSX-1144] - Not able make/edit/read datatable entries even though user have proper permissions



1.21.0.RELEASE
=============
This Feature release adds support for Automated Penalties for loans and externalizes the MySQL connection properties for a tenant along with other major fixes and improvements

Important: Those updating from an existing installation to 1.21.* release or higher should run the update https://github.com/openMF/mifosx/blob/develop/mifosng-db/migrations/list_db/V2__externalize-connection-properties.sql on `mifosplatform-tenants` schema

Platform & API

New Features & Improvements
 - [MIFOSX-1025] - Implement Overdue Penalties with recurrence and percentage based approach
 - [MIFOSX-949] - Externalizing the PoolConfiguration of data source per tenant
 - [MIFOSX-994] - Allow all savings account's(remove constrain for overdraft) as client default account
 - [MIFOSX-1015] - Need to Add Pentaho report which generates data for client saving transactions transaction and loan repayment schedule
 - [MIFOSX-1010] - stretchy report converted into Pentaho reports
 - [MIFOSX-986] - transferring of clients in same group but different loan officer
 - [MIFOSX-1013] - Add loanId and clientId in makercheckers api return
 - [MIFOSX-942] - Moving savings products and savings accounts api documentation out of beta
 - [MIFOSX-993] - JUnit Integration Test Cases for Group Savings
 - [MIFOSX-1003] - JUnit Integration Test Cases for Accounting with Savings
 - [MIFOSX-1012] - JUnit Test Cases for Fund Transfer in Savings
 - [MIFOSX-1026] - JUnit Integration Test Cases for Global Configurations
 - [MIFOSX-941] - Api Documentation for modify loan application is missing


Bugs
 - [MIFOSX-835] - For a Client (member of 2 Groups) taken JLG loan from 1 group, repayment amount is displaying in collection sheet of other Group
 - [MIFOSX-880] - In new JLG Loan application interest on first repayment is not getting calculated as per Disbursement date
 - [MIFOSX-909] - For the Loan under pending approval stage - modifications in the charges is not getting updated in "outstanding" column
 - [MIFOSX-996] - Not able to modify Loan application through API
 - [MIFOSX-1014] - approving maker checker for repayment and adjustments for work throws a 500 error
 
Community App
 - [MIFOSX-1002] - Issues with Angular JS and Require JS load on the community app
 - [MIFOSX-957] - Dashboard reports wont work when connected to MIfos X backend other than https://demo.openmf.org
 - [MIFOSX-1018] - User with all permission not able to change password of his own
 - [MIFOSX-956] - Ability to print Repayment Schedule in active loan
 - [MIFOSX-1005] - Improvement with respect to Accounting pages
 - [MIFOSX-1016] - Cleaning XBRL reports ui(inline styles)
 - [MIFOSX-990] - Navigation page is not working as expected
 - [MIFOSX-999] - Issues with User Generated Documents
 - [MIFOSX-953] - Associating checkbox to jobs are not working as expected.
 - [MIFOSX-972] - Log in screen freezing problem


1.20.1.RELEASE
=============
Bug Release

Bugs
 - [MIFOSX-995] - Grace periods not working when actualDisbursementDate != expectedDisbursementDate

1.20.0.RELEASE
=============
Feature Release

Platform & API

New Features & Improvements
 - [MIFOSX-216] - No. of days overdue before In Arrears Not in Mifos X
 - [MIFOSX-992] - Password Expiry and Idle user Logout
 - [MIFOSX-974] - Integration test cases for Savings functionality

Bugs
 - [MIFOSX-814] - Not able to create user with the current API parameters
 - [MIFOSX-877] - Retrieve Loan by external ID results in 500 error in API
 - [MIFOSX-879] - Sorting and Ordering not working on Loan as per API Documentation
 - [MIFOSX-930] - In modify loan application (Undo disbursal - Undo approval) not able to delete the charges added before.
 - [MIFOSX-945] - Not able to adjust the minimum opening balance amount in transactions page of savings account results in "Null point exception"
 - [MIFOSX-955] - Error in Aging Detail stretchy report
 - [MIFOSX-948] - No links to User Generated Documents in the api docs (called templates)


Community App
 - [MIFOSX-979] - show hide elements on user interface based on user permissions
 - [MIFOSX-975] - Clearly represent Loans In Arrears in the Community app
 - [MIFOSX-976] - Support editing values for global configurations
 - [MIFOSX-985] - Implementing user permissions for scheduler job
 - [MIFOSX-989] - Make easy to select particular entity in a table
 - [MIFOSX-947] - Issue with checkboxes because of upgradation in Angular JS
 - [MIFOSX-973] - Templates functionality is broken after Angular upgrade
 - [MIFOSX-977] - Not able to view data in stretchy reports if duplicate values present in JSON array
 - [MIFOSX-980] - issue with first repayment on the same day of disbursement
 - [MIFOSX-981] - Not able to enter data into data tables for code values
 - [MIFOSX-983] - Stretchy reports export csv including old results
 - [MIFOSX-987] - Not able to add member under Group with the backdated.
 - [MIFOSX-991] - Submit button is not working in currency configuration page
 - [MIFOSX-907] - In Edit Datatable page "new name" is not working as expected
 - [MIFOSX-909] - For the Loan under pending approval stage - modifications in the charges is not getting updated in "outstanding" column
 - [MIFOSX-915] - Not able to submit the JLG loan application if "Interest charged from" field is attached
 - [MIFOSX-919] - Charge on specified due date, added while submitting the loan application is not reflecting in preview repayments page
 - [MIFOSX-952] - Not able to assign Moratorium for Interest in new loan application page for a client

1.19.0.RELEASE
=============
Feature Release

This feature release adds the ability to associate current accounts with clients along with other improvements and enhancements

Platform & API

New Features & Improvements
 - [MIFOSX-931] - Add current account support (overdrafts) for savings
 - [MIFOSX-932] - Basic Savings reports
 - [MIFOSX-939] - Order of Saving transactions

Bugs 
 - [MIFOSX-884] - Charges removed in Modify loan application is not getting deleted after saving the application.
 - [MIFOSX-911] - Not able to edit client without passing name details
 - [MIFOSX-929] - User with no permission to activate the client is able to activate successfully while creating the client.


Community App
 - [MIFOSX-933] - Update to latest version of Angular and fix all observed issues
 - [MIFOSX-946] - Loan purpose is not captured for Group and JLG loans
 - [MIFOSX-864] - Improvements with respect to Closed Loans/Closed Savings
 - [MIFOSX-935] - Improvements to view particular transaction details in transaction page (Loan/savings)
 - [MIFOSX-937] - Ability to display Loan status in Client loan page

1.18.0.RELEASE
=============
Feature Release

This feature release ships the first implementation of Tranche Loans functionality along with various bug fixes and minor improvements

Platform & API

New Features & Improvements
 - [MIFOSX-92] - Tranche Loans
 - [MIFOSX-869] - Correct spelling mistakes observed in MifosX schema
 - [MIFOSX-902] - Generate Productive Collection sheet based on office, meeting date, loan officera search parameter
 - [MIFOSX-872] - Capture Payment Type while creating a journal entry
 - [MIFOSX-888] - Early Payment repayment strategy
 - [MIFOSX-923] - Missing entity status in search resultset
 
Bugs
 - [MIFOSX-847] - For a Client - 2nd loan(with same product) is not getting incremented as per Borrower Cycle.
 - [MIFOSX-867] - If a loan is created with first repayment on date, upon approving if loan is disbursed on other than expected disb date, schedule is not updated
 - [MIFOSX-870] - For Disbursed Loan - charge collected on disbursement is not reflecting in Loan summary page.(After running "Update Loan Summary" in schedular job)
 - [MIFOSX-895] - Checkbox for "Terms vary based on loan cycle" only controls display and not the desired principal definition
 - [MIFOSX-896] - Missing permission for "savingsaccountcharge" endpoint
 - [MIFOSX-897] - Missing _CHECKER permission for CREATE_JOURNALENTRY
 - [MIFOSX-900] - Deactivating a charge with assigned products causing integrity issues in product mapping
 - [MIFOSX-901] - "BIT" datatype/columntype missing displaytype, throws "error.msg.invalid.lookup.type" error message
 - [MIFOSX-904] - Loan disbursement checks cause corrupted schedule
 - [MIFOSX-906] - Scheduler job exception for post interest for savings
 - [MIFOSX-914] - Global configuration page is not working as exptected
 - [MIFOSX-920] - Not able to submit the Tranche Loan application for the higher amounts (in Crores)
 - [MIFOSX-921] - While submitting the Tranche Loan for a client Improper error message is displaying rather displaying for proper inputs
 - [MIFOSX-922] - In Modify application page for Tranche Loans if the Installment amount is modified then Last repayment amount is more than defined.

1.17.1.RELEASE
=============
Bug Release

Bugs
 - [MIFOSX-827] - Not able to disburse loan with the percentage charges applied while submitting the loan application.
 - [MIFOSX-860] - NOt able to reject the loan application with status "Submitted and pending for approval"
 - [MIFOSX-873] - Get clients by office id API is returing HTTP Status 500
 - [MIFOSX-875] - Attach duplicate mobile number while creating Client results in Unknown Data Integrity Issue
 - [MIFOSX-876] - Retrieve Loan by Loan account number returns all loan account in API
 - [MIFOSX-878] - Not able to use created datatable for a client results in Forbidden error
 - [MIFOSX-882] - API doccument related Issues
 - [MIFOSX-883] - Additional fields added in Edit datatables (for client) page is not woking as expected
 - [MIFOSX-912] - Client Loan amount is not getting rounded off even if "Currency in Multiples Of" is set as 100 in loan product.
 
1.17.0.RELEASE
=============
Feature Release

This feature release contains some API breaking changes around charges, loans and savings products (introducing mandatory short names) along with improvements around Collection Sheet, introducing the ability to activate/inactivate Staff and other Bug fixes

Platform & API

New Features & Improvements
 - [MIFOSX-831] - Collectionsheet improvements to make data entry easy
 - [MIFOSX-832] - Options to set short name for loan, charge and saving products
 - [MIFOSX-845] - Option to activate or inactivate a staff
 - [MIFOSX-849] - Update search API to include group external Id as a search parameter
 - [MIFOSX-866] - Return Transaction Type of loan or savings transaction associated with a journal entry

Bugs
 - [MIFOSX-720] - In modify loan application (Undo disbursal - Undo approval) if charge to be collected from savings is deleted then not able to save the application.
 - [MIFOSX-819] - Not able to modify Loan application after undo disburse once one of the charge paid.

1.16.1.RELEASE
=============
Bug Release

Bugs
 - [MIFOSX-802] - Loan life cycle is not considered in validations for loan operations
 - [MIFOSX-812] - Journalentries Api is not returning currency while retrieving transactions
 - [MIFOSX-825] - Timeline is not displaying while fetching the details of created centers
 - [MIFOSX-830] - APIs are returning invalid commandId for all actions when maker-checker is enabled.
 - [MIFOSX-834] - Reports-Loan products are not listing for currency ALL(Active Loans - Summary)

1.16.0.RELEASE
=============
Feature Release

This feature release adds support for undoing loan write-offs along with a couple other improvements

Platform & API

New Features & Improvements
 - [MIFOSX-796] - Allow undo of write off transaction (disallow undo or change of any other transaction when loan is written off)
 - [MIFOSX-824] - Add pagination to Audits endpoints
 - [MIFOSX-826] - Add holiday status in response data

1.15.2.RELEASE
=============
Bug Release

Bugs
 - [MIFOSX-816] - Future loan amounts should be with respect to Loan cycle (not to be confused with borrower cycle)
 - [MIFOSX-818] - Not able to retrieve Global Configurations through API
 - [MIFOSX-820] - Fix spelling mistakes in permissions for better Display on community app
 - [MIFOSX-821] - Null pointer exception while disbursing the loan
 - [MIFOSX-822] - Backdated transaction that results in transaction reversals does not create journal entries for new transactions


1.15.1.RELEASE
=============
Bug Release

Bugs
 - [MIFOSX-801] - Clean up implementation of [MIFOSX-765]
 - [MIFOSX-805] - Terms vary based on loan cycle is not getting updated in Edit Loan Product page.
 - [MIFOSX-810] - Unhandled API error while creating multiple users with same name
 - [MIFOSX-811] - Repayment schedule dates for Loan disbursement after undo disbursal is not proper
 - [MIFOSX-744] - Implementation of mifosx-735 should be optional

1.15.0.RELEASE
=============
Feature Release

This feature release contains some API breaking changes around loan and savings products in relation to the accounting section of their respective APIs. Some small improvements are also bundled into this release.

Platform & API

New Features & Improvements
 - [MIFOSX-569] - Calculate future loan amounts or installments based on previous loan cycle.
 - [MIFOSX-744] - Implementation of mifosx-735 should be optional
 - [MIFOSX-790] - Need ability to be able to link accounting journal entries with the additional payment details captured in m_payment_detail table
 - [MIFOSX-792] - Improve penalties introducing a redemption periods for MFIs that dont input data same day.
 - [MIFOSX-794] - Add roles list to /users endpoint
 - [MIFOSX-795] - Clean up API around Loan/ Savings product creation for consistency and better usability


Bug 
 - [MIFOSX-798] - Read not working for holidays and API documentation not updated
 - [MIFOSX-800] - Scheduler job is failed, if no accounting data is available.


1.14.0.RELEASE
=============
Feature Release

This feature release adds support for per installment fees for loans, introduces the ability to edit/delete existing holidays and other improvements around Calendar (recurring meetings) functionality.

Platform & API

New Features & Improvements
 - [MIFOSX-693] - Holidays cannot be edited or removed
 - [MIFOSX-730] - Implement "per installment fee" for Loans
 - [MIFOSX-649] - (Meeting) Calendar Improvements
 - [MIFOSX-682] - Data Validation & Helpful Developer Messages for Meetings
 - [MIFOSX-754] - Mobile phone number(s?) fields on Client & Staff on core database (not custom data tables)
 - [MIFOSX-765] - Add Timeline to the clients and groups api
 - [MIFOSX-119] - Spike: Produce boilerplate shell on platform/API for Outbound SMS Integration

Bug 
 - [MIFOSX-769] - Loans product: Min & max are not working as expected for Principal, No. of repayments & interest
 - [MIFOSX-784] - API - Client under pending status doesn't allow future activation date


1.13.4.RELEASE
=============
Bug Release

Bugs
 - [MIFOSX-763] - Organization Running balance on /glaccounts is incorrect for uncalculated entries
 - [MIFOSX-764] - Journal entries are not returned in correct order making running blance nonsensical
 - [MIFOSX-766] - Add and delete charge is not reflecting the schedule till loan disburse
 
1.13.3.RELEASE
=============
Bug Release

Bugs
 - [MIFOSX-740] - In Savings account monthly charge Recurrence Start month should be after activation of the account
 - [MIFOSX-757] - updating charges
 - [MIFOSX-759] - Retrieving journal entry information by transactionId is not gauranteed to return journal entries from same transaction
 - [MIFOSX-760] - Updating a charge does not evict the cache resulting in incorrect data returned from platform
 - [MIFOSX-762] - SubmittedBy User not stored when creating savings accounts

1.13.2.RELEASE
=============
Bug Release

Bugs
 - [MIFOSX-721] - "Interest charged from" is not working as per expected
 - [MIFOSX-722] - Due amount in Repayment installments are not equal
 - [MIFOSX-729] - Flat - Daily is not working as expected
 - [MIFOSX-749] - Scoping of clients by office hierarchy no longer works
 - [MIFOSX-750] - Scoping of clients by search term (to search any name part) no longer works

1.13.1.RELEASE
=============
Bug Release

Bugs
 - [MIFOSX-715] - NPE getting client image
 - [MIFOSX-733] - 500 exception when not passing a note during undo of disbursement
 - [MIFOSX-734] - Add further detail on the 'repayment strategies' concept
 
1.13.0.RELEASE
=============
Feature Release

This Feature Release adds support for min and maximum caps on % based charges for loans.

Platform & API

New Features & Improvements

 - [MIFOSX-437] - Minimum and maximimum caps on % based charges
 - [MIFOSX-747] - Update AMI and AMI related docs on readme
 - [MIFOSX-748] - Improve API documentation around charges

1.12.0.RELEASE
=============
Feature Release

This Feature Release adds support for Monthly fees for savings along with minor bug fixes

Platform & API

New Features & Improvements
 - [MIFOSX-728] - Extend Recurring fees for Savings
 - [MIFOSX-735] - add organizationRunningBalance and runningBalanceComputed to /glaccounts API output

Bug 
 - [MIFOSX-639] - loan transactions - externalId duplicates not being caught by exception
 - [MIFOSX-723] - In Savings, not able to adjust the transaction for the amount deposited as "Minimum opening balance"
 - [MIFOSX-732] - Payment type need not be specified for charges for savings products


1.11.1.RELEASE
=============
Bug Release

Platform & API

New Features & Improvements
 - [MIFOSX-726] - Add missing savings charges details to api doc

Bug
 - [MIFOSX-727] - Loan Write off account not being persisted on creating a new Loan Product

1.11.0.RELEASE
=============
Feature Release

This Feature release ships various improvements to the configurable fees functionality for savings, ability to consolidate running balances for Journal entries (both at a Branch and an Organizational Level) and various bug fixes

Platform & API

New Features & Improvements
 - [MIFOSX-435] - Capture derived/running balances for Journals
 - [MIFOSX-656] - Ability to Map specific savings fees to specific income accounts
 - [MIFOSX-690] - Accounting Not Done for over-payments and related work-flows (transfer overpaid amount to savings)
 - [MIFOSX-697] - Move existing withdrawal fees and Annual fees for savings into the configurable fees workflow

Bug 
 - [MIFOSX-687] - In View/Edit Savings Product not able to remove the charge added while defining the product.
 - [MIFOSX-688] - Transaction Number for accounting needs to have guaranteed uniqueness
 - [MIFOSX-689] - Ensure account Transfers (Savings to Loans etc) point to the right Loan Transaction
 - [MIFOSX-691] - Clean up current Configurable Savings Fees functionality
 - [MIFOSX-692] - Savings account accepting multiple currency charges
 - [MIFOSX-699] - Duplicate entry in accounting table for disbursement fee through linked savings account
 - [MIFOSX-718] - Not able to "Pay Annual Fee" and "Post Interest" for savings account through Schedular Jobs
 - [MIFOSX-719] - Savings account with min. opening balance if Annual fee is deducted on account activation date then interest is not getting posted.

Reference App
 - [MIFOSX-673] - Receipt number displayed in Client Transactions history table / Journal

1.10.3.RELEASE
=============
Bug Release

Platform & API

Bug
 - [MIFOSX-679] - Not able to Create holiday through API
 - [MIFOSX-694] - Unable to view users when caching is enabled
 - [MIFOSX-696] - Declining balance - Equal principal payments(Daily) is not working as expected.
 - [MIFOSX-716] - When updating details related to roles (name, permissions) - the incorrect cached details are returned for cached users
 - [MIFOSX-717] - Register/Deregister data tables not working with constraint based approach

Reference App
 - [MIFOSX-698] - Unable to save the created permissions for created Role


1.10.2.RELEASE
=============
Bug Release

Platform & API

New Features & Improvements
 - [MIFOSX-664] - Client transfer workflow change

Bug
 - [MIFOSX-677] - No mention of 'transfersInSuspenseAccountId' parameter in API docs
 - [MIFOSX-684] - Default transfersInSuspense Accounts set incorrectly for existing Saving Products
 - [MIFOSX-685] - Debit and Balance displayed for active savings account after a transfer in Transactions tab is incorrect
 - [MIFOSX-686] - Configurable fees payment does not return FeesDeduction as true


1.10.1.RELEASE
=============
Bug Release

Platform & API

Bug
 - [MIFOSX-675] - Not able to create savings account with/without charges.
 - [MIFOSX-680] - Duplicate entries for Payment channels to Fund source mapping

Reference App
 - [MIFOSX-683] - Update Reference UI to allow manual payments of Loan fees from savings


1.10.0.RELEASE
=============
Feature Release

This feature release ships a tighter integration between Loans and Savings (ability to pay Loan Charges from Savings) , introduces configurable fees for savings and adds the ability to Transfer clients with active savings accounts across branches along with a couple of bug fixes

Platform & API

New Features & Improvements
 - [MIFOSX-642] - Allow charges for a loan to be deducted from savings of the client
 - [MIFOSX-643] - Transfer Clients with Savings accounts between Branches
 - [MIFOSX-644] - Configurable Fees for savings

Bug 
 - [MIFOSX-662] - Not able to define "Transfer in Suspense" in cash based accounting for Savings product.
 - [MIFOSX-674] - Not able to disburse the Client's loan if charges attached to that loan is transfered from Savings account


1.9.2.RELEASE
=============
Bug Release

Platform & API

Bug
 - [MIFOSX-659] - Not able to transfer Client between Branch offices which were created under same parent office.
 - [MIFOSX-663] - No API documentation on Holidays or Working Days features
 - [MIFOSX-671] - "Minimum opening balance" assigned is not displaying as balance in Transaction page after activation of savings account
 - [MIFOSX-672] - Final installment of FLAT interest loans incorrectly has closing loan balance thats greater than zero.

Improvement
 - [MIFOSX-669] - Ability to not allow to "close" a client while transfer of client is in progress


1.9.1.RELEASE
=============
Bug Release

Platform & API

Bug
 - [MIFOSX-647] - Transfered amount in Loan account transaction page should not be allowed to Edit/Delete
 - [MIFOSX-648] - User with no read permissions should still be able to fetch their own user account data
 - [MIFOSX-658] - Unable to update existing Loan Products in demo server

Reference App
 - [MIFOSX-655] - Client Closure reason added in code values is not displayed in dropdon list of Client Close page


1.9.0.RELEASE
=============
Feature Release

This feature release ships various improvements to the core portfolio modules like adding the ability to close a savings account, reactivate a savings account by undoingingediting transactions, transferring money from overpaid loan to savings accounts, closing group accounts, transferring clients with active loans between groups and across branches and adding ability to capture attendance through the collection sheet along with various bug fixes

Platform & API

Bug 
 - [MIFOSX-611] - Not able to reassign loan from one loan officer to other in Bulk Loan Reassignment page
 - [MIFOSX-613] - Not able to assign Loan officer after submitting the Group Loan Application.
 - [MIFOSX-615] - regression issue fixes for integration test
 - [MIFOSX-624] - Applying Annual Fee results in Savings Account Having a Negative Balance
 - [MIFOSX-627] - End of day balance is calculated incorrectly for same day transations which results in negative interest been calculated
 - [MIFOSX-628] - The default Annual fee due on date is not displaying future date in case provided
 - [MIFOSX-635] - Currency in multiples of 1000 returns an an invalid integer error in edit saving product
 - [MIFOSX-640] - Approve Transfer Client button
 - [MIFOSX-517] - If "Code" is added and defined in "Create Data Table" and utilized for a client and then if that code is deleted wrong entry is displayed in Client's page

New Features & Improvements
 - [MIFOSX-547] - Client, Group and Center Transfers
 - [MIFOSX-567] - External Id work for migration
 - [MIFOSX-596] - Support ability to transfer money from overpaid loan accounts to savings accounts
 - [MIFOSX-619] - Support ability to close an existing active savings account
 - [MIFOSX-436] - Transfer clients between loanofficers, groups and branches
 - [MIFOSX-607] - Capture attendance through collectionsheet
 - [MIFOSX-622] - Savings transfers involving a withdrawal should not auto apply withdrawal fees if configured for account
 - [MIFOSX-625] - Support ability to close a group
 - [MIFOSX-634] - Support ability to undo/reverse transactions for the Savings account in closed state and activate again
 - [MIFOSX-440] - Meeting calendar clean up
 
Reference App
 - [MIFOSX-612] - Not able to configure Maker Checker Tasks in UI
 - [MIFOSX-646] - Client "Close" tab is not working
 - [MIFOSX-618] - Not able to add permisons for the new role created.
 
1.8.1.RELEASE
=============
Bug Release

Platform & API

Bug
 - [MIFOSX-617] - Unable to create a new Loan Application


1.8.0.RELEASE
=============
Feature Release

This feature release ships various improvements to the savings module (including ability to transfer funds from a savings accounts to another savings/loan accounts, adjust deposits/withdrawals, batch jobs for posting interest etc), ability to capture attendance details against center/group meetings, updates to the scheduler to run in a clustered environment and the beta release of the transfers functionality (with support for transferring clients with active accounts between groups and transferring clients with closed accounts between branches)

Platform & API

New Features & Improvements
 - [MIFOSX-310] - Generate Collection sheet based on meeting calendar dates
 - [MIFOSX-357] - Add Assign staff functionality to Group API call
 - [MIFOSX-433] - Allow rounding of loan schedules and charges to multiples of 100's or 1000's
 - [MIFOSX-441] - Synch JLG loans first repayment date with meeting dates
 - [MIFOSX-586] - Support ability to adjust/edit an existing savings transaction
 - [MIFOSX-591] - changes to group loan API
 - [MIFOSX-599] - Ability to add savings account details in Group Summary Page.
 - [MIFOSX-602] - Display transaction ID after applying withdrawal/repayment/etc.
 - [MIFOSX-605] - add support for scheduling to work in clustered mode
 - [MIFOSX-159] - Add batch job for posting of interest to savings accounts
 - [MIFOSX-432] - Support ability to transfer money from savings account to other savings accounts
 - [MIFOSX-496] - Capture Client Attendance details
 - [MIFOSX-574] - Add support for Savings accounts at the Center level
 - [MIFOSX-575] - Add support for Savings accounts for Groups
 - [MIFOSX-595] - Support ability to transfer money from savings account to loan accounts for purposes of making a repayment
 
Reference App
 - [MIFOSX-609] - Show "Add Holidays" link in demo server

1.7.2.RELEASE
=============
Bug Release

Platform & API

Bug
 - [MIFOSX-606] - Unable to activate client from pending state

1.7.1.RELEASE
=============
Bug Release

Platform & API

Bug
 - [MIFOSX-538] - Search is displaying duplicate result if acct number is part of client name
 - [MIFOSX-554] - In Group loan if meeting dates are getting rescheduled frequently, the repayment is getting extended.
 - [MIFOSX-555] - Return write-off date and transaction history for written off loans
 - [MIFOSX-581] - Batch job is starting even when scheduler is in suspended state
 - [MIFOSX-589] - Adjust Transactions accepts Holidays and non working days.
 - [MIFOSX-597] - Closed client under Group applying for JLG loan
 - [MIFOSX-598] - Closed client under Group applying for New Savings Account
 - [MIFOSX-600] - Allowing to Disassociate a client from the Group who is having a active savings account under that Group

New Features & Improvements
 - [MIFOSX-428] - Add user giving invalid emailid error

1.7.0.RELEASE
=============
Feature Release

This Feature Release ships a stable version of the Scheduler and Product mix functionality along with minor updates to Savings functionality

Platform & API

Bug 
 - [MIFOSX-397] - Annual fee collection date is displaying on 2nd year of opening/activation date of savings account.

New Features & Improvements
 - [MIFOSX-495] - Client performance History
 - [MIFOSX-565] - Support ability to undo/reverse transactions even when the loan is in closed state
 - [MIFOSX-563] - Repayment strategy is not displayed in loan account details page
 - [MIFOSX-548] - A holiday's "Reschedule Repayment to" date should not be a non-working day
 - [MIFOSX-506] - Batch Job Implementation - Part 2
 - [MIFOSX-478] - Product Mix Functionality
 - [MIFOSX-470] - Avoiding Deletion of active Charge which was already assigned to a Loan product.
 - [MIFOSX-428] - Add user giving invalid emailid error
 
Reference App
 - [MIFOSX-561] - Add loan cycle and loan product cycle details to Client Summary

1.6.1.RELEASE
=============
Bug Release

Platform & API

Bug
 - [MIFOSX-485] - In Groups -> Group loan under "Submitted and pending approval" state -> "Change loan application" page,not able to edit/update "Sync repayments with meeting" and "Loan term" fields.
 - [MIFOSX-564] - Create data table does not work for attached request
 
Reference App
 - [MIFOSX-560] - In "Modify Application page" if Product is changed, it not getting updated in Loan account overview list

1.6.0.RELEASE
=============
Feature (and Bug) Release

This Feature release focussed on various improvements to the Savings functionality as requested by Musoni, support for defining working days and the beta version of the batch jobs scheduler along with minor bug fixes

Platform & API

Bug 
 - [MIFOSX-400] - If one withdrawal is made between two deposits and amount equal to sum of 2 deposits it is accepting, (Though all 3 transactions happened in different dates)
 - [MIFOSX-404] - In Transactions -> Savings account activity table interest displayed is inconsistent.
 - [MIFOSX-412] - If only interest amount is balance in savings account, Compound interest is not getting calculated for that amount in future dates.
 - [MIFOSX-469] - In Groups unable to activate the created group
 - [MIFOSX-489] - If "New loan application" is submitted for an inactive client under Groups, no proper error message is displayed about the approval status, also activation of the client.
 - [MIFOSX-551] - For Groups, Loan First Repayment date is taking after expected disbursement date than actual disbursement date.
 - [MIFOSX-552] - Group Loan is getting disbursed on meeting date if holiday is declared on that day.
 - [MIFOSX-558] - In modify loan application, am able to define an "application submitted on date" after the loan product's "close date"

New Features & Improvements
 - [MIFOSX-406] - Provide ability to edit all details of pending savings account application
 - [MIFOSX-541] - Introduce application process around savings accounts similiar to loan accounts
 - [MIFOSX-543] - Support working days in loan lifecycle
 - [MIFOSX-550] - Ability to View Holidays
 
Reference App
 - [MIFOSX-500] - Select Applicable Codes from a Combobox while creating new Data Tables
 - [MIFOSX-549] - Loan Product inactivation date improvements

1.5.0.RELEASE
=============
Feature (and Bug) Release

This feature (and bug) release focussed on adding support for Holidays and various improvements and bug fixes for Group Functionality

Platform & API

Bug 
 - [MIFOSX-443] - Additional data tables added to groups are appearing for Centers
 - [MIFOSX-471] - Group activation is accepting dates prior to office opening
 - [MIFOSX-491] - In data table, if string length is more than the defined length, no error message is displayed
 - [MIFOSX-493] - For groups, in "Change Loan Application" page not able to modify the "Product" (ie modified product details not getting updated/saved)
 - [MIFOSX-494] - If any changes are made in "Change loan application" (For group loan), the loan cannot be approve (multiple pages popup for single click on "Approve" tab.)
 - [MIFOSX-498] - Searched "Center" is behaving as Group entity
 - [MIFOSX-516] - No/Incorrect error messages on "Register Data Table" screens
 - [MIFOSX-519] - In "add code" page if already existing Code name is entered in the field, no proper error message is displaying
 - [MIFOSX-521] - Not able to disburse the Group Loan
 - [MIFOSX-522] - In add new group form if activation date field is empty, showing connection failure on the UI.
 - [MIFOSX-525] - Not able to select an existing client as Guarantor
 - [MIFOSX-526] - Issues with fields in Add Guarantors Page
 - [MIFOSX-527] - In "New loan application" / "Change loan application" not able to add Collaterals
 - [MIFOSX-528] - For a loan, same guarantor can be associated more than once.
 - [MIFOSX-529] - For the loan under "Pending Approval" stage not able to edit or delete (already added) collaterals.
 - [MIFOSX-531] - Able to approve loans for a Closed client. (ie, client got closed after submitting "new loan application")
 - [MIFOSX-533] - Able to associate closed clients with Groups
 - [MIFOSX-534] - In Client's page not able to assign staff from parent office
 - [MIFOSX-536] - Client search doesn't work at all at present and should support not only account no but also search by any part of client name

New Features & Improvements
 - [MIFOSX-76]  - Support for Holidays
 - [MIFOSX-438] - Allow closing of clients
 - [MIFOSX-486] - Show loanofficers at higher hierarchical level in client product template
 - [MIFOSX-518] - Capture "Repayments rescheduled to" date for a holiday and add processed field
 - [MIFOSX-212] - loan cycle concept for loans
 - [MIFOSX-499] - Inactivate a loan Product
 - [MIFOSX-537] - Ability to display "Note" related to client in Client's page and "note" related to Loan in Loan's page
 - [MIFOSX-475] - Regenerate loan repayment dates if group meeting frequency is changed
 - [MIFOSX-361] - Inconsistent "From Date" and "To Date" formats in View Journal Entry Screens
 - [MIFOSX-362] - Date format is not similar In Balance Sheet as in other pages.
 
Reference App
 - [MIFOSX-535] - Client details screen centers on screen when there are many notes - it should align to top
 - [MIFOSX-309] - Move Global Search to end of the Header
 - [MIFOSX-530] - Connection fail message is displayed by clicking on delete button in Add/Edit code value page
 - [MIFOSX-483] - Support wizard view for Loan Product creation
 - [MIFOSX-476] - Group permissons (in portfolio grouping) makes reference UI grotesque

1.4.1.RELEASE
=============
Bug Release

Platform & API

Bug
 - [MIFOSX-362] - Date format is not similar In Balance Sheet as in other pages.
 - [MIFOSX-473] - "Accounting Rules" created for Branch office (at Head office) is not displaying in head office.
 - [MIFOSX-479] - Scheduled jobs only run for tenant with identifier 'default'
 - [MIFOSX-480] - Not able to "Create A Group" under Branch Office using Head Office staff.
 - [MIFOSX-488] - With DELETE datatables service, one can drop system tables
 
Reference App
 - [MIFOSX-388] - Buttons on table based reports no longer working
 - [MIFOSX-463] - Numeric ids are number formated (causing NumberFormatException )
 - [MIFOSX-467] - Unable to create group in spanish locale as activation date doesnt pick up on culture/locale in datepicker
 - [MIFOSX-481] - Not able to navigate to the immediate previous page from "Clients loan application" page
 - [MIFOSX-482] - In Groups -> Clients loan application page -> no error message is displaying asking for proper inputs
 - [MIFOSX-484] - While creating a "new client" for group (in branch office), Not able to select "Staff" from that (branch) office, if user is Admin.

1.4.0.RELEASE
=============
Feature Release

This feature release focussed on moving groups functionality from BETA to public API. 

Along with Group releated work, key improvements was the addition of a UI and API for adding 'additional data' and staff relationship to clients.

Platform & API

Bug 
 - [MIFOSX-420] - In "Change loan application" not able to save the modifications made because of error message getting displayed on charges.
 - [MIFOSX-424] - In new loan application, loan is getting approved for the Principal amount beyond the range (Minimum and Maximum) defined in the loan product.
 - [MIFOSX-429] - App user of one branch can edit client of another branch
 - [MIFOSX-446] - New loan application is not validating Min-Max Principal defined in the product defination
 - [MIFOSX-447] - New loan application is not validating Min-Max for "# Of Repayments" defined in the product defination
 - [MIFOSX-448] - New loan application is not validating Min-Max "Nominal interest rate %" defined in the product defination
 - [MIFOSX-468] - In "Change loan application" error message is displaying for the second charge which was applied before.
 - [MIFOSX-474] - Adjusting an existing loan transaction fails when 'note' information is passed

New Features & Improvements
 - [MIFOSX-365] - Simplify "Data Tables" functionality
 - [MIFOSX-421] - Ability to create and associate Group Roles
 - [MIFOSX-434] - Allow linking of staff to Clients
 - [MIFOSX-445] - Show loanofficers at higher hierarchical level in product template
 - [MIFOSX-449] - Group and JLG loans repayment schedule should match group meeting dates if loan repayment is synched with group meeting
 - [MIFOSX-457] - Update API param names for accounting rules to be consistent with rest of the accounting services
 - [MIFOSX-458] - Unable to create accounting rules with Tag Id, multiple debits/credits etc
  
Reference App
 - [MIFOSX-378] - TEVI: Produce 'role-based' dashboards as entry/landing page for users that login
 - [MIFOSX-455] - CodeValues functionality broken on the reference Client app
 - [MIFOSX-469] - In Groups unable to activate the created group
 - [MIFOSX-464] - Unable to edit the created charge in "charge details" page
 
BETA (functionality related to BETA functionality
 - [MIFOSX-456] - CRUD services and UI for creating Holidays
 - [MIFOSX-411] - While creating new savings account, If "Interest posting period" is defined Annually, it is getting posted monthly.


1.3.0.RELEASE
=============
Feature Release

This feature release focussed mainly on features requested by Musoni for their upcoming m-lite product and by Quipu technolgies for their TEVI project.

Key changes are improvements to accounting and flexible support for grace concepts in loan repayment schedule.

Platform & API

Bug
 - [MIFOSX-427] - Charges and penalties functionality wonky when multiple charges/penalties are due on the same date

New Features & Improvements
 - [MIFOSX-391] - "Advanced" accounting rules for compound Journal entries
 - [MIFOSX-413] - Ability to account different charges (both fees and penalties) under different heads
 - [MIFOSX-295] - Support Prinicpal and Interest Grace Periods for Loans, Loan Schedules
 - [MIFOSX-297] - Support over payment of loan scenarios common when repayments through mobile, third party
 - [MIFOSX-374] - TEVI: Repayment strategy to support payment ordering of Interest, Principal, Penalties, Fees
 - [MIFOSX-376] - TEVI: Track additional details on the loan repayment schedule
  
Reference App
 - [MIFOSX-405] - In client's General page not able to differentiate between Activated and not Activated savings accounts.
 - [MIFOSX-416] - Distinguish between Individual and JLG loans in clients general details
 - [MIFOSX-418] - Hide/show Centers and center creation based on permissions
 - [MIFOSX-419] - Display Center summary details in UI
 - [MIFOSX-423] - Unable to view group loan in group context
 
BETA (functionality related to BETA functionality
 - [MIFOSX-344] - Add overnight scheduled task to detect if annual fee is due on savings account and apply it
 - [MIFOSX-325] - GSOC 2013 - Savings accounts server side sorting and pagination on retrieve all
 - [MIFOSX-310] - Generate Collection sheet based on meeting calendar dates
 - [MIFOSX-356] - Modify unassign staff API call in group
 - [MIFOSX-69] - Amazon S3 appender for document upload
 - [MIFOSX-395] - TEVI: Support hierarchial staff and organisational roles on staff with linkage to Application User
 
1.2.1.RELEASE
=============
Bug Release

Platform & API

Bug
 - [MIFOSX-402] - Unable to use 'days' period frequency when entering value for 'lockin' on savings product/account
 - [MIFOSX-398] - In savings account - " New saving product" is getting activated before Client activation date.
 - [MIFOSX-399] - If full balance amount is withdrawn from saving account, it is displaying negative balance as withdrawal fee is defined in saving product.
    
Reference App
 - [MIFOSX-358] - Register/Deregister datatables for groups and centers
 - [MIFOSX-396] - Not able to deposit / Withdraw amount in savings account
 - [MIFOSX-401] - While Updating the saving product if withdrawal fee is defined as "0" error message is displayed.
 - [MIFOSX-403] - While defining and updating the saving product if Annual fee is defined as "0" error message is displayed.
 - [MIFOSX-407] - For some transactions by clicking on Deposit/Withdraw, 2-3 popup pages displayed as blank.
 - [MIFOSX-410] - While defining saving product if Annual fee and date text field kept blank it is displaying error. But while editing saving product if Annual fee and date text field kept blank it is accepting.

1.2.0.RELEASE
=============
Feature Release

This feature release is focussed on 'Accounting' improvements that provide greater flexibility in use of accounting for MFIs that use mobile money and wish to be able to track through what channels repayments are disbursements go.

The BETA functionality for savings is now integrated with accounting on platform/api side.

It also contains a number of improvements to the reference app UI.

Platform & API
 - [MIFOSX-223] - Add functionality for Loan Disbursement and Repayment channels/types
 - [MIFOSX-286] - Ability to setup and use Accounting rules for Manual Journal Entries
 - [MIFOSX-338] - Tagging Journal Entries
 - [MIFOSX-392] - Not able to upload .docx (word doccument) and .xlsx (Excel doccument) in Client's "Identity Doccuments" and in "Client Doccuments"
 - [MIFOSX-328] - Loan approval date should not be earlier to the Client activation date.
 - [MIFOSX-246] - Add ability to associate fees with savings product/account
 - [MIFOSX-331] - Add capability to filter the calendars based on Calendar Type.

Reference App
 - [MIFOSX-320] - Beautify Login page
 - [MIFOSX-149] - Add status colors to UI
 - [MIFOSX-147] - Allow ability to produce easy to print loan schedule from the loan screen
 - [MIFOSX-317] - Add Tooltips that explain the significance of mapped "Accounts" in Loan Product Screens
 - [MIFOSX-347] - When creating a new GL Account, pick up on selected category from tree view
 - [MIFOSX-313] - Fix CSS issues with Accounting section of "Define New Loan Products"
 - [MIFOSX-346] - Upgrade Reference UI to JQuery UI 1.10.3 (latest stable version)
 - [MIFOSX-384] - JQuery upgrading breaking the functionality of Add LoanProduct.
 - [MIFOSX-386] - In "Add Loan Product" not able to delete the "Charges"added , similarly in "Accounting" not able to delete "Advanced accounting rules"(ie, Payment Type and Fund Source). Issue may be of latest updation, because in demo version it is working fine.

1.1.4.RELEASE
=============
Bug Release

Platform & API
 - [MIFOSX-387] - Loans template for Client not showing currencies

Reference App
 - [MIFOSX-382] - In Change Loan Application by changing the "Product" name, Details are not getting updated (ie, in Terms & settings),since "currencies" of two loan products are different
 - [MIFOSX-390] - Regression in ability to associate a fund with a loan application through reference app


1.1.3.RELEASE
=============
Bug Release

Bug
 - [MIFOSX-367] - Not able to figure out the loan details, if proper Account no. is entered in Search text field
 - [MIFOSX-368] - To add a new client of client type "corporate" error message is showing "First name is mandatory" and Last name is mandatory" than Full name or Business name.
 - [MIFOSX-369] - Client is getting activated before the date of opening of the related office.
 - [MIFOSX-379] - For applying "New Loan Application", if loan product currency is in US Dollars the charges applied is in Indian Rupees also, it is accepting.
 - [MIFOSX-380] - Smart search on Gaurantors is broken after change of clients api to paginated response

Improvement

  - [MIFOSX-359] - Edit functionality for "Code Values" not intuitive (and inconsistent with rest of the User Interface)
  - [MIFOSX-360] - Inconsistent verbiage for "Cancel" button in "Add/Edit Code Value: " page
  - [MIFOSX-363] - Background is broken in the middle of the table in "Client Loans Listing" page
 
1.1.2.RELEASE
=============
Bug Release
 - MIFOSX-353 - datetime column in custom table report causes violation
 - MIFOSX-364 - New loan application save button restriction

1.1.1.RELEASE
=============
Bug Release
 - MIFOSX-348 - General API concepts do not work for paginated endpoints

1.1.0.RELEASE
=============
Feature Release

Scope of release:

General improvements to display of audit details, pagination of list apis. New api to list loans/loan applications.

In addition to items mentioned below see 1.1.0.RELEASE on JIRA: https://mifosforge.jira.com/browse/MIFOSX/fixforversion/11932 for full details of issues addressed in release.

Core Issues:

- [MIFOSX-334] - Group loses clients when updating
- [MIFOSX-226] - Add ability to API to list loans for applications that require loan centric view of data
- [MIFOSX-339] - Allow retrieving of all items on paginated endpoints

1.0.1.RELEASE
=============
Bug Release
 - MIFOSX-343 - When viewing Audit details, integer values (rather than string values) for officeId and clientId cause exception

1.0.0.RELEASE
=============

The first community sanctioned public release of new mifos platform project ('Mifos X').

Scope of release:

In addition to items mentioned below see 1.0.0.RELEASE on JIRA: https://mifosforge.jira.com/browse/MIFOSX/fixforversion/11833 for full details of issues addressed in release.

Operational Functionality:
 - Client Loan Portfolio Management
 - Cash-based Accounting
 - Audit of all changes
 - Reporting (about 30 Reports)
 - Comprehensive and flexible ability to configure (on request) MFI specific additional client and loan data.

Organisational Functionality:
 - Currency, Funds, Offices, Staff/Loan Officers
 - Loan Products
   - support for declining balance or flat interest methods
   - Ability to configure mfi customised repayment processing
 - Charges
  - Fees & Penalties

User Admin Functionality:
 - Users, Roles, Permissions
 - 4-eye principle / Maker-Checker

0.12.1.beta
==========

Bug Release
 - MIFOSX-311 - Zero % loan products or loan applications are not allowed due to validation checks

0.11.4.beta
==========
Back ported bug fixes in 0.12.1 to 0.11.x series for production installation.

Bug Release
 - MIFOSX-311 - Zero % loan products or loan applications are not allowed due to validation checks

0.12.0.beta
==========

Focus on stabalising API and functionality around centers, groups and clients. Contains breaking changes in this API from the 0.11.x releases.

Note: known issue with existing set of reports provided out of box. Due to changes in client database 
columns for is_deleted (removed), joining_date (changed to activation_date) some of the reports do not work correctly at present. 
We will provide patches to update these.

Bugs
  - [MIFOSX-262] - You can add a client to an office prior to the opening date of the office

Improvements/Features
  - [MIFOSX-213] - Support client and group 'statuses' to enable approval workflows
  - [MIFOSX-282] - Add min max constraints to loan product and enforce for loan application creation, disbursal
  - [MIFOSX-247] - Savings account deposits
  - [MIFOSX-248] - Savings account withdrawals
  - [MIFOSX-290] - Groups api for centers/groups/communal banks
  - [MIFOSX-277] - Add API Documentation for Loan Charges
  - [MIFOSX-285] - Fix Api docs css
  - [MIFOSX-288] - Improve documentation for charges

0.11.3.beta
==========
Bug Release.
  - [MIFOSX-299] - Only super users are able to update client and loan documents

0.11.2.beta
==========
Bug Release.
  - https://mifosforge.jira.com/browse/MIFOSX-292 - Retrieving an existing loan product does not return the correct value for decimalPlaces
  - https://mifosforge.jira.com/browse/MIFOSX-293 - Calculating the Loan Schedule does not use the correct value for decimalPlaces
  - https://mifosforge.jira.com/browse/MIFOSX-294 - Updating loan products min/max principal details incorrectly results in error message relating to interestRatePerPeriod

0.11.1.beta
==========
Bug Release.
  - Revert back to hibernate 4.1.9.Final to allow datatables to be persisted
  - MIFOSX-291 - unable to create office

0.11.0.beta
==========

Focus on Individual Lending with Cash Accounting, in development features such as centers, groups, 
savings accounts and accrual accounting in progress.

Known issues 
   - Due to https://jira.springsource.org/browse/SPR-10395 datatables not persisting correct
   - Due to upgrade to hibernate 4.2, JPA specs more strict causing some entities to not persist correctly (https://mifosforge.jira.com/browse/MIFOSX-291)
