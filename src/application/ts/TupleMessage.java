package application.ts;

import java.util.List;

import net.jini.core.entry.Entry;

@SuppressWarnings("serial")
public class TupleMessage implements Entry {
	public Boolean chat_type;
	public String content;
	public String sender_name;
	public String receiver_name;
	public String room_name;
    public List<String> receivers;
    
    public TupleMessage() {
    	
    }
}
