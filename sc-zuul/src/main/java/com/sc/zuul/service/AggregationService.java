package com.sc.zuul.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.sc.zuul.domain.User;

import rx.Observable;

/**
 * 聚合服务
 * @author zhangdb
 *
 */
@Service
public class AggregationService {

	@Autowired
	private RestTemplate restTemplate;

	@HystrixCommand(fallbackMethod = "fallback")
	public Observable<User> getUser1ById(Long id) {
		// 只是创建一个被观察者,还不执行
		return Observable.create(observer -> {
			User user = restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
			observer.onNext(user);
			observer.onCompleted();
		});
	}

	@HystrixCommand(fallbackMethod = "fallback")
	public Observable<User> getUser2ById(Long id) {
		// 只是创建一个被观察者,还不执行
		return Observable.create(observer -> {
			User user = restTemplate.getForObject("http://sc-sampleservice/{id}", User.class, id);
			observer.onNext(user);
			observer.onCompleted();
		});
	}

	public User fallback(Long id) {
		User user = new User();
		user.setId(-1l);
		user.setName("回退");
		return user;
	}

}
