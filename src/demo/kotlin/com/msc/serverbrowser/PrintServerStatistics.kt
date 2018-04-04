package com.msc.serverbrowser

import java.io.IOException

import com.msc.serverbrowser.util.ServerUtility
import com.msc.serverbrowser.util.samp.SampQuery

/**
 * Queries southclaws api, giving some information about how many servers are online/have querying
 * enabled.
 *
 * @throws IOException if problems occur while querying the samp servers api
 */
fun main(args: Array<String>) {
    println("Start")
    val start = System.currentTimeMillis()
    val servers = ServerUtility.fetchServersFromSouthclaws()
    val onlineServers = servers.stream().parallel().filter { server ->
        try {
            SampQuery(server.address, server.port).use { query -> query.basicServerInfo.isPresent }
        } catch (exception: Exception) {
            false
        }
    }.count()

    println("Out of " + servers.size + " only " + onlineServers + " are online/have querying enabled.")
    println("Time spent: " + (System.currentTimeMillis() - start))
}