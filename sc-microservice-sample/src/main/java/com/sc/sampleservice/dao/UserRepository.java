package com.sc.sampleservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sc.sampleservice.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
