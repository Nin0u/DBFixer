#!/usr/bin/env bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

psql -d $1 -c "\i $SCRIPT_DIR/standard.sql"
psql -d $1 -c "\i $SCRIPT_DIR/oblivious.sql"
psql -d $1 -c "\i $SCRIPT_DIR/core.sql"
psql -d $1 -c "\i $SCRIPT_DIR/example.sql"
/bin/python3 $SCRIPT_DIR/gen_example.py
psql -d $1 -c "\i $SCRIPT_DIR/fill_example.sql"