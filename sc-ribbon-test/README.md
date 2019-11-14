# ribbon测试服务

其提供了一个用于测试ribbon的服务，其会调用sc-sampleservice服务来测试ribbon的配置的正确性。



## @LoadBalanced修饰的RestTemplate

ribbon支持很简单，就是在RestTemplate加上@LoadBalanced源注释。然后改变restTemplate的调用uri参数，这个uri参数在ribbon支持下意义已经变了，其uri的主机和端口部分对应的请求的服务名，例如：

 this.restTemplate.getForObject("http://SC-SAMPLESERVICE/{id}", User.class, id)，这里的SC-SAMPLESERVICE就要调用的服务名，这个服务已经在eureka上注册。ribbon默认是延时初始化的，其会在首次调用服务时，从eureka上加载服务路由表，并会定时刷新服务路由表，ribbon只会从本地的服务路由表中(ribbon会定时从eureka上拉取服务路由表)查找服务位置，然后调用，不会每次调用都从eureka上获取某个服务位置，然后在调用。这样的好处是即使eureka宕机，ribbon缓存的本地路由也可以提供服务位置。



其提供的URI：

http://192.168.5.78:8003/user/1，其会基于@LoadBalanced修饰的RestTemplate来调用sc-sampleservice服务。

http://192.168.5.78:8003/log-user-instance，查看负载均衡请求分发到的主机。



# ribbon配置

在application.yml中加入

<clientName>.ribbon.x，用来配置某个对服务请求的ribbon配置。

ribbon.x，如果省略了前面的<clientName>则为全局配置。

具体配置可以看下面的**测试ribbon章节**。



## 测试ribbon

### 测试ribbon默认配置

启动一个sc-sampleservice服务(其会注册到eureka上)，浏览器发送请求到sc-ribbon-test服务(http://192.168.5.78:8003/user/1)，@LoadBalanced修饰的RestTemplate来调用sc-sampleservice服务。

### 测试ribbon的默认负载均衡(轮询)

启动两个sc-sampleservice服务(其会注册到eureka上)，浏览器发送请求到sc-ribbon-test服务(http://192.168.5.78:8003/user/1)，@LoadBalanced修饰的RestTemplate来调用sc-sampleservice服务。观察这两个sc-sampleservice产生的日志信息，两个服务会交替输出日志。并逐次调用http://192.168.5.78:8003/log-user-instance来观察ribbon选择发送请求的目标。

浏览器发出请求：http://192.168.5.31:8003/log-user-instance，观察rc-ribbon-test的日志输出，如下：

sc-sampleservice:192.168.5.78:8000
sc-sampleservice:192.168.5.78:8001
sc-sampleservice:192.168.5.78:8000
sc-sampleservice:192.168.5.78:8001
sc-sampleservice:192.168.5.78:8000
sc-sampleservice:192.168.5.78:8001
sc-sampleservice:192.168.5.78:8000
sc-sampleservice:192.168.5.78:8001
sc-sampleservice:192.168.5.78:8000
sc-sampleservice:192.168.5.78:8001

### 测试ribbon随机负载均衡

修改： https://github.com/zhangdberic/config-repo/blob/master/sc-ribbon-test/sc-ribbon-test-dev.yml ，加入如下配置，指定为随机负载均衡策略。

sc-sampleservice:
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule

浏览器发出请求：http://192.168.5.31:8003/log-user-instance，观察rc-ribbon-test的日志输出，如下：

sc-sampleservice:192.168.5.78:8001
sc-sampleservice:192.168.5.78:8000
sc-sampleservice:192.168.5.78:8001
**sc-sampleservice:192.168.5.78:8000**
**sc-sampleservice:192.168.5.78:8000**
sc-sampleservice:192.168.5.78:8001
sc-sampleservice:192.168.5.78:8000
**sc-sampleservice:192.168.5.78:8001**
**sc-sampleservice:192.168.5.78:8001**
**sc-sampleservice:192.168.5.78:8000**
**sc-sampleservice:192.168.5.78:8000**
**sc-sampleservice:192.168.5.78:8000**

### 测试饥饿(延时)加载

ribbon默认对服务请求都是延时加载处理的，调用那个服务才初始化这个服务调用的ribbon上下文(慢)，这对于某些访问量大的请求是无法容忍。因此需要在服务启动的时候，对于重要的服务初始化ribbon上下文对象。

修改： https://github.com/zhangdberic/config-repo/blob/master/sc-ribbon-test/sc-ribbon-test-dev.yml ，加入如下配置，指定sc-sampleservice服务调用ribbon上下文启动就加载。

ribbon: 
  eager-load: 
    enabled: true
    clients: sc-sampleservice

观察日志输出，如果在启动时能看到如下字样说明启动加载，如果只有在发送请求http://192.168.5.78:8003/user/1，后才能看到如下字样为延时加载。

NFLoadBalancer:name=sc-sampleservice,current list of Servers=[192.168.5.78:8001, 192.168.5.78:8000]

如果有多个服务需要启动加载，则使用：service1, service2, serivce3。如果你想启动加载所有的服务ribbon上下文，只能一个一个的服务名加上，不支持*。







