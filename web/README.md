# This is the Specmate UI

## Setup 
See [Wiki](https://github.com/qualicen/specmate/wiki/How-to-setup-a-Specmate-development-environment)

## Frontend Commands
See [Wiki](https://github.com/qualicen/specmate/wiki/Frontend-Commands)

## Frontend Structure
See [Wiki](https://github.com/qualicen/specmate/wiki/Frontend-Code-Overview)

## Build

We use webpack to build the Specmate UI. The build definitions are in the ```webpack```-folder.

Entry points for building the UI are ```main.ts```, ```vendor.ts```, ```polyfills.ts```, ```assets.ts```. 

Typescript files are compiled into specmate, once they are referenced via an import-statement in one of the entry points (or referenced by a file imported into the entry points; transitively).

Assets can be imported either by referencing them in html-files, e.g. images (see index.html; an image is referenced in an ```<img>```-tag; it is automatically included in the build).

## Release
See [Wiki](https://github.com/qualicen/specmate/wiki/How-to-Setup-a-Specmate-Release-Version)
