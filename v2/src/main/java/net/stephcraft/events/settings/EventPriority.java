package net.stephcraft.events.settings;

public class EventPriority {
	public static final int MONITOR  = Integer.MIN_VALUE;
	public static final int IMPL     = -1;
	public static final int HIGHEST  = 0;
	public static final int HIGH     = 1;
	public static final int NORMAL   = 2;
	public static final int LOW      = 3;
	public static final int LOWEST   = 4;
	public static final int OVERRIDE = 100;
	public static final int DEBUG    = Integer.MAX_VALUE;
}
