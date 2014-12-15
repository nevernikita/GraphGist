import sys
import math
print len(sys.argv)
if len(sys.argv) != 3 and len(sys.argv) != 4:
	sys.exit("Usage: python combineData.py file1 file2 [-logscale]")
map1 = {}
with open(sys.argv[1]) as f1:
	with open(sys.argv[2]) as f2:
		with open(sys.argv[1]+'_'+sys.argv[2]+'_combine','w') as fout:
			i = 0
			lines1 = f1.readlines()
			lines2 = f2.readlines()
			for line in lines1:
				attrs = line.split('\t')
				map1[attrs[0]] = attrs[1]
			for line in lines2:
				i = i + 1
				attrs = line.split('\t')
				if attrs[0] in map1:
					if len(sys.argv) == 4 and sys.argv[3] == "-logscale":
						fout.write(str(i)+"\t"+str(math.log10(float(map1[attrs[0]][:len(map1[attrs[0]])-1])))+"\t"+str(math.log10(float(attrs[1])))+'\n')
					else:
						fout.write(str(i)+"\t"+map1[attrs[0]][:len(map1[attrs[0]])-1]+"\t"+attrs[1])
				else:
					if len(sys.argv) == 4 and sys.argv[3] == "-logscale":
						fout.write(str(i)+'\t0\t'+str(math.log10(float(attrs[1])))+'\n')
					else:
						fout.write(str(i)+'\t0\t'+attrs[1])