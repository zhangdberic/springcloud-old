seata分布式事务介绍  
==========  

1.官网地址  
------------  
https://github.com/seata  
https://github.com/seata/seata-samples  
https://github.com/seata/seata-samples/blob/master/springcloud-jpa-seata  
https://github.com/seata/seata-samples/tree/master/springcloud-eureka-seata  



seata的意义比较大，其实现了分布式事物，其目前的版本没实现XA那么严格的二阶段提交，但在良好的业务设计下，90%可以满足分布式事物处理，其最大的优点是性能，但牺牲了事物的特性，隔离新、完整性、一致性等都会影响。

应用场景：微服务分布式事物、分库分表、跨数据库(一个全局事物内包括mysql和oracle)，因为在互联网模式下基于微服务的应用是不可避免的，因此对分布式事物的需求就非常迫切。其提供了AT和MT模式，AT可以实现非侵入代码模式，只需要在TM的方法上面声明@GlobalTransactional，在RM上加入DataSourceProxy类就就可以实现了分布式事物了。

需要注意：分布式环境不可能做到完美，只能力争完美，就想CAP理论一样。因此seata也是如此，其问题如下：

1.隔离性脏读，其RM(例如:RM1)在两阶段提交的第1阶段就已经执行了commit了，数据已经被写到数据库上，能被其它事物访问到。而这时如果整个全局事物中的某个RM(例如：RM2)出错需要回滚，但上面RM1提交的数据已经被人使用了，而这时TC通知RM1回滚，则要把这个数据恢复回去。这个需要有时间研究一下： @GlobalLock 和Select xxx for update了。

2.并发操作同一个数据回滚问题，因为其RM使用undo_log来保持sql操作前后的镜像，因此一旦某个局部事物代码抛出异常，需要回滚执行的时候，但需要回滚对应的行数据已经修改了（RM提交后，TC通知回滚前），那么将造成无法回滚，只能进行人工干预，因此一定要在设计的时候，就考虑这个问题。具体原理，见下面的"seata分析理解"。

3.seata的两阶段提交，注意：所有的RM第1阶段已经提交了(commit)，数据已经写到数据库中，其它的事物也可以查看到数据。RM的第2阶段，其实是在需要rollback的时候才执行的（如果全局事物提交成功则第2阶段对于各RM无意义），rollback时其获取undo_log的前镜像，翻译成sql，然后执行并提交（反向执行操作）。因此，这里就有问题了，如果第1阶段部分RM提交成功，某个RM需要回退，而这些RM中的某个需要反向操作的时候，连接的mysql数据库挂了，部分RM无法回退，则会破坏整个全局事物的完整性。



4.seata在RM是执行，如果使用update sql语句会使用select from update来生成前镜像，因此要注意性能问题，而且由于seata的undo机制，批量操作的sql语言可能不支持，即使支持了如果需要回退，那么效率也非常低，因此seata适合在操作单条数据或者是小范围操作数据的业务上。



如果应用在生产环境下，一定要严格的测试以上问题，特别是能否正常回滚(因为其回滚机制和数据库的回滚还是有区别的)，并且设计的时候要避免上面的问题。认证考虑全局事物的完整性和一致性。可以参照，这个事物补偿文档来建表设计： https://www.cnblogs.com/lijingshanxi/p/9943836.html 



有些问题的处理可以参见seata的FAQ： http://seata.io/zh-cn/docs/faq/faq.html 



## 2.约束

参照官网的例子：seata 0.9需要在spring boot 2.1+spring cloud Finchley的环境下运行。

目前测试，spring 1.5+spring Edawage不支持seata0.9。



3.TC 服务器端安装和配置  
----------  
下载地址：https://github.com/seata/seata/releases  

tar xvf seata-server-0.9.0.tar.gz  
cd seata  

### 3.1 初始化TC表结构  

