import java.rmi.*;

public interface Manager extends Remote {
	String put(String k, String value) throws RemoteException, AbortException;
	String get(String k) throws RemoteException, AbortException;
	void replicate (String k, ValueVersion valueVer) throws RemoteException;
	boolean alive() throws RemoteException;
	Manager[] readQuorum(Manager[] m) throws RemoteException;
	Manager[] writeQuorum(Manager[] m) throws RemoteException;
	int getNumVersion(String k) throws RemoteException, AbortException;
	void beat(int id) throws RemoteException;
	void heartbeat() throws RemoteException;
	void antientropy(int id) throws RemoteException;
} // interface
