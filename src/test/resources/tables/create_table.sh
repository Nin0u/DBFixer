#!/usr/bin/env bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

psql -d $1 -c "\i $SCRIPT_DIR/standard.sql"
psql -d $1 -c "\i $SCRIPT_DIR/oblivious.sql"
psql -d $1 -c "\i $SCRIPT_DIR/core.sql"
psql -d $1 -c "\i $SCRIPT_DIR/core2.sql"
psql -d $1 -c "\i $SCRIPT_DIR/example.sql"
psql -d $1 -c "\i $SCRIPT_DIR/eleve.sql"

# Mettre des 0 lorsque c'est inutile pour éviter d'insérer quoique ce soit
/bin/python3 $SCRIPT_DIR/gen_example.py
psql -d $1 -c "\i $SCRIPT_DIR/fill_example.sql"