package application.socket;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.concurrent.Semaphore;

import java.io.*;

public class SocketP2P implements Runnable {
	
	private Semaphore mutex;
	
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    
    private DataInputStream input_stream  = null;
    private DataOutputStream output_stream = null;
    
    private String message_input = "";
    private String message_output = "";
    
    private int thread_action;
    
    private Boolean is_connected;
    private String peer_type;
    private String ip;
    private int port;
    
    public SocketP2P() {
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
        		    byte[] resultBuff = new byte[0];
        		    byte[] buff = new byte[1024];
        		    int k = -1;
        		    while((k = input_stream.read(buff, 0, buff.length)) > -1) {
        		        byte[] tbuff = new byte[resultBuff.length + k]; // temp buffer size = bytes already read + bytes last read
        		        System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length); // copy previous bytes
        		        System.arraycopy(buff, 0, tbuff, resultBuff.length, k);  // copy current lot
        		        resultBuff = tbuff; // call the temp buffer as your result buff
        		    }
        		    byte[] file_name = new byte[P2PConstants.FILENAME_BYTE_SIZE];
        		    byte[] file_content = new byte[resultBuff.length-P2PConstants.FILENAME_BYTE_SIZE];
        		    ByteBuffer bb = ByteBuffer.wrap(resultBuff);
        		    bb.get(file_name, 0, file_name.length);
        		    bb.get(file_content, 0, file_content.length);
        			OutputStream os = new FileOutputStream(new File("/Users/well/Downloads/"+new String(file_name, "UTF-8").replace(" ", ""))); 
        			os.write(file_content);
        			os.close();
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
    public Boolean file_stack_full() {
    	String message_received = "";
		try {
			mutex.acquire();
			message_received = message_input;
	    	mutex.release();
		} catch (Exception e) {
			System.out.println(e);
		}

    	if(message_received.length()>0) {
    		if(P2PConstants.CHAT_CODEC.equals(message_received.substring(0,P2PConstants.CHAT_CODEC.length()))) {
    			return true;
    		}else {
    			return false;
    		}
    	}else {
    		return false;
    	}
    }

	public void receive_file_call(File file) {
    	String message_received = "";
		try {
			mutex.acquire();
			message_received = message_input;
			message_input = "";
	    	mutex.release();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void send_file_call(File file) {
		try {
			byte[] file_name = new byte[P2PConstants.FILENAME_BYTE_SIZE];
			file_name = String.format("%1$"+P2PConstants.FILENAME_BYTE_SIZE+"s", file.getName()).getBytes();
			byte[] file_content = Files.readAllBytes(file.toPath());
			output_stream.write(ByteBuffer.allocate(file_name.length + file_content.length).put(file_name).put(file_content).array());
			output_stream.flush();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
    
	// Chat methods
    public Boolean chat_stack_full() {
    	String message_received = "";
		try {
			mutex.acquire();
			message_received = message_input;
	    	mutex.release();
		} catch (Exception e) {
			System.out.println(e);
		}

    	if(message_received.length()>0) {
    		if(P2PConstants.CHAT_CODEC.equals(message_received.substring(0,P2PConstants.CHAT_CODEC.length()))) {
    			return true;
    		}else {
    			return false;
    		}
    	}else {
    		return false;
    	}
    }

	public String get_chat_msg() {
    	String message_received = "";
		try {
			mutex.acquire();
			message_received = message_input;
			message_input = "";
	    	mutex.release();
		} catch (Exception e) {
			System.out.println(e);
		}
    	return message_received.substring(P2PConstants.CHAT_CODEC.length(),message_received.length());
	}
	
	public void send_chat_msg_call(String msg) {
		try {
			message_output = P2PConstants.CHAT_CODEC + msg;
			output_stream.writeUTF(message_output);
			output_stream.flush();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}

