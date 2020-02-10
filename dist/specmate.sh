#!/bin/sh

java -version >/dev/null 2>&1
if [ $? -eq 127 ]
then
    echo -e "Error: \e[91mJava not found.\e[39m Specmate needs Java 11 to run."
    exit 1
fi

JAR=prod-specmate-all.jar


if [ ! -f "$JAR" ]
then
    echo -e "Error: \e[91mjar file $JAR not found.\e[0m"
    exit 1
fi

echo -e "Status: \e[92mRunning Specmate (\e[94m$JAR\e[92m).\e[0m"

java -Djdk.crypto.KeyAgreement.legacyKDF=true -jar "$JAR" --configurationFile specmate-config.properties