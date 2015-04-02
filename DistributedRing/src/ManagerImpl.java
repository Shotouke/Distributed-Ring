import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.rmi.*;
import java.rmi.server.*;
import java.util.Enumeration;
import java.util.Set;
import java.util.Timer;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import javax.net.ssl.ManagerFactoryParameters;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

/*Matar procesos: taskkill /IM java.exe /F*/
public class ManagerImpl extends UnicastRemoteObject implements Manager {
	private static final long serialVersionUID = 1L;
	public static int numServ = 5;
	private static int numVecinos = numServ - 2;	/*Numero de vecinos en los que queremos replicar el contenido*/
	private static String serveraddr = "localhost";	/*Dirección en la que se encuentra el RMIregistry*/
	private static boolean firstTime = true;
	private static boolean[] heartbeat;
	private Manager[] vecinos;
	private int id;
	private int n;
	private String _SERVICENAME;
	private Hashtable tabla = new Hashtable();
	private boolean alive;
	private boolean myQuorum;

	public ManagerImpl(int id, int n, String serverAddr, boolean alive) throws RemoteException {
		super();
		this.id = id;
		this.n = n;
		vecinos = new Manager[numVecinos];
		this._SERVICENAME = "rmi://" + serverAddr + "/" + id;
		this.alive = alive;
		heartbeat = new boolean[numServ];
	} // Constructor
	
	public void replicate(String k, ValueVersion stVer) throws RemoteException {
		if (alive){
			tabla.put(k, stVer);
			System.out.println("Replicamos en el server " + id + " el objeto con key " + k);
		}
	} // replicate
	
	public void preferenceList() {
		try {
			for (int i = 0;  i < numVecinos; i++)	/*Guardamos la lista de vecinos*/
				vecinos[i] = (Manager) Naming.lookup("rmi://" + serveraddr + "/" + (id + i + 1) % n);
		} catch (Exception ie) { System.out.println(ie); }
	} // preferenceList
	
	public Manager[] readQuorum(Manager[] m) throws RemoteException { /*Quorum de lectura*/
		Manager[] read = new Manager[vecinos.length];
		int j = 0;			
		try {
			for (int i = 0; i < vecinos.length; i++) {
					if(vecinos[i].alive()) {
						//System.out.println("Mi id "+ id + "   Vecino " + i + "alive!");
						read[j] = vecinos[i];
						j++;
					}
			}
		} catch (Exception e) { throw new RemoteException("Error Quorum lectura: ", e); }
		return read;
	} // readQuorum
	
	public Manager[] writeQuorum(Manager[] m) throws RemoteException { /*Quorum de escritura*/
		Manager[] write = new Manager[vecinos.length];
		int j = 0;
		try {
				for (int i = 0; i < vecinos.length; i++) {
					if(vecinos[i].alive()) {
						write[j] = vecinos[i];
						j++;
					}
				}
		} catch (Exception e) { throw new RemoteException("Error Quorum escritura: ", e); }
		return write;
		
	} // writeQuorum
	
