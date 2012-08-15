# --- First database schema

# --- !Ups

create table user (
  email                     varchar(255) not null primary key,
  name                      varchar(255) not null,
  password                  varchar(255) not null,
);

create table token (
  user_id                     varchar(255) not null primary key,
  series                      BIGINT not null,
  token                       BIGINT not null,
);



# --- !Downs

drop table if exists user;
drop table if exists token;