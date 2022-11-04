# CiRA tools bundles with Specmate
## Requirements
* Python >= 3.8
* PyInstaller (`pip install pyinstaller`)

## Init the tool repo
To init the CiRA tool run `init.sh`. This will install all required python packages and download the prediction models

## Start the tool
Run `start.sh --url [host] --port [port]`

Example: `start.sh --url 127.0.0.1 --port 8042`

## Make package
To distribute a package containing all dependencies and an executable, run `make_bundle.sh`. The created distribution will be created in the "dist" directiory. The executable will be called "service".

