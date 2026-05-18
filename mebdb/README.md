This document describes how to run database migrations with flyway locally.

In order to run the migrations, the following roles need to exist within the database:

- BFS_ADMIN_ROLE
- BFS_ADMIN
- MEB_APPL_ROLE

In Oracle, roles can be created like this: `create role <role_name>;`

Before flyway migrations can be run, the database password must be changed to the correct one in the conf/flyway.conf.local file.
Once this has been done, open up a terminal (e.g. terminal in IntelliJ), go to the 'mebdb' folder and then run the following:

  `mvn flyway:info -D flyway.configFile=conf/flyway.conf.local`

This command will validate the configuration and show the status of all migrations (defined in the migrations directory 'mebdb/database/scripts')

  `mvn flyway:migrate -D flyway.configFile=conf/flyway.conf.local`

This will run the actual migrations and output the result.
