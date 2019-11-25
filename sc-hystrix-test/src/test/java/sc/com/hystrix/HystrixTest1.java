package sc.com.hystrix;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sc.com.hystrix.domain.User;
/**
 * hystrix 测试用例1
 * 用于验证：超时、SEAMPHORE模式下是否支持超时（支持）。
 * 配置参数见：@see com.netflix.hystrix.HystrixCommandProperties
 * @author zhangdb
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HystrixTest1 extends HystrixTestBase {
	
	@Value("${local.server.port}")
	private int port;
	
	
	/**
	 * 测试方法：发送一个正确请求，休眠1100ms。
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void test() throws InterruptedException {
		// 发送一个正常请求，休眠500ms
		int sleep = 500;
		User user = this.sendSpeelRequest(sleep);
		logger.info("[{}]延时,返回正常User{}].",sleep,user);
		// 发送一个超时请求，休眠1001ms
		sleep = 1001;
		user = this.sendSpeelRequest(sleep);
		logger.info("[{}]延时,返回回退User{}].",sleep,user);

	}
	
	@Override
	int getPort() {
		return this.port;
	}

}
