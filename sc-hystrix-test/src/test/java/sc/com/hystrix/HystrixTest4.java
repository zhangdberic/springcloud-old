package sc.com.hystrix;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import sc.com.hystrix.domain.User;

/**
 * 测试hystrix的并发策略(也就是线程上下文变量传递问题)
 * @author zhangdb
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HystrixTest4 {

	@Value("${local.server.port}")
	private int port;
	
	@Test
	public void test() {
		RestTemplate rest = new RestTemplate();
		User user = rest.getForObject("http://localhost:{port}/user4/1?sleep={sleep}", User.class, this.getPort(),1100);
		System.out.println(user);
		Assert.assertNotNull(user);
	}

	public int getPort() {
		return port;
	}

}
