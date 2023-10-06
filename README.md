# pgdump-utils

Утилиты для преобразования дампов PostgreSQL - раскладывания по папкам, по объектам

## Примеры запуска

c:\java\jdk-17.0.2-full\bin\java -jar ../target/PGDumpUnparser-1.1-jar-with-dependencies.jar -TP -Fproject_test.sql -T -Pc:\github\pgdump-utils\_tmp\20220803\test -Oc:\github\pgdump-utils\_tmp\20220803\test\out\

Запуск с подстановками:

c:\java\jdk-17.0.2-full\bin\java -jar ../target/PGDumpUnparser-1.1-jar-with-dependencies.jar -TP -Fproject_dev.sql -T -Pc:\github\pgdump-utils\_tmp\%1\project_dev -Oc:\github\pgdump-utils\_tmp\%1\project_dev\out\ -Sproject_test_user=project_instance_user,project_dev_user=project_instance_user,project_prod_user=project_instance_user -ESEQ_COL_OWN

## Получение дампа PostgreSQL

C:\Postgres\14\bin\pg_dump.exe --file "project_dev.sql" --host "x.x.x.x" --port "5432" --username "project_owner_dev" --no-comments --no-tablespaces --schema-only --exclude-schema=cron --exclude-schema=public "project_dev_db"
