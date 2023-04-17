./src/test/resources/tables/create_table.sh $1
./exec.sh -dblp=src/test/resources/login/$1.login -dfp=src/test/resources/df/oblivious.test -mode=$2
