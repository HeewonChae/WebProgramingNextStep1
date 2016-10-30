package webserver;

public enum HttpMethod {
	POST,
	GET;
	
	public boolean isPost(){
		return this == POST;
	}
	
	public boolean isGET(){
		return this == GET;
	}
}
