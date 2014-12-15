import sys
import operator
# print len(sys.argv)
if len(sys.argv) != 3:
	sys.exit("Usage: python aggregateScore.py fileName djangoDataFolderPath")
mapIdScore = {}

gridSize = [0,8,16,32]
for i in gridSize:
	# print 'processing file with grid ' + str(i)
	with open('GFADD/gfadd/'+sys.argv[1]+'_forG_10nn_'+str(i)+'g_resultScore.txt') as fg:
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

mapLineNum = {}
for i in range(0,10):
	mapLineNum[sortedScore[i][0]] = sortedScore[i][0]
	# print sortedScore[i][0]

mapIdData = {}
with open(sys.argv[2]+sys.argv[1]) as forig:
	with open(sys.argv[2]+sys.argv[1]+'_anomaly.txt','w') as foutA:
		lines = forig.readlines()
		i = 1
		for line in lines:
			if str(i) in mapLineNum:
				foutA.write(line)
			i = i + 1



