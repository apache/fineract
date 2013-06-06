Changelog
=====

See VERSIONING (https://github.com/openMF/mifosx/blob/master/VERSIONING.md) for information on what updates to the version number implies for the platform.

Releases
===============

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
