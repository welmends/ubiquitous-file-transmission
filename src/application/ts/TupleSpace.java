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
    private String device_name;
    private String ip_address;
    private Integer port_number;
    private String environment_name;
    private Integer x_axis;
    private Integer y_axis;
    private Semaphore mutex;
    
    // Constructor
	public TupleSpace() {
		this.is_connected = false;
		this.device_name = "";
		this.ip_address = "";
		this.port_number = -1;
		this.environment_name = "";
		this.x_axis = -1;
		this.y_axis = -1;
		this.mutex = new Semaphore(1);
	}
	
	// Thread
	@Override
	public void run() {
		List<String> to_remove_envs = new ArrayList<String>();
		while(true) {
			try {
				// Sleep
				Thread.sleep(TupleSpaceConstants.THREAD_SLEEP_TIME_MILLIS);
				// Update tuple_admin
				to_remove_envs.clear();
	        	TupleAdmin template_admin = new TupleAdmin();
	        	TupleAdmin tuple_admin = (TupleAdmin) this.space_admin.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
	        	if(tuple_admin!=null) {
	        		TupleEnvironment template_env = new TupleEnvironment();
	        		for (int i=0; i<tuple_admin.environments.size(); i++) {
	        			template_env.env_name = tuple_admin.environments.get(i).name;
						TupleEnvironment tuple_env = (TupleEnvironment) this.space_admin.read(template_env, null, TupleSpaceConstants.TIMER_NO_WAIT);
						if(tuple_env==null) {
							to_remove_envs.add(tuple_admin.environments.get(i).name);
						}
					}
	        	}
	        	int envIndex;
        		for (int i=0; i<to_remove_envs.size(); i++) {
        			envIndex = tuple_admin.environmentIndex(to_remove_envs.get(i));
        			if(envIndex!=-1) {
        				tuple_admin.environments.remove(envIndex);
        			}
				}
	        	this.space_admin.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
			} catch (Exception e) {
				System.out.println("Error: TupleSpace (thread)");
			}
		}
	}
	
	// Connection
    public Boolean connect(String device_name, String ip_address, Integer port_number, String axis){
    	this.device_name = device_name;
    	this.ip_address = ip_address;
    	this.port_number = port_number;
    	set_axis(axis);
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
    		int env_index, device_index;
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin!=null) {
        		env_index = tuple_admin.environmentIndex(get_environment_name());
        		device_index = tuple_admin.deviceIndex(get_device_name(), get_environment_name());
        		if(env_index!=1 && device_index!=-1) {
        			tuple_admin.environments.remove(env_index);
        			tuple_admin.devices.remove(device_index);
        			this.space.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}
        	}
    		//Update tuple_environment
        	TupleEnvironment template_env, tuple_env;
        	template_env = new TupleEnvironment();
        	template_env.env_name = get_environment_name();
        	this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ENV);
        	//Update tuples_environments
        	for(int i=0; i<tuple_admin.environments.size(); i++) {
        		if(get_environment_name().contains(tuple_admin.environments.get(i).name) || tuple_admin.environments.get(i).name.contains(get_environment_name())) {
        			continue;
        		}
    			template_env = new TupleEnvironment();
    			template_env.env_name = tuple_admin.environments.get(i).name;
    			tuple_env = (TupleEnvironment) this.space.read(template_env, null, TupleSpaceConstants.TIMER_TAKE_ENV);
    			if(tuple_env!=null) {
    				device_index = tuple_env.deviceIndex(get_device_name());
    				if(device_index!=-1) {
            			tuple_env.devices.remove(device_index);
            			this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ENV);
            			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
            		}
    			}else {
    				return false;
    			}
    		}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (disconnect)");
		}
        return true;
    }
    
    // Administrator Control
    public Boolean init_admin_tuple() {
        try {
        	String env_name;
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin==null) {
        		template_admin.environments = new ArrayList<Environment>();
        		template_admin.devices = new ArrayList<Device>();
        		template_admin.devices.add(new Device(get_device_name(), get_x_axis(), get_y_axis(), get_ip_address(), get_port_number()));
        		this.space.write(template_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		env_name = TupleSpaceConstants.PREFIX_ENV+get_device_name();
        		add_environment(env_name);
        		select_environment(env_name);
        		set_environment_name(env_name);
        	}else {
        		for(int i=0; i<tuple_admin.devices.size(); i++) {
        			if(tuple_admin.devices.get(i).port_number.equals(get_port_number())) {
        				return false;
        			}
        		}
        		int device_amount = tuple_admin.deviceAmount(get_device_name());
        		if(device_amount>0) {
        			env_name = TupleSpaceConstants.PREFIX_ENV+get_device_name()+"_"+String.valueOf(device_amount+1);
        		}else {
        			env_name = TupleSpaceConstants.PREFIX_ENV+get_device_name();
        		}
    			tuple_admin.devices.add(new Device(get_device_name(), get_x_axis(), get_y_axis(), get_ip_address(), get_port_number()));
    			this.space.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
    			this.space.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
    			tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        		add_environment(env_name);
        		select_environment(env_name);
        		set_environment_name(env_name);
        		update_environments();
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (init_admin_tuple)");
		}
        return true;
    }
    
    public HashMap<Device, Environment> get_hash_devices_environments() {
    	HashMap<Device, Environment> hash = new HashMap<Device, Environment>();
    	List<Environment> envs = get_environments_list();
    	List<Device> devices;
    	for (int i=0; i<envs.size(); i++) {
    		devices = get_devices_list(envs.get(i).name);
	    	for (int j=0; j<devices.size(); j++) {
	    		hash.put(devices.get(j), envs.get(i));
			}
		}
    	return hash;
    }
    
    public List<Environment> get_environments_list() {
    	List<Environment> envs = new ArrayList<Environment>();
        try {
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin!=null) {
        		envs = tuple_admin.environments;
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (get_environments_list)");
		}
        return envs;
    }
    
    public List<Device> get_devices_list() {
    	List<Device> devices = new ArrayList<Device>();
        try {
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin!=null) {
        		devices = tuple_admin.devices;
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (get_devices_list)");
		}
        return devices;
    }
    
    public List<Device> get_devices_list(String environment_name) {
    	List<Device> devices = new ArrayList<Device>();
        try {
        	TupleEnvironment template_env = new TupleEnvironment();
        	template_env.env_name = environment_name;
        	TupleEnvironment tuple_env = (TupleEnvironment) this.space.read(template_env, null, TupleSpaceConstants.TIMER_TAKE_ENV);
        	if(tuple_env!=null) {
        		devices = tuple_env.devices;
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (get_devices_list)");
		}
        return devices;
    }
    
    public Device get_device(String device_name, String environment_name) {
    	Device device = null;
        try {
        	int device_index;
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin!=null) {
        		device_index = tuple_admin.deviceIndex(device_name, environment_name);
        		if(device_index!=-1) {
        			device = tuple_admin.devices.get(device_index);
        		}
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (get_device)");
		}
        return device;
    }
    
    public Boolean update_device() {
    	try {
    		int env_index, device_index;
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	TupleEnvironment template_env, tuple_env;
        	if(tuple_admin!=null) {
        		env_index = tuple_admin.environmentIndex(get_environment_name());
        		device_index = tuple_admin.deviceIndex(get_device_name(), get_environment_name());
        		if(env_index==-1 || device_index==-1) {
        			return false;
        		}else {
					tuple_admin.environments.get(env_index).x_axis = get_x_axis();
					tuple_admin.environments.get(env_index).y_axis = get_y_axis();
					tuple_admin.devices.get(device_index).x_axis = get_x_axis();
					tuple_admin.devices.get(device_index).y_axis = get_y_axis();
        			this.space.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        			this.space.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}
        		for(int i=0; i<tuple_admin.environments.size(); i++) {
        			template_env = new TupleEnvironment();
        			template_env.env_name = tuple_admin.environments.get(i).name;
        			tuple_env = (TupleEnvironment) this.space.read(template_env, null, TupleSpaceConstants.TIMER_TAKE_ENV);
        			if(tuple_env!=null) {
        				device_index = tuple_env.deviceIndex(get_device_name());
        				if(device_index!=-1) {
                			if(tuple_env.env_name.equals(get_environment_name())) {
                				tuple_env.env.x_axis = get_x_axis();
                				tuple_env.env.y_axis = get_y_axis();
                			}
                			tuple_env.devices.get(device_index).x_axis = get_x_axis();
                			tuple_env.devices.get(device_index).y_axis = get_y_axis();
                			this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ENV);
                			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
                		}
        			}else {
        				return false;
        			}
        		}
    			if(update_environments()) {
    				return true;
    			}else {
    				return false;
    			}
        	}else {
        		return false;
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (update_device)");
		}
        return true;
    }
    
    public Boolean update_environments() {
    	try {
    		int device_index;
    		int x_axis_1, y_axis_1, x_axis_2, y_axis_2;
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	TupleEnvironment template_env, tuple_env;
        	if(tuple_admin!=null) {
    			for(int i=0; i<tuple_admin.environments.size(); i++) {
        			template_env = new TupleEnvironment();
        			template_env.env_name = tuple_admin.environments.get(i).name;
        			tuple_env = (TupleEnvironment) this.space.read(template_env, null, TupleSpaceConstants.TIMER_TAKE_ENV);
        			if(tuple_env!=null) {
        				//Get current environment axis
        	    		x_axis_1 = tuple_env.env.x_axis;
        	    		y_axis_1 = tuple_env.env.y_axis;
        				//Remove devices from environment
        	    		List<Device> env_devs_temp = new ArrayList<Device>(tuple_env.devices);
        				for(int j=0; j<env_devs_temp.size(); j++) {
            	    		x_axis_2 = env_devs_temp.get(j).x_axis;
            	    		y_axis_2 = env_devs_temp.get(j).y_axis;
            				if(euclidian_distance(x_axis_1, y_axis_1, x_axis_2, y_axis_2)>TupleSpaceConstants.MAX_ENV_DISTANCE) {
            					tuple_env.devices.remove(env_devs_temp.get(j));
            				}
        				}
        				//Add devices to environment
        				for(int j=0; j<tuple_admin.devices.size(); j++) {
        					device_index = tuple_env.deviceIndex(tuple_admin.devices.get(j).name);
	            			if(device_index==-1) {
	            	    		x_axis_2 = tuple_admin.devices.get(j).x_axis;
	            	    		y_axis_2 = tuple_admin.devices.get(j).y_axis;
	            				if(euclidian_distance(x_axis_1, y_axis_1, x_axis_2, y_axis_2)<=TupleSpaceConstants.MAX_ENV_DISTANCE) {
	            					tuple_env.devices.add(tuple_admin.devices.get(j));
	            				}
	            			}
        				}
        				//Update current environment
            			this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ENV);
            			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        			}else {
        				return false;
        			}
    			}
        	}else {
        		return false;
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (update_environments)");
		}
        return true;
    }
    
    public Boolean add_environment(String environment_name) {
        try {
        	int env_index;
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin!=null) {
        		env_index = tuple_admin.environmentIndex(environment_name);
        		if(env_index==-1) {
        			Environment env = new Environment(environment_name, get_x_axis(), get_y_axis());
        			//Update tuple_admin
        			tuple_admin.environments.add(env);
        			this.space.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        			this.space.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        			//Create tuple_env
        			TupleEnvironment tuple_env = new TupleEnvironment();
        			tuple_env.env_name = environment_name;
        			tuple_env.env = env;
        			tuple_env.devices = new ArrayList<Device>();
        			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (add_environment)");
			return false;
		}
        return true;
    }
    
    public Boolean select_environment(String environment_name) {
    	try {
        	TupleEnvironment template_env = new TupleEnvironment();
        	TupleEnvironment tuple_env = new TupleEnvironment();
        	if(!get_environment_name().equals("")) {
        		template_env.env_name = get_environment_name();
        		tuple_env = (TupleEnvironment) this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ENV);
        		if(tuple_env!=null) {
            		int device_index = tuple_env.deviceIndex(get_device_name());
            		if(device_index!=-1) {
            			tuple_env.devices.remove(device_index);
                		if(tuple_env.devices.size()==0) {
                			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
                		}else {
                			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
                		}
            		}
        		}
        	}
        	template_env.env_name = environment_name;
        	tuple_env = (TupleEnvironment) this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ENV);
    		if(tuple_env!=null) {
    			tuple_env.devices.add(new Device(get_device_name(), get_x_axis(), get_y_axis(), get_ip_address(), get_port_number()));
        		if(tuple_env.devices.size()==0) {
        			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}else {
        			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}
    		}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (select_environment)");
			return false;
		}
    	return true;
    }
    
    // Getters
    public Boolean has_connection() {
    	return this.is_connected;
    }

    public String get_device_name() {
    	try { mutex.acquire(); } catch (Exception e) {}
    	String device = this.device_name;
    	try { mutex.release(); } catch (Exception e) {}
    	return device;
    }
    
    public String get_ip_address() {
    	try { mutex.acquire(); } catch (Exception e) {}
    	String ip = this.ip_address;
    	try { mutex.release(); } catch (Exception e) {}
    	return ip;
    }
    
    public Integer get_port_number() {
    	try { mutex.acquire(); } catch (Exception e) {}
    	Integer port = this.port_number;
    	try { mutex.release(); } catch (Exception e) {}
    	return port;
    }
    
    public String get_environment_name() {
    	try { mutex.acquire(); } catch (Exception e) {}
    	String room = this.environment_name;
    	try { mutex.release(); } catch (Exception e) {}
    	return room;
    }
    
    public Integer get_x_axis() {
    	try { mutex.acquire(); } catch (Exception e) {}
    	Integer x = this.x_axis;
    	try { mutex.release(); } catch (Exception e) {}
    	return x;
    }
    
    public Integer get_y_axis() {
    	try { mutex.acquire(); } catch (Exception e) {}
    	Integer y = this.y_axis;
    	try { mutex.release(); } catch (Exception e) {}
    	return y;
    }
    
    // Setters
    public void set_environment_name(String environment_name) {
    	try { mutex.acquire(); } catch (Exception e) {}
    	this.environment_name = environment_name;
    	try { mutex.release(); } catch (Exception e) {}
    }
    
    public void set_x_axis(Integer x) {
    	try { mutex.acquire(); } catch (Exception e) {}
    	this.x_axis = x;
    	try { mutex.release(); } catch (Exception e) {}
    }
    
    public void set_y_axis(Integer y) {
    	try { mutex.acquire(); } catch (Exception e) {}
    	this.y_axis = y;
    	try { mutex.release(); } catch (Exception e) {}
    }
    
    public void set_axis(String axis) {
    	try { mutex.acquire(); } catch (Exception e) {}
		this.x_axis = Integer.valueOf(axis.substring(0, axis.indexOf(",")));
		this.y_axis = Integer.valueOf(axis.substring(axis.indexOf(",")+1));
    	try { mutex.release(); } catch (Exception e) {}
    }

    // Utils
    public Double euclidian_distance(Integer x_axis_1, Integer y_axis_1, Integer x_axis_2, Integer y_axis_2) {
    	return Math.sqrt(Math.pow(x_axis_1-x_axis_2,2)+Math.pow(y_axis_1-y_axis_2,2));
    }

}
