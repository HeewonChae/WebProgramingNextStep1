package webserver;

import java.util.HashMap;
import java.util.Map;

import control.Controller;
import control.CreateUserController;
import control.ListUserController;
import control.LoginController;

public class RequestMapping {
	private static Map<String, Controller> controllers = new HashMap<>();
	
	static{
		controllers.put("/user/create", new CreateUserController());
		controllers.put("/user/login", new LoginController());
		controllers.put("/user/list", new ListUserController());
	}
	
	public static Controller getController(String requestUrl){
		return controllers.get(requestUrl);
	}
}
