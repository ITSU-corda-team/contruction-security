package net.cordaclub.itsu

import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party

object Projects {
    // TODO YGK - Add meaningful project names
    val allProjects = listOf(
            Project("HK Macau Bridge-Success", 20000000, 20000000,
                    0, 20000000),
            Project("HK Macau Bridge-Fail", 10000000, 10000000, 0, 10000000)
    )
}


object Banks {
    val allBanks = listOf(
            CordaX500Name("Bank of China", "Beijing", "CN"),
            CordaX500Name("HSBC Bank plc", "London", "UK")
    )
}

object Offtakers {
    val allOfftakers = listOf(
            CordaX500Name("China Central Govt", "Beijing", "CH"),
            CordaX500Name("Hongkong Govt", "Hongkong", "HK")
    )
}