package webserver;

import db.DataBase;
import model.User;

public class LoginController extends AbstractController {
	
	@Override
	protected void doPost(HttpRequest request, HttpResponse response){
		User user = DataBase.findUserById(request.getParam("userId"));

		if (user != null) { // 일치하는 유저가 없을 경우
			if (user.getPassword().equals(request.getParam("password"))) {
				response.addHeader("Set-cookie", "logined=true"); // 쿠키헤더 추가
				response.sendRedirect("/index.html");
			} else
				response.sendRedirect("/user/login_failed.html");
		} else
			response.sendRedirect("/user/login_failed.html");
	}
	
	@Override
	protected void doGet(HttpRequest request, HttpResponse response){
		
	}
}
