package com.goodwy.dialer.helpers

import android.content.Context

object StealthBlockedNumbersRepository {
    private const val PREFS_NAME = "stealth_blocked_numbers_prefs"
    private const val KEY_NUMBERS = "stealth_blocked_numbers"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getStealthBlockedNumbers(context: Context): List<String> {
        return prefs(context).getStringSet(KEY_NUMBERS, emptySet())
            ?.toList()
            ?.sorted()
            ?: emptyList()
    }

    fun addStealthBlockedNumber(context: Context, number: String) {
        val current = prefs(context).getStringSet(KEY_NUMBERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(number)
        prefs(context).edit().putStringSet(KEY_NUMBERS, current).apply()
    }

    fun removeStealthBlockedNumber(context: Context, number: String) {
        val current = prefs(context).getStringSet(KEY_NUMBERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (current.remove(number)) {
            prefs(context).edit().putStringSet(KEY_NUMBERS, current).apply()
        }
    }
}
