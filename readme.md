Work in progress Answer for [Stackoverflow](https://stackoverflow.com/questions/72915623)

This project assumes that a MySQL Server Version 8 is locally listening on a port 3306.

````shell
$ docker run --rm -it -e MYSQL_ROOT_PASSWORD=stackoverflow -e MYSQL_DATABASE=stackoverflow -p 3306:3306 mysql:8
````