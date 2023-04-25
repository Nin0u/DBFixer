# Execute un test 
# $1 = Le fichier login pour la bd : contient nom et mot de passe
# $2 = Le fichier contenant les df
# $3 = Le mode de Chase à tester
# $4 = Le nom de la BD

echo "==== Création des tables ===="
./src/test/resources/tables/create_table.sh $4
echo "============ Fin ============"
echo

./exec.sh -dblp=src/test/resources/login/$1.login -dfp=src/test/resources/df/$2.test -mode=$3