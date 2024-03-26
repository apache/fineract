#! /bin/bash

# Cleanup past runs intermediate files
rm -rf report-setup
rm -f report-setup.jtl
rm -f Client_to_Account.csv
rm -f Client_to_Account_to_Loan.csv
rm -f jmeter.log

READ_IP=127.0.0.1
WRITE_IP=127.0.0.1

READ_PORT=8443
WRITE_PORT=8443

jmeter -n -t Fineract-Setup.jmx -l report-setup.jtl -J"read_ip=$READ_IP" -J"write_ip=$WRITE_IP" -J"read_port=$READ_PORT" -J"write_port=$WRITE_PORT" -J"concurrent_users=10" -J"loop_count=100" -e -o report-setup
