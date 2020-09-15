package application.socket;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.concurrent.Semaphore;

import javafx.util.Pair;

import java.io.*;

public class SocketFile implements Runnable {
	
	private Semaphore mutex;
	
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    
    private DataInputStream input_stream  = null;
    private DataOutputStream output_stream = null;
    
    private String device_socket = "";
    private String filepath_socket = "";
    
    private int thread_action;
    
    private Boolean is_connected;
    private String peer_type;
    private String ip;
    private int port;
    
    public SocketFile() {
    	mutex = new Semaphore(1);
    	
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
    
 	public void run_server() {
 		new Thread(this).start();
 	}
 	
    public void run(){
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
        		    byte[] resultBuff = new byte[0];
        		    byte[] buff = new byte[1024];
        		    int k = -1;
        		    while((k = input_stream.read(buff, 0, buff.length)) > -1) {
        		        byte[] tbuff = new byte[resultBuff.length + k]; // temp buffer size = bytes already read + bytes last read
        		        System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length); // copy previous bytes
        		        System.arraycopy(buff, 0, tbuff, resultBuff.length, k);  // copy current lot
        		        resultBuff = tbuff; // call the temp buffer as your result buff
        		    }
        		    //Process data...
        		    byte[] device_name = new byte[SocketConstants.DEVICENAME_BYTE_SIZE];
        		    byte[] file_name = new byte[SocketConstants.FILENAME_BYTE_SIZE];
        		    byte[] file_content = new byte[resultBuff.length-SocketConstants.DEVICENAME_BYTE_SIZE-SocketConstants.FILENAME_BYTE_SIZE];
        		    ByteBuffer bb = ByteBuffer.wrap(resultBuff);
        		    bb.get(device_name, 0, device_name.length);
        		    bb.get(file_name, 0, file_name.length);
        		    bb.get(file_content, 0, file_content.length);
        		    String str_device_name = new String(device_name, "UTF-8").replace(" ", "");
        		    String str_file_name = new String(file_name, "UTF-8").replace(" ", "");
        		    //Save file...
        			OutputStream os = new FileOutputStream(new File(str_file_name));
        			os.write(file_content);
        			os.close();
        			//Finish the job...
    				mutex.acquire();
    				device_socket = str_device_name;
    				filepath_socket = System.getProperty("user.dir") + "/" + str_file_name;
    		    	mutex.release();
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
    
    // File methods
	public Pair<String, String> receive_file_call() {
		Pair<String, String> pair = null;
		
		String device = "";
    	String filepath = "";
		try {
			mutex.acquire();
			device = device_socket;
			filepath = filepath_socket;
	    	mutex.release();
	    	if(!device.equals("")) {
				mutex.acquire();
				filepath_socket = "";
				device_socket = "";
		    	mutex.release();
		    	pair = new Pair<String, String>(device, filepath);
	    	}
		} catch (Exception e) {
			System.out.println(e);
		}
		return pair;
	}
	
	public void send_file_call(File file, String sender_device_name) {
		try {
			byte[] device_name = new byte[SocketConstants.DEVICENAME_BYTE_SIZE];
			byte[] file_name = new byte[SocketConstants.FILENAME_BYTE_SIZE];
			byte[] file_content = Files.readAllBytes(file.toPath());
			device_name = String.format("%1$"+SocketConstants.DEVICENAME_BYTE_SIZE+"s", sender_device_name).getBytes();
			file_name = String.format("%1$"+SocketConstants.FILENAME_BYTE_SIZE+"s", file.getName()).getBytes();
			output_stream.write(ByteBuffer.allocate(device_name.length + file_name.length + file_content.length).put(device_name).put(file_name).put(file_content).array());
			output_stream.flush();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}

