package com.sc.seata.mstest1.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sc.seata.mstest1.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	

}
