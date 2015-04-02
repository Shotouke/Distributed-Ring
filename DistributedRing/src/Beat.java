import java.rmi.Naming;
import java.util.TimerTask;


public class Beat extends TimerTask {
	public String serveraddr;
	public int id;
	public int numServ;
	public Manager[] servers;

	public Beat (String serveraddr, int id, int numServ) {
		super();
		this.serveraddr = serveraddr;
		this.id = id;
		this.numServ = numServ;
		servers = new Manager[numServ];
	} // constructor
	
	public Manager[] lista() {
		for (int i = 0; i < numServ; i++) {
			try {
				servers[i] = (Manager) Naming.lookup("rmi://" + serveraddr + "/" + i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return servers;
	} // lista
	
	
	public void run() {
		servers = lista();
		for (int i = 0; i < numServ; i++) {
			try {
				if (i != id) {
					servers[i].beat(id);
				}
			} catch (Exception e) {
				//e.printStackTrace();	/*Comentado para no ver el mensaje de excepcion cuando matamos un server para hacer pruebas*/
			}
		}
	} // run
}