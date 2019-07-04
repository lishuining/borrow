package org.fkjava.commons.repository;

import org.fkjava.commons.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository

public interface UserRepository extends JpaRepository<User, String> {

	
	// 并且还会自动把查询得到的结果，转换为User对象
	User findByOpenId(String openId);
}
