package com.msc.sampbrowser.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataServiceInterface extends Remote
{
	public static String INTERFACE_NAME = "DataServiceInterface";

	byte[] getAllServers() throws RemoteException;

	void tellServerThatYouUseTheApp(String country) throws RemoteException;
}
