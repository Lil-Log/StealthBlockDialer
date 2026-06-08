package com.goodwy.dialer.helpers

import android.content.Context

import android.telephony.PhoneNumberUtils

object StealthBlockedNumbersRepository {
    private const val PREFS_NAME = "stealth_blocked_numbers_prefs"
    private const val KEY_NUMBERS = "stealth_blocked_numbers"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun normalize(number: String): String {
        val trimmed = number.trim()
        return try {
            PhoneNumberUtils.normalizeNumber(trimmed)
        } catch (_: Exception) {
            trimmed
        }
    }

    fun getStealthBlockedNumbers(context: Context): List<String> {
        return prefs(context).getStringSet(KEY_NUMBERS, emptySet())
            ?.map { normalize(it) }
            ?.toSet()
            ?.toList()
            ?.sorted()
            ?: emptyList()
    }

    fun addStealthBlockedNumber(context: Context, number: String) {
        val normalizedNumber = normalize(number)
        if (normalizedNumber.isBlank()) return

        val current = prefs(context).getStringSet(KEY_NUMBERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(normalizedNumber)
        prefs(context).edit().putStringSet(KEY_NUMBERS, current).apply()
    }

    fun removeStealthBlockedNumber(context: Context, number: String) {
        val normalizedNumber = normalize(number)
        if (normalizedNumber.isBlank()) return

        val current = prefs(context).getStringSet(KEY_NUMBERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (current.remove(normalizedNumber)) {
            prefs(context).edit().putStringSet(KEY_NUMBERS, current).apply()
        }
    }
}
