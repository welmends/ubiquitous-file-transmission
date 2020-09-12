package application.ts;

import net.jini.space.JavaSpace;

public class TupleSpaceConstants {
	public static Integer THREAD_SLEEP_TIME_MILLIS = 2000; // 2sec
	public static Long TIMER_KEEP_UNDEFINED = Long.MAX_VALUE; // Undefined
	public static Long TIMER_KEEP_ROOM      = new Long(10 * 60 * 1000); // 10min
	public static Long TIMER_KEEP_MESSAGE   = new Long(5 * 60 * 1000); // 5min
	public static Long TIMER_TAKE_ADMIN     = new Long(3 * 1000); // 3sec
	public static Long TIMER_TAKE_ROOM      = new Long(2 * 1000); // 2sec
	public static Long TIMER_TAKE_MESSAGE   = JavaSpace.NO_WAIT; // 2sec
	public static Long TIMER_NO_WAIT        = JavaSpace.NO_WAIT; // 0sec
	
	public static Boolean ROOM_CHAT = true;
	public static Boolean CONTACT_CHAT = false;
	
	public static String ALL_ROOM_TEXT = "All";
}
