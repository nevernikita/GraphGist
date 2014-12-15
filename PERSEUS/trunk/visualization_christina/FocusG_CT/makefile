TABLE = grid4
DB = dummy.db

OTHER= README.txt makefile minigraph.csv
SRC= demo.jar grid34.sql script.sql csv2db.bash
ALL= $(OTHER) $(SRC)

all: explore collapse christos_demo

christos_demo: minigraph.csv
	\rm -f $(DB)
	bash csv2db.bash minigraph.csv
	java -jar demo.jar explore -table grid3 -db $(DB)


dummy.db: grid34.sql
	sqlite3 dummy.db < grid34.sql

explore: dummy.db demo.jar
	java -jar demo.jar explore -table $(TABLE) -db $(DB)

collapse: dummy.db demo.jar
	java -jar demo.jar collapse

clean:
	\rm -f dummy.db

spotless: clean
	\rm -rf TST

all.tar: $(ALL)
	tar cvf all.tar $(ALL)
