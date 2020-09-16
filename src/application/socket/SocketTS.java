package application.socket;

import java.net.*;
import java.nio.ByteBuffer;

import application.ts.Device;
import application.ts.TupleSpace;

import java.io.*;

public class SocketTS implements Runnable {
	
	private TupleSpace ts;
	
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    
    private DataInputStream input_stream  = null;
    private DataOutputStream output_stream = null;
    
    private int thread_action;
    
    private Boolean is_connected;
    private String peer_type;
    private String ip;
    private int port;
    
    public String ts_env_name;
    public String ts_device_name;
    public String ts_ip_address;
    public Integer ts_port_number;
    
    public SocketTS() {
    	this.thread_action = 1;
    	
    	this.is_connected = false;
    	this.peer_type = "";
		this.ip = "";
		this.port = -1;
    }
    
    public void setup(String ip, int port) {
        this.ip   = ip;
        this.port = port;
    }
    
    public void setup_ts_variables(String device, String ip, int port) {
    	this.ts_device_name = device;
    	this.ts_ip_address = ip;
    	this.ts_port_number = port;
    }
    
 	public void run_server() {
 		new Thread(this).start();
 	}
 	
    public void run(){
    	ts = new TupleSpace();
    	ts.init_tuplespace_server();
    	while(true) {
    		if(thread_action==1) {
    			//WAIT CONNECTION
    	    	try {
    	        	this.peer_type = "server";
    	            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
    	            
    	            socket = serverSocket.accept();
    	            serverSocket.close();
    	            
    	            input_stream = new DataInputStream(socket.getInputStream());
    	            output_stream = new DataOutputStream(socket.getOutputStream());
    	            
    	            thread_action = 2;// Flag receive behavior
    	            
    	            is_connected = true;
    	    	} catch(Exception e) {}
        	}
        	if(thread_action==2) {
    			//RECEIVE INFORMATION
        		try {
        			//Socket receiving...
        		    byte[] resultBuff = receive_call();
        		    //Process data...
        		    byte[] resultBuffContent = new byte[resultBuff.length-1];
        		    byte[] code = new byte[1];
        		    ByteBuffer bb = ByteBuffer.wrap(resultBuff);
        		    bb.get(code, 0, code.length);
        		    bb.get(resultBuffContent, 0, resultBuffContent.length-1);
        		    String code_s = new String(code, "UTF-8");
        		    if(code_s=="a") {
        		    	bb = ByteBuffer.wrap(resultBuffContent);
        				byte[] a = new byte[1000];
        				byte[] b = new byte[1000];
        				byte[] c = new byte[1000];
        				byte[] d = new byte[1000];
             		    bb.get(a, 0, a.length);
            		    bb.get(b, 0, b.length);
            		    bb.get(c, 0, a.length);
            		    bb.get(d, 0, b.length);
            		    String a_s = new String(a, "UTF-8").replace(" ", "");
            		    String b_s = new String(a, "UTF-8").replace(" ", "");
            		    Integer c_s = Integer.valueOf(new String(a, "UTF-8").replace(" ", ""));
            		    String d_s = new String(a, "UTF-8").replace(" ", "");
        		    	Boolean response = ts.connect(a_s, b_s, c_s, d_s);
        		    	
        		    	serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
        	            socket = serverSocket.accept();
        	            serverSocket.close();
        	            input_stream = new DataInputStream(socket.getInputStream());
        	            output_stream = new DataOutputStream(socket.getOutputStream());
        	            
        	            send_call(response.toString().getBytes());
        	            disconnect(false);
        		    }
        		    else if(code_s=="b") {
        		    	String response = "";
        		    	if(ts.init_admin_tuple()) {
        		    		response = ts.get_environment_name();
        		    	}
        		    	
        		    	serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
        	            socket = serverSocket.accept();
        	            serverSocket.close();
        	            input_stream = new DataInputStream(socket.getInputStream());
        	            output_stream = new DataOutputStream(socket.getOutputStream());
        	            
        	            send_call(response.toString().getBytes());
        	            disconnect(false);
        		    }
        		    else if(code_s=="c") {
        		    	ts.start();
        		    }
        		    else if(code_s=="d") {
        		    	bb = ByteBuffer.wrap(resultBuffContent);
        				byte[] a = new byte[100];
             		    bb.get(a, 0, a.length);
            		    String a_s = new String(a, "UTF-8").replace(" ", "");
        		    	ts.set_axis(a_s);
        		    }
        		    else if(code_s=="e") {
        		    	Boolean response = ts.update_device();
        		    	
        		    	serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
        	            socket = serverSocket.accept();
        	            serverSocket.close();
        	            input_stream = new DataInputStream(socket.getInputStream());
        	            output_stream = new DataOutputStream(socket.getOutputStream());
        	            
        	            send_call(response.toString().getBytes());
        	            disconnect(false);
        		    }
                    is_connected = false;
                    thread_action = 1;
                } catch(Exception e) {
                    is_connected = false;
                    thread_action = 1;
                }
        	}
    	}
    }
    
