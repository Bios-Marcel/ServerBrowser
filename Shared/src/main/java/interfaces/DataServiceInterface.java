package interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface DataServiceInterface extends Remote
{
	public static String INTERFACE_NAME = "DataServiceInterface";

	public byte[] getAllServers() throws RemoteException;

	public String getLatestVersionChecksum() throws FileNotFoundException, NoSuchAlgorithmException, IOException, RemoteException;
}
