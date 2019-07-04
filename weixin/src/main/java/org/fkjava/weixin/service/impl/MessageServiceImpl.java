package org.fkjava.weixin.service.impl;

import org.fkjava.commons.domain.InMessage;
import org.fkjava.commons.domain.OutMessage;
import org.fkjava.weixin.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

// 相当于由Spring帮助开发者执行一个new，创建一个对象。
// 当需要使用的时候，可以直接用@Autowirdd注解，把对象注入到需要使用的地方。
@Service
public class MessageServiceImpl implements MessageService {

	private static final Logger LOG = LoggerFactory.getLogger(MessageServiceImpl.class);

	@Autowired // 自动注入
	@Qualifier("inMessageTemplate") // 根据名字自动注入
	private RedisTemplate<String, ? extends InMessage> inMessageTemplate;

	@Override
	public OutMessage onMessage(InMessage msg) {
		LOG.trace("转换后的消息对象：\n{}\n", msg);

		
		try {
			inMessageTemplate.convertAndSend("kemao_3_" + msg.getMsgType(), msg);
		} catch (Exception e) {
			LOG.error("把转换的消息放入队列中出现问题：" + e.getLocalizedMessage(), e);
		}

		LOG.trace("转换后的消息对象已经放入队列中");
		// 返回的信息先不管
		return null;
	}
}
