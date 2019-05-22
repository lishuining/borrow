package org.fkjava.weixin.menu.domain;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

// 这里所有的属性，都直接使用默认转换，即使不能满足微信菜单的要求，也不管。
// 最后在发送给微信的时候，手动来转！
@Entity
@Table(name = "wx_self_sub_menu")
public class Menu {

	@Id
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@GeneratedValue(generator = "uuid2")
	@Column(length = 36)
	private String id;
	private String name;
	private String type;
	// 使用反单引号的目的：避免把关键字生成SQL语句，关键字生成SQL语句，会造成创建表失败
	@Column(name = "`key`")
	private String key;
	private String appId;
	private String url;
	private String pagePath;
	private String mediaId;
	// 一个一级菜单最多7个二级菜单
	@OneToMany(cascade = CascadeType.ALL)  // 一对多
	@JoinColumn(name = "parent_id")
	private List<Menu> subMenus = new LinkedList<>();

	// 使用@Transient注解，表示值不要保存到数据库，用于维护页面的状态。
	@Transient
	private boolean show;
	@Transient
	private boolean active;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPagePath() {
		return pagePath;
	}

	public void setPagePath(String pagePath) {
		this.pagePath = pagePath;
	}

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public List<Menu> getSubMenus() {
		return subMenus;
	}

	public void setSubMenus(List<Menu> subMenus) {
		this.subMenus = subMenus;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
