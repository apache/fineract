# fineract-jmeter-suites

Contents of the jmeter suite are organized as below:
	1. Fineract-Setup.jmx
		Jmeter test suite for creating required bank setup viz., ledger accounts, financial aactivity accounting, codes, loan products, savings product, creating clients,  

	2. Fineract-Transactions.jmx
		Jmeter test suite for performing banking transactions viz., Saving deposits, With drawals, Balance enquiry (Get savings account), Get account with all associations, Post interest on accounts, Disburse loans, Undo disburse lons etc.

	3. runSetup.sh
		Script to initiate the banking setup.
		Change the IP and Port numbers in the script according to the instance of Fineract you are pointing it against. 
		When trying to re-run the script repetitively, stop fineract instance, cleanup the Db content to make sure already defined products, codes etc. doesnt fail the APIs.

	4. runTransactions.sh
		Script to initiate the transactions on the previously setup Fineract instance.
		Transactions can be run only after the setup is done.

	5. AccountNum.csv
		List of account numbers specified within the file which is read by the jmeter suite for purpose of concurrent execution of different use cases.

	6. exportRuntimeEnv.sh 
		Use this file to set JMETER and JAVA_HOME path
		Modify the path of JMETER and JAVA_HOME according to your system. 				


Note:

1. Before starting the jmeter suite through the scripts, make sure to set the Java and Jmeter paths through exportRuntimeEnv.sh
2. During re-run of the setup, stop your fineract instance and cleanup the DB contents. This is to avoid failures in setup APIs which try to create codes and products which were already present in the database.
	DROP DATABASE fineract_default; DROP DATABASE fineract_tenants;
	CREATE DATABASE fineract_default; CREATE DATABASE fineract_tenants;

Concurrency and Loop count:
---------------------------
In both setup and transactions you can set the concurrent users and the loop count for the APIs by adjusting the 'concurrent_users' and 'loop_count' parameters within the runSetup.sh and runTransactions.sh scripts.
Make sure to set the values in such a way that the multiple of them doesn't exceed the value set during Setup.

	-J"concurrent_users=10" -J"loop_count=100"

For example if concurrent_users=10 and loop_count=100 during setup, there will be a total of 1000 Client/Account/Loans be created and make sure to limit them to 1000 during transactions as well (multiple of concurrent_users and loop_count not be more than 1000).
The multiple of them can be upto a million Clients/Accounts/Loans during setup.

Example response:
-----------------


# ./runSetup.sh
Creating summariser <summary>
Created the tree successfully using Fineract-Setup.jmx
Starting standalone test @ 2024 Feb 13 13:21:31 IST (1707810691626)
Waiting for possible Shutdown/StopTestNow/HeapDump/ThreadDump message on port 4445
summary +      1 in 00:00:02 =    0.5/s Avg:  1389 Min:  1389 Max:  1389 Err:     0 (0.00%) Active: 1 Started: 1 Finished: 0
summary +   5727 in 00:00:26 =  218.2/s Avg:    36 Min:     5 Max:  1092 Err:     0 (0.00%) Active: 10 Started: 66 Finished: 56
summary =   5728 in 00:00:28 =  203.1/s Avg:    36 Min:     5 Max:  1389 Err:     0 (0.00%)
summary +    284 in 00:00:01 =  326.8/s Avg:    23 Min:    14 Max:    32 Err:     0 (0.00%) Active: 0 Started: 66 Finished: 66
summary =   6012 in 00:00:29 =  206.8/s Avg:    35 Min:     5 Max:  1389 Err:     0 (0.00%)
Tidying up ...    @ 2024 Feb 13 13:22:00 IST (1707810720872)
... end of run



# ./runTransactions.sh
Creating summariser <summary>
Created the tree successfully using Fineract-Transactions.jmx
Starting standalone test @ 2024 Feb 13 13:22:20 IST (1707810740549)
Waiting for possible Shutdown/StopTestNow/HeapDump/ThreadDump message on port 4445
summary +   5527 in 00:00:09 =  595.5/s Avg:     7 Min:     1 Max:   130 Err:     0 (0.00%) Active: 8 Started: 60 Finished: 52
summary +   2474 in 00:00:08 =  305.6/s Avg:    23 Min:     4 Max:   337 Err:     0 (0.00%) Active: 0 Started: 81 Finished: 81
summary =   8001 in 00:00:17 =  460.5/s Avg:    12 Min:     1 Max:   337 Err:     0 (0.00%)
Tidying up ...    @ 2024 Feb 13 13:22:38 IST (1707810758097)
... end of run


Look for any errors from 'Err:     0 (0.00%)'  above.
Also verify the failures if any from the report-setup.jtl and report-transactions.jtl files.