**mysql**   
创建一个seata的库，并执行seata/config/db_store.sql文件内的sql，创建成功后会生成3个表。  

branch_table、global_table、lock_table   

但注意：global_table表的transaction_service_group字段，应修改为varchar(128)，否则可能出现"数据截断异常"。  

### 3.2 编辑注册文件

**vi conf/registry.conf**

```json
registry {
  # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
  type = "eureka"

  eureka {
    serviceUrl = "http://sc-eureka:veDSJeUX-JSxkWrk@192.168.5.78:8070/eureka/,http://sc-eureka:veDSJeUX-JSxkWrk@192.168.5.78:8071/eureka/"
    application = "seata-server"
    weight = "1"
  }
}

config {
  # file、nacos 、apollo、zk、consul、etcd3
  type = "file"

  file {
    name = "file.conf"
  }
}

```

seata-server(tc)这样重要的应用，必须是高可用的。因为使用spring cloud全家桶，因此注册到eureka中。

eureka.serviceUrl，同spring cloud eureka client的eureka.client.service-url.defaultZone配置。

eureka.application，同spring cloud eureka client的spring.application.name的配置。

weight.weight，没研究。

当前的配置，把seata-server应用注册到eureka中，注册的应用名为seata-server。



config {

  type = "file"

}

指定了使用本地的file.conf配置，从目前支持的远程配置类型(nacos 、apollo、zk、consul、etcd3)看，不支持spring cloud config的远程配置，等待阿里后期版本吧。



### 3.3 编辑配置文件

**vi conf/file.conf**

修改对外提供的访问地址：

```json
service {
  #vgroup->rgroup
  vgroup_mapping.my_test_tx_group = "default"
  #only support single node
  default.grouplist = "192.168.5.254:8091"
  #degrade current not support
  enableDegrade = false
  #disable
  disable = false
  #unit ms,s,m,h,d represents milliseconds, seconds, minutes, hours, days, default permanent
  max.commit.retry.timeout = "-1"
  max.rollback.retry.timeout = "-1"
}
```

目前，只有default.grouplist = "192.168.5.254:8091"配置项需要修改。TC对外提供全局事物操作的ip:port。

注意：后期版本的file.conf配置项的内容可能支持spring cloud config，也就说这些配置会被放到git上。



修改基于数据库来存储事物信息：

```json
store {
  ## store mode: file、db
  mode = "db"  # 修改为基于db来存储事物信息
## database store
  db {
    ## the implement of javax.sql.DataSource, such as DruidDataSource(druid)/BasicDataSource(dbcp) etc.
    datasource = "dbcp"
    ## mysql/oracle/h2/oceanbase etc.
    db-type = "mysql"
    driver-class-name = "com.mysql.jdbc.Driver"
    url = "jdbc:mysql://192.168.5.78:3306/seata?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai"
    user = "root"
    password = "123456"
    min-conn = 1
    max-conn = 3
    global.table = "global_table"
    branch.table = "branch_table"
    lock-table = "lock_table"
    query-limit = 100
  }
}
```

这里需要修改一下连接mysql的配置项。



### 3.4 启动TC服务器

调试启动

./bin/seata-server.sh 

生产启动

nohup ./bin/seata-server.sh > /var/log/seata-server.log 2>&1 &

因为seata-server(TC)，也是基于java开发的，因此可以基于修改/bin/seata-server.sh的jvm启动项，来优化seata-server。



## 4.RM客户端配置

RM是集成到spring cloud的服务上，seata对数据(DataSource)对象进行拦截，模拟实现两阶段提交。



### 4.1 初始化RM表结构

**mysql**

执行seata-server服务器上conf/db_undo_log.sql脚本，创建回滚表(undo_log)，用于在二阶段，RM回滚数据(补偿)使用。注意：db_undo_log.sql只需要在RM上执行。为什么RM需要使用undo_log表，可以见："seata理解分析章节"。

### 4.2 pom.xml加入eureka客户端

