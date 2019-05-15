package org.fkjava.commons.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.fkjava.commons.domain.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service // 把对象放入Spring容器里面
public class TokenManager {

	private static final Logger LOG = LoggerFactory.getLogger(TokenManager.class);

	@Autowired
	private RedisTemplate<String, AccessToken> accessTokenTemplate;

	public String getToken() {
		// 1.获取本地令牌
		// 需要配置一个RedisTemplate用于管理令牌
		BoundValueOperations<String, AccessToken> ops = accessTokenTemplate.boundValueOps("weixin_access_token");
		AccessToken at = ops.get();

		// 2.检查本地令牌是否存在、是否有效（可以通过过期时间自动处理）
		if (at == null) {
			// 如果没有获得分布式事务锁，尝试十次，每次间隔1分钟。
			for (int i = 0; i < 10; i++) {
				LOG.trace("缓存中没有令牌，尝试加上分布式锁");
				// 3.调用远程接口获取令牌，并在获取到令牌以后，把令牌存储在Redis里面
				// 增加分布式锁： 如果key不存在则设置进去；而如果key存在则等待60秒才能设置进去
				Boolean result = accessTokenTemplate.boundValueOps("weixin_access_token_lock")//
						.setIfAbsent(new AccessToken());
				LOG.trace("增加分布式锁的结果：{}", result);
				if (result == true) {
					try {
						// 判断令牌是否在Redis里面
						at = ops.get();
						if (at == null) {
							LOG.trace("重新获取缓存的令牌，也没有在本地获取到，尝试获取远程令牌");
							at = getRemoteToken();
							// 把对象存储到Redis里面
							ops.set(at);
							// 在对象过期后，Redis会自动把对象从数据库里面删除
							ops.expire(at.getExpiresIn() - 60, TimeUnit.SECONDS);
						} else {
							LOG.trace("本次重试正常获得令牌: {}", at.getAccessToken());
						}
						break;
					} finally {
						LOG.trace("删除分布式锁");
						accessTokenTemplate.delete("weixin_access_token_lock");
						synchronized (TokenManager.class) {
							TokenManager.class.notifyAll();
						}
					}
				} else {
					synchronized (TokenManager.class) {
						try {
							LOG.trace("其他线程锁定了数据，等待通知。如果没有通知则1分后重试");
							TokenManager.class.wait(1000 * 60);
						} catch (InterruptedException e) {
							LOG.error("无法等待分布式事务锁的通知：" + e.getLocalizedMessage(), e);
						}
					}
				}
			}
		}

		return at.getAccessToken();
	}

	// 获取远程令牌
	public AccessToken getRemoteToken() {
		// 在微信的公众号没有认证通过之前，先使用开发者工具里面的测试号来进行测试
		String appId = "wx375cd9c53c364fc4";
		String appSecret = "ad91eb3762d1336c39a417173bc47aba";
		String url = "https://api.weixin.qq.com/cgi-bin/token"//
				+ "?grant_type=client_credential"//
				+ "&appid=" + appId//
				+ "&secret=" + appSecret;

		// 1.创建HttpClient对象
		// 在Java 11才内置了HttpClient，如果是早期JDK需要使用第三方的jar文件
		HttpClient client = HttpClient.newBuilder()//
				.version(Version.HTTP_1_1)// 设置HTTP 1.1的协议版本
				.build();

		// 2.创建HttpRequest对象
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))//
				.GET()// 以GET方式发送请求
				.build();

		// 3.调用远程接口，返回JSON
		// BodyHandlers里面包含了许多内置的请求体、响应体的处理程序，ofString意思是使用String方式返回
		// Charset.forName("UTF-8")指定字符编码
		try {
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString(Charset.forName("UTF-8")));

			// 4.把返回的JSON转换为Java对象
			String json = response.body();// 响应体
			LOG.trace("获取令牌的返回：\n{}", json);

			if (json.indexOf("errcode") > 0) {
				// 出现了问题
				throw new RuntimeException("获取令牌出现问题：" + json);
			}
			ObjectMapper mapper = new ObjectMapper();
			AccessToken at = mapper.readValue(json, AccessToken.class);

			// 返回令牌
//			return at.getAccessToken();
			return at;
		} catch (Exception e) {
			// 不处理异常，直接包异常封装以后再抛出去
			throw new RuntimeException("获取令牌出现问题：" + e.getLocalizedMessage(), e);
		}
	}
}
