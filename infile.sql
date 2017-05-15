create table test_infile(
  id int auto_increment primary key,
  val varchar(10)
);

LOAD DATA LOCAL INFILE '/Users/zcg/project/mysql-protocol/test_infile_data.txt'  into table test_infile(val);
