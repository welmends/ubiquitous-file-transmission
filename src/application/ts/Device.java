package application.ts;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Device implements Serializable {
	public String name;
	public Integer x_axis;
	public Integer y_axis;
	public String ip_address;
	public Integer port_number;
	
	public Device() {}
	
	public Device(String name, Integer x_axis, Integer y_axis, String ip_address, Integer port_number) {
		this.name = name;
		this.x_axis = x_axis;
		this.y_axis = y_axis;
		this.ip_address = ip_address;
		this.port_number = port_number;
	}
}
