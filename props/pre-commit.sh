#AuthorMullners
echo "Running pre-commit analysis..."

./gradlew clean licenseMain licenseTest licenseIntegrationTest check && ./gradlew integrationTest

status=$?

if [ "$status" = 0 ] ; then
    echo "Pre-commit analysis found no problems, ready to push code."
    exit 0
else
    echo 1>&2 "Pre-commit analysis found violations it could not fix, time to work."
    exit 1
fi
