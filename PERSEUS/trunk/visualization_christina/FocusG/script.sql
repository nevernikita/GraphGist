drop table if exists grid4;
CREATE TABLE grid4 (source INTEGER, destination INTEGER, weight DOUBLE);
insert into grid4 values (0,1,1.0);
insert into grid4 values (0,9,1.0);
insert into grid4 values (0,8,1.0);
insert into grid4 values (1,2,1.0);
insert into grid4 values (1,3,1.0);
insert into grid4 values (1,4,1.0);
insert into grid4 values (1,5,1.0);
insert into grid4 values (1,6,1.0);
insert into grid4 values (2,3,1.0);
insert into grid4 values (2,7,1.0);
insert into grid4 values (7,8,1.0);
insert into grid4 values (8,9,1.0);
-- select * from grid4;
.quit
