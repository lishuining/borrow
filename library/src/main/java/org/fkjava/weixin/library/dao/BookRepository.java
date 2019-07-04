package org.fkjava.weixin.library.dao;

import org.fkjava.weixin.library.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

	
	Page<Book> findByNameContaining(String keyword, Pageable pageable);
}
