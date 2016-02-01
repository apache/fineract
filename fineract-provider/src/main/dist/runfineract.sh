#!/bin/bash

# First of all, check if Java is installed on this computer

if hash java; then
    echo "Found Java:"
    echo $(java -version)
else
    echo "Java should be installed to run fineract."
    exit
fi

echo "Starting fineract ..... "

java -Djava.awt.headless=false -jar fineract-provider.war