	public String put(String k, String value) throws RemoteException, AbortException {
		String valorAnt = "";
		int ver = 1;
		
		if (alive) {	
			
			if (firstTime == true) {	/*Obtenemos la lista de vecinos*/
				preferenceList();
				firstTime = false;
			}
			
			if(quorum(k)){
				//if (readQuorum(vecinos).length >= 1) {	/*¿Podemos leer?*/
					if (writeQuorum(vecinos).length >= 2) {	/*¿Podemos escribir?*/
						valorAnt = get(k);
						if(valorAnt.equals("Nueva entrada")) { /*Insertamos en la tabla por primera vez (versión 1)*/
							tabla.put(k, new ValueVersion(value, ver));
							System.out.println("PUT: Server --> " + id +", Key --> "+ k);
							try {
								
						   		/*Creamos un Thread para cada replicate*/
						   		Hilo replica0 = new Hilo(k, new ValueVersion(value, ver), vecinos[0]);
						   		Hilo replica1 = new Hilo(k, new ValueVersion(value, ver), vecinos[1]);
						   		Hilo replica2 = new Hilo(k, new ValueVersion(value, ver), vecinos[2]);
		
						   		/*Arrancamos los Threads*/
						   		replica0.start();
						   		Thread.sleep(800);
						   		replica1.start();
						   		Thread.sleep(800);
						   		replica2.start();
						   		Thread.sleep(800);
						   		
						    } catch (Exception ie) { System.out.println("Error de excepcion (Nueva entrada): "+ie); }
						} else {
							ValueVersion vv = (ValueVersion) tabla.get(k);
							ver = vv.getVer() + 1;
							tabla.put(k, new ValueVersion(value, ver));
							System.out.println("PUT: Server --> " + id +", Key --> "+ k);
							try {
								
						   		/*Creamos un Thread para cada replicate*/
						   		Hilo replica0 = new Hilo(k, new ValueVersion(value, ver), vecinos[0]);
						   		Hilo replica1 = new Hilo(k, new ValueVersion(value, ver), vecinos[1]);
						   		Hilo replica2 = new Hilo(k, new ValueVersion(value, ver), vecinos[2]);
						   		
						   		/*Arrancamos los Threads*/
						   		replica0.start();
						   		Thread.sleep(800);
						   		replica1.start();
						   		Thread.sleep(800);
						   		replica2.start();
						   		Thread.sleep(800);
						   		
						    } catch (Exception ie) { System.out.println("Error de excepcion (Entrada Vieja): " + ie); }
						}
					} else throw new AbortException();
					return ("PUT: Valor anterior: " + valorAnt);
				//} else throw new AbortException();
			} else { /*Si no es nuestro quorum ejecutamos el mismo código teniendo en cuenta que solo tendremos dos vecinos en lugar de tres*/
				if ((readQuorum(vecinos).length - 1) >= 1) {	/*¿Podemos leer?*/
					if ((writeQuorum(vecinos).length - 1) >= 2) {	/*¿Podemos escribir?*/	/*Mirar aqui!!!!!!!!!!!!*/
						valorAnt = get(k);
						if(valorAnt.equals("Nueva entrada")) { /*Insertamos en la tabla por primera vez (versión 1)*/
							tabla.put(k, new ValueVersion(value, ver));
							System.out.println("PUT: Server --> " + id +", Key --> "+ k);
							try {
								
						   		/*Creamos un Thread para cada replicate*/
						   		Hilo replica0 = new Hilo(k, new ValueVersion(value, ver), vecinos[0]);
						   		Hilo replica1 = new Hilo(k, new ValueVersion(value, ver), vecinos[1]);
		
						   		/*Arrancamos los Threads*/
						   		replica0.start();
						   		Thread.sleep(800);
						   		replica1.start();
						   		Thread.sleep(800);
						   		
						    } catch (Exception ie) { System.out.println("Error de excepcion (Nueva entrada): "+ie); }
						} else {
							ValueVersion vv = (ValueVersion) tabla.get(k);
							ver = vv.getVer() + 1;
							tabla.put(k, new ValueVersion(value, ver));
							System.out.println("PUT: Server --> " + id +", Key --> "+ k);
							try {
								
						   		/*Creamos un Thread para cada replicate*/
						   		Hilo replica0 = new Hilo(k, new ValueVersion(value, ver), vecinos[0]);
						   		Hilo replica1 = new Hilo(k, new ValueVersion(value, ver), vecinos[1]);
						   		
						   		/*Arrancamos los Threads*/
						   		replica0.start();
						   		Thread.sleep(800);
						   		replica1.start();
						   		Thread.sleep(800);
						   		
						    } catch (Exception ie) { System.out.println("Error de excepcion (Entrada Vieja): " + ie); }
						}
					} else throw new AbortException();
					return ("PUT: Valor anterior: " + valorAnt);
				} else throw new AbortException();
			}
		} else throw new AbortException();
	} // put
	
