package com.aura.finance

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log

object SmsScanner {

    /**
     * Reads past banking SMS messages from the phone's SMS inbox (100% offline).
     * Includes automatic deduplication filtering by transaction fingerprint.
     */
    fun scanInbox(context: Context, limit: Int = 1000): List<ParsedTransaction> {
        val transactions = mutableListOf<ParsedTransaction>()
        val seenDedupKeys = HashSet<String>()
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI

        val projection = arrayOf(
            Telephony.Sms.Inbox._ID,
            Telephony.Sms.Inbox.ADDRESS,
            Telephony.Sms.Inbox.BODY,
            Telephony.Sms.Inbox.DATE
        )

        var cursor: android.database.Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.Inbox.DATE} DESC"
            )

            // Fallback 1: content://sms/inbox string URI
            if (cursor == null || cursor.count == 0) {
                cursor?.close()
                cursor = context.contentResolver.query(
                    Uri.parse("content://sms/inbox"),
                    projection,
                    null,
                    null,
                    "date DESC"
                )
            }

            // Fallback 2: Telephony.Sms.CONTENT_URI with type = 1
            if (cursor == null || cursor.count == 0) {
                cursor?.close()
                cursor = context.contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    projection,
                    "${Telephony.Sms.TYPE} = 1", // MESSAGE_TYPE_INBOX
                    null,
                    "${Telephony.Sms.DATE} DESC"
                )
            }

            // Fallback 3: content://sms/
            if (cursor == null || cursor.count == 0) {
                cursor?.close()
                cursor = context.contentResolver.query(
                    Uri.parse("content://sms/"),
                    projection,
                    "type = 1",
                    null,
                    "date DESC"
                )
            }

            cursor?.use { c ->
                val addressIdx = c.getColumnIndex(Telephony.Sms.Inbox.ADDRESS)
                val bodyIdx = c.getColumnIndex(Telephony.Sms.Inbox.BODY)
                val dateIdx = c.getColumnIndex(Telephony.Sms.Inbox.DATE)

                var count = 0
                var scannedMessages = 0
                val maxScanDepth = 6000

                val categoryOverrides = try { TransactionDbHelper(context).getAllCategoryRules() } catch (e: Exception) { emptyMap<String, String>() }

                while (c.moveToNext() && count < limit && scannedMessages < maxScanDepth) {
                    scannedMessages++
                    val address = if (addressIdx >= 0) c.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx >= 0) c.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) c.getLong(dateIdx) else System.currentTimeMillis()

                    // Strictly ignore any MMS / multimedia message payloads (SMS only)
                    val lowerBody = body.lowercase(java.util.Locale.ROOT)
                    if (lowerBody.contains("mms") || lowerBody.contains("multimedia message") || 
                        lowerBody.contains("application/vnd.wap.mms-message") || lowerBody.contains("mms received") ||
                        lowerBody.contains("download mms")) {
                        continue
                    }

                    val parsed = SmsParser.parse(address, body, date, categoryOverrides)
                    if (parsed != null) {
                        // Intelligent Deduplication check
                        val key = if (parsed.dedupKey.isNotBlank()) parsed.dedupKey else parsed.id
                        if (!seenDedupKeys.contains(key)) {
                            seenDedupKeys.add(key)
                            transactions.add(parsed)
                            count++
                        }
                    }
                }
                Log.i("AuraSmsScanner", "Scanned $scannedMessages SMS messages in inbox, extracted $count valid transactions.")
            }
        } catch (e: Exception) {
            Log.e("AuraSmsScanner", "Error reading SMS inbox: ${e.message}")
        } finally {
            try { cursor?.close() } catch (ignored: Exception) {}
        }

        return transactions
    }
}
