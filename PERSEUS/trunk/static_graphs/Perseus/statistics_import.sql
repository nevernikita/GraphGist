.separator "\t"
.mode tabs


-- Importing degree info (undirected graph only)
-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
delete from in_degree;
.import 'dd_node_deg_inDeg_local/part-00000' in_degree

delete from out_degree;
.import 'dd_node_deg_outDeg_local/part-00000' out_degree

delete from inout_degree;
.import 'dd_node_deg_inoutDeg_local/part-00000' inout_degree

delete from in_weighted_degree;
.import 'dd_node_deg_inDeg_weighted_local/part-00000' in_weighted_degree

delete from out_weighted_degree;
.import 'dd_node_deg_outDeg_weighted_local/part-00000' out_weighted_degree

delete from inout_weighted_degree;
.import 'dd_node_deg_inoutDeg_weighted_local/part-00000' inout_weighted_degree


-- Importing pagerank (plain + block)
-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
delete from pagerank_undirected;
.import 'pr_vector_local/part-00000' pagerank_undirected

delete from pagerank_blk_undirected;
.import 'prblk_vector_local/part-00000' pagerank_blk_undirected

-- Importing radius (plain + block)
-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
delete from radius_undirected;
.import 'hadi_output_local/part-00000' radius_undirected

delete from radius_blk_undirected;
.import 'hadi_output_block_local/part-00000' radius_blk_undirected

-- Importing connected components (plain + block)
-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
-- delete from cc;
-- .import 'hadi_output_local/part-00000' cc

-- delete from cc_blk;
-- .import 'hadi_output_block_local/part-00000' cc_blk


.quit
