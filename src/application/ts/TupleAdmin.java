package application.ts;

import java.util.List;

import net.jini.core.entry.Entry;

@SuppressWarnings("serial")
public class TupleAdmin implements Entry {
	public List<String> rooms;
	public List<String> contacts;
	
    public TupleAdmin() {
    	
    }
}
