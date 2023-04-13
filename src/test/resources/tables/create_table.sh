#!/usr/bin/env bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

psql -d $1 -c "\i $SCRIPT_DIR/standard.sql"
psql -d $1 -c "\i $SCRIPT_DIR/oblivious.sql"