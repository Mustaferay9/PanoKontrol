package com.panokontrol.gridcheck

data class SapMapping(
    val buyukCount: Int,
    val kucukCount: Int,
    val sapCodes: List<String>,
    val not: String = "",
    val count400A: Int = 0,
    val count250A: Int = 0,
    val count160A: Int = kucukCount
)

// ==== BURAYA YENİ KOMBİNASYONLAR EKLEYEBİLİRSİN ====
val sapMappingTable = listOf(
    SapMapping(3, 5, listOf("10008670", "10008872"), "1x 400A + 2x 250A, 5x 160A", count400A = 1, count250A = 2, count160A = 5),
    SapMapping(4, 3, listOf("10008671", "10008874"), "2x 400A + 2x 250A, 3x 160A", count400A = 2, count250A = 2, count160A = 3),
    SapMapping(1, 5, listOf("10008668", "10008877"), "1x 250A, 5x 160A", count400A = 0, count250A = 1, count160A = 5),
    SapMapping(1, 10, listOf("10008669", "10008873"), "1x 400A, 10x 160A", count400A = 1, count250A = 0, count160A = 10),
)

fun findSapCodes(buyukCount: Int, kucukCount: Int): SapMapping? {
    return sapMappingTable.find { it.buyukCount == buyukCount && it.kucukCount == kucukCount }
}
