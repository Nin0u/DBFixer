# Execute le code java
mvn compile -q
mvn exec:java -Dexec.args="$1 $2 $3" -q