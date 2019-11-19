# ribbon测试服务

其提供了一个用于测试ribbon的服务，其会调用sc-sampleservice服务来测试ribbon的配置的正确性。



## 使用LoadBalanced实现ribbon

ribbon支持很简单，就是在RestTemplate加上@LoadBalanced源注释。然后改变restTemplate的调用uri参数，这个uri参数在ribbon支持下意义已经变了，其uri的主机和端口部分对应的请求的服务名，例如：

 this.restTemplate.getForObject("http://SC-SAMPLESERVICE/{id}", User.class, id)，这里的SC-SAMPLESERVICE就要调用的服务名，这个服务已经在eureka上注册。ribbon默认是延时初始化的，其会在首次调用服务时，从eureka上加载服务路由表，并会定时刷新服务路由表，ribbon只会从本地的服务路由表中(ribbon会定时从eureka上拉取服务路由表)查找服务位置，然后调用，不会每次调用都从eureka上获取某个服务位置，然后在调用。这样的好处是即使eureka宕机，ribbon缓存的本地路由也可以提供服务位置。



使用ribbon支持的RestTemplate

```java
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
```

看这个@LoadBalanced的声明。



spring引用这个RestTemplate实例

```java
@RestController
public class RibbonLoadBalancedTestController {

	private static final Logger logger = LoggerFactory.getLogger(RibbonLoadBalancedTestController.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private LoadBalancerClient loadBalancerClient;

	@GetMapping("/user/{id}")
	public User findById(@PathVariable Long id) {
		return this.restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
	}
}    
```

这里的restTemplate实例已经是ribbon修饰过期的RestTemplate对象了。

this.restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);，注意这里的host部分已经被调用的服务名(**sc-sampleservice**)替换了。



### 测试ribbon的url

http://192.168.5.78:8003/user/1，其会基于@LoadBalanced修饰的RestTemplate来调用sc-sampleservice服务，获取一个User信息。

http://192.168.5.78:8003/log-user-instance，查看负载均衡请求分发到的主机。

http://192.168.5.78:8003/，其会基于@LoadBalanced修饰的RestTemplate来调用sc-sampleservice服务，基于post提交，增加一个User，并返回新增加的这个用户信息。

提交的Json如下：

{"username":"heige","name":"黑哥","age":39,"balance":10000000}

http://192.168.5.78:8003/user/1?sleep=xxxx，其会基于@LoadBalanced修饰的RestTemplate来调用sc-sampleservice服务，获取一个User信息，其sc-sampleservice执行的方法会延时参数sleep的毫秒，多用于测试延时，例如：readTimeout等。





# ribbon配置

在application.yml中加入

<clientName>.ribbon.x，用来配置某个对服务请求的ribbon配置。

ribbon.x，如果省略了前面的<clientName>则为全局配置。

具体配置可以看下面的**测试ribbon章节**。



## 测试ribbon

### 设置ribbon使用apache http客户端

```yml
ribbon: 
  restclient:
    enabled: true
```

ribbon默认的情况下使用的jdk自带的http客户端，其有很多问题，例如：ribbon.ReadTimeout设置无效，建议使用上面的配置，修改为使用apache http客户端。

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

### 测试ribbon的重试配置

#### 测试默认情况下(无ribbon配置)的重试

ribbon默认是支持重试的，测试方法：开启两个sc-sampleservice服务实例，浏览器发送请求http://192.168.5.31:8003/user/1到sc-ribbon-test，sc-ribbon-test再负载均衡调用sc-sampleservice，如果突然关闭了一个sc-sampleservice服务实例，并且浏览器再次发送两个请求http://192.168.5.31:8003/user/1到sc-ribbon-test，你会发现一个请求明显有卡顿，但任何可以正确的返回结果。

#### 测试禁用全局ribbon重试

```yml
spring:
  cloud:
    # 关闭除zuul所有的请求重试
    loadbalancer: 
      retry: 
        enabled: false
```

这个配置是全局配置，其将关闭除了zuul外其他所有组件的重试，测试方法：开启两个sc-sampleservice服务实例，浏览器发送请求http://192.168.5.31:8003/user/1到sc-ribbon-test，sc-ribbon-test再负载均衡调用sc-sampleservice，如果突然关闭了一个sc-sampleservice服务实例，并且浏览器再次发送两个请求http://192.168.5.31:8003/user/1到sc-ribbon-test，其中的一个请求抛出 ConnectException 异常。经过一段时间，eureka 90秒后踢掉没续租的服务+ribbon从eureka刷新服务路由表的到本地间隔时间，ribbon本地新的路由表中只有一个sc-sampleservice服务地址信息，然后再发送http://192.168.5.31:8003/user/1到sc-ribbon-test每次都是正确的了。

#### 测试MaxAutoRetries和MaxAutoRetriesNextServer设置

