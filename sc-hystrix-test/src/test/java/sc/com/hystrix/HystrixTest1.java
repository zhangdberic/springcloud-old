package sc.com.hystrix;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.hystrix.HystrixHealthIndicator;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
/**
 * hystrix 测试用例1
 * 用于验证：每当hystrix命令遇到服务错误时，它将开始一个10s的计时器，如果在10s内调用失败次数超出20次，
 * 并且整个故障的百分比大于50%，则开启短路器（跳闸）。
 * 配置参数见：@see com.netflix.hystrix.HystrixCommandProperties
 * @author zhangdb
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HystrixTest1 extends HystrixTestBase {
	
	@Value("${local.server.port}")
	private int port;
	
	@Autowired
	private HystrixHealthIndicator hystrixHealthIndicator;
	
	/**
	 * 测试方法：让21次失败请求，在5s（错误窗口期10s的50%）内执行完，既满足触发跳闸条件。
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void test() throws InterruptedException {
		logger.info("init hystrix status[{}].",this.hystrixHealthIndicator.health().getStatus());

		// 发送一个失败请求，激活错误窗口期
		this.sendFailureRequest();
		// 记录错误窗口期开始时间
		long failureBeginTime = System.currentTimeMillis();		
		
		this.sendFailureRequest(20, 230);
		logger.info("[{}]毫秒内执行了21个失败请求,短路器状态[{}].",System.currentTimeMillis() - failureBeginTime,this.hystrixHealthIndicator.health().getStatus());

	}
	
	@Override
	int getPort() {
		return this.port;
	}

}
