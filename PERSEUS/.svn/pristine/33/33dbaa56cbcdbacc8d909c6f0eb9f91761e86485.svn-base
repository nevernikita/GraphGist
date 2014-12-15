
/* from Namit, 4/11/2013 */

It can be run as java -jar demo.jar {explore/collapse}

The "explore" option allows the interactive exploration of the graph where one starts with a few seed nodes and clicking on a node displays it's neighbors.

The "collapse" options displays a static graph and one can select multiple nodes by dragging a box around them and clicking the collapse button at the bottom.

--- from christos, 4/15/2013 ---
it also needs an sqlite db
    dummy.db
with table
    create table grid4 (source, destination, weight);

--- from Namit, 4/16/2013 ---
I fixed some issues in my code and I think it shouldn't raise any exceptions now. I have also modified the makefile to include two commands (replaced run and run2): make explore and make collapse that run as mentioned above. 

The database is only useful for "make explore" - make collapse uses a graph which was a part of the library. I currently have two tables in the database: 3x3 grid and a 4x4 grid. I have added an option to specify the database and the table on command-line. One can run "make explore TABLE=grid3 DB=./dummy.db" to change the table or the database being used. In case they are not specified, table grid4 from dummy.db is used as default.

--- from Christina, 5/13/2013 ---
changed ConditionInvestigator.csv -> is now <condition | investigatorname>
changed csv2db, delimits with |


