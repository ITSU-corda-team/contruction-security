package net.cordaclub.itsu

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
 //       val (nodeA, nodeB, nodeC, nodeD, nodeE) = listOf(
        val (nodeA, nodeB, nodeC, nodeD ) = listOf(
                startNode(providedName = CordaX500Name("SecurityTrustee", "London", "GB"), rpcUsers = listOf(user)),
                startNode(providedName = CordaX500Name("BankOfChina", "Beijing", "CN"), rpcUsers = listOf(user)),
                startNode(providedName = CordaX500Name("SPV", "London", "GB"), rpcUsers = listOf(user)),
                startNode(providedName = CordaX500Name("OfftakerRPC", "Beijing", "CN"), rpcUsers = listOf(user))).map { it.getOrThrow() }
//               startNode(providedName = CordaX500Name("HSBCBank", "London", "GB"), rpcUsers = listOf(user)),

        startWebserver(nodeA)
        startWebserver(nodeB)
        startWebserver(nodeC)
        startWebserver(nodeD)
//        startWebserver(nodeE)

        waitForAllNodesToFinish()
    }
}