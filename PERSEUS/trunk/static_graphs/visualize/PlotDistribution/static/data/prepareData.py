import sys
# print len(sys.argv)
for arg in sys.argv[1:]:
	# print arg
	with open(arg) as f:
		with open(arg+'_processed','w') as fout:
			lines = f.readlines()
			i = 0
			for line in lines:
				i = i + 1
				fout.write(str(i)+'\t'+line)
