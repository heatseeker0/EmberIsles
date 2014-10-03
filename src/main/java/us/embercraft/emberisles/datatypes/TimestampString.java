package us.embercraft.emberisles.datatypes;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;


/**
 * This class is simply a string and a timestamp put together in the same
 * construct. The class is immutable, thread safe and serializable.
 * @author Catalin Ionescu <cionescu@gmail.com>
 *
 */
@ThreadSafe
@Immutable
public class TimestampString implements Serializable {
	public TimestampString(final String string, final long timeStamp) {
		this.string = string;
		this.timeStamp = timeStamp;
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public String getString() {
		return string;
	}
	
	private final long timeStamp;
	private final String string;
	private static final long serialVersionUID = 1L;
}
