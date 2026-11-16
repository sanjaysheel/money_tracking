package com.aura.finance

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.Settings
import android.webkit.JavascriptInterface
import org.json.JSONArray
import org.json.JSONObject

class WebAppInterface(private val activity: MainActivity) {

    private val dbHelper = TransactionDbHelper(activity)
    private val cardPrefs: SharedPreferences = activity.getSharedPreferences("aura_card_mappings", Context.MODE_PRIVATE)
    private val syncPrefs: SharedPreferences = activity.getSharedPreferences("aura_settings", Context.MODE_PRIVATE)

    @JavascriptInterface
    fun isAndroidApp(): Boolean {
        return true
    }

    @JavascriptInterface
    fun setSystemBarTheme(isDark: Boolean) {
        activity.runOnUiThread {
            activity.setSystemBarTheme(isDark)
        }
    }

    @JavascriptInterface
    fun hideKeyboard() {
        activity.hideKeyboard()
    }

    // ==========================================
    // NATIVE SQLITE DATABASE CRUD BRIDGE
    // ==========================================
    @JavascriptInterface
    fun dbSaveTransaction(jsonStr: String): Boolean {
        return try {
            val obj = JSONObject(jsonStr)
            dbHelper.insertOrUpdateTransaction(obj) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbSaveTransactionsBatch(jsonArrayStr: String): Int {
        return try {
            val arr = JSONArray(jsonArrayStr)
            dbHelper.insertTransactionsBatch(arr)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    @JavascriptInterface
    fun dbGetAllTransactions(): String {
        return try {
            dbHelper.getAllTransactions().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }

    @JavascriptInterface
    fun dbDeleteTransaction(id: String): Boolean {
        return try {
            dbHelper.deleteTransaction(id) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbUpdateNote(id: String, note: String): Boolean {
        return try {
            dbHelper.updateNote(id, note) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbUpdateCategory(id: String, category: String): Boolean {
        return try {
            dbHelper.updateCategory(id, category) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbClearAllTransactions(): Boolean {
        return try {
            dbHelper.clearAllTransactions()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbPurgePromotionalTransactions(): Int {
        return try {
            dbHelper.purgeInvalidAndPromotional()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    // Schedules SQLite
    @JavascriptInterface
    fun dbSaveSchedule(jsonStr: String): Boolean {
        return try {
            val obj = JSONObject(jsonStr)
            dbHelper.saveSchedule(obj) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbDeleteSchedule(id: String): Boolean {
        return try {
            dbHelper.deleteSchedule(id) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbGetAllSchedules(): String {
        return try {
            dbHelper.getAllSchedules().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }

    // ==========================================
    // BUDGETS NATIVE BRIDGE
    // ==========================================
    @JavascriptInterface
    fun dbSaveBudget(jsonStr: String): Boolean {
        return try {
            val obj = JSONObject(jsonStr)
            dbHelper.saveBudget(obj) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbDeleteBudget(category: String): Boolean {
        return try {
            dbHelper.deleteBudget(category) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbGetAllBudgets(): String {
        return try {
            dbHelper.getAllBudgets().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }

    // ==========================================
    // SAVINGS GOALS NATIVE BRIDGE
    // ==========================================
    @JavascriptInterface
    fun dbSaveSavingsGoal(jsonStr: String): Boolean {
        return try {
            val obj = JSONObject(jsonStr)
            dbHelper.saveSavingsGoal(obj) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbDeleteSavingsGoal(id: String): Boolean {
        return try {
            dbHelper.deleteSavingsGoal(id) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbGetAllSavingsGoals(): String {
        return try {
            dbHelper.getAllSavingsGoals().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }

    @JavascriptInterface
    fun dbAddGoalContribution(jsonStr: String): Boolean {
        return try {
            val obj = JSONObject(jsonStr)
            dbHelper.addGoalContribution(obj) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbGetGoalContributions(goalId: String): String {
        return try {
            dbHelper.getGoalContributions(goalId).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }

    // ==========================================
    // RECURRING SUBSCRIPTIONS BRIDGE
    // ==========================================
    @JavascriptInterface
    fun dbDismissRecurring(signature: String): Boolean {
        return try {
            dbHelper.dismissRecurring(signature) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbGetDismissedRecurring(): String {
        return try {
            dbHelper.getDismissedRecurring().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }

    // ==========================================
    // ADAPTIVE CATEGORY RULES (AI Local Memory)
    // ==========================================
    @JavascriptInterface
    fun dbSaveCategoryRule(merchantKey: String, category: String): Boolean {
        return try {
            dbHelper.saveCategoryRule(merchantKey, category) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun dbGetAllCategoryRules(): String {
        return try {
            val map = dbHelper.getAllCategoryRules()
            val json = JSONObject()
            for ((k, v) in map) {
                json.put(k, v)
            }
            json.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
    }

    @JavascriptInterface
    fun dbDeleteCategoryRule(merchantKey: String): Boolean {
        return try {
            dbHelper.deleteCategoryRule(merchantKey) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ==========================================
    // ATOMIC FULL RESTORE FROM JSON
    // ==========================================
    @JavascriptInterface
    fun dbRestoreDatabaseFromJson(jsonStr: String): String {
        return try {
            val obj = JSONObject(jsonStr)
            dbHelper.restoreFullDatabase(obj).toString()
        } catch (e: Exception) {
            val err = JSONObject().apply {
                put("success", false)
                put("error", e.message ?: "Invalid JSON backup data")
            }
            err.toString()
        }
    }

    @JavascriptInterface
    fun isDeviceSecure(): Boolean {
        return try {
            val km = activity.getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
            km?.isDeviceSecure ?: false
        } catch (e: Exception) {
            false
        }
    }

    @JavascriptInterface
    fun getBiometricStatus(): String {
        return activity.getBiometricStatus().toString()
    }

    @JavascriptInterface
    fun authenticateBiometrics() {
        activity.authenticateBiometrics()
    }

    @JavascriptInterface
    fun openBiometricEnrollSettings() {
        activity.openBiometricEnrollSettings()
    }

    @JavascriptInterface
    fun hasSmsPermission(): Boolean {
        return activity.hasSmsPermissions()
    }

    @JavascriptInterface
    fun requestSmsPermission() {
        activity.runOnUiThread {
            activity.checkAndRequestSmsPermissions()
        }
    }

    @JavascriptInterface
    fun openAppSettings() {
        activity.runOnUiThread {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activity.packageName, null)
                }
                activity.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JavascriptInterface
    fun setRealtimeSync(enabled: Boolean) {
        syncPrefs.edit().putBoolean("realtime_sms_sync", enabled).apply()
    }

    @JavascriptInterface
    fun setAutoScanOnOpen(enabled: Boolean) {
        syncPrefs.edit().putBoolean("autoscan_on_open", enabled).apply()
    }

    @JavascriptInterface
    fun getSyncSettings(): String {
        val obj = JSONObject().apply {
            put("realtimeSync", syncPrefs.getBoolean("realtime_sms_sync", true))
            put("autoScanOnOpen", syncPrefs.getBoolean("autoscan_on_open", true))
        }
        return obj.toString()
    }

    @JavascriptInterface
    fun exportFile(filename: String, content: String, mimeType: String) {
        activity.runOnUiThread {
            activity.shareOrSaveFile(filename, content, mimeType)
        }
    }

    @JavascriptInterface
    fun saveCardMapping(tailDigits: String, aliasName: String): Boolean {
        val cleanTail = tailDigits.trim().replace("[^0-9]".toRegex(), "")
        val cleanAlias = aliasName.trim()
        if (cleanTail.length >= 2 && cleanAlias.isNotBlank()) {
            cardPrefs.edit().putString(cleanTail, cleanAlias).apply()
            return true
        }
        return false
    }

    @JavascriptInterface
    fun deleteCardMapping(tailDigits: String): Boolean {
        val cleanTail = tailDigits.trim().replace("[^0-9]".toRegex(), "")
        if (cardPrefs.contains(cleanTail)) {
            cardPrefs.edit().remove(cleanTail).apply()
            return true
        }
        return false
    }

    @JavascriptInterface
    fun getCardMappings(): String {
        val all = cardPrefs.all
        val obj = JSONObject()
        for ((key, value) in all) {
            if (value is String) {
                obj.put(key, value)
            }
        }
        return obj.toString()
    }

    @JavascriptInterface
    fun syncNow() {
        activity.runOnUiThread {
            activity.checkAndRequestSmsPermissions()
        }
    }

    @JavascriptInterface
    fun triggerInboxSync() {
        activity.runOnUiThread {
            activity.triggerInboxSync()
        }
    }

    @JavascriptInterface
    fun scanInboxSms(): String {
        if (!activity.hasSmsPermissions()) {
            activity.runOnUiThread {
                activity.checkAndRequestSmsPermissions()
            }
            return "[]"
        }
        val list = SmsScanner.scanInbox(activity, limit = 500)
        val jsonArray = JSONArray()

        for (tx in list) {
            val tail = tx.accountTail ?: ""
            val savedAlias = if (tail.isNotBlank()) cardPrefs.getString(tail, null) else null
            val alias = savedAlias ?: tx.cardAlias

            val obj = JSONObject().apply {
                put("id", tx.id)
                put("title", tx.title)
                put("type", tx.type)
                put("amount", tx.amount)
                put("category", tx.category)
                put("date", tx.date)
                put("rawText", tx.rawText)
                put("paymentMethod", tx.paymentMethod)
                put("currencySymbol", tx.currencySymbol)
                put("dedupKey", tx.dedupKey)
                if (tx.balance != null) put("balance", tx.balance)
                if (tail.isNotBlank()) put("accountTail", tail)
                if (alias != null) put("cardAlias", alias)
            }
            jsonArray.put(obj)
        }

        try {
            dbHelper.insertTransactionsBatch(jsonArray)
        } catch (ignored: Exception) {}

        return jsonArray.toString()
    }

    @JavascriptInterface
    fun parseCustomSms(smsText: String): String {
        val parsed = SmsParser.parse("MANUAL", smsText, System.currentTimeMillis())
        if (parsed != null) {
            val tail = parsed.accountTail ?: ""
            val savedAlias = if (tail.isNotBlank()) cardPrefs.getString(tail, null) else null
            val alias = savedAlias ?: parsed.cardAlias

            val obj = JSONObject().apply {
                put("id", parsed.id)
                put("title", parsed.title)
                put("type", parsed.type)
                put("amount", parsed.amount)
                put("category", parsed.category)
                put("date", parsed.date)
                put("rawText", parsed.rawText)
                put("paymentMethod", parsed.paymentMethod)
                put("currencySymbol", parsed.currencySymbol)
                put("dedupKey", parsed.dedupKey)
                if (parsed.balance != null) put("balance", parsed.balance)
                if (tail.isNotBlank()) put("accountTail", tail)
                if (alias != null) put("cardAlias", alias)
            }
            return obj.toString()
        }
        return "{}"
    }
}
