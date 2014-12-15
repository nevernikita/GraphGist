GRAPHDATA=Perseus/soc-Epinions1.txt
DATADIRS=Perseus/soc-Epinions1
DJANGODATAPTH=visualize/PlotDistribution/static/data/



demo: 
	# prepare data
	# input paras: nodeInoutdegreeFile nodePagerankFile nodeRadiusFile nodeEigenValue1File nodeEigenValue2File nodeEigenValue3File
	python $(DJANGODATAPTH)prepareDataAll.py $(DATADIRS)/dd_node_deg_inout_local/part-00000  $(DATADIRS)/pr_vector_madeSym_local/part-00000  $(DATADIRS)/hadi_output_orig_local/part-00000  $(DATADIRS)/rz_u_eig_1/part-00000  $(DATADIRS)/rz_u_eig_2/part-00000  $(DATADIRS)/rz_u_eig_3/part-00000 $(DJANGODATAPTH)
	# populate database
	python $(DJANGODATAPTH)prepareEdgeData.py $(GRAPHDATA) $(DJANGODATAPTH)
	sqlite3 visualize/db.sqlite3 < popDB.sql
	python visualize/manage.py runserver

gfaddTest:
	# prepare data for GFADD
	python prepareForGFADD.py $(DJANGODATAPTH) GFADD/
	# run GFADD
	cd GFADD; bash runGFADD.sh;
	python aggregateScore.py degreeCount $(DJANGODATAPTH)
	python aggregateScore.py degreePagerank $(DJANGODATAPTH)
	python aggregateScore.py radiusCount $(DJANGODATAPTH)
	python aggregateScore.py degreeRadius $(DJANGODATAPTH)
	python aggregateScore.py ev1ev2 $(DJANGODATAPTH)
	python aggregateScore.py ev2ev3 $(DJANGODATAPTH)
		
	



