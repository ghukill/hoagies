package com.hoagies.games.games.mahjong.engine

import kotlinx.serialization.Serializable

enum class Suit { DOTS, BAMBOO, CHARACTERS, WIND, DRAGON, FLOWER, SEASON }

/**
 * The 42 different tile faces in a classic Mahjong set: three numbered suits (1–9),
 * four winds, three dragons, four flowers and four seasons.
 *
 * A full set has four of each numbered/wind/dragon face and one of each flower/season.
 */
@Serializable
enum class TileFace(val suit: Suit, val rank: Int) {
    DOTS_1(Suit.DOTS, 1), DOTS_2(Suit.DOTS, 2), DOTS_3(Suit.DOTS, 3),
    DOTS_4(Suit.DOTS, 4), DOTS_5(Suit.DOTS, 5), DOTS_6(Suit.DOTS, 6),
    DOTS_7(Suit.DOTS, 7), DOTS_8(Suit.DOTS, 8), DOTS_9(Suit.DOTS, 9),
    BAMBOO_1(Suit.BAMBOO, 1), BAMBOO_2(Suit.BAMBOO, 2), BAMBOO_3(Suit.BAMBOO, 3),
    BAMBOO_4(Suit.BAMBOO, 4), BAMBOO_5(Suit.BAMBOO, 5), BAMBOO_6(Suit.BAMBOO, 6),
    BAMBOO_7(Suit.BAMBOO, 7), BAMBOO_8(Suit.BAMBOO, 8), BAMBOO_9(Suit.BAMBOO, 9),
    CHARACTERS_1(Suit.CHARACTERS, 1), CHARACTERS_2(Suit.CHARACTERS, 2), CHARACTERS_3(Suit.CHARACTERS, 3),
    CHARACTERS_4(Suit.CHARACTERS, 4), CHARACTERS_5(Suit.CHARACTERS, 5), CHARACTERS_6(Suit.CHARACTERS, 6),
    CHARACTERS_7(Suit.CHARACTERS, 7), CHARACTERS_8(Suit.CHARACTERS, 8), CHARACTERS_9(Suit.CHARACTERS, 9),
    WIND_EAST(Suit.WIND, 1), WIND_SOUTH(Suit.WIND, 2), WIND_WEST(Suit.WIND, 3), WIND_NORTH(Suit.WIND, 4),
    DRAGON_RED(Suit.DRAGON, 1), DRAGON_GREEN(Suit.DRAGON, 2), DRAGON_WHITE(Suit.DRAGON, 3),
    FLOWER_1(Suit.FLOWER, 1), FLOWER_2(Suit.FLOWER, 2), FLOWER_3(Suit.FLOWER, 3), FLOWER_4(Suit.FLOWER, 4),
    SEASON_1(Suit.SEASON, 1), SEASON_2(Suit.SEASON, 2), SEASON_3(Suit.SEASON, 3), SEASON_4(Suit.SEASON, 4),
    ;

    /** Tiles match when their keys are equal. Any flower matches any flower; same for seasons. */
    val matchKey: String
        get() = when (suit) {
            Suit.FLOWER, Suit.SEASON -> suit.name
            else -> name
        }

    fun matches(other: TileFace) = matchKey == other.matchKey

    /** Image file name (without extension) used by picture tile sets, e.g. "dots_3", "wind_east". */
    val fileKey: String get() = name.lowercase()

    /** What a screen reader says, e.g. "3 of Bamboo", "East Wind", "Red Dragon", "Flower". */
    val spokenName: String
        get() = when (suit) {
            Suit.DOTS -> "$rank of Dots"
            Suit.BAMBOO -> "$rank of Bamboo"
            Suit.CHARACTERS -> "$rank of Characters"
            Suit.WIND -> listOf("East", "South", "West", "North")[rank - 1] + " Wind"
            Suit.DRAGON -> listOf("Red", "Green", "White")[rank - 1] + " Dragon"
            Suit.FLOWER -> "Flower"
            Suit.SEASON -> "Season"
        }
}
