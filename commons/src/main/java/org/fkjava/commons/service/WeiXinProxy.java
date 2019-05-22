package org.fkjava.commons.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

import org.fkjava.commons.domain.OutMessage;
import org.fkjava.commons.domain.User;
import org.fkjava.commons.domain.text.TextOutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 微信的代理接口，所有跟微信服务器的主动通讯（我们请求微信），都通过此类里面的方法进行封装
 * 
 * @author lwq
 *
 */
@Service
public class WeiXinProxy {

	private static final Logger LOG = LoggerFactory.getLogger(WeiXinProxy.class);
	@Autowired
	private TokenManager tokenManager;

	private HttpClient client = HttpClient.newBuilder()//
			.version(Version.HTTP_1_1)// 设置HTTP 1.1的协议版本
			.build();

	/**
	 * 利用OpenID到微信的服务器里面获取用户信息
	 * 
	 * @param openId
	 * @return
	 */
	public User getWxUser(String openId) {
		// 获取一个令牌
		String accessToken = this.tokenManager.getToken();

		String url = "https://api.weixin.qq.com/cgi-bin/user/info"//
				+ "?access_token=" + accessToken//
				+ "&openid=" + openId//
				+ "&lang=zh_CN";

		// 1.创建HttpClient对象
		// 在Java 11才内置了HttpClient，如果是早期JDK需要使用第三方的jar文件
//		HttpClient client = HttpClient.newBuilder()//
//				.version(Version.HTTP_1_1)// 设置HTTP 1.1的协议版本
//				.build();

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

			LOG.trace("获取用户信息的返回：\n{}", json);

			if (json.indexOf("errcode") > 0) {
				// 出现了问题
				throw new RuntimeException("获取用户信息出现问题：" + json);
			}
			ObjectMapper mapper = new ObjectMapper();
			User user = mapper.readValue(json, User.class);

			return user;
		} catch (Exception e) {
			// 不处理异常，直接包异常封装以后再抛出去
			throw new RuntimeException("获取令牌出现问题：" + e.getLocalizedMessage(), e);
		}
	}

	/**
	 * 通过客服接口，发送文本信息给指定的用户
	 * 
	 * @param openId 接收者
	 * @param string 文本信息的内容
	 */
	public void sendText(String openId, String string) {

		TextOutMessage text = new TextOutMessage(openId, string);
		// 把返回消息转换为JSON，然后调用远程接口发送出去
		send(text);
	}

	private void send(OutMessage msg) {
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			String json = mapper.writeValueAsString(msg);
//			LOG.trace("发送信息信息的内容：\n{}", json);
//
//			String accessToken = this.tokenManager.getToken();
//			String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + accessToken;
//			HttpRequest request = HttpRequest.newBuilder(URI.create(url))//
//					// 把消息的内容，转换为JSON字符串，然后发送给微信
//					.POST(BodyPublishers.ofString(json, Charset.forName("UTF-8")))// 以POST方式发送请求
//					.build();
////			HttpResponse<String> response = client.send(request, BodyHandlers.ofString(Charset.forName("UTF-8")));
////			LOG.trace("发送信息信息的返回：\n{}", response.body());
//
//			// 异步发送请求
//			CompletableFuture<HttpResponse<String>> future = client.sendAsync(request,
//					BodyHandlers.ofString(Charset.forName("UTF-8")));
//			// 异步处理结果
//			future.thenAcceptAsync(response -> {
//				LOG.trace("发送信息信息的返回：\n{}", response.body());
//			});
//		} catch (IOException e) {
//			throw new RuntimeException("发送消息出现问题：" + e.getLocalizedMessage(), e);
//		}

		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(msg);

			String accessToken = this.tokenManager.getToken();
			String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + accessToken;

			post(url, json);
		} catch (Exception e) {
			throw new RuntimeException("发送消息出现问题：" + e.getLocalizedMessage(), e);
		}
	}

	public void saveMenu(String json) {
		String accessToken = this.tokenManager.getToken();
		String url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + accessToken;

		post(url, json);
	}

	// 通用POST方法，负责把消息使用POST方式发送到微信公众号
	private void post(String url, String json) {
		try {
			LOG.trace("POST的URL地址：\n{}", url);
			LOG.trace("POST发送到微信公众号里面的信息：\n{}", json);
			HttpRequest request = HttpRequest.newBuilder(URI.create(url))//
					// 把消息的内容，转换为JSON字符串，然后发送给微信
					.POST(BodyPublishers.ofString(json, Charset.forName("UTF-8")))// 以POST方式发送请求
					.build();
			// 异步发送请求
			CompletableFuture<HttpResponse<String>> future = client.sendAsync(request,
					BodyHandlers.ofString(Charset.forName("UTF-8")));
			// 异步处理结果
			future.thenAcceptAsync(response -> {
				LOG.trace("POST发送以后的返回：\n{}", response.body());
			});
		} catch (Exception e) {
			throw new RuntimeException("POST发送信息给微信公众号出现问题：" + e.getLocalizedMessage(), e);
		}
	}
}
