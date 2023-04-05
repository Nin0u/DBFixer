# Execution des scripts SQL
#psql -d $3 -c "\i table.sql"

# Execution du code java
mvn clean
mvn compile
mvn exec:java -Dexec.args="$1 $2"