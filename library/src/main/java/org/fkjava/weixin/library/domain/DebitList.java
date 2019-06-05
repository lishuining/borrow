package org.fkjava.weixin.library.domain;

import java.util.List;

import org.fkjava.commons.domain.User;

// 在用户加入列表以后，只是准备借阅。一旦正式借阅以后会把图书从此列表移除，并且放入真正的借阅历史记录中。
public class DebitList {

	// 谁的借阅列表
	private User user;
	// 准备借阅哪些书：每次借阅的时候可以加入多个本书，每本数都是一个DebitItem
	private List<DebitItem> items;

	public List<DebitItem> getItems() {
		return items;
	}

	public void setItems(List<DebitItem> items) {
		this.items = items;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
