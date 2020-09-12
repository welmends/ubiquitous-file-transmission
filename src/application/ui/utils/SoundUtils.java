package application.ui.utils;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;

public class SoundUtils {
	
	private Media receive;
	private Media send;
	private AudioClip receivePlayer;
	private AudioClip sendPlayer;
	
	
	public SoundUtils() {
		try {
			receive  = new Media(getClass().getResource("/resources/sounds/receive.wav").toURI().toString());
			send     = new Media(getClass().getResource("/resources/sounds/send.wav").toURI().toString());
			
			receivePlayer  = new AudioClip(receive.getSource());
			sendPlayer     = new AudioClip(send.getSource());
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void playReceiveSound() {
		receivePlayer.play();
	}
	
	public void playSendSound() {
		sendPlayer.play();
	}
}