    public Boolean connect(){
    	try {
    		this.peer_type = "client";
    		
    		socket = new Socket(InetAddress.getByName(ip), port);
    		
    		input_stream = new DataInputStream(socket.getInputStream());
    		output_stream = new DataOutputStream(socket.getOutputStream());
    		
    		thread_action=2;// Flag receive behavior
    		
    		is_connected = true;
    		
    		new Thread(this).start();
    		
    		return true;
    	} catch(Exception e) {
    		System.out.println(e);
    		return false;
    	}
    }
    
    public Boolean disconnect(Boolean include_server){
    	try {
			socket.close();
			
			if(include_server) {
				if(is_server()) {
					serverSocket.close();
				}
			}
			
			return true;
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
    }
 	
    // Getters
    public String get_peer_type() {
    	return this.peer_type;
    }
    
    public String get_ip_address() {
    	return this.ip;
    }
    
    public Integer get_port_number() {
    	return this.port;
    }
    
    public Boolean is_server() {
    	if(this.peer_type.equals("server")) {
    		return true;
    	}
    	return false;
    }
    
    public Boolean is_client() {
    	if(this.peer_type.equals("client")) {
    		return true;
    	}
    	return false;
    }
    
    public Boolean has_connection() {
    	return is_connected;
    }
    
    // TS methods
	public byte[] receive_call() {
	    byte[] resultBuff = new byte[0];
	    byte[] buff = new byte[1024];
		try {
		    int k = -1;
		    while((k = input_stream.read(buff, 0, buff.length)) > -1) {
		        byte[] tbuff = new byte[resultBuff.length + k]; // temp buffer size = bytes already read + bytes last read
		        System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length); // copy previous bytes
		        System.arraycopy(buff, 0, tbuff, resultBuff.length, k);  // copy current lot
		        resultBuff = tbuff; // call the temp buffer as your result buff
		    }
		}catch(Exception e){
			System.out.println(e);
		}
		return resultBuff;
	}
	
