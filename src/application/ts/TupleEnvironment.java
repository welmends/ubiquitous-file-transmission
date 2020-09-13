package application.ts;

import java.util.List;

import net.jini.core.entry.Entry;

@SuppressWarnings("serial")
public class TupleEnvironment implements Entry {
	public String environment_name;
	public List<String> devices;
    
    public TupleEnvironment() {
    	
    }
}