因为使用eureka来发现TC服务位置，因此pom.xml中需要加入eureka-client配置。

```xml
		<!-- spring cloud eureka client-->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		</dependency>
```

### 4.3 spring boot文件加入eureka客户端配置

```properties
spring.application.name=sc-seata-mstest1
server.port=8101
spring.datasource.url=jdbc:mysql://192.168.5.78:3306/seata1?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456
spring.jpa.show-sql=true
eureka.client.service-url.defaultZone=http://sc-eureka:veDSJeUX-JSxkWrk@192.168.5.78:8070/eureka/,http://sc-eureka:veDSJeUX-JSxkWrk@192.168.5.78:8071/eureka/
eureka.instance.prefer-ip-address=true
eureka.healthcheck.enabled=true
```



### 4.4 编辑注册文件

**vi src/main/resources/registry.conf**

```json
registry {

  type = "eureka"

  eureka {
    serviceUrl = "http://sc-eureka:veDSJeUX-JSxkWrk@192.168.5.78:8070/eureka/,http://sc-eureka:veDSJeUX-JSxkWrk@192.168.5.78:8071/eureka/"
    application = "seata-server"
    weight = "1"
  }
}

config {
  # file、nacos 、apollo、zk
  type = "file"

  file {
    name = "file.conf"
  }
}
```

使用了eureka注册中心，从eureka上获取TC服务器的位置，从而实现TC服务器位置发现和高可用。

具体配置介绍：可以看上面的TC服务器配置文档。

注意：这里的serviceUrl和application配置项和TC服务器registry.conf上的serviceUrl和application配置项相同。也就是TC服务器把自己的位置注册到eureka上，RM客户端从eureka上发现TC服务器位置。



### 4.5 编辑配置文件

**vi src/main/resources/file.conf**

```json
service {
  #vgroup->rgroup
  vgroup_mapping.sc-seata-bztest1-fescar-service-group="seata-server"
  #vgroup_mapping.sc-seata-bztest1-fescar-service-group="default"
  #only support single node
  #default.grouplist = "192.168.5.254:8091"
  #degrade current not support
  enableDegrade = false
  #disable
  disable = false
  #unit ms,s,m,h,d represents milliseconds, seconds, minutes, hours, days, default permanent
  max.commit.retry.timeout = "-1"
  max.rollback.retry.timeout = "-1"
  disableGlobalTransaction = false
}
```



配置解释：

service.vgroup_mapping.**sc-seata-bztest1**-fescar-service-group="seata-server" # 指定了sc-seata-bztest1事物组(transactional group)使用的TC服务器，因为使用的eureka服务发现TC位置，因此这里要配置为注册到eureka上的TC应用名。sc-seata-bztest1对应于spring.application.name(应用名)的配置项，如果没有手工配置RM事物组名，则seata RM会使用当前应用名作为事物组名。这个配置项的格式为：

service.vgroup_mapping.**事物组名**-fescar-service-group="**TC注册到eureka的应用名**"

**spring boot启动->读取事物组名(没有指定,则默认为应用名)->根据事物组名获取对应TC位置。**

这样做的好处是，可以配置不同的RM使用不同的TC服务器，可以分类管理并减轻TC服务器压力。例如：XXX服务(RM)连接到TC1服务器上，YYY服务(RM)连接到TC2服务器上。



注释掉：#default.grouplist配置项，因为使用了eureka来发现TC位置，因此无需再使用本配置来直接访问TC服务器了。如果不使用"服务发现"，使用本地直连TC，则需要这个配置项指定TC位置。

### 4.6 包装(拦截)Datasource内方法，实现二阶段操作

如果本地事物要加入到全局事物中，则要加入这个DataSourceConfig类来实现本地事物提交和回滚(TC会通知相关的操作)。正常情况下只有RM需要添加这个类，如果只是TM，则没有比较加入这个类。

