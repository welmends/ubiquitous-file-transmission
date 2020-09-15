package application.ui.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.ts.Device;
import application.ts.Environment;
import application.ui.ConfigController;
import application.ui.constants.ConfigConstants;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class ConfigComponentsArrayUtils {
	
	private ConfigController config;
	private VBox vboxOnScroll;
	private HashMap<Device, Environment> hash;
	private List<TitledPane> envs_components;
	private List<Button> devices_components;
	
	public ConfigComponentsArrayUtils(ConfigController config, VBox vboxOnScroll){
		this.config = config;
		this.vboxOnScroll = vboxOnScroll;
		this.hash = new HashMap<Device, Environment>();
		this.envs_components = new ArrayList<TitledPane>();
		this.devices_components = new ArrayList<Button>();
	}
	
	@SuppressWarnings("unchecked")
	public void updateComponentsList(String ts_device_name, String ts_env_name, List<Environment> ts_envs, List<Device> ts_devices, HashMap<Device, Environment> ts_hash) {
		Boolean add_del;
		
		// Environments
		for (int i=0; i<ts_envs.size(); i++) {
			add_del = true;
			for (int j=0; j<envs_components.size(); j++) {
				if(ts_envs.get(i).name.equals(envs_components.get(j).getText())) {
					add_del = false;
					break;
				}
			}
			if(add_del) {
				add_env_titledPane(ts_env_name, ts_envs.get(i));
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
		
		// Devices
		Boolean alter;
		for (Map.Entry<Device, Environment> remote : ts_hash.entrySet()) {
			alter = true;
			for (Map.Entry<Device, Environment> local : ((HashMap<Device, Environment>) hash.clone()).entrySet()) {
				if(remote.getKey().name.equals(local.getKey().name) && remote.getValue().name.equals(local.getValue().name)) {
					alter = false;
					if(!remote.getKey().x_axis.equals(local.getKey().x_axis) || !remote.getKey().y_axis.equals(local.getKey().y_axis)) {
						del_device_button(local.getValue(), local.getKey());
						add_device_button(ts_device_name, remote.getValue(), remote.getKey());
					}
					break;
				}
			}
			if(alter) {
				add_device_button(ts_device_name, remote.getValue(), remote.getKey());
			}
		}
		
		for (Map.Entry<Device, Environment> local : ((HashMap<Device, Environment>) hash.clone()).entrySet()) {
			alter = true;
			for (Map.Entry<Device, Environment> remote : ts_hash.entrySet()) {
				if(local.getKey().name.equals(remote.getKey().name) && local.getValue().name.equals(remote.getValue().name)) {
					alter = false;
					if(!remote.getKey().x_axis.equals(local.getKey().x_axis) || !remote.getKey().y_axis.equals(local.getKey().y_axis)) {
						del_device_button(local.getValue(), local.getKey());
						add_device_button(ts_device_name, remote.getValue(), remote.getKey());
					}
					break;
				}
			}
			if(alter) {
				del_device_button(local.getValue(), local.getKey());
			}
		}
		
        vboxOnScroll.applyCss();
        vboxOnScroll.layout();
	}
	
	private void add_env_titledPane(String ts_env_name, Environment env) {
		TitledPane tp = new TitledPane();
		tp.setText(env.name);
		tp.setStyle(ConfigConstants.TITLED_PANE_STYLE);
		tp.setContentDisplay(ConfigConstants.DEFAULT_CONTENT_DISPLAY);
		tp.setContent(new VBox());
		if(!tp.getText().equals(ts_env_name)) {
			tp.setDisable(true);    
			tp.setExpanded(false); //***>>> Set true to see environments content
			//return;              //***>>> Uncomment to not show other environments 
		}
		
		envs_components.add(tp);
		
        vboxOnScroll.getChildren().add(tp);
	}
	
	private void add_device_button(String ts_device_name, Environment env, Device device) {
		Label l_axis = new Label();
		l_axis.setText("("+String.valueOf(device.x_axis)+","+String.valueOf(device.y_axis)+")");
		l_axis.setStyle(ConfigConstants.DEVICE_STYLE);
		
		Button b = new Button();
		b.setText(device.name);
		b.setStyle(ConfigConstants.DEVICE_STYLE);
		b.setPrefWidth(ConfigConstants.CONTACT_BUTTON_PREF_WIDTH);
		b.setContentDisplay(ConfigConstants.DEFAULT_CONTENT_DISPLAY);
		b.setGraphic(l_axis);
		if(device.name.equals(ts_device_name)) {
			b.setStyle(ConfigConstants.DEVICE_STYLE+" -fx-background-color: darkgray; -fx-border-color: gray;");
			config.setDeviceBtnPressedBehavior(env.name, device.name, b, true);
		}else {
			config.setDeviceBtnPressedBehavior(env.name, device.name, b, false);
		}
		
		devices_components.add(b);
		
		for (int i=0; i<envs_components.size(); i++) {
			if(envs_components.get(i).getText().equals(env.name)) {
				VBox content = (VBox) envs_components.get(i).getContent();
				content.getChildren().add(b);
				break;
			}
		}
		
		hash.put(device, env);
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
	
	private void del_device_button(Environment env, Device device) {
		for (int i=0; i<envs_components.size(); i++) {
			if(envs_components.get(i).getText().equals(env.name)) {
				for (int j=0; j<devices_components.size(); j++) {
					if(devices_components.get(j).getText().equals(device.name)) {
						VBox content = (VBox) envs_components.get(i).getContent();
						if(content.getChildren().contains(devices_components.get(j))){
							content.getChildren().remove(devices_components.get(j));
							devices_components.remove(j);
							break;
						}
					}
				}
				break;
			}
		}
		
		hash.remove(device, env);
	}
}
