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
	        			if(tuple_admin.environments.get(i).name.equals(TupleSpaceConstants.ALL_ROOM_TEXT)) {
	        				continue;
	        			}
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
    public Boolean connect(String device_name, String ip_address, Integer port_number, Integer x_axis, Integer y_axis){
    	this.device_name = device_name;
    	this.ip_address = ip_address;
    	this.port_number = port_number;
    	//Define environment at initial procedure
    	this.x_axis = x_axis;
    	this.y_axis = y_axis;
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
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin!=null) {
        		int device_index = tuple_admin.deviceIndex(get_device_name());
        		if(device_index!=-1) {
        			tuple_admin.devices.remove(device_index);
        			this.space.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}
        	}
    		//Update tuple_environment
			do {
				if(deselect_environment()) {
					break;
				}
			}while(true);
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (disconnect)");
		}
        return true;
    }
    
    // Admin Control
    public Boolean init_admin_tuple() {
        try {
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin==null) {
        		template_admin.environments = new ArrayList<Environment>();
        		template_admin.environments.add(new Environment(TupleSpaceConstants.ALL_ROOM_TEXT, 0, 0));
        		template_admin.devices = new ArrayList<Device>();
        		template_admin.devices.add(new Device(get_device_name(), get_x_axis(), get_y_axis(), get_ip_address(), get_port_number()));
        		this.space.write(template_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
    			TupleEnvironment tuple_env = new TupleEnvironment();
    			tuple_env.env_name = TupleSpaceConstants.ALL_ROOM_TEXT;
    			tuple_env.env = new Environment(TupleSpaceConstants.ALL_ROOM_TEXT, 0, 0);
    			tuple_env.devices = new ArrayList<Device>();
    			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        	}else {
        		if(tuple_admin.deviceIndex(get_device_name())!=-1) {
        			return false;
        		}else {
        			tuple_admin.devices.add(new Device(get_device_name(), get_x_axis(), get_y_axis(), get_ip_address(), get_port_number()));
        			this.space.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        			this.space.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (init_admin_tuple)");
			System.out.println(e);
		}
        return true;
    }
    
    public HashMap<String, String> get_hash_devices_environments() {
    	HashMap<String, String> hash = new HashMap<String, String>();
    	List<Environment> envs = get_environments_list();
    	List<Device> devices;
    	for (int i=0; i<envs.size(); i++) {
    		if(envs.get(i).name.equals(TupleSpaceConstants.ALL_ROOM_TEXT)) {
    			continue;
    		}
    		devices = get_devices_list(envs.get(i).name);
	    	for (int j=0; j<devices.size(); j++) {
	    		hash.put(devices.get(j).name, envs.get(i).name);
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
        	TupleEnvironment tuple_env = (TupleEnvironment) this.space.read(template_env, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
        	if(tuple_env!=null) {
        		devices = tuple_env.devices;
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (get_devices_list)");
		}
        return devices;
    }
    
    public Boolean add_environment(String environment_name) {
        try {
        	TupleAdmin template_admin = new TupleAdmin();
        	TupleAdmin tuple_admin = (TupleAdmin) this.space.read(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        	if(tuple_admin!=null) {
        		int envIndex = tuple_admin.environmentIndex(environment_name);
        		if(envIndex!=-1) {
        			//Update tuple_admin
        			tuple_admin.environments.add(new Environment(environment_name, 0, 0));
        			this.space.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        			this.space.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        			//Create tuple_room
        			TupleEnvironment tuple_env = new TupleEnvironment();
        			tuple_env.env_name = environment_name;
        			tuple_env.devices = new ArrayList<Device>();
        			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_ROOM);
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
        		tuple_env = (TupleEnvironment) this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
        		if(tuple_env!=null) {
            		int device_index = tuple_env.deviceIndex(get_device_name());
            		if(device_index!=-1) {
            			tuple_env.devices.remove(device_index);
                		if(tuple_env.devices.size()==0) {
                			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_ROOM);
                		}else {
                			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
                		}
            		}
        		}
        	}
        	template_env.env_name = get_environment_name();
        	tuple_env = (TupleEnvironment) this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
    		if(tuple_env!=null) {
    			tuple_env.devices.add(new Device(get_device_name(), get_x_axis(), get_y_axis(), get_ip_address(), get_port_number()));
        		if(tuple_env.devices.size()==0) {
        			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_ROOM);
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
    
    public Boolean deselect_environment() {
        try {
        	// Common Environment
        	TupleEnvironment template_env = new TupleEnvironment();
        	template_env.env_name = get_environment_name();
        	TupleEnvironment tuple_env = (TupleEnvironment) this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
        	if(tuple_env!=null) {
        		int device_index = tuple_env.deviceIndex(get_device_name());
        		if(device_index!=-1) {
        			tuple_env.devices.remove(device_index);
            		if(tuple_env.devices.size()==0) {
            			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_ROOM);
            		}else {
            			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
            		}
        		}
        	}
        	// All Room
        	template_env = new TupleEnvironment();
        	template_env.env_name = TupleSpaceConstants.ALL_ROOM_TEXT;
        	tuple_env = (TupleEnvironment) this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
        	if(tuple_env!=null) {
        		int device_index = tuple_env.deviceIndex(get_device_name());
        		if(device_index!=-1) {
        			tuple_env.devices.remove(device_index);
        			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (deselect_environment)");
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
}