```java
@Configuration
public class DataSourceConfig {
	@Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DruidDataSource druidDataSource() {
        return new DruidDataSource();
    }

    /**
     * 需要将 DataSourceProxy 设置为主数据源，否则事务无法回滚
     *
     * @param druidDataSource The DruidDataSource
     * @return The default datasource
     */
    @Primary
    @Bean("dataSource")
    public DataSource dataSource(DruidDataSource druidDataSource) {
        return new DataSourceProxy(druidDataSource);
    }
}

```

目前，只测试了alibaba的druid数据源，dbcp2还没有验证。





## 5.测试例子说明

seata目录下已经创建了4个项目：

sc-seata-bztest1  主入口，全局事物起点，模拟TM。

sc-seata-mstest1 参与到sc-seata-bztest1发起的全局事物中。

sc-seata-mstest2 参与到sc-seata-bztest1发起的全局事物中。

sc-seata-mstest3 参与到sc-seata-bztest1发起的全局事物中。

例如：对sc-seata-bztest1项目发起请求，http://192.168.5.31:8100/adduser?userId=10&name=mdmd，开启全局事物，调用链如下：

sc-seata-bztest1 -> 

​                                 sc-seata-mstest1

​                                 sc-seata-mstest2 -> 

​                                                                    sc-seata-mstest3

sc-seata-mstest1、sc-seata-mstest2、sc-seata-mstest3，分别往自己的mysql数据中增加一条用户数据，如果任何一个位置抛出异常则整个全局事物回滚。

### 5.1.创建测试用表结构

分别创建三个mysql数据库：seata1、seata2、seata3，然后在每个mysql数据库下创建两个表undo_log表和tuser表，建表sql如下：

undo_log表是seata RM的回滚表，用于二阶段回滚数据(如果任何一个RM提交失败，TC通知回滚)。这个表结构，最好使用seata-server的conf/db_undo_log.sql来创建，因为不同的seata版本可能表结构还不一样。

```sql
drop table `undo_log`;
CREATE TABLE `undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  `ext` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

