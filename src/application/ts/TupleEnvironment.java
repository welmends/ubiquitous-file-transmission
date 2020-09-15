package application.ts;

import java.util.List;

import net.jini.core.entry.Entry;

@SuppressWarnings("serial")
public class TupleEnvironment implements Entry {
	public String env_name;
	public Environment env;
	public List<Device> devices;
    
    public TupleEnvironment() {}
    
    public Integer deviceIndex(String device_name) {
    	for(int i=0; i<devices.size(); i++) {
    		if(devices.get(i).name.equals(device_name)) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    public Integer deviceAmount(String device_name) {
    	int count = 0;
    	for(int i=0; i<devices.size(); i++) {
    		if(devices.get(i).name.equals(device_name)) {
    			count+=1;
    		}
    	}
    	return count;
    }
}
