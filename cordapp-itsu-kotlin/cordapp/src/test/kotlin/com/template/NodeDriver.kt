package com.template

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.node.services.transactions.ValidatingNotaryService
import net.corda.nodeapi.User
import net.corda.nodeapi.internal.ServiceInfo
import net.corda.testing.driver.driver

fun main(args: Array<String>) {
    // No permissions required as we are not invoking flows.
    val user = User("user1", "test", permissions = setOf("ALL"))
    driver(startNodesInProcess = true) {
        startNode(providedName = CordaX500Name("Controller", "London", "GB"), advertisedServices = setOf(ServiceInfo(ValidatingNotaryService.type)))
        val (nodeA, nodeB, nodeC) = listOf(
                startNode(providedName = CordaX500Name("Trustee", "London", "GB"), rpcUsers = listOf(user)),
                startNode(providedName = CordaX500Name("Bank", "New York", "US"), rpcUsers = listOf(user)),
                startNode(providedName = CordaX500Name("Offtaker", "London", "GB"), rpcUsers = listOf(user))).map { it.getOrThrow() }

        startWebserver(nodeA)
        startWebserver(nodeB)
        startWebserver(nodeC)

        waitForAllNodesToFinish()
    }
}