package org.fkjava.weixin.processors.impl;

import org.fkjava.commons.domain.User;
import org.fkjava.commons.domain.event.EventInMessage;
import org.fkjava.commons.processors.EventMessageProcessor;
import org.fkjava.commons.repository.UserRepository;
import org.fkjava.commons.service.WeiXinProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("subscribeMessageProcessor")
public class SubscribeEventMessageProcessor implements EventMessageProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(SubscribeEventMessageProcessor.class);

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private WeiXinProxy proxy;

	@Override
	public void onMessage(EventInMessage event) {
		LOG.trace("关注事件处理器被调用，收到的消息:\n {} ", event);

		if (!event.getEvent().equals("subscribe")) {
			// 只处理关注事件
			return;
		}

		
		// 1.3.需要增加一个repository包，在此包里面增加UserRepository接口，用于操作User类的数据

		// 1.4.在UserRepository类里面增加一个根据用户的OpenID来查询用户信息的方法，如果返回一个User对象，表示之前有关注
		String openId = event.getFromUserName();
		User user = this.userRepository.findByOpenId(openId);

		// 2.如果用户已经关注，直接忽略不处理消息
		if (user != null) {
			if (user.getStatus() == User.Status.IS_SUBSCRIBED) {
				return;
			}

			// 3.如果用户之前曾经关注，但是已经取消，现在重新关注，把状态修改成【已关注】即可，并且重新获取用户信息
		}
		// 4.如果用户之前没有关注，新增关注记录，并且获取用户信息

		User wxUser = this.proxy.getWxUser(openId);// 调用远程接口获取用户信息
		if (user != null) {
			wxUser.setId(user.getId());
		}
		wxUser.setStatus(User.Status.IS_SUBSCRIBED);
		// 保存用户信息
		this.userRepository.save(wxUser);

		// 5.通过客服接口，返回一个关注后的欢迎消息给用户
		this.proxy.sendText(openId, "欢迎关注公众号，回复\"学习\"可以获得智能菜单。");
	}
}
