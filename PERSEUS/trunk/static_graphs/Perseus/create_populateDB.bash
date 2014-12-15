#!/bin/bash


folder=$1

cd $folder
rm $folder.gdb

sqlite3 $folder.gdb < ../pegasus_schema.sql
sqlite3 $folder.gdb < ../statistics_import.sql
sqlite3 $folder.gdb < ../perseus_populate_statistics_table.sql

awk -F"\t" 'BEGIN { FS = "\t"; OFS="\t"} ; NR>1{ $2++; $3++; $4++; $5++; $6++; $7++; $8++; $9++; $10++; $11++; $12++; $13++; $14++; $15++; $16++; $17++; $18++; $19++; print $0 }' all_statistics.csv > all_statistics_IncBy1.csv
