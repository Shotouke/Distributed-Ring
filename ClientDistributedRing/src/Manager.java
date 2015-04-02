import java.rmi.*;

public interface Manager extends Remote {
	String put(String k, String value) throws RemoteException, AbortException;
	String get(String k) throws RemoteException, AbortException;
} // interface
