from django.shortcuts import render
from django.http import HttpResponse
from django.db import connections
from django.core.exceptions import ObjectDoesNotExist
from django.db.utils import ConnectionDoesNotExist  
from django.db.models import F
from django.db.models import Q
import PlotDistribution
from PlotDistribution.models import *
from sets import Set
import time
import random
import json

# Create your views here.

def home(request):
	return render(request,"PlotDistribution/index.html", {})

def Plot(request):
	return render(request, "PlotDistribution/Plot.html", {})

def DBPlot(request):
	return render(request, "PlotDistribution/DBPlot.html", {})

def MultiPlots(request):
	return render(request, "PlotDistribution/MultiPlots.html", {})

def Heatmap(request):
	return render(request, "PlotDistribution/Heatmap.html", {})

def Egonet(request):
	return render(request, "PlotDistribution/Egonet.html", {})

def GetEgonet(request):
	print "enter GetEgonet"
	start_time = time.time()
	resultStr = ""
	nodeid = int(request.GET.get('nodeid',0))
	print nodeid
	outedges = Edge.objects.filter(fromNode = nodeid)
	inedges = Edge.objects.filter(toNode = nodeid)
	nodes = Set([])
	# nodes = Set([nodeid])
	for oe in outedges:
		nodes.add(oe.toNode)
	for ie in inedges:
		nodes.add(ie.fromNode)
	print len(nodes)
	# sample the neighbor
	sampleNum = 10;
	if len(nodes) > sampleNum:
		nodes = random.sample(nodes,sampleNum)
		nodes.append(nodeid)
	else:
		nodes.add(nodeid)
	validEdges = Edge.objects.filter(fromNode__in=nodes, toNode__in=nodes)
	for e in validEdges:
		resultStr = resultStr + str(e.fromNode) + "\t" + str(e.toNode)+ "\n"
	# for n1 in nodes:
	# 	for n2 in nodes:
	# 		try:
	# 			validedge = Edge.objects.filter(fromNode = n1).get(toNode = n2)
	# 			resultStr = resultStr + str(n1) + "\t" + str(n2)+ "\n"
	# 		except Edge.DoesNotExist:
	# 			# do nothing
	# 			print ""
	print len(resultStr)
	print("--- %s seconds ---" % str(time.time() - start_time))
	return HttpResponse(resultStr, content_type="text/plain")

def GetPlotData(request):
	dataCount = {}
	nodes = Node.objects.all()
	for node in nodes:
		key = str(node.inoutdegree)+'\t'+str(node.pagerank)
		if key in dataCount:
			dataCount[key] = dataCount[key] + 1
		else:
			dataCount[key] = 1
	resultStr = ""
	for key in dataCount:
		resultStr = resultStr + key + "\t" + str(dataCount[key])+ "\n"
	return HttpResponse(resultStr, content_type="text/plain")

def Update(request):
	plot = int(request.GET.get('plot',0))
	x = int(request.GET.get('x',0))
	y = int(request.GET.get('y',0))
	print plot, x, y
	resultStr = "";
	if plot == 1:
		print "click on plot 1"
		nodes = NodeIndegree.objects.filter(indegree = x)
		print len(nodes)
		for n in nodes:
			# print n.node
			try:
				correspondingNode = NodeOutdegree.objects.get(node = n.node)
				try:
					outDCount = OutdegreeCount.objects.get(outdegree = correspondingNode.outdegree)
					resultStr = resultStr + str(outDCount.outdegree) + "\t" + str(outDCount.count)+ ";"
				except RankOutdegree.DoesNotExist:
					resultStr = ""
			except NodeOutdegree.DoesNotExist:
				# outdegree = 0
				resultStr = ""
	else:
		print "click on plot 2"
		nodes = NodeOutdegree.objects.filter(outdegree = x)
		print len(nodes)
		for n in nodes:
			# print n.node
			try:
				correspondingNode = NodeIndegree.objects.get(node = n.node)
				try:
					inDCount = IndegreeCount.objects.get(indegree = correspondingNode.indegree)
					resultStr = resultStr + str(inDCount.indegree) + "\t" + str(inDCount.count)+ ";"
				except RankIndegree.DoesNotExist:
					resultStr = ""
			except NodeIndegree.DoesNotExist:
				# indegree = 0
				resultStr = ""
	print resultStr		
	return HttpResponse(resultStr, content_type="text/plain")


