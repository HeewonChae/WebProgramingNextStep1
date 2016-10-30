package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    @Override
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	//요청라인과 헤더를 분석하여 저장
        	HttpRequest request = new HttpRequest(in);
        	HttpResponse response = new HttpResponse(out);
        	String path = getDefaultPath(request.getPath());
        	
        	if(path.equals("/user/create")){
        		User user = new User(request.getParam("userId"), 
			        				request.getParam("password"), 
			        				request.getParam("name"), 
			        				request.getParam("email"));
        		log.debug("User : {}", user);
        		DataBase.addUser(user); //DB에 데이터 저장
        		
        		//응답패킷 보냄.
        		response.sendRedirect("/index.html");
        	}
        	else if(path.equals("/user/login")){ //요청 메시지가 로그인일경우
        		User user = DataBase.findUserById(request.getParam("userId"));
        		
        		if(user != null){ //일치하는 유저가 없을 경우
        			if(user.getPassword().equals(request.getParam("password"))){
            			response.addHeader("Set-cookie", "logined=true"); // 쿠키헤더 추가
            			response.sendRedirect("/index.html");
        			}else
        				response.sendRedirect("/user/login_failed.html");
        		}else
        			response.sendRedirect("/user/login_failed.html");
        	}
        	else if(path.equals("/user/list")){
        		if(!isLogin(request.getHeader("Cookie"))){
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
        	else{
        		response.forward(path);
        	}
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private String getDefaultPath(String path) {
		// TODO Auto-generated method stub
    	if(path.equals("/")){
    		return "/index.html";
    	}
		return path;
	}

	private boolean isLogin(String line) {
    	Map<String, String> cookies = HttpRequestUtils.parseCookies(line);
    	String value = cookies.get("logined");
    	if(value == null)
    		return false;
    	
		return Boolean.parseBoolean(value);
	}
}
