package sc.com.hystrix.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import feign.hystrix.FallbackFactory;
import sc.com.hystrix.domain.User;

@Component
public class SampleServiceFeignClientFallbackFactory implements FallbackFactory<SampleServiceFeignClient> {

	private static Logger logger = LoggerFactory.getLogger(SampleServiceFeignClientFallbackFactory.class);

	@Override
	public SampleServiceFeignClient create(Throwable cause) {
		return new SampleServiceFeignClient() {

			@Override
			public User findById(Long id) {
				logger.info("into fallback[{}].");
				User user = new User();
				user.setId(-1l);
				user.setName("默认用户");
				return user;
			
			}

			@Override
			public User findByIdWithSleep(Long id, Long sleep) {
				logger.info("into fallback[{}].");
				User user = new User();
				user.setId(-1l);
				user.setName("默认用户");
				return user;
			}
			
		};
	}

}
