package application;

import application.socket.SocketTS;

public class Main_EnvServer {
	
	private static SocketTS socket_TS_server;
	
	private static String ip_address;
	private static Integer port_number;
	
	public static void main(String[] args) {
		ip_address = "localhost";
		port_number = 9999;
		launch_server();
	}
	
	public static void launch_server() {
		socket_TS_server = new SocketTS();
		socket_TS_server.setup(ip_address, port_number);
		socket_TS_server.run_server();
	}
}