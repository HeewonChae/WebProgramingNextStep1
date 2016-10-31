package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {

	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

	private HttpMethod method;
	private String path;
	private Map<String, String> headers = new HashMap<String, String>();
	private Map<String, String> params = new HashMap<String, String>();

	public HttpRequest(InputStream in) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = br.readLine();
			if (line == null)
				return;

			// 요청라인 분석
			processRequestLine(line);

			// 헤더분석 각 메타데이터마다 map에 삽입.
			line = br.readLine();
			while (!line.equals("")) {
				log.debug("header : {}", line);
				String[] tokens = line.split(":");
				headers.put(tokens[0].trim(), tokens[1].trim());
				line = br.readLine();

			}

			if (method.isPost()) { // 메소드가 post일 경우
				// 바디의 길이만큼 데이터를 가져온후
				String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
				params = HttpRequestUtils.parseQueryString(body); // 데이터 파싱
			}
		} catch (IOException io) {
			// TODO: handle exception
			log.error(io.getMessage());
		}
	}

	private void processRequestLine(String RequestLine) {
		// TODO Auto-generated method stub
		log.debug("Request Line : {}", RequestLine);
		String[] tokens = RequestLine.split(" ");
		method = HttpMethod.valueOf(tokens[0]);

		if (method.isPost()) {
			path = tokens[1];
			return;
		}

		// 메소드가 get일 경우
		int index = tokens[1].indexOf("?");
		if (index == -1) { // 그냥 페이지 요청일경우
			path = tokens[1];
		} else {// 다른 사용자 요청이 있을경우
			path = tokens[1].substring(0, index);
			params = HttpRequestUtils.parseQueryString(tokens[1].substring(index + 1));
		}
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public String getHeader(String string) {
		return headers.get(string);
	}
	
	public String getParam(String string){
		return params.get(string);
	}
	
	public boolean isLogin()
	{
		String value = HttpRequestUtils.parseCookies(headers.get("Cookie")).get("logined");
    	if(value == null)
    		return false;
    	
		return Boolean.parseBoolean(value);
	}
	
	public HttpCookie getCookies(){
		return new HttpCookie(getHeader("Cookie"));
	}
	
	public HttpSession getSession(){
		return HttpSessions.getSession(getCookies().getCookie("JSESSIONID"));
	}
}
