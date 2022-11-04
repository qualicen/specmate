#!/bin/bash
source venv/bin/activate
python service.py "$@"
deactivate
