	    --客户端数据：明码密码，123456
	    insert into OAUTH_CLIENT_DETAILS (CLIENT_ID, RESOURCE_IDS, CLIENT_SECRET, SCOPE, AUTHORIZED_GRANT_TYPES, WEB_SERVER_REDIRECT_URI, AUTHORITIES, ACCESS_TOKEN_VALIDITY, REFRESH_TOKEN_VALIDITY, ADDITIONAL_INFORMATION, AUTOAPPROVE)
values ('test_client', null, '{bcrypt}$2a$10$uT8xtlOWnIiS9Es1QVN9LeKcWpoeuk.bZqgFpNVsCFWacuXn/Moei', 'service,web', 'refresh_token,password,authorization_code', 'http://localhost:6002/login', null, null, null, null, null);
		--用户表数据：明码密码，123456 
		insert into OAUTH_USER (USER_ID,USERNAME, PASSWORD, ENABLED ) 
values (999, 'test_user', '{bcrypt}$2a$10$uT8xtlOWnIiS9Es1QVN9LeKcWpoeuk.bZqgFpNVsCFWacuXn/Moei', '1');
		--授权表数据：
		insert into OAUTH_AUTHORITY (AUTHORITY_ID, NAME) values (1001, 'test_admin');
		insert into OAUTH_AUTHORITY (AUTHORITY_ID, NAME) values (1002, 'test_user');
		--用户授权表数据：
		insert into OAUTH_USER_AUTHORITY (USER_ID, AUTHORITY_ID) values (999, 1001);
		insert into OAUTH_USER_AUTHORITY (USER_ID, AUTHORITY_ID) values (999, 1002);

-- 如果为了测试单点登录，可以再增加一个客户端
	    insert into OAUTH_CLIENT_DETAILS (CLIENT_ID, RESOURCE_IDS, CLIENT_SECRET, SCOPE, AUTHORIZED_GRANT_TYPES, WEB_SERVER_REDIRECT_URI, AUTHORITIES, ACCESS_TOKEN_VALIDITY, REFRESH_TOKEN_VALIDITY, ADDITIONAL_INFORMATION, AUTOAPPROVE)
values ('test_client1', null, '{bcrypt}$2a$10$uT8xtlOWnIiS9Es1QVN9LeKcWpoeuk.bZqgFpNVsCFWacuXn/Moei', 'service,web', 'refresh_token,password,authorization_code', 'http://localhost:6003/login', null, null, null, null, null);
		