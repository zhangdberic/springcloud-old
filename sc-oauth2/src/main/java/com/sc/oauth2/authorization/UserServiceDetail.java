package com.sc.oauth2.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
/**
 * 用户信息服务
 * @author Administrator
 *
 */
@Service
public class UserServiceDetail  implements UserDetailsService {

    @Autowired
    private UserDao userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByUsername(username);
    }
}