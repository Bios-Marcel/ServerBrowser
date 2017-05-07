package interfaces;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UpdateServiceInterface extends Remote
{
	public static String INTERFACE_NAME = "UpdateServiceInterface";

	String getLatestVersionURL() throws IOException, RemoteException;

}
