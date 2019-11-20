package com.sc.feign.serviceclient;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.sc.feign.domain.UploadInfo;
import com.sc.feign.domain.User;

@FeignClient(name = "sc-sampleservice")
public interface SampleServiceFeignClient {

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	User findById(@PathVariable("id") Long id);

	@RequestMapping(value = "/{id}", params = "sleep", method = RequestMethod.GET)
	User findByIdWithSleep(@PathVariable("id") Long id, @RequestParam(value = "sleep", required = false, defaultValue = "0") Long sleep);
	
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	List<User> findUsers(@RequestParam(value="num",required=false,defaultValue="10") int num);
	
	@RequestMapping(value = "/add_users", method = RequestMethod.POST)
	List<User> addUsers(List<User> users);
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	User addUser(User user);
	
	@RequestMapping(value="/uploadFile",method=RequestMethod.POST,produces= {MediaType.APPLICATION_JSON_UTF8_VALUE},consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	UploadInfo handleFileUpload(@RequestPart(value="file") MultipartFile file);
	

}
