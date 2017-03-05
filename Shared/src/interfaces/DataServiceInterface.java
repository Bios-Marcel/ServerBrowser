package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataServiceInterface extends Remote
{
	public static String INTERFACE_NAME = "DataServiceInterface";

	public byte[] getAllServers() throws RemoteException;

	public String getLatestVersionChecksum() throws RemoteException;
}
