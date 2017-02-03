package serviceImplementations;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import entities.SampServerSerializeable;
import interfaces.DataServiceInterface;

public class DataServiceServerImplementation implements DataServiceInterface
{
	private static Set<SampServerSerializeable> servers = new HashSet<SampServerSerializeable>();

	public static void clearList()
	{
		servers.clear();
	}

	public static void addToServers(Collection<SampServerSerializeable> list)
	{
		servers.addAll(list);
	}

	@Override
	public Collection<SampServerSerializeable> getAllServers() throws RemoteException
	{
		return servers;
	}
}
