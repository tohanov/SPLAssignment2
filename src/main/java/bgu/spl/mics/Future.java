package bgu.spl.mics;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {

	private T result;
	// private boolean isResolved;
	// private Object isResolvedLock;
	

	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		result = null;
		// isResolved = false;
		// isResolvedLock = new Object();
	}
	
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */
	public T get() {
		//TODO: implement this.

		// waiting without timeout
		return get(0, TimeUnit.MILLISECONDS);
	}
	

	/**
     * Resolves the result of this Future object.
	 * @post this.result == _result
     */
<<<<<<< HEAD
	public void resolve (T result) {
		this.result=result;
		isResolved=true;
		notifyAll();

=======
	public void resolve (T _result) {
		synchronized (result) {
			this.result = _result;
			this.result.notifyAll();
			// isResolved = true;
			// isResolvedLock.notifyAll();
		}
>>>>>>> 44e25761566cf5d78f8beba4ab83c14f9533e10e
	}
	

	/**
     * @return true if this object has been resolved, false otherwise
     */
	public boolean isDone() {
		//TODO: implement this.
		synchronized (result) {
			return result != null;
		}
	}
	

	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	public T get(long timeout, TimeUnit unit) {
		//TODO: implement this.
<<<<<<< HEAD
		// TODO: add a timer on another thread and call regular get()
		
		long toWait=unit.toMicros(timeout);
		
		if(isResolved)
			return result;
		
		try {
			wait(toWait);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
=======
		synchronized (result) {
			if ( ! isDone()) {
				try {
					result.wait(unit.toMillis(timeout));
				}
				catch (InterruptedException exception) { }
			}
		}
>>>>>>> 44e25761566cf5d78f8beba4ab83c14f9533e10e

		return result; // TODO: move into synchronized body??
	}
}
