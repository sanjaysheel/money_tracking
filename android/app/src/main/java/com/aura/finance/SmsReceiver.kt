package com.aura.finance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val prefs = context.getSharedPreferences("aura_settings", Context.MODE_PRIVATE)
            val realtimeEnabled = prefs.getBoolean("realtime_sms_sync", true)

            if (!realtimeEnabled) {
                Log.d("AuraSmsReceiver", "Real-time SMS listener is paused by user setting.")
                return
            }

            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
            val categoryOverrides = try { TransactionDbHelper(context).getAllCategoryRules() } catch (e: Exception) { emptyMap<String, String>() }

            for (sms in messages) {
                val sender = sms.displayOriginatingAddress ?: ""
                val body = sms.displayMessageBody ?: ""
                val timestamp = sms.timestampMillis

                Log.d("AuraSmsReceiver", "Intercepted SMS from: $sender")

                val parsed = SmsParser.parse(sender, body, timestamp, categoryOverrides)
                if (parsed != null) {
                    Log.d("AuraSmsReceiver", "Parsed Bank SMS: ${parsed.type.uppercase()} ${parsed.amount} at ${parsed.title}")

                    // Send targeted intra-app broadcast to MainActivity to update WebView immediately
                    val updateIntent = Intent("com.aura.finance.NEW_TRANSACTION").apply {
                        setPackage(context.packageName)
                        putExtra("id", parsed.id)
                        putExtra("title", parsed.title)
                        putExtra("type", parsed.type)
                        putExtra("amount", parsed.amount)
                        putExtra("category", parsed.category)
                        putExtra("date", parsed.date)
                        putExtra("rawText", parsed.rawText)
                        putExtra("balance", parsed.balance ?: -1.0)
                        putExtra("accountTail", parsed.accountTail ?: "")
                        putExtra("paymentMethod", parsed.paymentMethod)
                        putExtra("currencySymbol", parsed.currencySymbol)
                        putExtra("dedupKey", parsed.dedupKey)
                    }
                    context.sendBroadcast(updateIntent)
                }
            }
        }
    }
}