def ClickPlot(request):
	plot = request.GET.get('plot',0)
	x = request.GET.get('x',0)
	y = request.GET.get('y',0)
	print plot, x, y

	response_data = {}
	response_data['degreeCount'] = ''
	response_data['degreePagerank'] = ''
	response_data['radiusCount'] = ''
	response_data['degreeRadius'] = ''
	response_data['ev1ev2'] = ''
	response_data['ev2ev3'] = ''

	response_data['sampleNodes'] = []

	degreeCountSet = Set([])
	degreePagerankSet = Set([])
	radiusCountSet = Set([])
	degreeRadiusSet = Set([])

	if plot == "degreeCount":
		print "click on plot degreeCount"
		degree = int(x)
		nodes = Node.objects.filter(inoutdegree = degree)
		response_data['degreeCount'] = x + '\t' + y + ';'
	elif plot == "degreePagerank":
		print "click on plot degreePagerank"
		degree = int(x)
		pagerank = y
		nodes = Node.objects.filter(inoutdegree = degree, pagerank = pagerank)
		response_data['degreePagerank'] = x + '\t' + y + ';'
	elif plot == "radiusCount":
		print "click on plot radiusCount"
		radius = x
		nodes = Node.objects.filter(radius = radius)
		response_data['radiusCount'] = x + '\t' + y + ';'
	elif plot == "degreeRadius":
		print "click on plot degreeRadius"
		degree = int(x)
		radius = y
		nodes = Node.objects.filter(inoutdegree = degree, radius = radius)
		response_data['degreeRadius'] = x + '\t' + y + ';'
	elif plot == "ev1ev2":
		print "click on plot ev1ev2"
		ev1 = x
		ev2 = y
		nodes = Node.objects.filter(ev1 = ev1, ev2 = ev2)
		response_data['ev1ev2'] = x + '\t' + y + ';'
	elif plot == "ev2ev3":
		print "click on plot ev2ev3"
		ev2 = x
		ev3 = y
		nodes = Node.objects.filter(ev2 = ev2, ev3 = ev3)
		response_data['ev2ev3'] = x + '\t' + y + ';'
	
	print len(nodes)
	i = 0
	for n in nodes:
		if i < 10:
			nodeInfo = {}
			print n.nodeId, n.inoutdegree, n.pagerank, n.radius, n.ev1, n.ev2, n.ev3
			nodeInfo['nodeId'] = n.nodeId
			nodeInfo['inoutdegree'] = n.inoutdegree
			nodeInfo['pagerank'] = n.pagerank
			nodeInfo['radius'] = n.radius
			nodeInfo['ev1'] = n.ev1
			nodeInfo['ev2'] = n.ev2
			nodeInfo['ev3'] = n.ev3
			response_data['sampleNodes'].append(nodeInfo)
			i = i + 1
		if plot != "degreeCount":
			degreeCount = str(n.inoutdegree) + '\t' + str(InoutdegreeCount.objects.get(inoutdegree=n.inoutdegree).count)
			if degreeCount not in degreeCountSet:
				degreeCountSet.add(degreeCount)
		if plot != "degreePagerank":
			degreePagerank = str(n.inoutdegree) + '\t' + n.pagerank
			if degreePagerank not in degreePagerankSet:
				degreePagerankSet.add(degreePagerank)
		if plot != "radiusCount":
			radiusCount = n.radius + '\t' + str(RadiusCount.objects.get(radius=n.radius).count)
			if radiusCount not in radiusCountSet:
				radiusCountSet.add(radiusCount)
		if plot != "degreeRadius":
			degreeRadius = str(n.inoutdegree) + '\t' + n.radius
			if degreeRadius not in degreeRadiusSet:
				degreeRadiusSet.add(degreeRadius)
		if plot != "ev1ev2":
			response_data['ev1ev2'] = response_data['ev1ev2'] + n.ev1 + '\t' + n.ev2 + ';'
		if plot != "ev2ev3":
			response_data['ev2ev3'] = response_data['ev2ev3'] + n.ev2 + '\t' + n.ev3 + ';'

	for degreeCount in degreeCountSet:
		response_data['degreeCount'] = response_data['degreeCount'] + degreeCount + ';'
	for degreePagerank in degreePagerankSet:
		response_data['degreePagerank'] = response_data['degreePagerank'] + degreePagerank + ';'
	for radiusCount in radiusCountSet:
		response_data['radiusCount'] = response_data['radiusCount'] + radiusCount + ';'
	for degreeRadius in degreeRadiusSet:
		response_data['degreeRadius'] = response_data['degreeRadius'] + degreeRadius + ';'

	return HttpResponse(json.dumps(response_data), content_type="application/json")

	