```yml
# 1.配置某个服务的ribbon属性
sc-sampleservice:
  ribbon:
    # 同一个实例最大重试测试，不包括首次调用
    MaxAutoRetries: 1
    # 重试其它实例的最大次数(例如:部署了同一个服务3个实例,此处应该设置为2）,不包括首次调用所选的实例
    MaxAutoRetriesNextServer: 1
    
    restclient: 
      enabled: true
    # 读取超时时间设置(毫秒)
    ReadTimeout: 3000
    ConnectTimeout: 1000      
      
```

测试方法，部署两个sc-sampleservice实例，浏览器发送请求：http://192.168.5.31:8003/user/1?sleep=3001到sc-ribbon-test服务，sc-ribbon-test负载均衡调用sc-sampleservice实例，因为ReadTimeout设置为3000，而请求的延时设置为3001一定会报错，从而测试请求重试。观察sc-sampleservice的日志输出，明显看到一个实例执行了两次，两个实例都执行到了，也就是说sc-samplservice一共接收到了4个请求。

流程：http://192.168.5.31:8003/user/1?sleep=3001请求发送到实例1，实例1超时报错，再重试实例1，实例1再超时报错。然后自动重试实例2，超时报错，自动再重试实例2。日志输出http://192.168.5.31:8003/user/1?sleep=3001请求耗时是12488 mills，正好是4个请求(1个正常请求，3个重试)的执行耗时。



测试方法：去掉MaxAutoRetries或MaxAutoRetriesNextServer配置(**默认配置**)，或配置MaxAutoRetries: 0，MaxAutoRetriesNextServer: 1，两者都是一样的。

测试方法，部署两个sc-sampleservice实例，浏览器发送请求：http://192.168.5.31:8003/user/1?sleep=3001到sc-ribbon-test服务，sc-ribbon-test负载均衡调用sc-sampleservice实例，因为ReadTimeout设置为3000，而请求的延时设置为3001一定会报错，从而测试请求重试。观察sc-sampleservice的日志输出，明显看到一个实例执行了1次，两个实例都执行到了，也就是说sc-samplservice一共接收到了2个请求。

```yml
# 1.配置某个服务的ribbon属性
sc-sampleservice:
  ribbon:
    # 同一个实例最大重试测试，不包括首次调用
    MaxAutoRetries: 0
    # 重试其它实例的最大次数(例如:部署了同一个服务3个实例,此处应该设置为2）,不包括首次调用所选的实例
    MaxAutoRetriesNextServer: 1
    
    restclient: 
      enabled: true
    # 读取超时时间设置(毫秒)
    ReadTimeout: 3000
    ConnectTimeout: 1000      
```

流程：http://192.168.5.31:8003/user/1?sleep=3001请求发送到实例1，实例1超时报错，因为MaxAutoRetries=0实例1不再重试。然后自动重试实例2，因为MaxAutoRetriesNextServer=1，超时报错。日志输出http://192.168.5.31:8003/user/1?sleep=3001请求耗时是6312 mills，正好是2个请求(1个正常请求，1个重试)的执行耗时。

#### 测试禁用某个服务的ribbon重试

```yml
# 1.配置某个服务的ribbon属性
sc-sampleservice:
  ribbon:
    # 同一个实例最大重试测试，不包括首次调用
    MaxAutoRetries: 0
    # 重试其它实例的最大次数(例如:部署了同一个服务3个实例,此处应该设置为2）,不包括首次调用所选的实例
    MaxAutoRetriesNextServer: 0
    
```

服务的ribbon属性MaxAutoRetries和MaxAutoRetriesNextServer都设置为0，则禁用了这个服务的重试。

生产环境建议这两个值都设置为1。

测试方法：开启两个sc-sampleservice服务实例，浏览器发送请求http://192.168.5.31:8003/user/1到sc-ribbon-test，sc-ribbon-test再负载均衡调用sc-sampleservice，如果突然关闭了一个sc-sampleservice服务实例，并且浏览器再次发送两个请求http://192.168.5.31:8003/user/1到sc-ribbon-test，其中的一个请求抛出 ConnectException 异常，没有启动重试。

#### 测试ribbon请求失败多少次从ribbon路由表剔除服务

测试方法，禁用sc-ribbon-test服务访问sc-sampleservice的重试，并开启两个sc-sampleservice服务实例，浏览器发送请求http://192.168.5.31:8003/user/1到sc-ribbon-test，sc-ribbon-test再负载均衡调用sc-sampleservice，如果突然关闭了一个sc-sampleservice服务实例，并且浏览器再次发送几次请求http://192.168.5.31:8003/user/1到sc-ribbon-test，如果其中的两次请求落到已经关闭的sc-sampleservice实例上（抛出 ConnectException 异常），则从ribbon路由表中剔除这个sc-sampleservice实例，后续的请求http://192.168.5.31:8003/user/1请求都会正常。

#### 测试OkToRetryOnAllOperations设置

