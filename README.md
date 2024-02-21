# DBFixer
A Database Fixer that given functional dependencies, apply SQL requests in ordre to make the DB consistant.

## Prerequisite
- An Unix System
- Java
- PostgreSQL

## Features
The program implements 5 variants of Chase algorithm which are :
- Standard Chase (mode 0)
- Oblivious Chase (mode 1)
- Skolem Chase (mode 2)
- Core Chase (mode 3)

## How to use
- Write your DFs in a file (1 DF/line)
- Optionnal : Write a file containing you login to the DB
- run at the root of the project :
```
  ./exec.sh -dfp="Path_to_DF_file" -dblp="Path_to_login_file" -mode={A value between 0 and 3}
```
`-mode` accepts 4 as value, but the program will run all the algorithms which is useless in practice.
