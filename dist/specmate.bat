@echo off

java -version 2> nul
IF %ERRORLEVEL% EQU 9009 (
  echo Error - Java not found in your PATH
  exit /B 1
)
echo Running Specmate
java -Djdk.crypto.KeyAgreement.legacyKDF=true -jar prod-specmate-all.jar --configurationFile specmate-config.properties