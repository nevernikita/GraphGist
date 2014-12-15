drop table if exists grid4;
CREATE TABLE grid4 (source INTEGER, destination INTEGER, weight DOUBLE);
insert into grid4 values (0,1,1.0);
insert into grid4 values (1,2,1.0);
insert into grid4 values (2,3,1.0);
insert into grid4 values (4,5,1.0);
insert into grid4 values (5,6,1.0);
insert into grid4 values (6,7,1.0);
insert into grid4 values (8,9,1.0);
insert into grid4 values (9,10,1.0);
insert into grid4 values (10,11,1.0);
insert into grid4 values (12,13,1.0);
insert into grid4 values (13,14,1.0);
insert into grid4 values (14,15,1.0);
insert into grid4 values (0,4,1.0);
insert into grid4 values (4,8,1.0);
insert into grid4 values (8,12,1.0);
insert into grid4 values (1,5,1.0);
insert into grid4 values (5,9,1.0);
insert into grid4 values (9,13,1.0);
insert into grid4 values (2,6,1.0);
insert into grid4 values (6,10,1.0);
insert into grid4 values (10,14,1.0);
insert into grid4 values (3,7,1.0);
insert into grid4 values (7,11,1.0);
insert into grid4 values (11,15,1.0);
-- select * from grid4

drop table if exists grid3;
CREATE TABLE grid3 (source INTEGER, destination INTEGER, weight DOUBLE);
insert into grid3 values (0,1,1.0);
insert into grid3 values (1,2,1.0);
insert into grid3 values (3,4,1.0);
insert into grid3 values (4,5,1.0);
insert into grid3 values (6,7,1.0);
insert into grid3 values (7,8,1.0);
insert into grid3 values (0,3,1.0);
insert into grid3 values (3,6,1.0);
insert into grid3 values (1,4,1.0);
insert into grid3 values (4,7,1.0);
insert into grid3 values (2,5,1.0);
insert into grid3 values (5,8,1.0);
-- select * from grid3
.quit