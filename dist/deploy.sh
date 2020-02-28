#!/bin/bash

DIST_DIR=./deploy
JAR_DIR=./jars

OPTS=$1
VERSION=$2

if [ -z "$VERSION" ]
then
      echo 
      echo -e "\e[94mSpecmate Deployment Script\e[39m"
      echo 
      echo -e "Usage:   \e[92m$0 VERSION [OPTIONS]\e[39m"
      echo -e "Example: \e[92m$0 bz v0.4.0-dev-3\e[39m"
      echo
      echo -e "Options:"
      echo -e "\e[96mb\e[39m: Build specmate"
      echo -e "\e[96mr\e[39m: Create and push tag <VERSION> to git"
      echo -e "\e[96mz\e[39m: Create an example distributable with requirements and config"
      echo 
      echo -e "\e[91mRun this script from this directory.\e[39m"
      exit
fi

echo 
echo 
echo -e "\e[91m   ██████  ██▓███  ▓█████  ▄████▄   ███▄ ▄███▓ ▄▄▄     ▄▄▄█████▓▓█████ \e[39m"
echo -e "\e[91m ▒██    ▒ ▓██░  ██▒▓█   ▀ ▒██▀ ▀█  ▓██▒▀█▀ ██▒▒████▄   ▓  ██▒ ▓▒▓█   ▀ \e[39m"
echo -e "\e[91m ░ ▓██▄   ▓██░ ██▓▒▒███   ▒▓█    ▄ ▓██    ▓██░▒██  ▀█▄ ▒ ▓██░ ▒░▒███   \e[39m"
echo -e "\e[91m   ▒   ██▒▒██▄█▓▒ ▒▒▓█  ▄ ▒▓▓▄ ▄██▒▒██    ▒██ ░██▄▄▄▄██░ ▓██▓ ░ ▒▓█  ▄ \e[39m"
echo -e "\e[91m ▒██████▒▒▒██▒ ░  ░░▒████▒▒ ▓███▀ ░▒██▒   ░██▒ ▓█   ▓██▒ ▒██▒ ░ ░▒████▒\e[39m"
echo -e "\e[91m ▒ ▒▓▒ ▒ ░▒▓▒░ ░  ░░░ ▒░ ░░ ░▒ ▒  ░░ ▒░   ░  ░ ▒▒   ▓▒█░ ▒ ░░   ░░ ▒░ ░\e[39m"
echo -e "\e[91m ░ ░▒  ░ ░░▒ ░      ░ ░  ░  ░  ▒   ░  ░      ░  ▒   ▒▒ ░   ░     ░ ░  ░\e[39m"
echo -e "\e[91m ░  ░  ░  ░░          ░   ░        ░      ░     ░   ▒    ░         ░   \e[39m"
echo -e "\e[91m       ░              ░  ░░ ░             ░         ░  ░           ░  ░\e[39m"
echo -e "\e[91m                          ░                                            \e[39m"
echo 
echo 

DIR="$(pwd)"

if [[ $OPTS == *"b"* ]]
then
      cd $DIR
      sed -i "s/\"version\": \"[^\"]*\"/\"version\": \"$VERSION\"/g" ../web/package.json

      cd ../web
      npm run build-prod
      cd $DIR

      rm -rf ../bundles/*/bin* ../bundles/*/generated

      cp ../bundles/settings.gradle ../bundles/settings.gradle.bak
      sed -i "s/\(startParameter.*\)/\/\/\1/g" ../bundles/settings.gradle

      cd ../bundles
      ./gradlew --no-daemon clean build export -x check
      cd $DIR

      rm ../bundles/settings.gradle
      mv ../bundles/settings.gradle.bak ../bundles/settings.gradle

      rm -rf $JAR_DIR
      mkdir -p $JAR_DIR
      for FILE in ../bundles/specmate-std-env/generated/distributions/executable/*.jar ; do mv "$FILE" "${FILE/.jar/_$VERSION.jar}" ; done
      for FILE in ../bundles/specmate-std-env/generated/distributions/executable/*.jar ; do mv "$FILE" "$JAR_DIR" ; done
fi

if [[ $OPTS == *"r"* ]]
then
      cd $DIR
      cd ..
      rm -rf bundles/.gradle/5.4.1 bundles/.gradle/vcs-1
      git add web/package.json
      git commit -m "Release $VERSION"
      git reset --hard HEAD
      git tag -a $VERSION -m "Release $VERSION [automated deployment script]"
      git push
      git push origin $VERSION
      cd $DIR
fi

if [[ $OPTS == *"z"* ]]
then
      cd $DIR
      SPECMATE_DIST=prod-specmate-all
      rm -rf $DIST_DIR/*
      mkdir -p $DIST_DIR
      cp -r specmate* requirements $JAR_DIR/${SPECMATE_DIST}_$VERSION.jar $DIST_DIR
      sed -i "s/$SPECMATE_DIST\.jar/${SPECMATE_DIST}_$VERSION.jar/g" $DIST_DIR/specmate.*
fi
