DELETE FROM PlotDistribution_edge;
DELETE FROM PlotDistribution_node;
DELETE FROM PlotDistribution_inoutdegreecount;
DELETE FROM PlotDistribution_radiuscount;
.separator "\t"
.import visualize/PlotDistribution/static/data/degreeCount_forDB PlotDistribution_inoutdegreecount
.import visualize/PlotDistribution/static/data/radiusCount_forDB PlotDistribution_radiuscount
.import visualize/PlotDistribution/static/data/forDBNode PlotDistribution_node
.import visualize/PlotDistribution/static/data/edges PlotDistribution_edge
DROP INDEX PlotDistribution_node_inoutdegree;
DROP INDEX PlotDistribution_node_pagerank;
DROP INDEX PlotDistribution_node_radius;
DROP INDEX PlotDistribution_node_ev1;
DROP INDEX PlotDistribution_node_ev2;
DROP INDEX PlotDistribution_node_ev3;
DROP INDEX PlotDistribution_edge_fromNode;
DROP INDEX PlotDistribution_edge_toNode;
DROP INDEX PlotDistribution_inoutdegreecount_inoutdegree;
DROP INDEX PlotDistribution_inoutdegreecount_count;
DROP INDEX PlotDistribution_radiuscount_radius;
DROP INDEX PlotDistribution_radiuscount_count;
CREATE INDEX PlotDistribution_node_inoutdegree ON "PlotDistribution_node" ("inoutdegree");
CREATE INDEX PlotDistribution_node_pagerank ON "PlotDistribution_node" ("pagerank");
CREATE INDEX PlotDistribution_node_radius ON "PlotDistribution_node" ("radius");
CREATE INDEX PlotDistribution_node_ev1 ON "PlotDistribution_node" ("ev1");
CREATE INDEX PlotDistribution_node_ev2 ON "PlotDistribution_node" ("ev2");
CREATE INDEX PlotDistribution_node_ev3 ON "PlotDistribution_node" ("ev3");
CREATE INDEX PlotDistribution_edge_fromNode ON "PlotDistribution_edge" ("fromNode");
CREATE INDEX PlotDistribution_edge_toNode ON "PlotDistribution_edge" ("toNode");
CREATE INDEX PlotDistribution_inoutdegreecount_inoutdegree ON "PlotDistribution_inoutdegreecount" ("inoutdegree");
CREATE INDEX PlotDistribution_inoutdegreecount_count ON "PlotDistribution_inoutdegreecount" ("count");
CREATE INDEX PlotDistribution_radiuscount_radius ON "PlotDistribution_radiuscount" ("radius");
CREATE INDEX PlotDistribution_radiuscount_count ON "PlotDistribution_radiuscount" ("count");
