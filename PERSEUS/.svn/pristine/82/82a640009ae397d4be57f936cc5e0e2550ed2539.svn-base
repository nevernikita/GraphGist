.header ON

-- create indices on node ids

CREATE INDEX node_idx1 ON pagerank_undirected(node_id);
CREATE INDEX node_idx2 ON pagerank_directed(node_id);
CREATE INDEX node_idx3 ON pagerank_blk_undirected(node_id);
CREATE INDEX node_idx4 ON pagerank_blk_directed(node_id);
CREATE INDEX node_idx5 ON radius_undirected(node_id);
CREATE INDEX node_idx6 ON radius_directed(node_id);
CREATE INDEX node_idx7 ON radius_blk_undirected(node_id);
CREATE INDEX node_idx8 ON radius_blk_directed(node_id);
CREATE INDEX node_idx9 ON in_degree(node_id);
CREATE INDEX node_idx10 ON out_degree(node_id);
CREATE INDEX node_idx11 ON inout_degree(node_id);
CREATE INDEX node_idx12 ON in_weighted_degree(node_id);
CREATE INDEX node_idx13 ON out_weighted_degree(node_id);
CREATE INDEX node_idx14 ON inout_weighted_degree(node_id);

-- create table with all the statistics (multiple joins)
DROP TABLE IF EXISTS node_invariants;
CREATE TABLE node_invariants 
AS SELECT pagerank_undirected.node_id AS node_id, 
          pagerank_undirected.pagerank AS pr_undirected, 
          pagerank_directed.pagerank AS pr_directed, 
          pagerank_blk_undirected.pagerank AS prblk_undirected, 
          pagerank_blk_directed.pagerank as prblk_directed, 
          radius_undirected.max_radius AS max_radius, 
          radius_undirected.eff_radius AS eff_radius, 
          radius_blk_undirected.max_radius AS max_radiusblk, 
          radius_blk_undirected.eff_radius AS eff_radiusblk, 
          radius_directed.max_radius AS max_radius_directed, 
          radius_directed.eff_radius AS eff_radius_directed, 
          radius_directed.max_radius AS max_radiusblk_directed, 
          radius_directed.eff_radius AS eff_radiusblk_directed, 
          in_degree.degree as indeg, 
          out_degree.degree as outdeg, 
          inout_degree.degree as inoutdeg, 
          in_weighted_degree.degree as indeg_weighted, 
          out_weighted_degree.degree as outdeg_weighted, 
          inout_weighted_degree.degree as inoutdeg_weighted
  FROM pagerank_undirected
     LEFT JOIN pagerank_directed ON pagerank_directed.node_id = pagerank_undirected.node_id
     LEFT JOIN pagerank_blk_undirected ON pagerank_blk_undirected.node_id = pagerank_undirected.node_id
     LEFT JOIN pagerank_blk_directed ON pagerank_blk_directed.node_id = pagerank_directed.node_id
     LEFT JOIN radius_undirected ON radius_undirected.node_id = pagerank_undirected.node_id
     LEFT JOIN radius_directed ON radius_directed.node_id = pagerank_undirected.node_id
     LEFT JOIN radius_blk_undirected ON radius_blk_undirected.node_id = pagerank_undirected.node_id
     LEFT JOIN radius_blk_directed ON radius_blk_directed.node_id = pagerank_directed.node_id
     LEFT JOIN in_degree ON pagerank_undirected.node_id = in_degree.node_id
     LEFT JOIN out_degree ON pagerank_undirected.node_id = out_degree.node_id
     LEFT JOIN inout_degree ON pagerank_undirected.node_id = inout_degree.node_id
     LEFT JOIN in_weighted_degree ON pagerank_undirected.node_id = in_weighted_degree.node_id
     LEFT JOIN out_weighted_degree ON pagerank_undirected.node_id = out_weighted_degree.node_id
     LEFT JOIN inout_weighted_degree ON pagerank_undirected.node_id = inout_weighted_degree.node_id;


.separator "\t"
.output all_statistics.csv
SELECT node_id, 
       coalesce(indeg,0) as indegree, 
       coalesce(outdeg,0) as outdegree, 
       coalesce(inoutdeg,0) as inoutdegree, 
       coalesce(indeg_weighted,0) as indegree_weighted, 
       coalesce(outdeg_weighted,0) as outdegree_weighted, 
       coalesce(inoutdeg_weighted,0) as inoutdegree_weighted, 
       pr_undirected, 
       prblk_undirected, 
       pr_directed, 
       prblk_directed, 
       max_radius, 
       eff_radius, 
       max_radiusblk, 
       eff_radiusblk, 
       max_radius_directed, 
       eff_radius_directed, 
       max_radiusblk_directed, 
       eff_radiusblk_directed 
FROM node_invariants;
