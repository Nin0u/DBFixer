for i in 1 2 3
do 
    psql -d $1 -c "\i table$i.sql"
done