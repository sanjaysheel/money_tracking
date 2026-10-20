package com.aura.finance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return

            for (sms in messages) {
                val sender = sms.displayOriginatingAddress ?: ""
                val body = sms.displayMessageBody ?: ""
                val timestamp = sms.timestampMillis

                Log.d("AuraSmsReceiver", "Intercepted SMS from: $sender")

                val parsed = SmsParser.parse(sender, body, timestamp)
                if (parsed != null) {
                    Log.d("AuraSmsReceiver", "Parsed Bank SMS: ${parsed.type.uppercase()} ${parsed.amount} at ${parsed.title}")

                    // Send local broadcast to MainActivity to update WebView immediately
                    val updateIntent = Intent("com.aura.finance.NEW_TRANSACTION").apply {
                        putExtra("id", parsed.id)
                        putExtra("title", parsed.title)
                        putExtra("type", parsed.type)
                        putExtra("amount", parsed.amount)
                        putExtra("category", parsed.category)
                        putExtra("date", parsed.date)
                        putExtra("rawText", parsed.rawText)
                        putExtra("balance", parsed.balance ?: -1.0)
                    }
                    context.sendBroadcast(updateIntent)
                }
            }
        }
    }
}
