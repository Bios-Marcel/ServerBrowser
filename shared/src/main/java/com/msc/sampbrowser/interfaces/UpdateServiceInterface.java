package com.msc.sampbrowser.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;

/**
 * Contains methods for providing clients with updates.
 *
 * @author Marcel
 */
public interface UpdateServiceInterface extends Remote
{
	/**
	 * The name to be used for binding the interface to the {@link Registry}.
	 */
	public static String INTERFACE_NAME = "UpdateServiceInterface";

	/**
	 * Queries GitHub to retrieve the direct link to download the latest release.
	 *
	 * @return GitHub download url
	 * @throws IOException
	 * @throws RemoteException
	 */
	String getLatestVersionURL() throws IOException, RemoteException;

	/**
	 * @return the SHA-256 Checksum of the latest Client jar file.
	 * @throws FileNotFoundException
	 *             if the file to take the Hash sum of couldn't be found
	 * @throws NoSuchAlgorithmException
	 *             if the algorithm to be used couldn't be found
	 * @throws IOException
	 *             if an error occurs while reading the file
	 * @throws RemoteException
	 */
	String getLatestVersionChecksum() throws FileNotFoundException, NoSuchAlgorithmException, IOException, RemoteException;
}
