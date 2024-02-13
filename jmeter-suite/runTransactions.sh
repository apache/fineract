#! /bin/bash


rm -f report-transactions.jtl
rm -rf report-transactions

READ_IP=127.0.0.1
WRITE_IP=127.0.0.1

READ_PORT=8443
WRITE_PORT=8443

jmeter -n -t Fineract-Transactions.jmx -l report-transactions.jtl -J"read_ip=$READ_IP" -J"write_ip=$WRITE_IP" -J"protocol=https" -J"read_port=$READ_PORT" -J"write_port=$WRITE_PORT" -J"concurrent_users=10" -J"loop_count=100" -e -o report-transactions
