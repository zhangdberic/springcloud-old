# ribbon测试服务

其提供了一个用于测试ribbon的服务，其会调用sc-sampleservice服务来测试ribbon的配置的正确性。



## @LoadBalanced修饰的RestTemplate

ribbon支持很简单，就是在RestTemplate加上@LoadBalanced源注释。然后改变restTemplate的调用uri参数，这个uri参数在ribbon支持下意义已经变了，其uri的主机和端口部分对应的请求的服务名，例如：

 this.restTemplate.getForObject("http://SC-SAMPLESERVICE/{id}", User.class, id)，这里的SC-SAMPLESERVICE就要调用的服务名，这个服务已经在eureka上注册。ribbon默认是延时初始化的，其会在首次调用服务时，从eureka上加载服务路由表，并会定时刷新服务路由表，ribbon只会从本地的服务路由表中(ribbon会定时从eureka上拉取服务路由表)查找服务位置，然后调用，不会每次调用都从eureka上获取某个服务位置，然后在调用。这样的好处是即使eureka宕机，ribbon缓存的本地路由也可以提供服务位置。



其提供的URI：

http://192.168.5.78:8003/user/1，其会基于@LoadBalanced修饰的RestTemplate来调用sc-sampleservice服务。

http://192.168.5.78:8003/log-user-instance，查看负载均衡请求分发到的主机。



## 测试ribbon均衡

启动两个sc-sampleservice服务(其会注册到eureka上)，浏览器发送请求到sc-ribbon-test服务(http://192.168.5.78:8003/user/1)，@LoadBalanced修饰的RestTemplate来调用sc-sampleservice服务。观察这两个sc-sampleservice产生的日志信息，两个服务会交替输出日志。并逐次调用http://192.168.5.78:8003/log-user-instance来观察ribbon选择发送请求的目标。

通过编写用例来验证ribbon负载均衡的正确性。





