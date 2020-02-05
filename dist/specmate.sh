#!/bin/sh

javas -version >/dev/null 2>&1
if [ $? -eq 127 ]
then
    echo "Java not found."
    exit 1
fi

echo "Running Specmate"
#java -Djdk.crypto.KeyAgreement.legacyKDF=true -jar prod-specmate-all.jar --configurationFile specmate-config.properties