create database familyradio;

create table songs (id int auto_increment not null primary key, songid varchar(100) unique, artist varchar(255) not null, song varchar(255) not null) engine InnoDB;
create table users (id varchar(100) not null primary key, name varchar (100) not null) engine InnoDB;
create table usersongs (userid varchar (100), songid int, rating int, constraint foreign key (songid) references songs(id), constraint foreign key (userid) references users(id));
create table genres (userid varchar(100), genre varchar (100), primary key (userid, genre), constraint foreign key (userid) references users (id));