```

tuser表，用于测试

```sql
DROP TABLE IF EXISTS `tuser`;
CREATE TABLE `tuser`  (
  `id` int(255) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

```

# seata理解分析

## seata的基本原理

### seata的三部分

![](https://camo.githubusercontent.com/b3a71332ae0a91db7f8616286a69b879fcbea672/68747470733a2f2f63646e2e6e6c61726b2e636f6d2f6c61726b2f302f323031382f706e672f31383836322f313534353239363739313037342d33626365376263652d303235652d343563332d393338362d3762393531333564616465382e706e67)

**Transaction Coordinator(TC):** Maintain status of global and branch transactions, drive the global commit or rollback.

**Transaction Manager(TM):** Define the scope of global transaction: begin a global transaction, commit or rollback a global transaction.

**Resource Manager(RM):** Manage resources that branch transactions working on, talk to TC for registering branch transactions and reporting status of branch transactions, and drive the branch transaction commit or rollback.



关于TC、TM、RM的定义，可以参照官网文档介绍，我们这里将结合测试用例：seata-server、sc-seata-bztest1、 sc-seata-mstest1、 sc-seata-mstest2、sc-seata-mstest3解析seata。



seata-server 相当于TC，已经被安装部署到了测试服务器192.168.5.254上。其会连接mysql，并创建三个表来维护全局事物的状态。

sc-seata-bztest1 相当于TM，其是全局事物(Global Transaction)的起点，其发起全局事物，从TC上获取xid，并发起feign和ribbon请求时附带xid进行传播。

sc-seata-mstest1、sc-seata-mstest2、sc-seata-mstest3 相当于RM，其是全局事物的一部分(Branch Transaction)，参与到全局事物中，其通过获取请求的xid来识别全局事物。

上面的sc-seata-bztest1、sc-seata-mstest1、sc-seata-mstest2、sc-seata-mstest3，整个调用链中任何一个位置，出现错误或异常，都将回滚整个事物。



### seata的两阶段提交

我们还是通过测试用例的sc-seata-bztest1、sc-seata-bztest1、sc-seata-bztest2、sc-seata-mstest3四个项目来分析两阶段提交。

例如：对sc-seata-bztest1项目发起请求，http://192.168.5.31:8100/adduser?userId=10&name=mdmd，开启全局事物，调用链如下：

sc-seata-bztest1 -> 

​                                 sc-seata-mstest1

​                                 sc-seata-mstest2 -> 

​                                                                    sc-seata-mstest3

sc-seata-mstest1、sc-seata-mstest2、sc-seata-mstest3，分别往自己的mysql数据中增加一条用户(User)数据，如果任何一个位置抛出异常则整个全局事物回滚。

sc-seata-bztest1调用代码：

```java
	@GlobalTransactional(name="addUser")
	public Collection<User> addUser(Long userId, String name) {
		User user1 = mstest1FeignClient.addUser(userId, name);
		User user2 = mstest2FeignClient.addUser(userId, name);
		Collection<User> users = new ArrayList<User>();
		users.add(user1);
		users.add(user2);
		return users;
	}
```

sc-seata-mstest1调用代码：

```java
@RestController
public class SeataMicroserviceTest1Controller {
	
	@Autowired
	private UserService userService;
	
	@GetMapping("/addUser")
	public User addUser(@RequestParam("userId") Long userId,
            @RequestParam("name") String name) {
		User user = new User(userId,name);
		user = this.userService.addUser(user);
		return user;
	}

}

```

```java
@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;
	
	@Transactional
    public User addUser(User user) {
		return this.userRepository.saveAndFlush(user);
	}

}
```

		User user1 = mstest1FeignClient.addUser(userId, name);
		User user2 = mstest2FeignClient.addUser(userId, name);
sc-seata-bztest1(TM)分布调用了sc-seata-mstest1(RM)和sc-seata-mstest2(RM)服务来增加用户，sc-seata-mstest1调用成功后sc-seata-mstest1的本地事物提交(spring transaction commit)，新增的用户写到seata1数据库tuser表中，但sc-seata-mstest2调用失败，则sc-seata-bztest1会收到调用失败的异常返回，其也会抛出异常，其通知TC全局事物失败，TC则通知sc-seata-mstest1(RM)回退，sc-seata-mstest1接收到TC回退请求后，根据TC发送过来的xid获取undo_log的镜像前数据，翻译成sql，反向执行并提交。



## seata如何实现的undo

我们就拿最复杂的update来解析seata如果生成undo的前后镜像：

前镜像(beforeImage)生成：

```java
protected TableRecords beforeImage() throws SQLException {
	...
	StringBuffer selectSQLAppender = new StringBuffer("SELECT ");
	if (!tmeta.containsPK(updateColumns)) {
		selectSQLAppender.append(this.getColumnNameInSQL(tmeta.getPkName()) + ", ");
	}
	...
	selectSQLAppender.append(" FROM " + this.getFromTableInSQL());
	if (StringUtils.isNotBlank(whereCondition)) {
		selectSQLAppender.append(" WHERE " + whereCondition);
	}
	
	selectSQLAppender.append(" FOR UPDATE");
	String selectSQL = selectSQLAppender.toString();
	...
	return beforeImage;
}
```

前镜像生成，其根据update语句，反向解析成select语句，并使用for upldate关键字，这里的重点就是for update语句，其特点是在本地事物的范围内select涉及到行数据全部加共享锁，也就是说其它事物无法修改。这样就保证这个update语句执行前，获取到前镜像数据正确性。

后镜像生成，由于oracle和mysql这种数据库本地事物的特性（隔离性），当前事物修改后的数据，如果未提交，只有当前事物能看到，其它事物看不到，因此也保证了后镜像数据的正确性。





























