java -version >nul 2>&1 && (
    echo "Found Java."
) || (
    echo "Java should be installed to run MifosX."
    exit
)

echo "Starting MifosX ..... "

java -Djava.awt.headless=false -jar mifosng-provider.war
