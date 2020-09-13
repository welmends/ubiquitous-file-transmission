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
    private String environment_name;
    private Semaphore mutex;
    
    // Constructor
	public TupleSpace() {
		this.is_connected = false;
		this.device_name = "";
		this.environment_name = "";
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
	        			if(tuple_admin.environments.get(i).equals(TupleSpaceConstants.ALL_ROOM_TEXT)) {
	        				continue;
	        			}
	        			template_env.environment_name = tuple_admin.environments.get(i);
						TupleEnvironment tuple_env = (TupleEnvironment) this.space_admin.read(template_env, null, TupleSpaceConstants.TIMER_NO_WAIT);
						if(tuple_env==null) {
							to_remove_envs.add(tuple_admin.environments.get(i));
						}
					}
	        	}
        		for (int i=0; i<to_remove_envs.size(); i++) {
					tuple_admin.environments.remove(to_remove_envs.get(i));
				}
	        	this.space_admin.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
			} catch (Exception e) {
				System.out.println("Error: TupleSpace (thread)");
			}
		}
	}
	
	// Connection
    public Boolean connect(String device_name){
    	this.device_name = device_name;
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
        		if(tuple_admin.devices.contains(get_device_name())) {
        			tuple_admin.devices.remove(get_device_name());
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
        		template_admin.environments = new ArrayList<String>();
        		template_admin.environments.add(TupleSpaceConstants.ALL_ROOM_TEXT);
        		template_admin.devices = new ArrayList<String>();
        		template_admin.devices.add(get_device_name());
        		this.space.write(template_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
    			TupleEnvironment tuple_env = new TupleEnvironment();
    			tuple_env.environment_name = TupleSpaceConstants.ALL_ROOM_TEXT;
    			tuple_env.devices = new ArrayList<String>();
    			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        	}else {
        		if(tuple_admin.devices.contains(get_device_name())) {
        			return false;
        		}else {
        			tuple_admin.devices.add(get_device_name());
        			this.space.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        			this.space.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        		}
        	}
		} catch (Exception e) {
			System.out.println("Error: TupleSpace (init_admin_tuple)");
		}
        return true;
    }
    
    public HashMap<String, String> get_hash_rooms_contacts() {
    	HashMap<String, String> hash = new HashMap<String, String>();
    	List<String> envs = get_environments_list();
    	List<String> devices;
    	for (int i=0; i<envs.size(); i++) {
    		if(envs.get(i).equals(TupleSpaceConstants.ALL_ROOM_TEXT)) {
    			continue;
    		}
    		devices = get_devices_list(envs.get(i));
	    	for (int j=0; j<devices.size(); j++) {
	    		hash.put(devices.get(j), envs.get(i));
			}
		}
    	return hash;
    }
    
    public List<String> get_environments_list() {
    	List<String> envs = new ArrayList<String>();
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
    
    public List<String> get_devices_list() {
    	List<String> devices = new ArrayList<String>();
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
    
    public List<String> get_devices_list(String environment_name) {
    	List<String> devices = new ArrayList<String>();
        try {
        	TupleEnvironment template_env = new TupleEnvironment();
        	template_env.environment_name = environment_name;
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
        		if(!tuple_admin.environments.contains(environment_name)) {
        			//Update tuple_admin
        			tuple_admin.environments.add(environment_name);
        			this.space.take(template_admin, null, TupleSpaceConstants.TIMER_TAKE_ADMIN);
        			this.space.write(tuple_admin, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
        			//Create tuple_room
        			TupleEnvironment tuple_env = new TupleEnvironment();
        			tuple_env.environment_name = environment_name;
        			tuple_env.devices = new ArrayList<String>();
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
        		template_env.environment_name = get_environment_name();
        		tuple_env = (TupleEnvironment) this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
        		if(tuple_env!=null) {
        			tuple_env.devices.remove(get_device_name());
            		if(tuple_env.devices.size()==0) {
            			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_ROOM);
            		}else {
            			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
            		}
        		}
        	}
        	template_env.environment_name = get_environment_name();
        	tuple_env = (TupleEnvironment) this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
    		if(tuple_env!=null) {
    			tuple_env.devices.add(get_device_name());
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
        	template_env.environment_name = get_environment_name();
        	TupleEnvironment tuple_env = (TupleEnvironment) this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
        	if(tuple_env!=null) {
        		if(tuple_env.devices.contains(get_device_name())) {
        			tuple_env.devices.remove(get_device_name());
            		if(tuple_env.devices.size()==0) {
            			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_ROOM);
            		}else {
            			this.space.write(tuple_env, null, TupleSpaceConstants.TIMER_KEEP_UNDEFINED);
            		}
        		}
        	}
        	// All Room
        	template_env = new TupleEnvironment();
        	template_env.environment_name = TupleSpaceConstants.ALL_ROOM_TEXT;
        	tuple_env = (TupleEnvironment) this.space.take(template_env, null, TupleSpaceConstants.TIMER_TAKE_ROOM);
        	if(tuple_env!=null) {
        		if(tuple_env.devices.contains(get_device_name())) {
        			tuple_env.devices.remove(get_device_name());
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
    
    public String get_environment_name() {
    	try { mutex.acquire(); } catch (Exception e) {}
    	String room = this.environment_name;
    	try { mutex.release(); } catch (Exception e) {}
    	return room;
    }
    
    // Setters
    public void set_environment_name(String environment_name) {
    	try { mutex.acquire(); } catch (Exception e) {}
    	this.environment_name = environment_name;
    	try { mutex.release(); } catch (Exception e) {}
    }
}
