# --- First database schema

# --- !Ups

create table user (
  email                     varchar(255) not null primary key,
  name                      varchar(255) not null,
  password                  varchar(255) not null,
  remember_me               numeric(1) default 0
);

create table token (
  user_id                     varchar(255) not null primary key,
  series                      numeric(13) not null,
  token                       numeric(13) not null,
);



# --- !Downs

drop table if exists user;
drop table if exists token;