# database ownership
POSTGRESQL_PORT=5432
POSTGRESQL_USER=awips
AWIPS_DEFAULT_GROUP="fxalpha"

# AWIPS II installations
POSTGRESQL_INSTALL="/awips2/postgresql"
PSQL_INSTALL="/awips2/psql"
AWIPS2_DATA_DIRECTORY="/awips2/data"
DATABASE_INSTALL="/awips2/database"

# SQL Data Directories
METADATA=${AWIPS2_DATA_DIRECTORY}/metadata
IFHS=${AWIPS2_DATA_DIRECTORY}/pgdata_ihfs
MAPS=${AWIPS2_DATA_DIRECTORY}/maps
DAMCAT=${AWIPS2_DATA_DIRECTORY}/damcat
HMDB=${AWIPS2_DATA_DIRECTORY}/hmdb
EBXML=${AWIPS2_DATA_DIRECTORY}/ebxml

# paths
POSTGIS_CONTRIB=${POSTGRESQL_INSTALL}/share/contrib/postgis-2.2

# executables
PERL=/usr/bin/perl
PG_RESTORE=${POSTGRESQL_INSTALL}/bin/pg_restore
PG_DUMP=${POSTGRESQL_INSTALL}/bin/pg_dump
DROPDB=${POSTGRESQL_INSTALL}/bin/dropdb
VACUUMDB=${POSTGRESQL_INSTALL}/bin/vacuumdb
POSTGIS_RESTORE=${POSTGIS_CONTRIB}/postgis_restore.pl
PSQL=${PSQL_INSTALL}/bin/psql
