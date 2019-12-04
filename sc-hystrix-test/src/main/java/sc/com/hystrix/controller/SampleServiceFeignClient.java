package sc.com.hystrix.controller;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import sc.com.hystrix.domain.User;


@FeignClient(name = "sc-sampleservice",fallbackFactory=SampleServiceFeignClientFallbackFactory.class)
public interface SampleServiceFeignClient {

	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	User findById(@PathVariable("id") Long id);
	
	@RequestMapping(value = "/{id}", params = "sleep", method = RequestMethod.GET)
	User findByIdWithSleep(@PathVariable("id") Long id, @RequestParam(value = "sleep", required = false, defaultValue = "0") Long sleep);
	
}
