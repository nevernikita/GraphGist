import sys
if len(sys.argv) != 3:
	sys.exit("Usage: python prepareEdgeData.py graphDataFile dataFolderPath")

with open(sys.argv[1]) as f:
	with open(sys.argv[2]+'edges','w') as fout:
		i = 1
		lines = f.readlines()
		for line in lines:
			if not line.startswith('#'):
				fout.write(str(i)+'\t'+line)
				i = i + 1
