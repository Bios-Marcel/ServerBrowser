package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import entities.SampServerSerializeable;

public interface DataServiceInterface extends Remote
{
	public Collection<SampServerSerializeable> getAllServers() throws RemoteException;
}
