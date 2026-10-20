package com.aura.finance

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log

object SmsScanner {

    /**
     * Reads past banking SMS messages from the phone's SMS inbox (100% offline).
     */
    fun scanInbox(context: Context, limit: Int = 100): List<ParsedTransaction> {
        val transactions = mutableListOf<ParsedTransaction>()
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI

        val projection = arrayOf(
            Telephony.Sms.Inbox._ID,
            Telephony.Sms.Inbox.ADDRESS,
            Telephony.Sms.Inbox.BODY,
            Telephony.Sms.Inbox.DATE
        )

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.Inbox.DATE} DESC"
            )

            cursor?.use { c ->
                val addressIdx = c.getColumnIndex(Telephony.Sms.Inbox.ADDRESS)
                val bodyIdx = c.getColumnIndex(Telephony.Sms.Inbox.BODY)
                val dateIdx = c.getColumnIndex(Telephony.Sms.Inbox.DATE)

                var count = 0
                while (c.moveToNext() && count < limit) {
                    val address = if (addressIdx >= 0) c.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx >= 0) c.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) c.getLong(dateIdx) else System.currentTimeMillis()

                    val parsed = SmsParser.parse(address, body, date)
                    if (parsed != null) {
                        transactions.add(parsed)
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AuraSmsScanner", "Error reading SMS inbox: ${e.message}")
        }

        return transactions
    }
}
