package com.msc.serverbrowser.serviceImplementations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import com.msc.sampbrowser.entities.SampServerSerializeable;
import com.msc.sampbrowser.interfaces.DataServiceInterface;
import com.msc.serverbrowser.data.MySQLConnection;

@SuppressWarnings("all") // Class will be deleted soon
public class DataServiceServerImplementation implements DataServiceInterface
{
	private static List<SampServerSerializeable> servers = new ArrayList<>();

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
		catch (final IOException exception)
		{
			throw new RemoteException("Couldn't serialize and compress data", exception);
		}
	}

	@Override
	public void tellServerThatYouUseTheApp(final String country) throws RemoteException
	{
		MySQLConnection.addStatistic(country);
	}
}
