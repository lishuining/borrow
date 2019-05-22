package org.fkjava.weixin.menu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/self-menu")
public class MenuController {

	@GetMapping
	public ModelAndView index() {
		// 默认只会在/META-INF/resources/目录下去找JSP文件。
		// 由于使用了JSP文件，所以必须要加入JSP的解析器（修改pom.xml），否则JSP文件找不到！
		return new ModelAndView("/WEB-INF/views/self-menu/index.jsp");
	}

	@PostMapping
	public ModelAndView save() {
		return null;
	}
}
