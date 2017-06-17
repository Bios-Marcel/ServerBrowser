package com.msc.sampbrowser.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public interface UpdateServiceInterface extends Remote
{
	public static String INTERFACE_NAME = "UpdateServiceInterface";

	/**
	 * Queries GitHub to retrieve the direct link to download the latest release.
	 *
	 * @return GitHub download url
	 * @throws IOException
	 * @throws RemoteException
	 */
	String getLatestVersionURL() throws IOException, RemoteException;

	String getLatestVersionChecksum() throws FileNotFoundException, NoSuchAlgorithmException, IOException, RemoteException;
}
