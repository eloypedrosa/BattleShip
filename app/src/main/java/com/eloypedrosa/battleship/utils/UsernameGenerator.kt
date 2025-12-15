package com.eloypedrosa.battleship.utils

object UsernameGenerator {
    private val adjectives = listOf(
        "Capitán", "Almirante", "Marinero", "Pirata",
        "Furioso", "Veloz", "Fantasma", "Valiente", "Torpedo"
    )
    private val nouns = listOf(
        "Shark", "Kraken", "Hook", "Sparrow",
        "Poseidon", "Nemo", "Ahab", "Morgan", "Barbarroja"
    )

    fun generate(): String {
        return "${adjectives.random()}${nouns.random()}${((10..99).random())}"
    }
}