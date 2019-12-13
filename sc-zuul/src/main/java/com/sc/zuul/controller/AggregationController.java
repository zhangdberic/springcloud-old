package com.sc.zuul.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.sc.zuul.domain.User;
import com.sc.zuul.service.AggregationService;

import rx.Observable;
import rx.Observer;

/**
 * 聚合控制器
 * @author zhangdb
 *
 */
@RestController
public class AggregationController {
	public static final Logger logger = LoggerFactory.getLogger(AggregationController.class);

	@Autowired
	private AggregationService aggregationService;
	
	@GetMapping("/aggregate/{id}")
	public DeferredResult<HashMap<String,User>> aggregate(@PathVariable Long id){
		Observable<HashMap<String, User>> result = this.aggregateObservable(id);
		return this.toDeferredResult(result);
	}

	public Observable<HashMap<String, User>> aggregateObservable(Long id) {
		// 合并下面两个Observables发射出的数据项
		// 参数1，为Observable<User>
		// 参数2，为Observable<User>
		// 参数3，为把两个Observable<User>合并为一个Observable<HashMap<String, User>>
		return Observable.zip(this.aggregationService.getUser1ById(id), this.aggregationService.getUser2ById(id), (user1, user2) -> {
			HashMap<String, User> map = new HashMap<>();
			map.put("user1", user1);
			map.put("user2", user2);
			return map;
		});
	}
	
	public DeferredResult<HashMap<String,User>> toDeferredResult(Observable<HashMap<String, User>> details){
		DeferredResult<HashMap<String,User>> result = new DeferredResult<>();
		// 订阅Observable
		details.subscribe(new Observer<HashMap<String, User>>(){

			@Override
			public void onCompleted() {
				logger.info("完成...");
			}

			@Override
			public void onError(Throwable e) {
				logger.info("错误...",e);
			}

			@Override
			public void onNext(HashMap<String, User> users) {
				result.setResult(users);
			}
			
		});
		return result;
	}

}
