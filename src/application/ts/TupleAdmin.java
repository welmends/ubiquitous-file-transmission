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
    
    public Integer deviceIndex(String device_name, String environment_name) {
    	int degree = 1;
    	if(environment_name.indexOf("_")!=environment_name.lastIndexOf("_")) {
    		degree = Integer.valueOf(environment_name.substring(environment_name.lastIndexOf("_")+1));
    	}
    	for(int i=0; i<devices.size(); i++) {
    		if(devices.get(i).name.equals(device_name)) {
    			if(degree==1) {
    				return i;
    			}else {
    				degree-=1;
    			}
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
