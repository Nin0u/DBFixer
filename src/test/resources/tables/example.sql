DROP TABLE IF EXISTS Personne;
DROP TABLE IF EXISTS Scrutin;
DROP TABLE IF EXISTS Vote;

CREATE TABLE Personne(
    nom VARCHAR,
    prenom VARCHAR,
    date_naissance DATE,
    nss INTEGER
);

CREATE TABLE Scrutin(
    nom_scrutin VARCHAR,
    date_debut DATE,
    duree_jours INTEGER
);

CREATE TABLE Vote(
    nss INTEGER,
    nom_scrutin VARCHAR,
    reponse VARCHAR
);