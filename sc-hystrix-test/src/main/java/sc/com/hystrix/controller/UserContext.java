package sc.com.hystrix.controller;

import sc.com.hystrix.domain.User;

public class UserContext {
	
	private static final ThreadLocal<User> userThreadLocal = new ThreadLocal<User>();
	
	public static User getUser() {
		return userThreadLocal.get();
	}
	
	public static void setUser(User user) {
		userThreadLocal.set(user);
	}
	
	public static void remove() {
		userThreadLocal.remove();
	}

}