	public String get(String k) throws RemoteException, AbortException {
		String valorAnt = "";
		int version;
		int versionAnt;
		
		if (quorum(k)) {
			if (alive) {		
				if (firstTime == true) {	/*Obtenemos la lista de vecinos*/
					preferenceList();
					firstTime = false;
				}
				if (readQuorum(vecinos).length >= 1) {
					if (tabla.get(k) != null) {
						versionAnt = getNumVersion(k);
						System.out.println("Version anterior: " + versionAnt);
						ValueVersion vv = (ValueVersion)tabla.get(k);
						valorAnt =  vv.getValue();	
						
							for(int i = 0; i < vecinos.length; i++) {
								version = vecinos[i].getNumVersion(k);
								if (version > versionAnt) {	
									valorAnt = vecinos[i].get(k);
								}
							}
						return ("GET: Key --> "+ k + ", Valor --> " + valorAnt);
					} else { return "Nueva entrada"; }
				} else throw new AbortException();
			} else throw new AbortException();
		} else {
			if (alive) {		
				if (firstTime == true) {	/*Obtenemos la lista de vecinos*/
					preferenceList();
					firstTime = false;
				}
				
				if (readQuorum(vecinos).length - 1>= 1) {
					if (tabla.get(k) != null) {
						versionAnt = getNumVersion(k);
						System.out.println("Version anterior: " + versionAnt);
						ValueVersion vv = (ValueVersion)tabla.get(k);
						valorAnt =  vv.getValue();	
						
							for(int i = 0; i < vecinos.length - 1; i++){
								version = vecinos[i].getNumVersion(k);
								if (version > versionAnt){
									valorAnt = vecinos[i].get(k);
								}
							}
						return ("GET: Key --> "+ k + ", Valor --> " + valorAnt);
					} else { return "Nueva entrada"; }
				} else throw new AbortException();
			} else throw new AbortException();
		}
	} // get
	
	public int getNumVersion(String k) throws RemoteException, AbortException {
		if (firstTime == true) {	/*Obtenemos la lista de vecinos*/
			preferenceList();
			firstTime = false;
		}
		if (readQuorum(vecinos).length >= 1) {
			if ((ValueVersion) tabla.get(k) != null) {
				ValueVersion vv = (ValueVersion) tabla.get(k);
				return vv.getVer();
			} else { return 0; }
		} else throw new AbortException();
	}
	
	public boolean alive() throws RemoteException { /*Comprovar si server esta vivo*/
		return alive;
	} // alive
	
	public int myId() {
		return id;
	}
	
	/*COMPROVACIÓN QUORUM +++++++++++++++++++++++++++++*/
	public long hashCode(String k) {
		byte bytes[] = k.getBytes();
		Checksum checksum = new Adler32();
		checksum.update(bytes, 0, bytes.length);
		return checksum.getValue();
	} // hashCode

	public boolean quorum(String k) {
		long totalElements = 0;
		long elementsServ = 4294967296L / numServ;
		long elementsServAnt = 0;
		long key = hashCode(k);
		int server = 0;
		for (int i = 0; i < numServ; i++) {
			totalElements = elementsServ * (i + 1);
			elementsServAnt = elementsServ * i;
			if (key <= totalElements && key > elementsServAnt) {
				server = i;
			}
		}
		
		if (server == myId()) myQuorum = true;
		else myQuorum = false;
		return myQuorum;
	} // connectToServer
	/*FIN COMPROVACIÓN QUORUM +++++++++++++++++++++++*/
	
	public void beat(int id) throws RemoteException {
		heartbeat[id] = true;
	} // beat
	
	public void heartbeat() throws RemoteException {
		int[] dead = new int[numServ - 1];
		int[] restart = new int[numServ - 1];

		for (int i = 0; i < dead.length; i++) { /*Inicializo tabla*/
			dead[i] = -1;
			restart[i] = -1;
		}
		int j = 0;
			for (int i = 0; i < heartbeat.length; i++) {
				if (myId() != i) {
					if (heartbeat[i] == false) { /*Comprovamos si a caido algún servidor*/
						dead[j] = i;
						System.out.println("Servidor " + dead[j] + " caido!" );
						j++;
					}
				}
			}
		j = 0;
		
		for (int i = 0; i < dead.length; i++) { /*Guardamos en restart los servidores a los que tenemos que enviar las tablas cuando se reactiven*/
			if (dead[i] != -1) {				/*Los guardamos en la tabla restart xq no se borra al ejecutar de nuevo el método*/
				restart[j] = dead[i];
				j++;
			}
		}
		
		for (int i = 0; i < restart.length; i++) {
			for (int k = 0; k < heartbeat.length; k++) {
				if (heartbeat[k] == true && restart[i] == k) { /*Si heartbeat tiene un servidor 'k' = true y restart tiene 'k' guardado, significa que ese servidor habia caido y vuelve a estar activo*/
					antientropy(k);
					restart[i] = -1;
				}
			}
		}
		
		for (int i = 0; i < numServ; i++)
			heartbeat[i] = false;
	} // heartbeat
	
