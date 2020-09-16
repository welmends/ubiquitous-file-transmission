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
    
    public Integer deviceIndex(Device env_device) {
    	for(int i=0; i<devices.size(); i++) {
    		if(devices.get(i).name.equals(env_device.name)
    				&& devices.get(i).x_axis.equals(env_device.x_axis)
    				&& devices.get(i).y_axis.equals(env_device.y_axis)
    				&& devices.get(i).ip_address.equals(env_device.ip_address) 
    				&& devices.get(i).port_number.equals(env_device.port_number)) {
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
