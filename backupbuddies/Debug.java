package backupbuddies;

import java.util.Arrays;

/*
 * Adam's no-frills logging mini-library
 * 
 * It prints "classname@line <thing>".
 * 
 * eg. "PeerServicer
 * 
 * Extracted from https://github.com/planetguy32/Utils/blob/master/src/main/java/me/planetguy/lib/util/Debug.java
 * 
 */

public abstract class Debug {

	//Prints a message, prepending the caller's caller's class name and line number
	//Mostly useful for debugging purposes.
	public static void printWithCaller(Object o) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		System.out.println(trace[3].getClassName().replaceAll("[a-zA-Z]*\\.", "") + "@" + trace[3].getLineNumber() + "   " + o);
	}
	
	public static void dbg(Object o) {
		printWithCaller(o);
	}

	public static void exception(Throwable t) {
		printWithCaller(t.toString());
	}
	
	@Deprecated
	public static String dump(Object o) {
		String ret = "";
		if (o == null) {
			ret += ("NULL");
		} else {
			ret += (o.getClass());
			for (java.lang.reflect.Field f : o.getClass().getFields()) {
				try {
					ret += (f.getName() + ":   " + f.get(o));
				} catch (IllegalArgumentException e) {} catch (IllegalAccessException e) {}
			}
		}
		return ret;
	}

	public static void mark() {
		printWithCaller("executed");
	}
	
}