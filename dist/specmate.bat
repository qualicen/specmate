@echo off

java -version 2> nul
IF %ERRORLEVEL% EQU 9009 (
  echo Error: [91mJava not found.[0m Specmate needs Java 11 to run.
  exit /B 1
)

set JAR=prod-specmate-all.jar

if exist %JAR% (
    rem file exists
) else (
    echo Error: [91mjar file %JAR% not found.[0m
    exit /B 1
)

echo Status: [92mRunning Specmate ([94m%JAR%[92m).[0m

java -Djdk.crypto.KeyAgreement.legacyKDF=true -jar %JAR% --configurationFile specmate-config.properties