package serviceImplementations;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import entities.SampServerSerializeable;
import interfaces.DataServiceInterface;
import util.Hashing;

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
	public String getLatestVersionChecksum() throws FileNotFoundException, NoSuchAlgorithmException, IOException, RemoteException
	{
		return Hashing.verifyChecksum("/var/www/html/sampversion/launcher/launcher.jar");
	}

	// @Override
	// public void tellServerThatYouUseTheApp()
	// {
	// TODO(MSC) I should do this at some time
	// }
}
