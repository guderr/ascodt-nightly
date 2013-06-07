package de.tum.ascodt.plugin.services;


public class SocketService{
	
	private static SocketService singleton;
	private int port=50000;
	public SocketService() {
		
		
	}
	public static SocketService getDefault(){
		if(singleton==null)
			singleton=new SocketService();
		return singleton;
	}
	public synchronized int getFreePort(){
		return port++; 
	}
	
}
