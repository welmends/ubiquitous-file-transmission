package application.ui.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import application.ts.Device;
import application.ts.Environment;
import application.ts.TupleSpaceConstants;
import application.ui.ConfigController;
import application.ui.constants.ConfigConstants;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ConfigComponentsArrayUtils {
	
	private ConfigController config;
	private VBox vboxOnScroll;
	private HashMap<Device, Environment> hash;
	private List<TitledPane> envs_components;
	private List<Button> devices_components;
	private TitledPane env_all;
	private List<Button> devices_components_on_all;
	
	public ConfigComponentsArrayUtils(ConfigController config, VBox vboxOnScroll){
		this.config = config;
		this.vboxOnScroll = vboxOnScroll;
		this.hash = new HashMap<Device, Environment>();
		this.envs_components = new ArrayList<TitledPane>();
		this.devices_components = new ArrayList<Button>();
		this.env_all = null;
		this.devices_components_on_all = new ArrayList<Button>();
	}
	
	public void init_all_env(String ts_device_name, Integer x_axis, Integer y_axis) {
		TitledPane tp = new TitledPane();
		tp.setText(TupleSpaceConstants.ALL_ROOM_TEXT);
		tp.setStyle(ConfigConstants.TITLED_PANE_STYLE);
		tp.setContentDisplay(ConfigConstants.ROOM_BUTTON_CONTENT_DISPLAY);
		tp.setContent(new VBox());
		
		env_all = tp;
		
        vboxOnScroll.getChildren().add(tp);
        
        add_device_button_on_all(ts_device_name, new Device(ts_device_name, x_axis, y_axis, "", -1));
	}
	
	@SuppressWarnings("unchecked")
	public void updateComponentsList(String ts_device_name, List<Environment> ts_envs, List<Device> ts_devices, HashMap<Device, Environment> ts_hash) {
		Boolean add_del;
		
		// All Room
		for (int i=0; i<ts_devices.size(); i++) {
			add_del = true;
			for (int j=0; j<devices_components_on_all.size(); j++) {
				if(ts_devices.get(i).name.equals(devices_components_on_all.get(j).getText())) {
					add_del = false;
					break;
				}
			}
			if(add_del) {
				add_device_button_on_all(ts_device_name, ts_devices.get(i));
			}
		}
		
		for (int i=0; i<devices_components_on_all.size(); i++) {
			add_del = true;
			for (int j=0; j<ts_devices.size(); j++) {
				if(devices_components_on_all.get(i).getText().equals(ts_devices.get(j).name)) {
					add_del = false;
					break;
				}
			}
			if(add_del) {
				del_device_button_on_all(devices_components_on_all.get(i).getText());
			}
		}
		
		// Common Rooms
		for (int i=0; i<ts_envs.size(); i++) {
			if(ts_envs.get(i).name.equals(TupleSpaceConstants.ALL_ROOM_TEXT)) {
				continue;
			}
			add_del = true;
			for (int j=0; j<envs_components.size(); j++) {
				if(ts_envs.get(i).name.equals(envs_components.get(j).getText())) {
					add_del = false;
					break;
				}
			}
			if(add_del) {
				add_env_titledPane(ts_envs.get(i));
			}
		}
		
		for (int i=0; i<envs_components.size(); i++) {
			add_del = true;
			for (int j=0; j<ts_envs.size(); j++) {
				if(envs_components.get(i).getText().equals(ts_envs.get(j).name)) {
					add_del = false;
					break;
				}
			}
			if(add_del) {
				del_env_titledpane(envs_components.get(i).getText());
			}
		}
		
		// Common Contacts
		ts_hash.forEach((key, value) -> {
			if(hash.containsKey(key)) {
				if(!hash.get(key).equals(value)) {
					del_device_button(hash.get(key).name, key.name);
					add_device_button(ts_device_name, value, key);
				}
			}else {
				add_device_button(ts_device_name, value, key);
			}
		});
		
		((HashMap<Device, Environment>) hash.clone()).forEach((key, value) -> {
			if(ts_hash.containsKey(key)) {
				if(!ts_hash.get(key).equals(value)) {
					del_device_button(value.name, key.name);
					add_device_button(ts_device_name, ts_hash.get(key), key);
				}
			}else {
				del_device_button(value.name, key.name);
			}
		});
		
        vboxOnScroll.applyCss();
        vboxOnScroll.layout();
	}
	
	private void add_env_titledPane(Environment env) {
		HBox h = new HBox();
		
		Button b_enter = new Button();
		b_enter.setText(ConfigConstants.ENTER_ROOM_BUTTON_TEXT);
		b_enter.setStyle(ConfigConstants.ROOM_BUTTON_STYLE);
		
		Button b_leave = new Button();
		b_leave.setText(ConfigConstants.LEAVE_ROOM_BUTTON_TEXT);
		b_leave.setStyle(ConfigConstants.ROOM_BUTTON_STYLE);

		h.getChildren().addAll(b_enter, b_leave);
		
		TitledPane tp = new TitledPane();
		tp.setText(env.name + " ("+String.valueOf(env.x_axis)+","+String.valueOf(env.y_axis)+")");
		tp.setStyle(ConfigConstants.TITLED_PANE_STYLE);
		tp.setContentDisplay(ConfigConstants.ROOM_BUTTON_CONTENT_DISPLAY);
		tp.setGraphic(h);
		tp.setContent(new VBox());
		
		h.translateXProperty().bind(Bindings.createDoubleBinding(() -> 
			tp.getWidth() - h.getLayoutX() - h.getWidth() - ConfigConstants.ROOM_BUTTON_GRAPHIC_MARGIN_RIGHT, tp.widthProperty())
		);
		
		config.setEnvBtnPressedBehavior(tp, b_enter, b_leave);
		
		envs_components.add(tp);
		
        vboxOnScroll.getChildren().add(tp);
	}
	
	private void add_device_button(String ts_device_name, Environment env, Device device) {
		Button b = new Button();
		b.setText(device.name + " ("+String.valueOf(device.x_axis)+","+String.valueOf(device.y_axis)+")");
		b.setStyle(ConfigConstants.CONTACT_BUTTON_STYLE);
		b.setPrefWidth(ConfigConstants.CONTACT_BUTTON_PREF_WIDTH);
		config.setDeviceBtnPressedBehavior(b);
		if(device.name.equals(ts_device_name)) {
			b.setDisable(true);
		}
		
		devices_components.add(b);
		
		for (int i=0; i<envs_components.size(); i++) {
			if(envs_components.get(i).getText().equals(env.name)) {
				VBox content = (VBox) envs_components.get(i).getContent();
				content.getChildren().add(b);
			}
		}
		
		hash.put(device, env);
	}
	
	private void add_device_button_on_all(String ts_device_name, Device device) {
		Button b = new Button();
		b.setText(device.name + " ("+String.valueOf(device.x_axis)+","+String.valueOf(device.y_axis)+")");
		b.setStyle(ConfigConstants.CONTACT_BUTTON_STYLE);
		b.setPrefWidth(ConfigConstants.CONTACT_BUTTON_PREF_WIDTH);
		config.setDeviceBtnPressedBehavior(b);
		if(device.name.equals(ts_device_name)) {
			b.setDisable(true);
		}
		
		devices_components_on_all.add(b);
		
		VBox content = (VBox) env_all.getContent();
		content.getChildren().add(b);
	}
	
	private void del_env_titledpane(String env_name) {
		for (int i=0; i<envs_components.size(); i++) {
			if(envs_components.get(i).getText().equals(env_name)) {
				vboxOnScroll.getChildren().remove(envs_components.get(i));
				envs_components.remove(i);
				break;
			}
		}
	}
	
	private void del_device_button(String env_name, String device_name) {
		for (int i=0; i<envs_components.size(); i++) {
			if(envs_components.get(i).getText().equals(env_name)) {
				for (int j=0; j<devices_components.size(); j++) {
					if(devices_components.get(j).getText().equals(device_name)) {
						VBox content = (VBox) envs_components.get(i).getContent();
						content.getChildren().remove(devices_components.get(j));
						devices_components.remove(j);
		        		break;
					}
				}
				break;
			}
		}
		
		hash.remove(device_name, env_name);
	}
	
	private void del_device_button_on_all(String device_name) {
		for (int i=0; i<devices_components_on_all.size(); i++) {
			if(devices_components_on_all.get(i).getText().equals(device_name)) {
				VBox content = (VBox) env_all.getContent();
				content.getChildren().remove(devices_components_on_all.get(i));
				devices_components_on_all.remove(i);
        		break;
			}
		}
	}
	
}
