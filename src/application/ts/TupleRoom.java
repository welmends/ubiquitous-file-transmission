package application.ts;

import java.util.List;

import net.jini.core.entry.Entry;

@SuppressWarnings("serial")
public class TupleRoom implements Entry {
	public String room_name; 
	public List<String> contacts;
    
    public TupleRoom() {
    	
    }
}
