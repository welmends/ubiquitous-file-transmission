package application.ts;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Environment implements Serializable {
	public String name;
	public Integer x_axis;
	public Integer y_axis;
	
	public Environment() {}
	
	public Environment(String name, Integer x_axis, Integer y_axis) {
		this.name = name;
		this.x_axis = x_axis;
		this.y_axis = y_axis;
	}
}
