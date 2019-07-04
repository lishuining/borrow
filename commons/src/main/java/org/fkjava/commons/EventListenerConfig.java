package org.fkjava.commons;

import org.fkjava.commons.domain.AccessToken;
import org.fkjava.commons.domain.InMessage;
import org.fkjava.commons.domain.event.EventInMessage;
import org.fkjava.commons.service.JsonRedisSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

public interface EventListenerConfig extends CommandLineRunner, DisposableBean {

	public static final Logger LOG = LoggerFactory.getLogger(EventListenerConfig.class);
	// 运行器的监视器，将会在这个监视器上等待停止的通知
	public final Object runnerMonitor = new Object();

	// 此程序中，run方法必须实现，否则无法收到消息！
	@Override
	public default void run(String... args) throws Exception {
		// 实现CommandLineRunner接口，在Spring Boot项目启动之初执行的
		// 等待退出通知
		Thread t = new Thread(() -> {
			synchronized (runnerMonitor) {
				try {
					runnerMonitor.wait();
				} catch (InterruptedException e) {
					LOG.error("运行时监视器出现问题：" + e.getLocalizedMessage(), e);
				}
			}
		});
		t.start();
	}

	@Override
	public default void destroy() throws Exception {
		
		// 发送退出通知
		synchronized (runnerMonitor) {
			runnerMonitor.notify();
		}
	}

	// RedisTemplate是一个模板，用于访问数据库的！
	@Bean // 把对象放入容器里面
	public default RedisTemplate<String, ? extends InMessage> inMessageTemplate(//
			
			@Autowired RedisConnectionFactory connectionFactory) {

		RedisTemplate<String, ? extends InMessage> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		// 使用序列化程序完成对象的序列化和反序列化，可以自定义
		
		template.setValueSerializer(jsonRedisSerializer());

		return template;
	}

	// 最终把令牌存储到Redis里面、从Redis里面获取令牌都是通过这个模板来实现的
	@Bean
	public default RedisTemplate<String, AccessToken> accessTokenTemplate(//
			@Autowired RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, AccessToken> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setValueSerializer(jsonRedisSerializer());

		return template;
	}

	@Bean
	public default JsonRedisSerializer<InMessage> jsonRedisSerializer() {
		return new JsonRedisSerializer<InMessage>();
	}

	@Bean
	public default RedisMessageListenerContainer messageListenerContainer(//
			@Autowired RedisConnectionFactory connectionFactory) {
		RedisMessageListenerContainer c = new RedisMessageListenerContainer();

		// 设置连接工厂，设置连接工厂以后才能连接到消息队列
		c.setConnectionFactory(connectionFactory);

		// 订阅event消息，当队列中有event消息我们就可以收到
		ChannelTopic topic = new ChannelTopic("kemao_3_event");

		//
		MessageListener listener = (message, pattern) -> {
//				byte[] channel = message.getChannel();// 通道名称
			byte[] body = message.getBody();// 消息内容

			// 把消息转换为Java对象
			JsonRedisSerializer<InMessage> serializer = jsonRedisSerializer();
			InMessage msg = serializer.deserialize(body);

			// 强制转换，然后根据消息的事件类型，执行不同的业务
			EventInMessage event = (EventInMessage) msg;

			handleEvent(event);

		};
		c.addMessageListener(listener, topic);

		return c;
	}

	public void handleEvent(EventInMessage event);
}
