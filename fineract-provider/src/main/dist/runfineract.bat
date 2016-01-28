java -version >nul 2>&1 && (
    echo "Found Java."
) || (
    echo "Java should be installed to run Fineract."
    exit
)

echo "Starting Fineract ..... "

java -Djava.awt.headless=false -jar fineract-provider.war
