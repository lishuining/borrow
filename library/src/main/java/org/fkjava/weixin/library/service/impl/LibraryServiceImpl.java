package org.fkjava.weixin.library.service.impl;

import java.util.LinkedList;

import org.fkjava.weixin.library.dao.BookRepository;
import org.fkjava.weixin.library.domain.Book;
import org.fkjava.weixin.library.domain.DebitItem;
import org.fkjava.weixin.library.domain.DebitList;
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

	@Override
	public void add(DebitList list, String bookId) {

		if (list.getItems() == null) {
			list.setItems(new LinkedList<>());
		}

		// 1.根据ID查询图书
		Book book = this.bookRepository.findById(bookId).get();
		// 2.判断图书是否已经在list里面存在，如果不在里面才能存储进去
		boolean exists = false;
		for (DebitItem b : list.getItems()) {
			if (b.getBook().getId().equals(bookId)) {
				// 图书已经存在
				exists = true;
			}
		}

		if (!exists) {
			DebitItem item = new DebitItem();
			item.setBook(book);
			list.getItems().add(item);
		}
	}

	@Override
	public void remove(DebitList list, String id) {
		// Java 8以及以后非常流行的【函数式编程】方式
		list.getItems().stream()//
				// 过滤需要的数据，把ID相同的图书的Item返回
				.filter(item -> item.getBook().getId().equals(id))//
				// 返回找到的第一条记录
				.findFirst()//
				// 如果第一条记录存在，则从集合里面删除
				.ifPresent(item -> list.getItems().remove(item));

		// 命令式编程，已经过时
//		// 1.找到需要被删除的对象
//		DebitItem item = null;
//		for (DebitItem i : list.getItems()) {
//			if (i.getBook().getId().equals(id)) {
//				item = i;
//				break;
//			}
//		}
//		// 2.如果找到了可以被删除的对象
//		if (item != null) {
//			// 3.从集合里面删除
//			list.getItems().remove(item);
//		}
	}
}
