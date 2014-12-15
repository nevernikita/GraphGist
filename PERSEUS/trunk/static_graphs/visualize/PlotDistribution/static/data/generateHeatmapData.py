with open('part-00000-rankindegree') as f:
	with open('testHeatmap','w') as fout:
		lines = f.readlines()
		for line in lines:
			fout.write(line[:len(line)-1]+'\t'+line.split('\t')[0]+'\n')