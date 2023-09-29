package jiharp.asembleya.app

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesHelper {
    private const val PREFS_NAME = "app_prefs"
    private const val PREF_URL = "local_url"
    private const val PREF_AMOUNT = "game_record"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setUrl(context: Context, url: String) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putString(PREF_URL, url).apply()
    }

    fun getUrl(context: Context): String? {
        val prefs = getSharedPreferences(context)
        val savedUrl = prefs.getString(PREF_URL, "")
        return if (savedUrl.isNullOrEmpty()) null else savedUrl
    }

    fun setRecord(context: Context, amount: Int) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putInt(PREF_AMOUNT, amount).apply()
    }

    fun getRecord(context: Context): Int {
        val prefs = getSharedPreferences(context)
        return prefs.getInt(PREF_AMOUNT, 0)
    }

}
