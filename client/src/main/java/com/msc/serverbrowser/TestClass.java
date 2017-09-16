package com.msc.serverbrowser;

import com.msc.serverbrowser.util.ServerUtil;

@SuppressWarnings("javadoc")
public class TestClass
{
	public static void main(final String[] args) throws Exception
	{
		System.out.println("JSON");

		ServerUtil.retrieveAnnouncedServers().forEach(server ->
		{
			System.out.println(server.getAddress() + ":" + server.getHostname());
		});

		// ServerUtil.getServerInfo();
	}
}