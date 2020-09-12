package application.ui.constants;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageConstants {
	public static Image MONO_USER_ICON =  new Image(ImageConstants.class.getResourceAsStream("/resources/images/mono_user_icon.png"), 40, 40, true, true);
	public static Image MULTI_USER_ICON =  new Image(ImageConstants.class.getResourceAsStream("/resources/images/multi_user_icon.png"), 40, 40, true, true);
	public static Image CHAT_TOP_ICON = new Image(ImageConstants.class.getResourceAsStream("/resources/images/chat_icon.png"), 30, 30, true, true);
	public static ImageView ADD_BTN_ICON = new ImageView(new Image(ImageConstants.class.getResourceAsStream("/resources/images/add.png"), 13, 13, true, true));
}