	public void send_call(byte[] bytes) {
		try {
			output_stream.write(bytes);
			output_stream.flush();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
    public Boolean ts_connect(String device_name, String ip_address, Integer port_number, String axis) {
		if(connect()) {
			byte[] code = new byte[1];
			code = "a".getBytes();
			byte[] a = new byte[1000];
			byte[] b = new byte[1000];
			byte[] c = new byte[1000];
			byte[] d = new byte[1000];
			a = String.format("%1$"+1000+"s", device_name).getBytes();
			b = String.format("%1$"+1000+"s", ip_address).getBytes();
			c = String.format("%1$"+1000+"s", port_number).getBytes();
			d = String.format("%1$"+1000+"s", axis).getBytes();
			byte[] bytes = ByteBuffer.allocate(code.length + a.length + b.length + c.length + d.length).put(code).put(a).put(b).put(c).put(d).array();
        	send_call(bytes);
        	disconnect(false);
        	do {
        		if(connect()) {
        			byte[] resultBuff = receive_call();
        		    byte[] e = new byte[1];
        		    ByteBuffer bb = ByteBuffer.wrap(resultBuff);
        		    bb.get(e, 0, e.length);
        		    disconnect(false);
        		    try {
						Boolean result = new Boolean(new String(e, "UTF-8"));
						return result;
					} catch (Exception exp) {
						System.out.println(exp);
						return false;
					}
        		}
        	}while(true);
		}
		return false;
    }
    
    public String ts_init_admin_tuple() {
		if(connect()) {
			byte[] code = new byte[1];
			code = "b".getBytes();
        	send_call(code);
        	disconnect(false);
        	do {
        		if(connect()) {
        			byte[] resultBuff = receive_call();
        		    byte[] a = new byte[1];
        		    ByteBuffer bb = ByteBuffer.wrap(resultBuff);
        		    bb.get(a, 0, a.length);
        		    disconnect(false);
        		    try {
						String result = new String(a, "UTF-8");
						return result;
					} catch (Exception exp) {
						System.out.println(exp);
						return "";
					}
        		}
        	}while(true);
		}
		return "";
    }
    
    public void ts_start() {
		if(connect()) {
			byte[] code = new byte[1];
			code = "c".getBytes();
        	send_call(code);
        	disconnect(false);
		}
		return;
    }

    public void ts_set_axis(String axis) {
    	if(connect()) {
			byte[] code = new byte[1];
			code = "d".getBytes();
			byte[] a = new byte[100];
			a = String.format("%1$"+100+"s", axis).getBytes();
			byte[] bytes = ByteBuffer.allocate(code.length + a.length).put(code).put(a).array();
        	send_call(bytes);
        	disconnect(false);
		}
    }

    public Boolean ts_update_device() {
		if(connect()) {
			byte[] code = new byte[1];
			code = "e".getBytes();
			byte[] a = new byte[100];
			a = String.format("%1$"+1000+"s", a).getBytes();
			byte[] bytes = ByteBuffer.allocate(code.length + a.length).put(code).put(a).array();
        	send_call(bytes);
        	disconnect(false);
        	do {
        		if(connect()) {
        			byte[] resultBuff = receive_call();
        		    byte[] e = new byte[1];
        		    ByteBuffer bb = ByteBuffer.wrap(resultBuff);
        		    bb.get(e, 0, e.length);
        		    disconnect(false);
        		    try {
						Boolean result = new Boolean(new String(e, "UTF-8"));
						return result;
					} catch (Exception exp) {
						System.out.println(exp);
						return false;
					}
        		}
        	}while(true);
		}
		return false;
    }

    public Device ts_get_device(String device_name, String env_name) {
		if(connect()) {
			byte[] code = new byte[1];
			code = "f".getBytes();
			byte[] a = new byte[1000];
			byte[] b = new byte[1000];
			a = String.format("%1$"+1000+"s", device_name).getBytes();
			b = String.format("%1$"+1000+"s", env_name).getBytes();
			byte[] bytes = ByteBuffer.allocate(code.length + a.length + b.length).put(code).put(a).put(b).array();
        	send_call(bytes);
        	disconnect(false);
        	do {
        		if(connect()) {
        			byte[] resultBuff = receive_call();
        		    disconnect(false);
        		    try {
        		    	ByteArrayInputStream bis = new ByteArrayInputStream(resultBuff);
        		    	ObjectInput in = null;
        		    	in = new ObjectInputStream(bis);
        		    	Device dev = (Device) in.readObject(); 
						return dev;
					} catch (Exception exp) {
						System.out.println(exp);
						return null;
					}
        		}
        	}while(true);
		}
		return null;
    }

}

