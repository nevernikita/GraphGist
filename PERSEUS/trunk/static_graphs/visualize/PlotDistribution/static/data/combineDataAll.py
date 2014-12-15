import sys
import math
print len(sys.argv)
if len(sys.argv) != 7:
	sys.exit("Usage: python combineDataAll.py GraphName nodeInoutdegreeFile nodePagerankFile nodeRadiusFile nodeEigenValue1File nodeEigenValue2File")
mapIdDegree = {}
with open(sys.argv[2]) as fIdDegree:
	lines = fIdDegree.readlines()
	for line in lines:
		attrs = line.split('\t')
		mapIdDegree[attrs[0]] = attrs[1][:len(attrs[1])-1]

with open(sys.argv[3]) as fPR:
	with open(sys.argv[4]) as fRadius:
		with open(sys.argv[5]) as fEV1:
			with open(sys.argv[6]) as fEV2:
				with open(sys.argv[1]+'_combine','w') as fout:
					linePR = fPR.readline()
					while linePR:
						attrs = linePR.split('\t')
						nId = attrs[0];
						pr = attrs[1][:len(attrs[1])-1]
						lineRadius = fRadius.readline()
						attrs = lineRadius.split('\t')
						radius = attrs[2][:len(attrs[2])-1]
						lineEV1 = fEV1.readline()
						attrs = lineEV1.split('\t')
						ev1 = attrs[1][:len(attrs[1])-1]
						lineEV2 = fEV2.readline()
						attrs = lineEV2.split('\t')
						ev2 = attrs[1][:len(attrs[1])-1]
						linePR = fPR.readline()
						if nId in mapIdDegree:
							fout.write(nId+'\t'+mapIdDegree[nId]+'\t'+pr+'\t'+radius+'\t'+ev1+'\t'+ev2+'\n')
						else:
							fout.write(nId+'\t0\t'+pr+'\t'+radius+'\t'+ev1+'\t'+ev2+'\n')
						