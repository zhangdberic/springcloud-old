# 配置spring cloud config 配置中心

config配置中代码比较简单，就不多说了。



## 集群



spring cloud 配置集群，最头疼的就是，是把spring cloud config以服务的方式注册的eureka上来保证可靠性，还是把spring cloud config做成vip的HA。就好比是一个先有鸡还是先有蛋的问题。

第1种：

​	好处：简单，可靠性也能保证，config服务也注册到eureka上，由eureka统一管理。

​	缺点：eureka无法使用spring cloud config，application.yml要放在eureka项目本地文件系统上，如果配置改变需要到本地修改，然后重启eureka。

​                每个服务，需要写死eureka.client.service-url.defaultZone的地址，因为要先到eureka注册中心获取springcloudconfig服务的地址，然后才能调用config来获取服务配置。

​    整个spring cloud系统需要先启动eureka集群。

第2种：

​	好处：eureka项目的应用配置也可以加入到spring cloud config中，可以直接在git上查看和修改(配置统一管理)。

​	坏处：需要配置VIP，加入了复杂性。

​    整个spring cloud系统需要先启动spring clolud config集群。



但不管是第1种，还是第2种，gitlabs不能是单点，也应该是双机(VIP)。



目前我的选择是使用第2种：config vip方式，因为gitlabs本身也要实现双机。

基于VIP的方案来构建集群，VIP后接两个spring cloud config的docker，VIP可以使用keepalived来实现，但这需要两台宿主机上安装和配置keeaplived。

服务配置客户端spring.cloud.config.uri: http://vip:port配置为vip地址。

gitlabs也基于HA方式，也可以安装到spring cloud config docker的宿主机上（这个具体没安装过，但应该没问题），gitlabs的数据基于drbd来保证可靠性。











也就是说，是程序启动先从spring.cloud.config.*中加载eureka的配置，还是先从eureka中查找springcloudconfig的服务，然后在加载配置。第1种：需要写死 eureka.client.service-url.defaultZone配置，而且eureka项目不能支持spring cloud config，eureka的application.yml配置要写到本地。而且第1种：需要在bootstrap.yml中写死spring.cloud.config.uri，