测试方法：开启两个sc-sampleservice服务实例，postman发送请求http://192.168.5.31:8003/到sc-ribbon-test，sc-ribbon-test再负载均衡调用sc-sampleservice，如果突然关闭了一个sc-sampleservice服务实例，并且postman再次发送两个请求http://192.168.5.31:8003/到sc-ribbon-test，其中的一个请求抛出 ConnectException 异常，证明OkToRetryOnAllOperations默认值(false)的情况下，post请求不支持重试。

设置OkToRetryOnAllOperations=true，还是使用上面的测试，测试结果不会抛出ConnectException 异常，在关闭一个sc-sampleservice服务实例的情况下，仍然可以正确的发送请求和返回结果。但要注意，post支持重试是一个非常危险配置，如果post处理的方法不支持幂等性，可以出现重复提交和重复数据的情况。

```yml
sc-sampleservice:
  ribbon:
    # 是否所有的操作都进行重试(默认只有GET请求开启重试，POST请求关闭重试)，默认为false
    OkToRetryOnAllOperations: true
```

#### 测试ribbon.restclient.enabled=true设置

默认情况下ribbon的http客户端使用的jdk自带的httpconneciton，如果设置为ribbon.restclient.enabled=true其使用的apache http客户端，如果设置为ribbon.okhttp.enabled=true使用的okhttp客户端。

目前建议设置ribbon.restclient.enabled=true使用apache http客户端，因为只有在这个配置下，与http client的配置才能生效，例如：ribbon.ReadTimeout和ribbon.ConnectTimeout属性设置才能有效，具体原因见： https://blog.csdn.net/hsz2568952354/article/details/89466511 。已经做过实验来证明了。

#### 测试ReadTimeout设置

设置ReadTimeout值的前提是ribbon.restclient.enabled=true，否则ReadTimeout设置无效。

测试方法：设置sc-sampleservice的ribbon的调用ReadTimeout=3000，发送请求http://192.168.5.31:8003/user/1，如果关闭重试的请求下，查看sc-ribbon-test.findByIdWithSleep方法的请求耗时为3473mills，如果开启重试MaxAutoRetries: 1和MaxAutoRetriesNextServer: 1 的情况下sc-ribbon-test.findByIdWithSleep方法的请求耗时为12488mills（4个请求，1个正常请求，3个重试请求）。

**开启重试**抛出异常：

 com.netflix.client.ClientException: Number of retries on next server  exceeded max 1 retries, while making a call for: 192.168.5.78:8001 

对应的最底层异常，java.net.SocketTimeoutException: Read timed out

```yml
sc-sampleservice: 
  ribbon: 
    restclient: 
      enabled: true
    # 测试随机负载均衡 
    #NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
    # 同一个实例最大重试测试，不包括首次调用
    MaxAutoRetries: 1
    # 重试其它实例的最大次数(例如:部署了同一个服务3个实例,此处应该设置为2）,不包括首次调用所选的实例
    MaxAutoRetriesNextServer: 1
    # 是否所有的操作都进行重试(默认只有GET请求开启重试，POST请求关闭重试)，默认为false
    #OkToRetryOnAllOperations: true
    # 读取超时时间设置(毫秒)
    ReadTimeout: 3000
    ConnectTimeout: 1000
```

**重试关闭**的情况下：

com.netflix.client.ClientException; nested exception is java.io.IOException: com.netflix.client.ClientException 

java.net.SocketTimeoutException: Read timed out

```yml
sc-sampleservice: 
  ribbon: 
    restclient: 
      enabled: true
    # 测试随机负载均衡 
    #NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
    # 同一个实例最大重试测试，不包括首次调用
    MaxAutoRetries: 0
    # 重试其它实例的最大次数(例如:部署了同一个服务3个实例,此处应该设置为2）,不包括首次调用所选的实例
    MaxAutoRetriesNextServer: 0
    # 是否所有的操作都进行重试(默认只有GET请求开启重试，POST请求关闭重试)，默认为false
    #OkToRetryOnAllOperations: true
    # 读取超时时间设置(毫秒)
    ReadTimeout: 3000
    ConnectTimeout: 1000
```



#### 测试ConnectTimeout设置

设置ConnectTimeout值的前提是ribbon.restclient.enabled=true，否则ConnectTimeout设置无效。

ConnectTimeout对应socket建立连接的时间，如果在ConnectTimeout指定的时间内没能建立连接，则抛出Connection Time out异常。

如果ConnectTimeout设置的值小，底层网络情况不好，那么可以频繁的报出异常。如果ConnectTimeout设置的值大，并且不能正确的建立连接，那么客户端阻塞的时候会很长。如果开启重试，则(MaxAutoRetriesNextServer+1)*ConnectTimeout才是最终的连接超时时间，需要等待这么久才能抛出连接超时异常。

一般在内网情况下，默认值1000ms已经足够了。如果连接的服务在互联网，则要设置为2000 ms.

