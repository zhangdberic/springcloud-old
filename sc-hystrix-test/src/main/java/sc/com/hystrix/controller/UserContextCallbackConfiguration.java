package sc.com.hystrix.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import sc.com.hystrix.concurrentstrategy.HystrixCallableWrapper;

@Configuration
public class UserContextCallbackConfiguration {

	@Bean
	public Collection<HystrixCallableWrapper> hystrixCallableWrappers() {
		Collection<HystrixCallableWrapper> wrappers = new ArrayList<>();
		wrappers.add(new HystrixCallableWrapper() {
			@Override
			public <V> Callable<V> wrap(Callable<V> callable) {
				return new UserContextCallable<V>(callable, UserContext.getUser());
			}
		});
		return wrappers;
	}

}
