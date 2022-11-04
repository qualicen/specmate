#!/bin/bash
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
deactivate
mkdir -p bin
wget -O bin/cira-models.zip https://zenodo.org/record/7186287/files/cira-models.zip
unzip -d bin bin/cira-models.zip
rm bin/cira-models.zip
