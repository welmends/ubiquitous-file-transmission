package application.ts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import net.jini.space.JavaSpace;

public class TupleSpace extends Thread {
	private Lookup lookup;
	private JavaSpace space;
	private JavaSpace space_admin;
	
    private Boolean is_connected;
    private Boolean chat_type;
    private String user_name;
    private String contact_name;
    private String room_name;
    
    private Semaphore mutex;
    
    // Constructor
	public TupleSpace() {
		this.is_connected = false;
		this.chat_type = null;
		this.user_name = "";
		this.contact_name = "";
		this.room_name = "";
		this.mutex = new Semaphore(1);
	}
	
	// Thread
	@Override
	public void run() {
		List<String> to_remove_rooms = new ArrayList<String>();
		while(true) {
			try {
				// Sleep
				Thread.sleep(TupleSpaceConstants.THREAD_SLEEP_TIME_MILLIS);
				// Update tuple_admin
				to_remove_rooms.clear();
	        	TupleAdmin template_admin = new TupleAdmin();
	        	TupleAdmin tuple_admin = (TupleAdmin) this.space_admin.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
	        	if(tuple_admin!=null) {
	        		TupleRoom template_room = new TupleRoom();
	        		for (int i=0; i<tuple_admin.rooms.size(); i++) {
	        			if(tuple_admin.rooms.get(i).equals(TupleSpaceConstants.ALL_ROOM_TEXT)) {
	        				continue;
	        			}
						template_room.room_name = tuple_admin.rooms.get(i);
						TupleRoom tuple_room = (TupleRoom) this.space_admin.read(template_room, null, TupleSpaceConstants.TIMER_NO_WAIT);
						if(tuple_room==null) {
							to_remove_rooms.add(tuple_admin.rooms.get(i));
						}
					}
	        	}
        		for (int i=0; i<to_remove_rooms.size(); i++) {
					tuple_admin.rooms.remove(to_remove_rooms.get(i));
				}
	        	this.space_admin.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
			} catch (Exception e) {
				System.out.println("Error: TupleSpace (thread)");
			}
		}
	}
	
	// Connection
    public Boolean connect(String user_name){
    	this.user_name = user_name;
    	this.lookup = new Lookup(JavaSpace.class);
		this.space = (JavaSpace) this.lookup.getService();
		this.space_admin = (JavaSpace) this.lookup.getService();
        if (space != null) {
        	this.is_connected = true;
        }else {
        	this.is_connected = false;
        }
		return this.is_connected;
    }
    
