-- use the command 'sqlite3.exe epinions.gdb < create_schema_pegasus.sql'


-- degree table
CREATE TABLE degree (
    node_id INTEGER,
    in_degree DOUBLE,
    out_degree DOUBLE,
    inout_degree DOUBLE,
    PRIMARY KEY(node_id)
);

-- weighted degree table
CREATE TABLE weightedDegree (
    node_id INTEGER,
    inout_degree DOUBLE,
    in_degree DOUBLE,
    out_degree DOUBLE,
    PRIMARY KEY(node_id)
);

-- (undirected) PageRank table
CREATE TABLE pagerank_undirected (
    node_id INTEGER,
    pagerank DOUBLE,
    PRIMARY KEY(node_id)
);

-- (directed) PageRank table
CREATE TABLE pagerank_directed (
    node_id INTEGER,
    pagerank DOUBLE,
    PRIMARY KEY(node_id)
);

-- (undirected) PageRank_blk table
CREATE TABLE pagerank_blk_undirected (
    node_id INTEGER,
    pagerank DOUBLE,
    PRIMARY KEY(node_id)
);

-- (directed) PageRank_blk table
CREATE TABLE pagerank_blk_directed (
    node_id INTEGER,
    pagerank DOUBLE,
    PRIMARY KEY(node_id)
);

-- (undirected) radius table
CREATE TABLE radius_undirected (
    node_id INTEGER,
    max_radius INTEGER,
    eff_radius DOUBLE,
    PRIMARY KEY(node_id)
);

-- (directed) radius table
CREATE TABLE radius_directed (
    node_id INTEGER,
    max_radius INTEGER,
    eff_radius DOUBLE,
    PRIMARY KEY(node_id)
);

-- (undirected) radius_blk table
CREATE TABLE radius_blk_undirected (
    node_id INTEGER,
    max_radius INTEGER,
    eff_radius DOUBLE,
    PRIMARY KEY(node_id)
);

-- (directed) radius_blk table
CREATE TABLE radius_blk_directed (
    node_id INTEGER,
    max_radius INTEGER,
    eff_radius DOUBLE,
    PRIMARY KEY(node_id)
);

-- connected components table
CREATE TABLE cc (
    node_id INTEGER,
    cc INTEGER,
    PRIMARY KEY(node_id)
);

-- connected components table
CREATE TABLE cc_blk (
    node_id INTEGER,
    cc INTEGER,
    PRIMARY KEY(node_id)
);

-- eigenvector table
CREATE TABLE eigvec (
    node_id INTEGER,
    u1 DOUBLE,
    u2 DOUBLE,
    u3 DOUBLE,
    u4 DOUBLE,
    u5 DOUBLE,
    u6 DOUBLE,
    u7 DOUBLE,
    u8 DOUBLE,
    u9 DOUBLE,
    u10 DOUBLE,
    PRIMARY KEY(node_id)
);

-- number of triangles table
CREATE TABLE triangle (
    node_id INTEGER,
    triangle INTEGER,
    PRIMARY KEY(node_id)
);


-- in-degree distribution table
CREATE TABLE in_degree (
    node_id INTEGER ASC,
    degree DOUBLE,
    PRIMARY KEY(node_id)
);

-- out-degree distribution table
CREATE TABLE out_degree (
    node_id INTEGER,
    degree DOUBLE,
    PRIMARY KEY(node_id)
);

-- inout-degree distribution table
CREATE TABLE inout_degree (
    node_id INTEGER,
    degree DOUBLE,
    PRIMARY KEY(node_id)
);

-- weighted in-degree distribution table
CREATE TABLE in_weighted_degree (
    node_id INTEGER,
    degree DOUBLE,
    PRIMARY KEY(node_id)
);


-- weighted out-degree distribution table
CREATE TABLE out_weighted_degree (
    node_id INTEGER,
    degree DOUBLE,
    PRIMARY KEY(node_id)
);


-- weighted inout-degree distribution table
CREATE TABLE inout_weighted_degree (
    node_id INTEGER,
    degree DOUBLE,
    PRIMARY KEY(node_id)
);



-- table with all the statistics
CREATE TABLE node_invariants_pr_radius (
    node_id INTEGER PRIMARY KEY,
    pr DOUBLE,
    prblk DOUBLE,
    pr_directed DOUBLE,
    prblk_directed DOUBLE,
    max_radius DOUBLE,
    eff_radius DOUBLE,
    max_radiusblk DOUBLE,
    eff_radiusblk DOUBLE,
    max_radius_directed DOUBLE,
    eff_radius_directed DOUBLE,
    max_radiusblk_directed DOUBLE,
    eff_radiusblk_directed DOUBLE
);

--    indeg DOUBLE,
--    outdeg DOUBLE,
--    inoutdeg DOUBLE,
--    indeg_weighted DOUBLE,
--    outdeg_weighted DOUBLE,
--    inoutdeg_weighted DOUBLE,
