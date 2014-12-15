import sys
# print len(sys.argv)
if len(sys.argv) != 3:
	sys.exit("Usage: python combineData4Plot.py file1 file2")
map1 = {}
with open(sys.argv[1]) as f1:
	with open(sys.argv[2]) as f2:
		with open(sys.argv[1]+'_'+sys.argv[2]+'_combine4plot','w') as fout:
			lines1 = f1.readlines()
			lines2 = f2.readlines()
			for line in lines1:
				attrs = line.split('\t')
				map1[attrs[0]] = attrs[1]
			for line in lines2:
				attrs = line.split('\t')
				if attrs[0] in map1:
					fout.write(map1[attrs[0]][:len(map1[attrs[0]])-1]+"\t"+attrs[1])
				else:
					fout.write('1\t'+attrs[1])