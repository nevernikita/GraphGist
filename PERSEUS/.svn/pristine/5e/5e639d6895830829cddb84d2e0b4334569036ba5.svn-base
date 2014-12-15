SRC= anonymize-ibm.pl inp.txt
AUXILIARY= makefile inp.txt
ALL= $(SRC) $(AUXILIARY)

top: demo

demodiskf: anonymize-ibm.pl inp.txt
	anonymize-ibm.pl -p -d -c1,2 inp.txt

demo: anonymize-ibm.pl inp.txt
	anonymize-ibm.pl -c1,2 inp.txt


all.tar: $(ALL)
	tar cvf all.tar $(ALL)

clean:
	\rm -rf TST

spotless: clean
	\rm -f *.dbm
