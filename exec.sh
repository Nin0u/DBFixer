mvn clean
mvn compile
mvn exec:java -Dexec.args="$1 $2"