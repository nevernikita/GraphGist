import sys
import math
print len(sys.argv)
if len(sys.argv) != 8:
	sys.exit("Usage: python combineDataAll.py GraphName nodeInoutdegreeFile nodePagerankFile nodeRadiusFile nodeEigenValue1File nodeEigenValue2File nodeEigenValue3File")
mapIdDegree = {}
mapDegreePRCount = {}
mapDegreeRadiusCount = {}
mapRadiusCount = {}
with open(sys.argv[2]) as fIdDegree:
	lines = fIdDegree.readlines()
	for line in lines:
		attrs = line.split('\t')
		mapIdDegree[attrs[0]] = attrs[1][:len(attrs[1])-1]

with open(sys.argv[3]) as fPR:
	with open(sys.argv[4]) as fRadius:
		with open(sys.argv[5]) as fEV1:
			with open(sys.argv[6]) as fEV2:
				with open(sys.argv[7]) as fEV3:
					with open(sys.argv[1]+'_forDBNode','w') as fout:
						with open(sys.argv[1]+'_degreePagerank','w') as fdegreePR:
							with open(sys.argv[1]+'_ev1ev2','w') as fev1ev2:
								with open(sys.argv[1]+'_ev2ev3','w') as fev2ev3:
									with open(sys.argv[1]+'_degreeRadius','w') as fdegreeRadius:
										with open(sys.argv[1]+'_radiusCount','w') as fradiusCount:
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
											
											for radius in mapRadiusCount:
												fradiusCount.write(radius+'\t'+str(mapRadiusCount[radius])+'\n')
											for degreePR in mapDegreePRCount:
												fdegreePR.write(degreePR+'\t'+str(mapDegreePRCount[degreePR])+'\n')
											for degreeRadius in mapDegreeRadiusCount:
												fdegreeRadius.write(degreeRadius+'\t'+str(mapDegreeRadiusCount[degreeRadius])+'\n')

						