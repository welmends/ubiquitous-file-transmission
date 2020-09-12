package application.ui.utils;

import java.util.ArrayList;
import java.util.List;

import application.ts.TupleSpaceConstants;

public class StorageMessages {
	public static String ROOM_PREFIX = "r:";
	public static String CONTACT_PREFIX = "c:";
	
	public static String SEND_PREFIX = "out";
	public static String RECEIVE_PREFIX = "in";
	
	public List<String> messages;
	public List<String> text_times;
	public List<String> sender_names;
	public List<String> directions;
	
	
	public StorageMessages() {
		messages = new ArrayList<String>();
		text_times = new ArrayList<String>();
		sender_names = new ArrayList<String>();
		directions = new ArrayList<String>();
		
	}
	
	public void push_back(String msg, String text_t, String sender, String dir) {
		messages.add(msg);
		text_times.add(text_t);
		sender_names.add(sender);
		directions.add(dir);
	}
	
	public static String generate_storkey(Boolean type, String room_name, String contact_name) {
		if(type.equals(TupleSpaceConstants.ROOM_CHAT)) {
			return StorageMessages.ROOM_PREFIX + room_name;
		}else {
			return StorageMessages.CONTACT_PREFIX + contact_name;
		}
	}
}
