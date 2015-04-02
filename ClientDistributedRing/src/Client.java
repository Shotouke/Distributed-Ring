import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class Client {

	private static int numServ = 5;

	public static long hashCode(String k) {
		byte bytes[] = k.getBytes();
		Checksum checksum = new Adler32();
		checksum.update(bytes, 0, bytes.length);
		return checksum.getValue();
	} // hashCode

	public static int connectToServer(String k) {
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
	} // connectToServer

	public static void main(String[] args) throws NumberFormatException, IOException, NotBoundException {
		BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
		int num, server;
		String key, value, serveraddr = "localhost";
		Manager m;
		try {
			do{
				System.out.println("\nPRACTICA 3         MANUEL ROJO HORNO\n");
				System.out.println("1.- Put");
				System.out.println("2.- Get");
				System.out.println("3.- Salir");
				num = Integer.parseInt(read.readLine());	
				
				try{
				switch(num){
					case 1:
						System.out.println("Introduzca key del objeto:\n");
						key = read.readLine();
						System.out.println("Introduzca valor del objeto");
						value = read.readLine();
						
						server = connectToServer(key);
						
						m = (Manager) Naming.lookup("rmi://" + serveraddr + ":1099/" + server);
			
						System.out.println("Conectando al server " + server + " ...");
						try {
							System.out.println(m.put(key, value));
							Thread.sleep(2000);
						} catch (Exception ae) {
							//System.out.println(ae);
							server++;
							m = (Manager) Naming.lookup("rmi://" + serveraddr + ":1099/" + server);
							System.out.println("Conectando al server " + server + " ...");
							System.out.println(m.put(key, value));
							Thread.sleep(2000);
						}
			
						break;
						
					case 2:
						System.out.println("Introduzca key del objeto:\n");
						key = read.readLine();
						
						server = connectToServer(key);
						
						m = (Manager) Naming.lookup("rmi://" + serveraddr + ":1099/" + server);
						System.out.println("Conectando al server " + server + " ...");
						try {
							System.out.println(m.get(key));
							Thread.sleep(2000);
						} catch (Exception ae) {
							//System.out.println(ae);
							server++;
							m = (Manager) Naming.lookup("rmi://" + serveraddr + ":1099/" + server);
							System.out.println("Conectando al server " + server + " ...");
							System.out.println(m.get(key));
							Thread.sleep(2000);
						}
						break;
				}
				}catch (Exception e){
					System.out.println(e);
				}
			} while(num != 3);	
		} catch (Exception e){
			System.out.println("Excepcion: "+ e);
		}
		
	} // main
	
}