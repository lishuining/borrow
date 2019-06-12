package org.fkjava.weixin.library.dao;

import org.fkjava.weixin.library.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

	// 注意：Spring Data JPA会自动实现此方法，并且会根据方法名生成一个SQL语句
	// find表示生成select语句
	// ByName 表示生成 where name
	// Containing 表示模糊查询，会自动在传入的参数值的前后加上%
	// 最终: select * from Book where name like '%keyword%'
	Page<Book> findByNameContaining(String keyword, Pageable pageable);
}
