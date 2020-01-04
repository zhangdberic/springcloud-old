-- Create table
-- 客户端信息表
create table OAUTH_CLIENT_DETAILS
(
  client_id               VARCHAR2(256) not null,
  resource_ids            VARCHAR2(256),
  client_secret           VARCHAR2(256),
  scope                   VARCHAR2(256),
  authorized_grant_types  VARCHAR2(256),
  web_server_redirect_uri VARCHAR2(256),
  authorities             VARCHAR2(256),
  access_token_validity   INTEGER,
  refresh_token_validity  INTEGER,
  additional_information  VARCHAR2(3072),
  autoapprove             VARCHAR2(256)
);
alter table OAUTH_CLIENT_DETAILS
  add constraint PK_OAUTH_CLIENT_DETAILS primary key (CLIENT_ID);
  
-- Create table
-- 自定义的用户表
create table OAUTH_USER
(
  user_id  NUMBER not null,
  username VARCHAR2(45) not null,
  password VARCHAR2(256) not null,
  enabled  CHAR(1) default '1'
)
alter table OAUTH_USER
  add constraint PK_OAUTH_USER primary key (USER_ID);

-- Create table
-- 自定义的权限表
create table OAUTH_AUTHORITY
(
  authority_id NUMBER not null,
  name         VARCHAR2(100)
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table OAUTH_AUTHORITY
  add constraint PK_OAUTH_AUTHORITY_ID primary key (AUTHORITY_ID);

-- Create table
-- 自定义的用户权限表    
create table OAUTH_USER_AUTHORITY
(
  user_id      NUMBER not null,
  authority_id NUMBER not null
);
alter table OAUTH_USER_AUTHORITY
  add constraint PK_OAUTH_USER_ROLE primary key (USER_ID, AUTHORITY_ID);
  
-- 下面的所有表只要在JdbcTokenStore的情况下有意义，如果使用RedisTokenStore则不需要创建。
-- Create table
create table OAUTH_CLIENT_TOKEN
(
  authentication_id VARCHAR2(256) not null,
  token_id          VARCHAR2(256),
  token             BLOB,
  user_name         VARCHAR2(256),
  client_id         VARCHAR2(256)
);
alter table OAUTH_CLIENT_TOKEN
  add constraint PK_OAUTH_CLIENT_TOKEN primary key (AUTHENTICATION_ID);
  
-- Create table
create table OAUTH_ACCESS_TOKEN
(
  authentication_id VARCHAR2(256) not null,
  token_id          VARCHAR2(256),
  token             BLOB,
  user_name         VARCHAR2(256),
  client_id         VARCHAR2(256),
  authentication    BLOB,
  refresh_token     VARCHAR2(256)
);

-- Create/Recreate primary, unique and foreign key constraints 
alter table OAUTH_ACCESS_TOKEN
  add constraint PK_OAUTH_ACCESS_TOKEN primary key (AUTHENTICATION_ID);
  
  
create table oauth_refresh_token (
  token_id VARCHAR(256),
  token BLOB,
  authentication BLOB
);

create table oauth_code (
  code VARCHAR(256), 
  authentication BLOB
);

create table oauth_approvals (
  userId VARCHAR(256),
  clientId VARCHAR(256),
  scope VARCHAR(256),
  status VARCHAR(10),
  expiresAt TIMESTAMP,
  lastModifiedAt TIMESTAMP
);



  
  
  