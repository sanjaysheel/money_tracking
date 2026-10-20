package com.aura.finance

import android.webkit.JavascriptInterface
import org.json.JSONArray
import org.json.JSONObject

class WebAppInterface(private val activity: MainActivity) {

    @JavascriptInterface
    fun isAndroidApp(): Boolean {
        return true
    }

    @JavascriptInterface
    fun requestSmsPermission() {
        activity.runOnUiThread {
            activity.checkAndRequestSmsPermissions()
        }
    }

    @JavascriptInterface
    fun scanInboxSms(): String {
        val list = SmsScanner.scanInbox(activity, limit = 80)
        val jsonArray = JSONArray()

        for (tx in list) {
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("title", tx.title)
                put("type", tx.type)
                put("amount", tx.amount)
                put("category", tx.category)
                put("date", tx.date)
                put("rawText", tx.rawText)
                if (tx.balance != null) put("balance", tx.balance)
                if (tx.accountTail != null) put("accountTail", tx.accountTail)
            }
            jsonArray.put(obj)
        }

        return jsonArray.toString()
    }

    @JavascriptInterface
    fun parseCustomSms(smsText: String): String {
        val parsed = SmsParser.parse("MANUAL", smsText, System.currentTimeMillis())
        if (parsed != null) {
            val obj = JSONObject().apply {
                put("id", parsed.id)
                put("title", parsed.title)
                put("type", parsed.type)
                put("amount", parsed.amount)
                put("category", parsed.category)
                put("date", parsed.date)
                put("rawText", parsed.rawText)
                if (parsed.balance != null) put("balance", parsed.balance)
            }
            return obj.toString()
        }
        return "{}"
    }
}
