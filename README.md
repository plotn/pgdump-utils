# pgdump-utils

Утилиты для преобразования дампов PostgreSQL - раскладывания по папкам, по объектам

## Примеры запуска

c:\java\jdk-17.0.2-full\bin\java -jar ../target/PGDumpUnparser-1.1-jar-with-dependencies.jar -TP -Fjaga_test.sql -T -Pc:\github\pgdump-utils\_tmp\20220803\test -Oc:\github\pgdump-utils\_tmp\20220803\test\out\

## Получение дампа PostgreSQL

C:\Postgres\14\bin\pg_dump.exe --file "jaga_dev.sql" --host "10.42.126.34" --port "5432" --username "jaga_owner_dev" --no-comments --no-tablespaces --schema-only --exclude-schema=cron --exclude-schema=public "jaga_dev_db"