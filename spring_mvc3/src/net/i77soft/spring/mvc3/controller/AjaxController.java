/**
 * 
 */
package net.i77soft.spring.mvc3.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.i77soft.spring.mvc3.model.Client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Administrator
 *
 */

@Controller
@RequestMapping(value = "/ajax")
public class AjaxController {

	@SuppressWarnings("unused")
	private final static Log log = LogFactory.getLog(AjaxController.class);

	@RequestMapping("/ajax_test")
	public ModelAndView ajax_test(HttpServletRequest request)
	{
		ModelAndView mv = new ModelAndView("ajax/ajax_test");
		StaticController.addGlobalObjects(mv);
		mv.addObject("hello", "Hello World!");    // model中增加一个名为hello的字符串

		Client client = new Client();
		client.setName("User");
		mv.addObject("client", client);    // 再增加一个名为client的自定义对象
		return mv;
	}

	/**
	 * 一个返回json的方法，用ResponseBody标识
	 * 可以在url中定义参数中，实现RESTful真是太简单了
	 * 传参很灵活，可以从url中取，也可以定义普通的
	 */
	@RequestMapping(value="/client/{name}", method = RequestMethod.GET, headers = "Accept=application/json")
	//@RequestMapping(value="/client/{name}", method = RequestMethod.GET, headers = "Accept=*/*")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Client getClient(@PathVariable String name, String title){
		System.out.println("ajax /client/" + name);

		Client client = new Client();
		client.setName(title + " : " + name);

		return client;
	}

	@RequestMapping(value="/ajax1", method = RequestMethod.GET, headers = "Accept=application/json")
	//@RequestMapping(value="/ajax1", method = RequestMethod.GET, headers = "Accept=*/*")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Client getAjax1(String title) {
		System.out.println("ajax /ajax1 request--");

		Client client = new Client();
		client.setName("ajax1 test: title = \"" + title + "\"");

		return client;
	}

	@RequestMapping(value="/ajax1/*", method = RequestMethod.GET, headers = "Accept=application/json")
	//@RequestMapping(value="/ajax1", method = RequestMethod.GET, headers = "Accept=*/*")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Client getAjax1_(String title) {
		System.out.println("ajax /ajax1 request--");

		Client client = new Client();
		client.setName("ajax1 test: title = \"" + title + "\"");

		return client;
	}

	@RequestMapping(value="/ajax2/{name}", method = RequestMethod.GET, headers = "Accept=application/json")
	//@RequestMapping(value="/ajax2/{name}", method = RequestMethod.GET, headers = "Accept=*/*")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<Client> getAjax2(@PathVariable String name, String title) {
		System.out.println("ajax /ajax2/" + name);

		List<Client> clientList = new ArrayList<Client>();
		for (int i=0; i<5; i++) {
			Client client = new Client();
			client.setName("ajax2 test" + (i + 1));
			clientList.add(client);
		}

		return clientList;
	}

}
