package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	
        	//사용자로부터 입력값을 받아오기위해..
        	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        	//요청라인
        	String line = bufferedReader.readLine();
        	log.debug("request lins : {}", line);
        	
        	if(line == null)
        		return;
        	
        	String[] tokens = line.split(" ");
        	
        	//본문의 길이
        	int contentLength = 0;
        	boolean logined = false; //로그인 상태인지
        	//헤더 읽기
        	while(!line.equals(""))
        	{
        		line = bufferedReader.readLine();
        		log.debug("header : {}", line);
        		if(line.contains("Content-Length"))
        			contentLength = getContentLength(line);
        		else if(line.contains("Cookie"))
        			logined = isLogin(line);
        	}
        	
        	//위에서 요청라인을 파싱한 값.
        	String url = tokens[1];
        	if(url.endsWith(".css")){
        		DataOutputStream dos = new DataOutputStream(out);
        		byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        		response200CSSHeader(dos, body.length);
        		responseBody(dos, body);
        	}
        	else if(url.equals("/user/create")){
        		//int index = url.indexOf("?"); //경로와 쿼리스트링을 구분하는 ?의 위치를 찾음
        		//String queryString = url.substring(index+1);
        		
        		String body = IOUtils.readData(bufferedReader, contentLength);
        		//바디를 파싱함
        		Map<String, String> params = HttpRequestUtils.parseQueryString(body);
        		//유저 객체로 저장
        		User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        		log.debug("User : {}", user);
        		DataBase.addUser(user); //DB에 데이터 저장
        		
        		//응답패킷 보냄.
        		DataOutputStream dos = new DataOutputStream(out);
        		response302Header(dos, "/index.html");
        	}
        	else if(url.equals("/user/login")){ //요청 메시지가 로그인일경우
        		String body = IOUtils.readData(bufferedReader, contentLength);
        		//바디를 파싱함
        		Map<String, String> params = HttpRequestUtils.parseQueryString(body);
        		User user = DataBase.findUserById(params.get("userId"));
        		
        		if(user == null){ //일치하는 유저가 없을 경우
        			responseResource(out, "/user/login_failed.html");
        		}
        		
        		if(user.getPassword().equals(params.get("password"))){
        			DataOutputStream dos = new DataOutputStream(out);
        			response302LoginSuccessHeader(dos, "/index.html");
        		}else{
        			responseResource(out, "/user/login_failed.html");
        		}
        	}
        	else if(url.equals("/user/list")){
        		if(!logined){
        			responseResource(out, "/user/login.html");
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
        		sb.append("</table>");
        		byte[] body = sb.toString().getBytes();
        		DataOutputStream dos = new DataOutputStream(out);
        		response200Header(dos, body.length);
        		responseBody(dos, body);
        	}
        	else{
        		responseResource(out, url);
        	}
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response200CSSHeader(DataOutputStream dos, int length) {
		// TODO Auto-generated method stub
    	try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css\r\n");
            dos.writeBytes("Content-Length: " + length + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
	}

	private boolean isLogin(String line) {
		// TODO Auto-generated method stub
    	String[] headerTokens = line.split(":");
    	Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
    	String value = cookies.get("logined");
    	if(value == null)
    		return false;
    	
		return Boolean.parseBoolean(value);
	}

	private void response302LoginSuccessHeader(DataOutputStream dos, String url) {
		// TODO Auto-generated method stub
    	try{
			dos.writeBytes("Http/1.1 302 Redirect \r\n");
			dos.writeBytes("Set-Cookie: logined=true \r\n"); // 로그인 성공 쿠키를 헤더 써넣어 보냄
			dos.writeBytes("Location: " + url + " \r\n");
			dos.writeBytes("\r\n");
		}catch(IOException e){
			log.error(e.getMessage());
		}
		
	}

	private void responseResource(OutputStream out, String url) throws IOException {
		// TODO Auto-generated method stub
    	DataOutputStream dos = new DataOutputStream(out);
    	byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
    	response200Header(dos, body.length);
    	responseBody(dos, body);
		
	}

	//302 응답 해더
    private void response302Header(DataOutputStream dos, String url) {
		// TODO Auto-generated method stub
		try{
			dos.writeBytes("Http/1.1 302 Redirect \r\n");
			dos.writeBytes("Location: " + url + " \r\n");
			dos.writeBytes("\r\n");
		}catch(IOException e){
			log.error(e.getMessage());
		}
	}

	//콘텐트의 길이 파싱
    private int getContentLength(String line) {
		// TODO Auto-generated method stub
    	String[] headerTokens = line.split(":");
		return Integer.parseInt(headerTokens[1].trim());
	}

	//헤더를 만드는 부분
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
