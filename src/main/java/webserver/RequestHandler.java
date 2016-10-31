package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import control.Controller;
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
        	
        	//세션아이디 확인
        	if(request.getCookies().getCookie("JSESSIONEID") == null){//아직 세션이 설정되어있지 않을 경우
        		response.addHeader("Set-Cookie", "JSESSIONEID=" + UUID.randomUUID()); // 랜덤하게 세션값을 생성하여 설정
        	}
        	
        	//컨트롤 맵핑
        	Controller controller = RequestMapping.getController(request.getPath());
        	if(controller == null){
        		String path = getDefaultPath(request.getPath());
        		response.forward(path);
        	}else{
        		controller.service(request, response);
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
}
