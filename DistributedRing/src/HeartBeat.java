import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.TimerTask;


public class HeartBeat extends TimerTask {
	public String serveraddr;
	public int id;
	
	public HeartBeat (String serveraddr, int id) {
		super();
		this.serveraddr = serveraddr;
		this.id = id;
	} // constructor
	
	public void run() {
		try {
			Manager m = (Manager) Naming.lookup("rmi://" + serveraddr + "/" + id);
			m.heartbeat();
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // run
}