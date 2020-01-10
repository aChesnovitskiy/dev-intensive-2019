package ru.skillbranch.devintensive.utils

import java.util.*
import kotlin.collections.HashMap

object Utils {
    fun parseFullName (fullName: String?) : Pair<String?, String?> {
        val firstName: String?
        val lastName: String?
        when {
            fullName.isNullOrBlank() -> {
                firstName = null
                lastName = null
            }
            else -> {
                val parts: List<String>? = fullName.trim().split(" ")
                firstName = parts?.getOrNull(0)
                lastName = parts?.getOrNull(1)}
        }
        return firstName to lastName
    }

    fun transliteration(payload: String, divider: String = " "): String {
//        val payloadTrasliteration: String
        val dictionary: HashMap<String, String> = hashMapOf("а" to "a", "б" to "b", "в" to "v", "г" to "g",
            "д" to "d", "е" to "e", "ё" to "e", "ж" to "zh", "з" to "z", "и" to "i", "й" to "i", "к" to "k",
            "л" to "l", "м" to "m", "н" to "n", "о" to "o", "п" to "p", "р" to "r", "с" to "s", "т" to "t",
            "у" to "u", "ф" to "f", "х" to "h", "ц" to "c", "ч" to "ch", "ш" to "sh", "щ" to "sh'", "ъ" to "",
            "ы" to "i", "ь" to "", "э" to "e", "ю" to "yu", "я" to "ya")
        return buildString {
            for (char: Char in payload) {
                when {
                    dictionary.get(char.toString()) != null -> append(dictionary.getValue(char.toString()))
                    char.isUpperCase() && dictionary.get(char.toString().toLowerCase(Locale.getDefault())) != null ->
                        append(dictionary.getValue(char.toString().toLowerCase(Locale.getDefault())).capitalize())
                    char.toString().equals(" ") -> append(divider)
                    else -> append(char.toString())
                }
            }
        }
    }

    fun toInitials(firstName: String?, lastName: String?): String? {
        val initials: String?
        when {
            firstName.isNullOrBlank() && lastName.isNullOrBlank() -> initials = null
            !firstName.isNullOrBlank() && lastName.isNullOrBlank() -> initials = firstName.take(1).toUpperCase(Locale.getDefault())
            firstName.isNullOrBlank() && !lastName.isNullOrBlank() -> initials = lastName.take(1).toUpperCase(Locale.getDefault())
            else -> initials = "${firstName?.take(1)?.toUpperCase(Locale.getDefault())}${lastName?.take(1)?.toUpperCase(Locale.getDefault())}"
        }
        return initials
    }
}