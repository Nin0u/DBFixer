import random as rd

def rand_date() :
    annee = rd.randint(1900,2023)
    mois = rd.randint(1,12)
    if (mois % 2 == 1 and mois < 8) or (mois % 2 == 0 and mois > 7) : 
        jour = format(rd.randint(1,31), '02d')
    elif mois == 2 and ((annee % 4 == 0 and annee % 100 != 0) or(annee % 400 == 0)) :
        jour = format(rd.randint(1,29))
    elif mois == 2 :
        jour = format(rd.randint(1,28))
    else :
        jour = format(rd.randint(1,30), '02d')
    annee = str(annee)
    mois = format(mois, '02d')

    return jour,mois,annee
    
if __name__ == "__main__" :
    path = __file__[0:len(__file__) - 14]
    out = open(path + "fill_example.sql", "w")

    # On génère les personnes
    nb_personnes = int(input("Nombres d'entrées pour Personne : "))
    nom = open(path + "noms.txt", "r").read().splitlines()
    prenom = open(path + "prenoms.txt", "r").read().splitlines()
    for i in range(nb_personnes) :
        insert = "INSERT INTO Personne VALUES("
        insert += "'" + nom[rd.randint(0, len(nom) - 1)] + "', "
        insert += "'" + prenom[rd.randint(0, len(prenom) - 1)] + "', "

        # Date de naissance random
        jour,mois,annee = rand_date()
        insert += "TO_DATE('" + jour + "/" + mois + "/" + annee + "', 'DD/MM/YYYY')" + ", "

        # nss
        insert += str(i) + ");"
        out.write(insert + "\n")

    # On généère les scrutins
    nb_scrutins = int(input("Nombres d'entrées pour Scutin : "))
    for i in range(nb_scrutins) :
        insert = "INSERT INTO Scrutin VALUES("
        insert += "'Reférundum " + str(rd.randint(2000,2023)) + "', "

        jour,mois,annee = rand_date()
        insert += "TO_DATE('" + jour + "/" + mois + "/" + annee + "', 'DD/MM/YYYY')" + ", "
        insert += str(rd.randint(7,28)) + ");"
        out.write(insert + "\n")
    
    # On génère les votes
    nb_votes = int(input("Nombres d'entrées pour Vote : "))
    for _ in range(nb_votes) :
        insert = "INSERT INTO Vote VALUES("
        insert += str(rd.randint(0, 2 * nb_personnes)) + ", "
        insert += "'Reférundum " + str(rd.randint(1998,2030)) + "', "
        insert += "'Oui');" if rd.randint(0,1)%2 == 0 else "'Non');"
        out.write(insert + "\n")