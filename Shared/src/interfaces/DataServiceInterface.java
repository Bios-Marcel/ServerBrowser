package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataServiceInterface extends Remote
{
	public byte[] getAllServers() throws RemoteException;
}
