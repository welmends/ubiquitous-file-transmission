package application.ts;

import net.jini.space.JavaSpace;

public class TupleSpaceConstants {
	public static Integer THREAD_SLEEP_TIME_MILLIS = 2000; // 2sec
	public static Integer MAX_ENV_DISTANCE  = 10;
	
	public static Long TIMER_KEEP_UNDEFINED = Long.MAX_VALUE; // Undefined
	public static Long TIMER_TAKE_ADMIN     = new Long(3 * 1000); // 3sec
	public static Long TIMER_TAKE_ENV       = new Long(2 * 1000); // 2sec
	public static Long TIMER_NO_WAIT        = JavaSpace.NO_WAIT; // 0sec
	
	public static String PREFIX_ENV         = "Env_";
}