	public void antientropy(int k) throws RemoteException {	/*Copiar contenido de la tabla de hash en el server cuando vuelva a estar activo*/
		String key = "";
		if (myId() == (k+1)) { /*Comprovamos si somos el server siguiente en el enillo al que se ha recuperado*/
			try {
				Manager m = (Manager) Naming.lookup("rmi://" + serveraddr + "/" + k);
				/*Enviamos las keys que corresponden al server que se ha recuperado*/
				/*Utilizmaos Adler32 para saber en que servidor se guarda la key*/
				Enumeration<String> keys = tabla.keys();
		        while (keys.hasMoreElements()) {
		            key = keys.nextElement();
		            if (connectToServer(keys.nextElement()) == k) {
		            	m.put(key, (String) tabla.get(key));
		            }
		        }
			} catch (Exception e){
				System.out.println("Excepción: " + e);
			}
		} else {
			try {
				Manager m = (Manager) Naming.lookup("rmi://" + serveraddr + "/" + k);
				Enumeration<String> keys = tabla.keys();
		        while (keys.hasMoreElements()) {
		            key = keys.nextElement();
		            if (connectToServer(keys.nextElement()) == myId()) {
		            	int ver = getNumVersion(key);
		            	String value = get(key);
		            	m.replicate(key, new ValueVersion(value,ver));
		            }
		        }
			} catch (Exception e){
				System.out.println("Excepción: " + e);
			}
		}
	}
	
	public int connectToServer(String k) {
		long totalElements = 0;
		long elementsServ = 4294967296L / numServ;
		long elementsServAnt = 0;
		long key = hashCode(k);
		int server = 0;
		for (int i = 0; i < numServ; i++) {
			totalElements = elementsServ * (i + 1);
			elementsServAnt = elementsServ * i;
			if (key <= totalElements && key > elementsServAnt) {
				server = i;
			}
		}
		return server;
	}
	
	public void quit( ) throws RemoteException, InterruptedException {
		  System.out.println("\t ..." + this.id + " is trying to quit ...");
		  try {
			Naming.unbind(_SERVICENAME);
		    UnicastRemoteObject.unexportObject(this, false);
		  } catch (Exception e) { throw new RemoteException("Could not unregister service, quiting anyway", e); }
		  System.out.print("Shutting down " + this.id + "! ");
		  System.exit(0);
		} // quit
	
	public static void main(String[] args) throws Exception {
			 System.setProperty("java.security.policy", "server.policy");
		     if ( System.getSecurityManager() == null ) {
		    	 System.setSecurityManager(new RMISecurityManager( ) );
			 }
		     int id = Integer.parseInt(args[0]); 	// Identifies itself
		     String serveraddr = args[1];  // Identifies server (IP or host name)
		     int n = Integer.parseInt( args[2] );  // Number of manager in the ring
		     boolean alive = Boolean.parseBoolean(args[3]);
			 ManagerImpl manager = new ManagerImpl ( id, n, serveraddr, alive );
			 
		     Naming.rebind("rmi://" + serveraddr + "/" + id,  manager);
		     
		     /*PID del servidor*/
			 RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
			 String jvmName = bean.getName();
			 long pid = Long.valueOf(jvmName.split("@")[0]);
			 System.out.println("Server " + id + " PID = " + pid);
			 /*Fin PID del servidor*/	/*taskkill /PID pid /F*/
			 
		     Thread.sleep(500);
		     String[] lista = Naming.list(serveraddr);
		     Thread.sleep(1000);

		     /*Protocolo HeartBeat*/
		     Beat beat = new Beat(serveraddr, id, lista.length);
		     Timer timerBeat = new Timer(true);
		     timerBeat.schedule(beat, 0, 1000);
		     Thread.sleep(5000);
			 HeartBeat heartBeat = new HeartBeat(serveraddr, id);
			 Timer timer = new Timer (true);
			 timer.scheduleAtFixedRate(heartBeat, 0, 3000);
		     /*Fin protocolo HeartBeat*/
	}  // main
}
