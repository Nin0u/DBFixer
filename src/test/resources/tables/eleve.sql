DROP TABLE IF EXISTS Eleve;

CREATE TABLE Eleve(
    nss INTEGER,
    prenom VARCHAR(50),
    nom VARCHAR(50),
    age INTEGER
);

INSERT INTO Eleve VALUES
(0, 'Yoan', 'ROUGEOLLE', 21),
(0, 'Roan', 'YOUGEOLLE', 12),
(1, 'Nico', 'Ye', 25),
(1, 'Yico', 'Ne', 52),
(2, 'Jean', 'TRUC', 1);