    public Boolean disconnect(){
    	this.is_connected = false;
    	try {
    		//Update tuple_admin
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin!=null) {
        		if(tuple_admin.contacts.contains(user_name)) {
        			tuple_admin.contacts.remove(user_name);
        			this.space.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}
        	}
    		//Update tuple_room
			do {
				if(deselect_room()) {
					break;
				}
			}while(true);
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (disconnect)");
		}
        return true;
    }
    
    // Admin Control
    public Boolean init_admin_tuple() {
        try {
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin==null) {
        		template_admin.rooms = new ArrayList<String>();
        		template_admin.rooms.add(TupleSpaceConstants.ALL_ROOM_TEXT);
        		template_admin.contacts = new ArrayList<String>();
        		template_admin.contacts.add(user_name);
        		this.space.write(template_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
    			TupleRoom tuple_room = new TupleRoom();
    			tuple_room.room_name = TupleSpaceConstants.ALL_ROOM_TEXT;
    			tuple_room.contacts = new ArrayList<String>();
    			this.space.write(tuple_room, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        	}else {
        		if(tuple_admin.contacts.contains(user_name)) {
        			return false;
        		}else {
        			tuple_admin.contacts.add(user_name);
        			this.space.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        			this.space.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (init_admin_tuple)");
		}
        return true;
    }
    
    public HashMap<String, String> get_hash_rooms_contacts() {
    	HashMap<String, String> hash = new HashMap<String, String>();
    	List<String> rooms = get_rooms_list();
    	List<String> contacts;
    	for (int i=0; i<rooms.size(); i++) {
    		if(rooms.get(i).equals(TupleSpaceConstants.ALL_ROOM_TEXT)) {
    			continue;
    		}
			contacts = get_contacts_list(rooms.get(i));
	    	for (int j=0; j<contacts.size(); j++) {
	    		hash.put(contacts.get(j), rooms.get(i));
			}
		}
    	return hash;
    }
    
    public List<String> get_rooms_list() {
    	List<String> rooms = new ArrayList<String>();
        try {
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin!=null) {
        		rooms = tuple_admin.rooms;
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (get_rooms_list)");
		}
        return rooms;
    }
    
    public List<String> get_contacts_list() {
    	List<String> contacts = new ArrayList<String>();
        try {
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin!=null) {
        		contacts = tuple_admin.contacts;
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (get_contacts_list)");
		}
        return contacts;
    }
    
    public List<String> get_contacts_list(String room_name) {
    	List<String> contacts = new ArrayList<String>();
        try {
        	TupleRoom template_room = new TupleRoom();
        	template_room.room_name = room_name;
        	TupleRoom tuple_room = (TupleRoom) this.space.read(template_room, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
        	if(tuple_room!=null) {
        		contacts = tuple_room.contacts;
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (get_contacts_list)");
		}
        return contacts;
    }
    
    public Boolean add_room(String room_name) {
        try {
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin!=null) {
        		if(!tuple_admin.rooms.contains(room_name)) {
        			//Update tuple_admin
        			tuple_admin.rooms.add(room_name);
        			this.space.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        			this.space.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        			//Create tuple_room
        			TupleRoom tuple_room = new TupleRoom();
        			tuple_room.room_name = room_name;
        			tuple_room.contacts = new ArrayList<String>();
        			this.space.write(tuple_room, null, TupleSpaceConstants.TIMER_KEEP_ROOM);
        		}
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (add_room)");
			return false;
		}
        return true;
    }
    
    public Boolean select_room(String room_name) {
    	try {
        	TupleRoom template_room = new TupleRoom();
        	TupleRoom tuple_room = new TupleRoom();
        	if(!this.room_name.equals("")) {
        		template_room.room_name = get_room_name();
        		tuple_room = (TupleRoom) this.space.take(template_room, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
        		if(tuple_room!=null) {
        			tuple_room.contacts.remove(this.user_name);
            		if(tuple_room.contacts.size()==0) {
            			this.space.write(tuple_room, null, TupleSpaceConstants.TIMER_KEEP_ROOM);
            		}else {
            			this.space.write(tuple_room, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
            		}
        		}
        	}
        	template_room.room_name = room_name;
    		tuple_room = (TupleRoom) this.space.take(template_room, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
    		if(tuple_room!=null) {
    			tuple_room.contacts.add(this.user_name);
        		if(tuple_room.contacts.size()==0) {
        			this.space.write(tuple_room, null, TupleSpaceConstants.TIMER_KEEP_ROOM);
        		}else {
        			this.space.write(tuple_room, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}
    		}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (select_room)");
			return false;
		}
    	return true;
    }
    
    public Boolean deselect_room() {
        try {
        	// Common Room
        	TupleRoom template_room = new TupleRoom();
        	template_room.room_name = get_room_name();
        	TupleRoom tuple_room = (TupleRoom) this.space.take(template_room, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
        	if(tuple_room!=null) {
        		if(tuple_room.contacts.contains(get_user_name())) {
        			tuple_room.contacts.remove(get_user_name());
            		if(tuple_room.contacts.size()==0) {
            			this.space.write(tuple_room, null, TupleSpaceConstants.TIMER_KEEP_ROOM);
            		}else {
            			this.space.write(tuple_room, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
            		}
        		}
        	}
        	// All Room
        	template_room = new TupleRoom();
        	template_room.room_name = TupleSpaceConstants.ALL_ROOM_TEXT;
        	tuple_room = (TupleRoom) this.space.take(template_room, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
        	if(tuple_room!=null) {
        		if(tuple_room.contacts.contains(get_user_name())) {
        			tuple_room.contacts.remove(get_user_name());
        			this.space.write(tuple_room, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (deselect_room)");
			return false;
		}
        return true;
    }
    
    // Message Control
    public void send_message(String content) {
        try {
        	TupleMessage tuple_message  = new TupleMessage();
        	tuple_message.chat_type     = get_chat_type();
        	tuple_message.content       = content;
        	tuple_message.sender_name   = get_user_name();
        	if(get_chat_type().equals(TupleSpaceConstants.CONTACT_CHAT)) {
        		//CONTACT_CHAT
            	tuple_message.receiver_name = get_contact_name();
            	
        	}else {
        		//ROOM_CHAT
            	tuple_message.room_name     = get_room_name();
            	tuple_message.receivers     = new ArrayList<String>();
        	}
			this.space.write(tuple_message, null, TupleSpaceConstants.TIMER_KEEP_MESSAGE);
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (send_message)");
		}
    }
    
    public TupleMessage receive_message() {
        try {
        	if(get_chat_type()==null) { return null; }
        	TupleMessage template_message  = new TupleMessage();
        	template_message.chat_type     = get_chat_type();
        	if(get_chat_type().equals(TupleSpaceConstants.CONTACT_CHAT)) {
        		//CONTACT_CHAT
        		template_message.sender_name   = get_contact_name();
        		template_message.receiver_name = get_user_name();
        		TupleMessage tuple_message = (TupleMessage) this.space.take(template_message, null, TupleSpaceConstants.TIMER_TAKE_MESSAGE);
            	if(tuple_message!=null) {
            		return tuple_message;
            	}else {
            		return null;
            	}
        	}else {
        		//ROOM_CHAT
        		template_message.room_name = get_room_name();
        		TupleMessage tuple_message = (TupleMessage) this.space.take(template_message, null, TupleSpaceConstants.TIMER_TAKE_MESSAGE);
            	if(tuple_message!=null) {
            		List<String> contacts = get_contacts_list(get_room_name());
            		if(tuple_message.sender_name.equals(get_user_name())) {
                		if(contacts.size()>1) {
                			this.space.write(tuple_message, null, TupleSpaceConstants.TIMER_KEEP_MESSAGE);
                		}
            			return null;
            		}
            		else if(tuple_message.receivers.contains(get_user_name())) {
            			this.space.write(tuple_message, null, TupleSpaceConstants.TIMER_KEEP_MESSAGE);
            			return null;
            		}else {
            			tuple_message.receivers.add(get_user_name());
                		if(tuple_message.receivers.size()<contacts.size()-1) {
                			this.space.write(tuple_message, null, TupleSpaceConstants.TIMER_KEEP_MESSAGE);
                		}
                		return tuple_message;
            		}
            	}else {
            		return null;
            	}
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (receive_message)");
			return null;
		}
    }
    
    // Getters
    public Boolean has_connection() {
    	return this.is_connected;
    }
    
    public String get_user_name() {
    	return this.user_name;
    }
    
    public Boolean get_chat_type() {
    	try { mutex.acquire(); } catch (Exception e) {}
    	Boolean type = this.chat_type;
    	try { mutex.release(); } catch (Exception e) {}
    	return type;
    }
    
    public String get_contact_name() {
    	try { mutex.acquire(); } catch (Exception e) {}
    	String nick = this.contact_name;
    	try { mutex.release(); } catch (Exception e) {}
    	return nick;
    }
    
    public String get_room_name() {
    	try { mutex.acquire(); } catch (Exception e) {}
    	String room = this.room_name;
    	try { mutex.release(); } catch (Exception e) {}
    	return room;
    }
    
    // Setters
    public void set_chat_type(Boolean type) {
    	try { mutex.acquire(); } catch (Exception e) {}
    	this.chat_type = type;
    	try { mutex.release(); } catch (Exception e) {}
    }
    
    public void set_contact_name(String contact_name) {
    	try { mutex.acquire(); } catch (Exception e) {}
    	this.contact_name = contact_name;
    	try { mutex.release(); } catch (Exception e) {}
    }
    
    public void set_room_name(String room_name) {
    	try { mutex.acquire(); } catch (Exception e) {}
    	this.room_name = room_name;
    	try { mutex.release(); } catch (Exception e) {}
    }
}
