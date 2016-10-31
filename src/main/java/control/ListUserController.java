package control;

import java.util.Collection;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class ListUserController extends AbstractController{

	@Override
	protected void doPost(HttpRequest request, HttpResponse response){
	
	}
	
	@Override
	protected void doGet(HttpRequest request, HttpResponse response){
		if(request.getSession().getAttribute("user") != null){ // 세션에 유저데이터가 연결 되었는지?(로그인 되었는지)
			response.sendRedirect("/user/login.html");
			return;
		}
		
		Collection<User> users = DataBase.findAll();
		StringBuilder sb = new StringBuilder();
		//유저 테이블 생성
		sb.append("<table border='1'>");
		for(User user : users){
			sb.append("<tr>");
			sb.append("<td>" + user.getUserId() + "</td>");
			sb.append("<td>" + user.getName() + "</td>");
			sb.append("<td>" + user.getEmail() + "</td>");
			sb.append("</tr>");
		}
		response.forwardBody(sb.toString());
	}
}
