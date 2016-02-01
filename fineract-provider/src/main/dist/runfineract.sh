#!/bin/bash

# First of all, check if Java is installed on this computer

if hash java; then
    echo "Found Java:"
    echo $(java -version)
else
    echo "Java should be installed to run MifosX."
    exit
fi

echo "Starting MifosX ..... "

java -Djava.awt.headless=false -jar mifosng-provider.war
