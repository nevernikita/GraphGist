import sys
import math
if len(sys.argv) != 8:
	sys.exit("Usage: python combineDataAll.py nodeInoutdegreeFile nodePagerankFile nodeRadiusFile nodeEigenValue1File nodeEigenValue2File nodeEigenValue3File dataFolderPath")
mapIdDegree = {}
mapDegreeCount = {}
mapDegreePRCount = {}
mapDegreeRadiusCount = {}
mapRadiusCount = {}
with open(sys.argv[1]) as fIdDegree:
	lines = fIdDegree.readlines()
	for line in lines:
		attrs = line.split('\t')
		degree  = attrs[1][:len(attrs[1])-1]
		mapIdDegree[attrs[0]] = degree
		if degree in mapDegreeCount:
			mapDegreeCount[degree] = mapDegreeCount[degree] + 1
		else:
			mapDegreeCount[degree] = 1

i = 1
with open(sys.argv[7]+'degreeCount','w') as fdegreeCount:
	with open(sys.argv[7]+'degreeCount_forDB','w') as fdegreeCountDB:
		for degree in mapDegreeCount:
			fdegreeCount.write(degree+'\t'+str(mapDegreeCount[degree])+'\n')
			fdegreeCountDB.write(str(i)+'\t'+degree+'\t'+str(mapDegreeCount[degree])+'\n')
			i = i + 1


with open(sys.argv[2]) as fPR:
	with open(sys.argv[3]) as fRadius:
		with open(sys.argv[4]) as fEV1:
			with open(sys.argv[5]) as fEV2:
				with open(sys.argv[6]) as fEV3:
					with open(sys.argv[7]+'forDBNode','w') as fout:
						with open(sys.argv[7]+'degreePagerank','w') as fdegreePR:
							with open(sys.argv[7]+'ev1ev2','w') as fev1ev2:
								with open(sys.argv[7]+'ev2ev3','w') as fev2ev3:
									with open(sys.argv[7]+'degreeRadius','w') as fdegreeRadius:
										with open(sys.argv[7]+'radiusCount','w') as fradiusCount:
											with open(sys.argv[7]+'radiusCount_forDB','w') as fradiusCountDB:
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
													
													lineEV3 = fEV3.readline()
													attrs = lineEV3.split('\t')
													ev3 = attrs[1][:len(attrs[1])-1]
													
													linePR = fPR.readline()
													
													fev1ev2.write(ev1+'\t'+ev2+'\n')
													fev2ev3.write(ev2+'\t'+ev3+'\n')

													if radius in mapRadiusCount:
														mapRadiusCount[radius] = mapRadiusCount[radius] + 1
													else:
														mapRadiusCount[radius] = 1
													
													if nId in mapIdDegree:
														degreePR = mapIdDegree[nId]+'\t'+pr
														degreeRadius = mapIdDegree[nId]+'\t'+radius
														fout.write(nId+'\t'+mapIdDegree[nId]+'\t'+pr+'\t'+radius+'\t'+ev1+'\t'+ev2+'\t'+ev3+'\n')
													else:
														degreePR = '0\t'+pr
														degreeRadius = '0\t'+radius
														fout.write(nId+'\t0\t'+pr+'\t'+radius+'\t'+ev1+'\t'+ev2+'\t'+ev3+'\n')
													
													if degreePR in mapDegreePRCount:
														mapDegreePRCount[degreePR] = mapDegreePRCount[degreePR] + 1
													else:
														mapDegreePRCount[degreePR] = 1
													
													if degreeRadius in mapDegreeRadiusCount:
														mapDegreeRadiusCount[degreeRadius] = mapDegreeRadiusCount[degreeRadius] + 1
													else:
														mapDegreeRadiusCount[degreeRadius] = 1
												i = 1
												for radius in mapRadiusCount:
													fradiusCount.write(radius+'\t'+str(mapRadiusCount[radius])+'\n')
													fradiusCountDB.write(str(i)+'\t'+radius+'\t'+str(mapRadiusCount[radius])+'\n')
													i = i + 1
												for degreePR in mapDegreePRCount:
													fdegreePR.write(degreePR+'\t'+str(mapDegreePRCount[degreePR])+'\n')
												for degreeRadius in mapDegreeRadiusCount:
													fdegreeRadius.write(degreeRadius+'\t'+str(mapDegreeRadiusCount[degreeRadius])+'\n')

						