from django.db import models

# Create your models here.

class Node(models.Model):
	nodeId = models.IntegerField(default= 0, primary_key=True)
	inoutdegree = models.IntegerField(default= 0, db_index=True)
	pagerank = models.CharField(default= 0, max_length=30, db_index=True)
	radius = models.CharField(default= 0, max_length=30, db_index=True)
	ev1 = models.CharField(default= 0, max_length=30, db_index=True)
	ev2 = models.CharField(default= 0, max_length=30, db_index=True)
	ev3 = models.CharField(default= 0, max_length=30, db_index=True)

class IndegreeCount(models.Model):
	indegree = models.IntegerField(default= 0)
	count = models.IntegerField(default= 0)

class OutdegreeCount(models.Model):
	outdegree = models.IntegerField(default= 0)
	count = models.IntegerField(default= 0)

class InoutdegreeCount(models.Model):
	inoutdegree = models.IntegerField(default= 0, db_index=True)
	count = models.IntegerField(default= 0, db_index=True)

class RadiusCount(models.Model):
	radius = models.CharField(default= 0, max_length=30, db_index=True)
	count = models.IntegerField(default= 0, db_index=True)
	
class Edge(models.Model):
	fromNode = models.IntegerField(default= 0, db_index=True)
	toNode = models.IntegerField(default= 0, db_index=True)

class NodeIndegree(models.Model):
	node = models.IntegerField(default= 0)
	indegree = models.IntegerField(default= 0)

class NodeOutdegree(models.Model):
	node = models.IntegerField(default= 0)
	outdegree = models.IntegerField(default= 0)

class NodeInoutdegree(models.Model):
	node = models.IntegerField(default= 0)
	inoutdegree = models.IntegerField(default= 0)

class NodePagerank(models.Model):
	node = models.IntegerField(default= 0)
	pagerank = models.FloatField(default= 0)
