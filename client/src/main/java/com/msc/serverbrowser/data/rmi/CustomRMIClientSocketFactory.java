package com.msc.serverbrowser.data.rmi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

import org.eclipse.jdt.annotation.Nullable;

public class CustomRMIClientSocketFactory implements RMIClientSocketFactory
{
	@Override
	public Socket createSocket(final @Nullable String host, final int port) throws IOException
	{
		final Socket socket = new Socket();
		socket.setSoTimeout(1500);
		socket.setSoLinger(false, 0);
		socket.connect(new InetSocketAddress(host, port), 1500);
		return socket;
	}
}
