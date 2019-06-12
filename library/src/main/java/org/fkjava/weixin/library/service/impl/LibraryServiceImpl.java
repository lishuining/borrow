package org.fkjava.weixin.library.service.impl;

import org.fkjava.weixin.library.dao.BookRepository;
import org.fkjava.weixin.library.domain.Book;
import org.fkjava.weixin.library.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LibraryServiceImpl implements LibraryService {

	// 很多时候，DAO的名字经常改变：dao、repository
	@Autowired
	private BookRepository bookRepository;

	@Override
	public Page<Book> search(String keyword, int pageNumber) {
		// Pageable表示分页条件，PageRequest用于创建分页条件。
		// 第一个参数表示页面，从0开始！
		// 第二个参数表示每页查询多少条数据出来。
		Pageable pageable = PageRequest.of(pageNumber, 3);

		Page<Book> page;
		if (StringUtils.isEmpty(keyword)) {
			// 没有关键字
			page = this.bookRepository.findAll(pageable);
		} else {
			// 有关键字
			// Containing表示包含，查询的时候只要数据中的name属性包含了keyword的内容，就会被查询出来。
			page = this.bookRepository.findByNameContaining(keyword, pageable);
		}

		return page;
	}
}
