package sc.com.hystrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import sc.com.hystrix.domain.User;

public abstract class HystrixTestBase {

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	User successUser() {
		User user = new User();
		user.setId(1l);
		user.setName("黑哥");
		return user;	
	}
	
	
	User fallbackUser() {
		User user = new User();
		user.setId(-1l);
		user.setName("默认用户");
		return user;		
	}
	

	User sendFailureRequest() {
		RestTemplate rest = new RestTemplate();
		User user = rest.getForObject("http://localhost:{port}/user2/1?status=false", User.class, this.getPort());
		return user;
	}

	User sendSuccessRequest() {
		RestTemplate rest = new RestTemplate();
		User user = rest.getForObject("http://localhost:{port}/user2/1?status=true", User.class, this.getPort());
		return user;
	}
	
	void sendSuccessRequest(int num,int sleep) throws InterruptedException {
		for(int i=0;i<num;i++) {
			this.sendSuccessRequest();
			Thread.sleep(sleep);
		}
	}
	
	void sendFailureRequest(int num,int sleep) throws InterruptedException {
		for(int i=0;i<num;i++) {
			this.sendFailureRequest();
			Thread.sleep(sleep);
		}
	}
	
	abstract int getPort() ;
}
