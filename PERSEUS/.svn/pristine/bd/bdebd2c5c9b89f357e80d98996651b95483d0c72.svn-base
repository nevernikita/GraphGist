.separator "\t"
.mode tabs
create index nodeDidx on degree(node_id);
create index nodePidx on pagerank(node_id);


.output deg_pgrank.out
select degree.node_id, degree, pagerank from degree, pagerank where degree.node_id = pagerank.node_id
order by degree.node_id;


.quit
