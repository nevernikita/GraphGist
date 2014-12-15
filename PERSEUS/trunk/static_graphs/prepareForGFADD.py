import sys
import math
if len(sys.argv) != 3:
	sys.exit("Usage: python prepareForGFADD.py DjangoDataFolderPath GFADDFolderPath")

# plot data files: degreeCount, degreePagerank, degreeRadius, radiusCount, ev1ev2, ev2ev3
# plot data files with line number: degreeCount_forG, degreePagerank_forG, degreeRadius_forG, radiusCount_forG, ev1ev2_forG, ev2ev3_forG

i = 1
with open(sys.argv[1]+'degreeCount') as fin:
	with open(sys.argv[2]+'degreeCount_forG','w') as fout:
		lines = fin.readlines()
		for line in lines:
			attrs = line.split('\t')
			d = float(attrs[0])
			d = str(math.exp(math.log10(d+1))-1)
			c = float(attrs[1])
			c = str(math.exp(math.log10(c))-1)
			fout.write(str(i)+'\t'+d+'\t'+c+'\n')
			i = i + 1

i = 1
with open(sys.argv[1]+'degreePagerank') as fin:
	with open(sys.argv[2]+'degreePagerank_forG','w') as fout:
		lines = fin.readlines()
		for line in lines:
			attrs = line.split('\t')
			d = float(attrs[0])
			d = str(math.exp(math.log10(d+1))-1)
			pr = float(attrs[1])
			pr = str(math.exp(math.log10(pr))-1)
			fout.write(str(i)+'\t'+d+'\t'+pr+'\n')
			i = i + 1

i = 1
with open(sys.argv[1]+'degreeRadius') as fin:
	with open(sys.argv[2]+'degreeRadius_forG','w') as fout:
		lines = fin.readlines()
		for line in lines:
			attrs = line.split('\t')
			d = float(attrs[0])
			d = str(math.exp(math.log10(d+1))-1)
			r = float(attrs[1])
			r = str(math.exp(r)-1)
			fout.write(str(i)+'\t'+d+'\t'+r+'\n')
			i = i + 1

i = 1
with open(sys.argv[1]+'radiusCount') as fin:
	with open(sys.argv[2]+'radiusCount_forG','w') as fout:
		lines = fin.readlines()
		for line in lines:
			attrs = line.split('\t')
			r = float(attrs[0])
			r = str(math.exp(r)-1)
			c = float(attrs[1])
			c = str(math.exp(math.log10(c))-1)
			fout.write(str(i)+'\t'+r+'\t'+c+'\n')
			i = i + 1

i = 1
with open(sys.argv[1]+'ev1ev2') as fin:
	with open(sys.argv[2]+'ev1ev2_forG','w') as fout:
		lines = fin.readlines()
		for line in lines:
			attrs = line.split('\t')
			ev1 = float(attrs[0])
			ev1 = str(math.exp(ev1)-1)
			ev2 = float(attrs[1])
			ev2 = str(math.exp(ev2)-1)
			fout.write(str(i)+'\t'+ev1+'\t'+ev2+'\n')
			i = i + 1

i = 1
with open(sys.argv[1]+'ev2ev3') as fin:
	with open(sys.argv[2]+'ev2ev3_forG','w') as fout:
		lines = fin.readlines()
		for line in lines:
			attrs = line.split('\t')
			ev2 = float(attrs[0])
			ev2 = str(math.exp(ev2)-1)
			ev3 = float(attrs[1])
			ev3 = str(math.exp(ev3)-1)
			fout.write(str(i)+'\t'+ev2+'\t'+ev3+'\n')
			i = i + 1