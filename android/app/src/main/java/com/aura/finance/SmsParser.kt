package com.aura.finance

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

data class ParsedTransaction(
    val id: String,
    val title: String,
    val type: String, // "credit" or "debit"
    val amount: Double,
    val category: String,
    val date: String,
    val rawText: String,
    val balance: Double? = null,
    val accountTail: String? = null
)

/**
 * 100% Offline Local SMS Parser
 * Uses deterministic regex patterns to parse banking & payment SMS
 * from HDFC, SBI, ICICI, Axis, Kotak, Chase, Citi, UPI, Google Pay, PhonePe, Paytm, etc.
 */
object SmsParser {

    private val CREDIT_KEYWORDS = listOf(
        "credited", "credited with", "received", "deposited", "refunded", 
        "salary", "cashback", "added to", "inflow"
    )

    private val DEBIT_KEYWORDS = listOf(
        "debited", "spent", "paid", "sent to", "withdrawn", 
        "purchase", "transaction of", "transferred to", "vpa", "outflow"
    )

    // Regex patterns for monetary amounts
    private val AMOUNT_PATTERNS = listOf(
        Pattern.compile("""(?:Rs\.?|INR|USD|\$|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:INR|USD|Rs\.?|₹)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:debited\s+by|credited\s+with|spent)\s*([0-9,]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE)
    )

    // Regex for Account Number tails
    private val ACCOUNT_PATTERNS = listOf(
        Pattern.compile("""(?:a/c|acct|account|card|ending)\s*(?:no\.?|num)?\s*(?:[xX*]*([0-9]{4}))""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:[xX*]{2,})([0-9]{4})""")
    )

    // Regex for Available Balance
    private val BALANCE_PATTERNS = listOf(
        Pattern.compile("""(?:avl|avail|available|total)?\s*(?:bal|balance|limit)\s*(?:is|:)?\s*(?:Rs\.?|INR|USD|\$|₹)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE)
    )

    // Regex for Merchant / Beneficiary
    private val MERCHANT_PATTERNS = listOf(
        Pattern.compile("""(?:at|to|info)\s+([A-Za-z0-9\s\.\*\-\&]+?)(?:\s+on|\s+ref|\s+avl|\s+balance|\s+using|\.|\,|$)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:vpa|upi)\s+([A-Za-z0-9\.\@\_\-]+)""", Pattern.CASE_INSENSITIVE)
    )

    fun isFinancialSms(sender: String, body: String): Boolean {
        val lower = body.lowercase(Locale.ROOT)
        val hasAmount = lower.contains("rs") || lower.contains("inr") || lower.contains("usd") || lower.contains("$") || lower.contains("₹")
        val hasKeyword = CREDIT_KEYWORDS.any { lower.contains(it) } || DEBIT_KEYWORDS.any { lower.contains(it) }
        val hasAccount = lower.contains("a/c") || lower.contains("acct") || lower.contains("card") || lower.contains("upi") || lower.contains("vpa")
        
        return hasAmount && (hasKeyword || hasAccount)
    }

    fun parse(sender: String, body: String, timestamp: Long = System.currentTimeMillis()): ParsedTransaction? {
        if (!isFinancialSms(sender, body)) return null

        val lower = body.lowercase(Locale.ROOT)

        // 1. Determine Type (Credit vs Debit)
        var type = "debit"
        if (CREDIT_KEYWORDS.any { lower.contains(it) }) {
            type = "credit"
        }

        // 2. Extract Amount
        var amount = 0.0
        for (pattern in AMOUNT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val cleanStr = matcher.group(1)?.replace(",", "") ?: ""
                val parsed = cleanStr.toDoubleOrNull()
                if (parsed != null && parsed > 0.0) {
                    amount = parsed
                    break
                }
            }
        }
        if (amount <= 0.0) return null

        // 3. Extract Merchant
        var merchant = "Payment"
        for (pattern in MERCHANT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val rawMerchant = matcher.group(1)?.trim() ?: ""
                if (rawMerchant.length in 3..35 && !rawMerchant.lowercase(Locale.ROOT).contains("a/c")) {
                    merchant = rawMerchant
                    break
                }
            }
        }

        // 4. Extract Account Tail
        var accountTail: String? = null
        for (pattern in ACCOUNT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                accountTail = matcher.group(1)
                break
            }
        }

        // 5. Extract Available Balance
        var balance: Double? = null
        for (pattern in BALANCE_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val balStr = matcher.group(1)?.replace(",", "") ?: ""
                balance = balStr.toDoubleOrNull()
                if (balance != null) break
            }
        }

        // 6. Infer Category
        val category = inferCategory(merchant, body)

        // 7. Format Date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = sdf.format(Date(timestamp))

        val cleanTitle = formatMerchantTitle(merchant, type)

        return ParsedTransaction(
            id = "sms-" + timestamp + "-" + (amount.toInt()),
            title = cleanTitle,
            type = type,
            amount = amount,
            category = category,
            date = dateStr,
            rawText = body,
            balance = balance,
            accountTail = accountTail
        )
    }

    private fun inferCategory(merchant: String, body: String): String {
        val text = (merchant + " " + body).lowercase(Locale.ROOT)
        return when {
            text.contains("salary") || text.contains("payroll") || text.contains("wages") -> "Salary & Wages"
            text.contains("dividend") || text.contains("interest") || text.contains("zerodha") || text.contains("groww") || text.contains("vanguard") || text.contains("mutual fund") -> "Investment & Dividends"
            text.contains("swiggy") || text.contains("zomato") || text.contains("mcdonald") || text.contains("starbucks") || text.contains("cafe") || text.contains("food") || text.contains("restaurant") || text.contains("supermarket") || text.contains("groceries") || text.contains("blinkit") || text.contains("zepto") || text.contains("instamart") -> "Groceries & Food"
            text.contains("uber") || text.contains("ola") || text.contains("metro") || text.contains("fuel") || text.contains("petrol") || text.contains("hpcl") || text.contains("bpcl") || text.contains("shell") -> "Transportation"
            text.contains("rent") || text.contains("maintenance") || text.contains("lease") || text.contains("nobroker") -> "Housing & Rent"
            text.contains("electricity") || text.contains("water") || text.contains("bescom") || text.contains("airtel") || text.contains("jio") || text.contains("wifi") || text.contains("broadband") -> "Utilities & Bills"
            text.contains("netflix") || text.contains("spotify") || text.contains("prime") || text.contains("aws") || text.contains("google cloud") || text.contains("apple") || text.contains("github") -> "Tech & Subscriptions"
            text.contains("movie") || text.contains("bookmyshow") || text.contains("flight") || text.contains("makemytrip") || text.contains("airbnb") || text.contains("hotel") -> "Leisure & Travel"
            else -> if (text.contains("credited")) "Freelance & Consulting" else "Tech & Subscriptions"
        }
    }

    private fun formatMerchantTitle(merchant: String, type: String): String {
        if (merchant.isBlank() || merchant.equals("payment", ignoreCase = true)) {
            return if (type == "credit") "Bank Deposit" else "Card/UPI Payment"
        }
        return merchant.split(" ")
            .filter { it.isNotBlank() }
            .take(3)
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}
