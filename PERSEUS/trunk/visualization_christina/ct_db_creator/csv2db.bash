#!/bin/bash

################################
# Author: christos faloutsos
# Date: 4/29 2013
# slightly modified by c.c.
################################

#
# goal: turn a csv file of edges, into a suitable sqlite3 db
#

if [ $# -ne 1 ] ; then
    echo "ERROR: $0 csv-fname"
    exit 1
fi

csv_fname=$1
if [ ! -f $csv_fname ] ; then
    echo "ERROR $0 : fname '${csv_fname}' not a file"
    exit 1
fi

db_fname="ct.db"

\rm -f ${db_fname}

sqlite3 ${db_fname} <<EOS
drop table if exists grid3;
-- create table grid3 (source INTEGER, destination INTEGER);
create table grid3 (source varchar(20), destination varchar(100));
insert into grid3 values (1, 2);
.header on
-- select * from grid3;
.separator "|"
.import "${1}" grid3
.mode column
-- select * from grid3;
alter table grid3 add column weight DOUBLE default 1.0;
-- select * from grid3;

-- just to keep the jar script happy...
drop table if exists grid4;
-- create table grid4 (source INTEGER, destination INTEGER, weight DOUBLE default 1.0);
create table grid4 (source varchar(20), destination varchar(100), weight DOUBLE default 1.0);
.quit
EOS

echo "    $0 checking: here is the edge file "
sqlite3 ${db_fname} <<EOS2
.headers on
.mode column
select * from grid3;
EOS2
