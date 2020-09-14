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
	private HashMap<String, String> hash;
	private List<TitledPane> envs_components;
	private List<Button> devices_components;
	private TitledPane env_all;
	private List<Button> contacts_components_on_all;
	
	public ConfigComponentsArrayUtils(ConfigController config, VBox vboxOnScroll){
		this.config = config;
		this.vboxOnScroll = vboxOnScroll;
		this.hash = new HashMap<String, String>();
		this.envs_components = new ArrayList<TitledPane>();
		this.devices_components = new ArrayList<Button>();
		this.env_all = null;
		this.contacts_components_on_all = new ArrayList<Button>();
	}
	
	public void init_all_env(String ts_device_name) {
		TitledPane tp = new TitledPane();
		tp.setText(TupleSpaceConstants.ALL_ROOM_TEXT);
		tp.setStyle(ConfigConstants.TITLED_PANE_STYLE);
		tp.setContentDisplay(ConfigConstants.ROOM_BUTTON_CONTENT_DISPLAY);
		tp.setContent(new VBox());
		
		env_all = tp;
		
        vboxOnScroll.getChildren().add(tp);
        
        add_contact_button_on_all(ts_device_name, ts_device_name);
	}
	
	@SuppressWarnings("unchecked")
	public void updateComponentsList(String device_name, List<Environment> ts_envs, List<Device> ts_devices, HashMap<String, String> ts_hash) {
		Boolean add_del;
		
		// All Room
		for (int i=0; i<ts_devices.size(); i++) {
			add_del = true;
			for (int j=0; j<contacts_components_on_all.size(); j++) {
				if(ts_devices.get(i).name.equals(contacts_components_on_all.get(j).getText())) {
					add_del = false;
					break;
				}
			}
			if(add_del) {
				add_contact_button_on_all(device_name, ts_devices.get(i).name);
			}
		}
		
		for (int i=0; i<contacts_components_on_all.size(); i++) {
			add_del = true;
			for (int j=0; j<ts_devices.size(); j++) {
				if(contacts_components_on_all.get(i).getText().equals(ts_devices.get(j).name)) {
					add_del = false;
					break;
				}
			}
			if(add_del) {
				del_contact_button_on_all(contacts_components_on_all.get(i).getText());
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
				add_room_titledPane(ts_envs.get(i).name);
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
				del_room_titledpane(envs_components.get(i).getText());
			}
		}
		
		// Common Contacts
		ts_hash.forEach((key, value) -> {
			if(hash.containsKey(key)) {
				if(!hash.get(key).equals(value)) {
					del_contact_button(hash.get(key), key);
					add_contact_button(device_name, value, key);
				}
			}else {
				add_contact_button(device_name, value, key);
			}
		});
		
		((HashMap<String, String>) hash.clone()).forEach((key, value) -> {
			if(ts_hash.containsKey(key)) {
				if(!ts_hash.get(key).equals(value)) {
					del_contact_button(value, key);
					add_contact_button(device_name, ts_hash.get(key), key);
				}
			}else {
				del_contact_button(value, key);
			}
		});
		
        vboxOnScroll.applyCss();
        vboxOnScroll.layout();
	}
	
	private void add_room_titledPane(String room_name) {
		HBox h = new HBox();
		
		Button b_enter = new Button();
		b_enter.setText(ConfigConstants.ENTER_ROOM_BUTTON_TEXT);
		b_enter.setStyle(ConfigConstants.ROOM_BUTTON_STYLE);
		
		Button b_leave = new Button();
		b_leave.setText(ConfigConstants.LEAVE_ROOM_BUTTON_TEXT);
		b_leave.setStyle(ConfigConstants.ROOM_BUTTON_STYLE);

		h.getChildren().addAll(b_enter, b_leave);
		
		TitledPane tp = new TitledPane();
		tp.setText(room_name);
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
	
	private void add_contact_button(String ts_device_name, String room_name, String contact_name) {
		Button b = new Button();
		b.setText(contact_name);
		b.setStyle(ConfigConstants.CONTACT_BUTTON_STYLE);
		b.setPrefWidth(ConfigConstants.CONTACT_BUTTON_PREF_WIDTH);
		config.setDeviceBtnPressedBehavior(b);
		if(contact_name.equals(ts_device_name)) {
			b.setDisable(true);
		}
		
		devices_components.add(b);
		
		for (int i=0; i<envs_components.size(); i++) {
			if(envs_components.get(i).getText().equals(room_name)) {
				VBox content = (VBox) envs_components.get(i).getContent();
				content.getChildren().add(b);
			}
		}
		
		hash.put(contact_name, room_name);
	}
	
	private void add_contact_button_on_all(String ts_device_name, String contact_name) {
		Button b = new Button();
		b.setText(contact_name);
		b.setStyle(ConfigConstants.CONTACT_BUTTON_STYLE);
		b.setPrefWidth(ConfigConstants.CONTACT_BUTTON_PREF_WIDTH);
		config.setDeviceBtnPressedBehavior(b);
		if(contact_name.equals(ts_device_name)) {
			b.setDisable(true);
		}
		
		contacts_components_on_all.add(b);
		
		VBox content = (VBox) env_all.getContent();
		content.getChildren().add(b);
	}
	
	private void del_room_titledpane(String room_name) {
		for (int i=0; i<envs_components.size(); i++) {
			if(envs_components.get(i).getText().equals(room_name)) {
				vboxOnScroll.getChildren().remove(envs_components.get(i));
				envs_components.remove(i);
				break;
			}
		}
	}
	
	private void del_contact_button(String room_name, String contact_name) {
		for (int i=0; i<envs_components.size(); i++) {
			if(envs_components.get(i).getText().equals(room_name)) {
				for (int j=0; j<devices_components.size(); j++) {
					if(devices_components.get(j).getText().equals(contact_name)) {
						VBox content = (VBox) envs_components.get(i).getContent();
						content.getChildren().remove(devices_components.get(j));
						devices_components.remove(j);
		        		break;
					}
				}
				break;
			}
		}
		
		hash.remove(contact_name, room_name);
	}
	
	private void del_contact_button_on_all(String contact_name) {
		for (int i=0; i<contacts_components_on_all.size(); i++) {
			if(contacts_components_on_all.get(i).getText().equals(contact_name)) {
				VBox content = (VBox) env_all.getContent();
				content.getChildren().remove(contacts_components_on_all.get(i));
				contacts_components_on_all.remove(i);
        		break;
			}
		}
	}
	
}
