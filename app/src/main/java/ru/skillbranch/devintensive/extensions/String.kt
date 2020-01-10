package ru.skillbranch.devintensive.extensions

fun String.truncate(characters: Int = 16): String {
    val placeholder = "..."
    val stringTrimmed = this.trimEnd()

    return when {
        stringTrimmed.length <= characters -> stringTrimmed
        else -> "${stringTrimmed.subSequence(0, characters).trimEnd()}$placeholder"
    }
}

fun String.stripHtml(): String {
    val stringStrip1 = Regex("<.*?>").replace(this, "")
    val stringStrip2 = Regex("&.*?;").replace(stringStrip1, "")

    return Regex("\\s+").replace(stringStrip2, " ")
}