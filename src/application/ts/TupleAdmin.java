package application.ts;

import java.util.List;

import net.jini.core.entry.Entry;

@SuppressWarnings("serial")
public class TupleAdmin implements Entry {
	public List<Environment> environments;
	public List<Device> devices;
	
    public TupleAdmin() {}
    
    public Integer environmentIndex(String environment_name) {
    	for(int i=0; i<environments.size(); i++) {
    		if(environments.get(i).name.equals(environment_name)) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    public Integer deviceIndex(String device_name) {
    	for(int i=0; i<devices.size(); i++) {
    		if(devices.get(i).name.equals(device_name)) {
    			return i;
    		}
    	}
    	return -1;
    }
}
