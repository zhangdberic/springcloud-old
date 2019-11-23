package sc.com.hystrix;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.hystrix.HystrixHealthIndicator;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import sc.com.hystrix.domain.User;

/**
 * hystrix 测试用例2
 * 用于验证：进入短路器打开(跳闸)状态后，5s内正确的请求也会被回退， 超出5s后正确的请求会关闭短路器。
 * @author zhangdb
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HystrixTest2 extends HystrixTestBase {

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private HystrixHealthIndicator hystrixHealthIndicator;

	@Test
	public void test() throws InterruptedException {
		logger.info("init hystrix status[{}].", this.hystrixHealthIndicator.health().getStatus());

		// 记录错误窗口期开始时间
		long failureBeginTime = System.currentTimeMillis();
		// 发送一个失败请求，激活错误窗口期
		this.sendFailureRequest();

		// 触发短路器跳闸
		this.sendFailureRequest(20, 230);
		logger.info("[{}]毫秒内执行了21个失败请求,短路器状态[{}].", System.currentTimeMillis() - failureBeginTime, this.hystrixHealthIndicator.health().getStatus());

		// 5s内的正确请求，都会被回退
		for (int i = 0; i < 4; i++) {
			User user = this.sendSuccessRequest();
			this.logger.info("5s内的正确请求都会被回退，User:{}，短路器状态[{}].", user, this.hystrixHealthIndicator.health().getStatus());
			Thread.sleep(1000);
		}

		// 5s后的正确的请求，会正确返回，断路器关闭
		Thread.sleep(1000);
		User user = this.sendSuccessRequest();
		this.logger.info("超出5s后正确的请求正常执行，User:{}，短路器状态[{}].", user, this.hystrixHealthIndicator.health().getStatus());

	}

	@Override
	int getPort() {
		return this.port;
	}

}
