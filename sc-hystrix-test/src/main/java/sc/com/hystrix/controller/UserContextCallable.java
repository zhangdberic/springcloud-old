package sc.com.hystrix.controller;

import java.util.concurrent.Callable;

import sc.com.hystrix.domain.User;

public class UserContextCallable<V> implements Callable<V> {
	private final User user;
	private final Callable<V> callable;

	/**
	 * 外围线程初始化(例如:tomcat请求线程)
	 * @param callable
	 * @param user
	 */
	public UserContextCallable(Callable<V> callable,User user) {
		super();
		this.user = user;
		this.callable = callable;
	}

	/**
	 * Hystrix隔离仓线程调用(hystrix执行线程)
	 */
	@Override
	public V call() throws Exception {
		UserContext.setUser(this.user);
		try {
			V v = this.callable.call();
			return v;
		} finally {
			UserContext.remove();
		}
	}

}
