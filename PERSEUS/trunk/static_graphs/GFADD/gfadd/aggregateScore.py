import sys
import operator
print len(sys.argv)
if len(sys.argv) != 2:
	sys.exit("Usage: python aggregateScore.py fileName")
mapIdScore = {}

gridSize = [0,8,16,32]
for i in gridSize:
	print 'processing file with grid ' + str(i)
	with open(sys.argv[1]+'_10nn_'+str(i)+'g_resultScore.txt') as fg:
		if i == 0:
			lines = fg.readlines();
			for line in lines:
				attrs = line.split('\t')
				nId = attrs[2]
				score = float(attrs[5])
				mapIdScore[nId] = score
		else:
			lines = fg.readlines();
			for line in lines:
				attrs = line.split('\t')
				nId = attrs[2]
				score = float(attrs[5])
				mapIdScore[nId] = mapIdScore[nId] + score

sortedScore = sorted(mapIdScore.items(), key=operator.itemgetter(1))

mapIdData = {}
with open('../'+sys.argv[1]) as forig:
	lines = forig.readlines()
	for line in lines:
		attrs = line.split('\t',1)
		mapIdData[attrs[0]] = attrs[1]
	with open(sys.argv[1]+'_aggregate_resultScore.txt','w') as fout:
		with open('../'+sys.argv[1]+'_anomaly.txt','w') as foutA:
			for i in range(0,10):
				fout.write(sortedScore[i][0] + '\t' + str(sortedScore[i][1]) + '\n')
				foutA.write(mapIdData[sortedScore[i][0]])



