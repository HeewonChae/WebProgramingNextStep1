package control;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;
import webserver.HttpSession;

public class LoginController extends AbstractController {
	
	@Override
	protected void doPost(HttpRequest request, HttpResponse response){
		User user = DataBase.findUserById(request.getParam("userId"));

		if (user != null) { // 일치하는 유저가 없을 경우
			if (user.getPassword().equals(request.getParam("password"))) {
				HttpSession session = request.getSession();
				session.setAttribute("user", user); // 세션에 유저 데이터 넣음.
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
