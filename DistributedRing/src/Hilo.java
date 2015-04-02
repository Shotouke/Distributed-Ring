import java.rmi.RemoteException;


public class Hilo extends Thread {
	private String key;
	private ValueVersion valueVer;
	private Manager manager;
	
	public Hilo(String key, ValueVersion valueVer, Manager manager) {
		this.key = key;
		this.valueVer = valueVer;
		this.manager = manager;
	} // Constructor
	
	public void run() {
			try {
				manager.replicate(key, valueVer);
			} catch (RemoteException e) {
				//e.printStackTrace();
			}
	} // run
}
