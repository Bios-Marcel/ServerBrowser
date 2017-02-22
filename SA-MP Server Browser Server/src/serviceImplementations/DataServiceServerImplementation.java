package serviceImplementations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import entities.SampServerSerializeable;
import interfaces.DataServiceInterface;

public class DataServiceServerImplementation implements DataServiceInterface
{
	private static Set<SampServerSerializeable> servers = new HashSet<>();

	public static void clearList()
	{
		servers.clear();
	}

	public static void addToServers(final Collection<SampServerSerializeable> list)
	{
		servers.addAll(list);
	}

	@Override
	public byte[] getAllServers() throws RemoteException
	{
		return convertToCompressedBytes(servers);
	}

	private byte[] convertToCompressedBytes(final Object object) throws RemoteException
	{
		try
		{
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
			final ObjectOutputStream objectOutputStream = new ObjectOutputStream(gzipOutputStream);
			objectOutputStream.writeObject(object);
			objectOutputStream.close();
			return byteArrayOutputStream.toByteArray();
		}
		catch (final IOException e)
		{
			throw new RemoteException("Couldn't serialize and compress data", e);
		}
	}
}
