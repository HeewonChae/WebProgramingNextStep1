package control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class CreateUserController extends AbstractController{
	
	private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);
	
	@Override
	protected void doPost(HttpRequest request, HttpResponse response){
		User user = new User(request.getParam("userId"), 
				request.getParam("password"), 
				request.getParam("name"), 
				request.getParam("email"));
		
		log.debug("User : {}", user);
		DataBase.addUser(user); //DB에 데이터 저장
		
		//응답패킷 보냄.
		response.sendRedirect("/index.html");
	}
	
	@Override
	protected void doGet(HttpRequest request, HttpResponse response){
		
	}

}
