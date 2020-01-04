package com.sc.oauth2.authorization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sc.oauth2.domain.User;

/**
 * 根据用户名获取用户信息
 * @author Administrator
 *
 */
@Repository
public interface UserDao extends JpaRepository<User, Long> {
	User findByUsername(String username);
}
