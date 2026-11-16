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
    val accountTail: String? = null,
    val paymentMethod: String = "UPI/Card",
    val currencySymbol: String = "₹",
    val dedupKey: String = "",
    val cardAlias: String? = null
)

/**
 * 100% Offline Local Intelligent SMS Parser & Extractor Engine
 *
 * Deeply optimized for Indian & Global banking SMS formats:
 *  - E-commerce & Quick-Commerce: Zepto (Kiranakart), Blinkit (Grofers), Swiggy, Zomato, Instamart
 *  - UPI & Wallets: GPay, PhonePe, Paytm, CRED, BHIM, Amazon Pay
 *  - Banks: HDFC, SBI, ICICI, Axis, Kotak, PNB, BOB, Canara, Union, IDFC, IndusInd, Federal, RBL
 *  - Chinese OEM ROMs: Vivo (Funtouch/OriginOS), Xiaomi (MIUI/HyperOS), Oppo/Realme (ColorOS), OnePlus (OxygenOS)
 * Extracts:
 *  - Credit vs Debit
 *  - Exact Monetary Amount & Currency (₹, $, €, £)
 *  - Payment Method (UPI, Credit Card, Debit Card, NetBanking, ATM, Direct Deposit)
 *  - Account / Card Last Digits (e.g. 1190)
 *  - Available Balance
 *  - Merchant / Beneficiary / Platform
 *  - Smart Deduplication Key
 */
object SmsParser {

    private val CREDIT_KEYWORDS = listOf(
        "credited", "credited with", "received", "deposited", "refunded", 
        "salary", "cashback", "added to", "inflow", "credit alert", "received rs", "credited by"
    )

    private val DEBIT_KEYWORDS = listOf(
        "debited", "spent", "paid", "sent to", "withdrawn", 
        "purchase", "transaction of", "transferred to", "vpa", "outflow",
        "debit alert", "spent rs", "debited by", "used at"
    )

    // Comprehensive known brand mapping from legal entity names, VPAs, and DLT sender tags
    private val MERCHANT_ENTITY_MAP = mapOf(
        "kiranakart" to "Zepto",
        "kiranakart tech" to "Zepto",
        "kiranakart technologies" to "Zepto",
        "zepto" to "Zepto",
        "grofers" to "Blinkit",
        "blink commerce" to "Blinkit",
        "blinkit" to "Blinkit",
        "bundl" to "Swiggy",
        "bundl tech" to "Swiggy",
        "bundl technologies" to "Swiggy",
        "swiggy" to "Swiggy",
        "instamart" to "Swiggy Instamart",
        "zomato" to "Zomato",
        "eternal" to "Zomato",
        "ani technologies" to "Ola Cabs",
        "ola" to "Ola Cabs",
        "uber india" to "Uber",
        "uber" to "Uber",
        "rapido" to "Rapido",
        "roppen" to "Rapido",
        "bigbasket" to "BigBasket",
        "innovative retail" to "BigBasket",
        "bbdaily" to "BB Daily",
        "dmart" to "DMart",
        "avenue supermarts" to "DMart",
        "amazon" to "Amazon",
        "flipkart" to "Flipkart",
        "myntra" to "Myntra",
        "meesho" to "Meesho",
        "nykaa" to "Nykaa",
        "ajio" to "Ajio",
        "reliance retail" to "Reliance",
        "jio mart" to "JioMart",
        "jiomart" to "JioMart",
        "tata neu" to "Tata Neu",
        "tataneu" to "Tata Neu",
        "1mg" to "Tata 1mg",
        "apollo" to "Apollo Pharmacy",
        "pharmeasy" to "PharmEasy",
        "netmeds" to "Netmeds",
        "cultfit" to "Cult.fit",
        "curefit" to "Cult.fit",
        "starbucks" to "Starbucks",
        "tata starbucks" to "Starbucks",
        "mcdonalds" to "McDonald's",
        "hardcastle" to "McDonald's",
        "dominos" to "Domino's Pizza",
        "jubilant" to "Domino's Pizza",
        "subway" to "Subway",
        "kfc" to "KFC",
        "pizza hut" to "Pizza Hut",
        "devyani" to "KFC / Pizza Hut",
        "irctc" to "IRCTC",
        "makemytrip" to "MakeMyTrip",
        "goibibo" to "Goibibo",
        "cleartrip" to "Cleartrip",
        "easemytrip" to "EaseMyTrip",
        "indigo" to "IndiGo Airlines",
        "interglobe" to "IndiGo Airlines",
        "air india" to "Air India",
        "bookmyshow" to "BookMyShow",
        "bigtree" to "BookMyShow",
        "pvr" to "PVR Cinemas",
        "inox" to "INOX"
    )

    // JUNK words that must NEVER be treated as a recipient or merchant name
    private val JUNK_WORDS = setOf(
        "ref", "refno", "rrn", "txn", "txnid", "upi", "vpa", "imps", "neft", "rtgs",
        "bank", "account", "acct", "user", "payment", "info", "alert", "balance",
        "avl", "bal", "amount", "rs", "inr", "card", "debit", "credit", "transfer", "trf", "id", "no",
        "you", "your", "customer", "dear", "done", "not", "call", "sms", "service", "tollfree"
    )

    // Regex patterns for monetary amounts (ordered by specificity)
    private val AMOUNT_PATTERNS = listOf(
        // Explicit action + amount: "debited by Rs 249", "paid Rs. 149", "payment of Rs 349", "spent Rs 500", "charged USD 20"
        Pattern.compile("""(?:debited\s+(?:by|for|with)?|credited\s+(?:by|for|with)?|spent|paid|payment\s+of|sent|transferred|charged|withdrawn)\s*(?:Rs\.?|INR|USD|\$|₹|EUR|€|GBP|£)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE),
        // Standard currency prefix: "Rs. 249.00", "INR 199", "₹80"
        Pattern.compile("""(?:Rs\.?|INR|USD|\$|₹|EUR|€|GBP|£)\s*([0-9,]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE),
        // Currency suffix: "149.00 INR", "249 Rs"
        Pattern.compile("""([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:INR|USD|Rs\.?|₹|EUR|€|GBP|£)""", Pattern.CASE_INSENSITIVE),
        // Suffix slash-dash: "500/-", "1200.00/-"
        Pattern.compile("""([0-9,]+(?:\.[0-9]{1,2})?)/-""", Pattern.CASE_INSENSITIVE)
    )

    // Regex to strip payment gateway prefixes/suffixes like .payu, .rzp, .razorpay, .cashfree
    private val GATEWAY_STRIP_REGEX = Pattern.compile("""\.(?:payu|rzp|razorpay|cashfree|billdesk|pty|ccavenue|pine|paytm|airtel|gpay|phonepe)$""", Pattern.CASE_INSENSITIVE)

    fun cleanVpaHandle(handle: String): String {
        val stripped = GATEWAY_STRIP_REGEX.matcher(handle.trim()).replaceFirst("")
        val parts = stripped.split(".", "_", "-").filter { it.isNotBlank() && !JUNK_WORDS.contains(it.lowercase(Locale.ROOT)) && !it.all { c -> c.isDigit() } }
        if (parts.isNotEmpty()) {
            val candidate = parts[0]
            for ((token, brand) in MERCHANT_ENTITY_MAP) {
                if (candidate.lowercase(Locale.ROOT).contains(token)) {
                    return brand
                }
            }
            return candidate.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }
        return handle
    }

    // Patterns to detect SENDEE / BENEFICIARY accounts (to be STRICTLY EXCLUDED from user's card list)
    private val SENDEE_ACCOUNT_PATTERNS = listOf(
        Pattern.compile("""(?:to|transferred\s+to|trf\s+to|transfer\s+to|sent\s+to|beneficiary|bene|payee)\s+(?:bank\s+)?(?:a\/c|acct|account)\s*(?:no\.?|num)?\s*(?:[xX*.]*([0-9]{4}))""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:to\s+a\/c\s+no\.?|to\s+account\s+no\.?|to\s+a\/c)\s*(?:[xX*.]*([0-9]{4}))""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:in\s+favour\s+of|favouring)\s+.*?(?:a\/c|acct|account)\s*(?:no\.?|num)?\s*(?:[xX*.]*([0-9]{4}))""", Pattern.CASE_INSENSITIVE)
    )

    // Patterns for USER'S OWN SOURCE ACCOUNT (Account from which money was sent / debited / credited)
    private val USER_ACCOUNT_PATTERNS = listOf(
        // "from A/c ...7812", "from your A/c XX1234", "debited from A/c XX4821"
        Pattern.compile("""\b(?:from|from\s+your|debited\s+from|drawn\s+on)\s+(?:bank\s+)?(?:a\/c|acct|account|card)\s*(?:no\.?|num)?\s*(?:[xX*.]*([0-9]{4}))""", Pattern.CASE_INSENSITIVE),
        // "A/c XX4821 debited", "your A/c 4821 is debited"
        Pattern.compile("""\b(?:your\s+)?(?:a\/c|acct|account|card)\s*(?:no\.?|num)?\s*(?:[xX*.]*([0-9]{4}))\s*(?:is|has\s+been)?\s*debited""", Pattern.CASE_INSENSITIVE),
        // "debited for Rs 500 from A/c 7812"
        Pattern.compile("""\bdebited\s+(?:for|by|with)?\s*(?:rs\.?|inr|usd|\$|₹)?\s*[\d,\.]+\s*(?:from|from\s+your)?\s*(?:a\/c|acct|account|card)\s*(?:no\.?|num)?\s*(?:[xX*.]*([0-9]{4}))""", Pattern.CASE_INSENSITIVE),
        // Credit card / debit card: "card ending 9012", "spent on card XX4821", "card XX1234"
        Pattern.compile("""\b(?:on\s+card|spent\s+on\s+card|using\s+card|card\s+ending\s+in|card\s+ending\s+with|ending\s+in|ending\s+with|card\s+no\.?)\s*(?:[xX*.]*([0-9]{4}))""", Pattern.CASE_INSENSITIVE),
        // Bank specific source account: "SBI A/c ...7812", "HDFC A/c XX4821"
        Pattern.compile("""\b(?:sbi|hdfc|icici|axis|kotak|pnb|bob|bank)\s+(?:a\/c|acct|account|card)\s*(?:no\.?|num)?\s*(?:[xX*.]*([0-9]{4}))""", Pattern.CASE_INSENSITIVE),
        // HDFC / Bank SMS BLOCK patterns: "SMS BLOCKPCC 3229", "SMS BLOCKCC 1234", "BLOCK card 4321"
        Pattern.compile("""\b(?:sms\s+block[a-z]{0,6}\s+|block\s+(?:your\s+)?card\s*(?:no\.?)?\s*|block[a-z]{0,6}\s+)([0-9]{4})\b""", Pattern.CASE_INSENSITIVE),
        // Card named in text with 4 digits: "Credit Card ending 3229", "Card 3229"
        Pattern.compile("""\b(?:card\s*(?:ending)?\s*|ending\s*)([0-9]{4})\b""", Pattern.CASE_INSENSITIVE),
        // Incoming credit: "Your A/c 9012 is credited", "credited to your A/c 9012"
        Pattern.compile("""\b(?:to\s+your|in\s+your|your)\s+(?:a\/c|acct|account)\s*(?:no\.?|num)?\s*(?:[xX*.]*([0-9]{4}))\s*(?:is|has\s+been)?\s*credited""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\bcredited\s+(?:to|in|into)?\s*(?:your\s+)?(?:a\/c|acct|account)\s*(?:no\.?|num)?\s*(?:[xX*.]*([0-9]{4}))""", Pattern.CASE_INSENSITIVE),
        // Standard user A/c followed by available balance: "A/c XX1234 ... Avl Bal"
        Pattern.compile("""\b(?:a\/c|acct|account)\s*(?:no\.?|num)?\s*(?:[xX*.]*([0-9]{4}))(?=.*(?:avl|avail|balance|bal))""", Pattern.CASE_INSENSITIVE)
    )

    // Regex for Available Balance
    private val BALANCE_PATTERNS = listOf(
        Pattern.compile("""(?:avl|avail|available|total|clr)?\s*(?:bal|balance|limit)\s*(?:is|:)?\s*(?:Rs\.?|INR|USD|\$|₹|EUR|€|GBP|£)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", Pattern.CASE_INSENSITIVE)
    )

    // Enhanced Universal Recipient & Merchant Patterns
    private val RECIPIENT_PATTERNS = listOf(
        // 1. NPCI UPI Standard format: "UPI/42910291029/Swiggy" or "UPI/P2M/4291829102/BundlTechnologies" or "UPI/DR/123/Merchant"
        Pattern.compile("""\bUPI(?:/P2M|/P2P|/DR|/CR)?/\d+/([A-Za-z0-9_\.\s\-]{2,30}?)(?:/|\s|\.|\,|$)""", Pattern.CASE_INSENSITIVE),

        // 2. ICICI / Axis UPI Tag format: "UPI:Zepto:zepto@icici:4291829102" or "UPI:FreshBites:fresh@hdfc:123"
        Pattern.compile("""\bUPI[:\s]+([A-Za-z0-9\s\.\&]{2,30}?):[A-Za-z0-9_\.\-]+@[A-Za-z0-9]+""", Pattern.CASE_INSENSITIVE),

        // 3. VPA with human name in parens: "to VPA abc@xyz (FreshBites Cafe)"
        Pattern.compile("""(?:to\s+(?:vpa\s+)?[^\s\(\)]+)\s*\(([^)]{2,30})\)""", Pattern.CASE_INSENSITIVE),

        // 4. "towards <Merchant>" e.g. "towards Swiggy via UPI", "towards Kiranakart Technologies"
        Pattern.compile("""(?:towards)\s+([A-Za-z0-9\s\.\&]{2,35}?)(?:\s+(?:ref|refno|rrn|upi|vpa|txn|txnid|on|via|using|avl|bal|from|a\/c|account)|\.|\,|$)""", Pattern.CASE_INSENSITIVE),

        // 5. "for <order at / purchase at> <Merchant>" e.g. "for Swiggy order", "for Blinkit order"
        Pattern.compile("""(?:for\s+(?:your\s+)?(?:order\s+(?:at|on|for)\s+|purchase\s+(?:at|on)\s+)?)([A-Za-z0-9\s\.\&]{2,30}?)(?:\s+(?:order|ref|refno|rrn|upi|vpa|txn|on|via|using)|\.|\,|$)""", Pattern.CASE_INSENSITIVE),

        // 6. "paid at / spent at / used at / purchase at / at <Merchant>"
        Pattern.compile("""(?:at|used\s+at|spent\s+at|purchase\s+at|paid\s+at)\s+([A-Za-z0-9\s\.\*\-\&@]{2,40}?)(?:\s+(?:ref|refno|rrn|on|via|using|avl|bal|from|a\/c|account)|\.|\,|$)""", Pattern.CASE_INSENSITIVE),

        // 7. "paid to / transferred to / sent to / to <Merchant>"
        Pattern.compile("""(?:trf\s+to|transfer(?:red)?\s+to|paid\s+to|sent\s+to)\s+(?:mr\.?|mrs\.?|ms\.?|dr\.?)?\s*([A-Za-z][A-Za-z0-9\s\.\&]{1,30}?)(?:\s+(?:ref|refno|rrn|upi|vpa|txn|txnid|on|via|using|avl|bal|from|a\/c|account)|\.|\,|$)""", Pattern.CASE_INSENSITIVE),

        // 8. Direct "to <Merchant>" (excluding common junk pronouns)
        Pattern.compile("""(?:\bto)\s+(?:mr\.?|mrs\.?|ms\.?|dr\.?)?\s*([A-Za-z][A-Za-z0-9\s\.\&]{1,30}?)(?:\s+(?:ref|refno|rrn|upi|vpa|txn|txnid|on|via|using|avl|bal|from|a\/c|account)|\.|\,|$)""", Pattern.CASE_INSENSITIVE),

        // 9. Credit "by/from <Employer>" (excluding 'done by you')
        Pattern.compile("""(?:credited\s+by|from)\s+([A-Za-z][A-Za-z0-9\s\.\&]{2,30}?)(?:\s+(?:salary|wages|payroll|ref|on|via|avl|bal)|\.|\,|$)""", Pattern.CASE_INSENSITIVE),

        // 10. Direct VPA handle with intervening words: "to GPay user rahul@okhdfc" or "vpa swiggy.payu@icici"
        Pattern.compile("""(?:to|paid\s+to|sent\s+to|trf\s+to|transfer\s+to|vpa).*?\b([A-Za-z0-9\.\_\-]+)@[A-Za-z0-9]+""", Pattern.CASE_INSENSITIVE)
    )

    private val STRICT_OTP_PATTERNS = listOf(
        Pattern.compile("""(?:\bis\s+your\s+otp|\botp\s+is\s+\d+|\bsecret\s+otp\b|\bone\s*time\s*password\s+is\b|\bverification\s*code\s+is\b|\buse\s+(?:this\s+)?otp\b|\blogin\s+otp\b|\buse\s+code\s+\d+\s+as\b)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:to\s+authorize\s+transaction|request\s+to\s+approve|enter\s+otp\s+to\s+complete)""", Pattern.CASE_INSENSITIVE)
    )

    // Promotional, Marketing & Loan Spam Patterns
    private val PROMOTIONAL_AND_OFFER_PATTERNS = listOf(
        Pattern.compile("""\b(?:loan\s+approved|approved\s+loan|loan\s+is\s+approved|loan\s+has\s+been\s+approved)\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(?:loan\s+proposal|proposal\s+approved|loan\s+offer|amount\s+offer|special\s+offer)\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(?:pre-?approved|preapproved|pre-?qualified)\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(?:loan\s+sanctioned|sanctioned\s+loan|sanction\s+letter|disburs(?:al|e))\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(?:apply\s+(?:for\s+)?loan|avail\s+(?:a\s+)?loan|instant\s+loan|personal\s+loan|business\s+loan|gold\s+loan|cash\s+loan|flexi\s+loan)\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(?:eligible\s+for\s+(?:a\s+)?loan|check\s+(?:your\s+)?eligibility|congratulations\s+you\s+are\s+eligible)\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(?:jumbo\s+loan|insta\s+loan|encash(?:\s+loan)?|loan\s+on\s+(?:credit\s+)?card|loan\s+against\s+(?:credit\s+)?card|cash\s+on\s+card|borrow\s+on\s+card)\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(?:flat\s+\d+%\s+off|discount\s+offer|voucher\s+worth|gift\s+voucher)\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(?:use\s+coupon|promo\s+code|coupon\s+code|limited\s+period\s+offer)\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(?:credit\s+score|cibil\s+score|check\s+free\s+score)\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(?:bill\s+due|payment\s+due|amount\s+due|due\s+date\s+is|minimum\s+(?:amount\s+)?due|statement\s+generated)\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(?:data\s+pack|talktime|validity\s+expir(?:es|ing)|daily\s+data)\b""", Pattern.CASE_INSENSITIVE)
    )

    // Comprehensive Transaction Action Confirmation Patterns
    private val TRANSACTION_ACTION_PATTERNS = listOf(
        // Explicit debit
        Pattern.compile("""\b(?:debited|debited\s+by|debited\s+for|debited\s+with|is\s+debited|has\s+been\s+debited|auto-?debited)\b""", Pattern.CASE_INSENSITIVE),
        // Explicit credit
        Pattern.compile("""\b(?:credited|credited\s+by|credited\s+with|is\s+credited|has\s+been\s+credited|received|deposited|refunded|salary\s+credited|salary\s+deposited)\b""", Pattern.CASE_INSENSITIVE),
        // Spent / Paid / Transferred / Sent
        Pattern.compile("""\b(?:spent|spent\s+rs|paid|paid\s+rs|paid\s+to|paid\s+at|paid\s+for|paid\s+towards|paid\s+online|paid\s+using|paid\s+via|withdrawn|charged|purchase\s+of|purchase\s+at)\b""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""\b(?:transferred|transferred\s+to|transferred\s+from|transfer\s+to|sent|sent\s+to|sent\s+rs)\b""", Pattern.CASE_INSENSITIVE),
        // Transaction completed / was made
        Pattern.compile("""\b(?:transaction\s+(?:of|at|was|is|successful)|was\s+made|made\s+using|used\s+(?:your\s+)?card|used\s+at|order\s+confirmed|order\s+placed|order\s+#|trip\s+was)\b""", Pattern.CASE_INSENSITIVE),
        // Payment of Rs X received / successful
        Pattern.compile("""\b(?:payment\s+of|payment\s+for|payment\s+(?:is\s+|was\s+)?successful|payment\s+received)\b""", Pattern.CASE_INSENSITIVE)
    )

    fun isFromBankOrFinancialSender(sender: String, body: String): Boolean {
        val s = sender.trim().lowercase(Locale.ROOT)
        // If testing manually or in demo mode
        if (s.isEmpty() || s == "manual" || s == "test" || s == "demo") {
            return true
        }

        // 1. In India & globally, banks and commercial platforms NEVER send transactional alerts from personal 10-14 digit numbers
        val isPersonalPhone = Pattern.compile("""^[+]?[0-9]{10,14}$""").matcher(s).matches()
        if (isPersonalPhone) {
            return false
        }

        // 2. Any alphanumeric sender header (DLT 6-character or prefix format like AD-SWIGGY, VK-HDFCBK, SBIINB, SARASW, COSMOS, BP-CASHFR)
        // is accepted as a valid commercial/financial sender.
        return true
    }

    fun isFinancialSms(sender: String, body: String): Boolean {
        // 1. Filter: Must be sent from a commercial/service alphanumeric header, not a personal phone
        if (!isFromBankOrFinancialSender(sender, body)) {
            return false
        }

        val lower = (sender + " " + body).lowercase(Locale.ROOT)

        // 2. Filter: Strictly reject MMS (Multimedia messaging), WAP push, or media attachments (SMS only!)
        if (lower.contains("mms") || lower.contains("multimedia message") || 
            lower.contains("application/vnd.wap.mms-message") || lower.contains("mms received") ||
            lower.contains("download mms")) {
            return false
        }

        // 3. Filter: Strictly filter out OTP authorization requests
        for (pattern in STRICT_OTP_PATTERNS) {
            if (pattern.matcher(lower).find()) {
                return false
            }
        }

        // 3. Filter: Must contain a confirmed past-tense debit or credit transactional action
        val hasRealAction = TRANSACTION_ACTION_PATTERNS.any { it.matcher(lower).find() }
        if (!hasRealAction) {
            return false
        }

        // 4. Filter: Must contain a monetary amount
        val hasAmountWord = lower.contains("rs") || lower.contains("inr") || lower.contains("usd") || 
                            lower.contains("$") || lower.contains("₹") || lower.contains("eur") || 
                            lower.contains("€") || lower.contains("spent") || lower.contains("paid")
        val hasAmountPattern = hasAmountWord || AMOUNT_PATTERNS.any { it.matcher(body).find() }
        if (!hasAmountPattern) {
            return false
        }

        // 5. Promotional rejection (Only reject pure ads with NO real debit/spend action)
        val isPromo = PROMOTIONAL_AND_OFFER_PATTERNS.any { it.matcher(lower).find() }
        val isLoanEmiDebit = lower.contains("emi") && (lower.contains("debited") || lower.contains("auto-debited") || lower.contains("paid"))
        if (isPromo && !hasRealAction && !isLoanEmiDebit) {
            return false
        }

        return true
    }

    fun extractMerchant(sender: String, body: String): String {
        val lowerBody = body.lowercase(Locale.ROOT)
        val lowerSender = sender.lowercase(Locale.ROOT)

        // 1. First priority: Check whole word match in message body or sender for known brands (e.g. Zepto, Swiggy, Blinkit, Zomato)
        for ((token, brand) in MERCHANT_ENTITY_MAP) {
            val pattern = Pattern.compile("""\b""" + Pattern.quote(token) + """\b""", Pattern.CASE_INSENSITIVE)
            if (pattern.matcher(lowerBody).find()) {
                return brand
            }
            if (lowerSender.contains(token)) {
                return brand
            }
        }

        // 2. Check enhanced universal recipient regex patterns
        for (pattern in RECIPIENT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                var candidate = matcher.group(1)?.trim() ?: ""
                candidate = candidate.replace("""(?i)\s+(?:upi|ref|refno|rrn|vpa|txn|order|via|on)$""".toRegex(), "").trim()
                val lowerCand = candidate.lowercase(Locale.ROOT)

                if (candidate.length in 2..40 && 
                    !JUNK_WORDS.contains(lowerCand) && 
                    !lowerCand.startsWith("ref") && 
                    !lowerCand.startsWith("rrn") &&
                    !lowerCand.contains("a/c")) {

                    // Check if candidate contains a VPA handle (@)
                    if (candidate.contains("@")) {
                        val handle = candidate.substringBefore("@")
                        return cleanVpaHandle(handle)
                    }

                    // Check if candidate matches any known entity
                    for ((token, brand) in MERCHANT_ENTITY_MAP) {
                        if (lowerCand.contains(token)) {
                            return brand
                        }
                    }

                    // Clean VPA handle if dot-separated
                    if (candidate.contains(".")) {
                        candidate = cleanVpaHandle(candidate)
                    }

                    if (candidate.length >= 2) {
                        val words = candidate.split(" ")
                            .map { it.trim() }
                            .filter { it.isNotBlank() && !JUNK_WORDS.contains(it.lowercase(Locale.ROOT)) }
                        if (words.isNotEmpty()) {
                            return words.take(3).joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                        }
                    }
                }
            }
        }

        // 3. Fallback: Extract brand from DLT sender header (e.g. "AD-SWIGGY" -> "Swiggy", "VM-FITPLS" -> "FITPLS")
        val dltMatcher = Pattern.compile("""^[a-zA-Z]{2}[-]([a-zA-Z0-9]{3,8})$""").matcher(sender.trim())
        if (dltMatcher.matches()) {
            val code = dltMatcher.group(1)?.lowercase(Locale.ROOT) ?: ""
            for ((token, brand) in MERCHANT_ENTITY_MAP) {
                if (code.contains(token)) {
                    return brand
                }
            }
            val bankCodes = setOf("hdfcbk", "sbiinb", "icicib", "axisbk", "kotakb", "pnbbnk", "bobcrd", "unionb", "canbnk", "idfcbk", "yesbnk", "indusb", "fedbnk", "sarasw", "cosmos", "aubank", "equita", "hsbc", "citibk")
            if (!bankCodes.contains(code) && code.length >= 3) {
                return code.uppercase(Locale.ROOT)
            }
        }

        // 4. Fallback: 6-char commercial sender without hyphen (e.g. "SWIGGY", "ZEPTO")
        val sixCharMatcher = Pattern.compile("""^[a-zA-Z0-9]{3,8}$""").matcher(sender.trim())
        if (sixCharMatcher.matches()) {
            val code = sender.trim().lowercase(Locale.ROOT)
            for ((token, brand) in MERCHANT_ENTITY_MAP) {
                if (code.contains(token)) {
                    return brand
                }
            }
            val bankCodes = setOf("hdfcbk", "sbiinb", "icicib", "axisbk", "kotakb", "pnbbnk", "bobcrd", "unionb", "canbnk", "idfcbk", "yesbnk", "indusb", "fedbnk", "sarasw", "cosmos", "aubank", "equita", "hsbc", "citibk")
            if (!bankCodes.contains(code) && code.length >= 3) {
                return code.uppercase(Locale.ROOT)
            }
        }

        return ""
    }

    fun parse(sender: String, body: String, timestamp: Long = System.currentTimeMillis(), categoryOverrides: Map<String, String>? = null): ParsedTransaction? {
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

        // 3. Currency Symbol Detection
        val currencySymbol = when {
            body.contains("$") || lower.contains("usd") -> "$"
            body.contains("€") || lower.contains("eur") -> "€"
            body.contains("£") || lower.contains("gbp") -> "£"
            else -> "₹"
        }

        // 4. Extract Merchant / Beneficiary Name using Enhanced Engine
        val merchant = extractMerchant(sender, body)

        // 5. Extract User's Own Source Account / Card Tail (STRICTLY EXCLUDE SENDEE ACCOUNTS)
        val sendeeTails = mutableSetOf<String>()
        for (pattern in SENDEE_ACCOUNT_PATTERNS) {
            val matcher = pattern.matcher(body)
            while (matcher.find()) {
                matcher.group(1)?.let { sendeeTails.add(it) }
            }
        }

        var accountTail: String? = null
        for (pattern in USER_ACCOUNT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val cand = matcher.group(1)
                // If candidate is the sendee's account, reject it!
                if (cand != null && !sendeeTails.contains(cand)) {
                    accountTail = cand
                    break
                }
            }
        }

        // 6. Detect Payment Method
        val paymentMethod = detectPaymentMethod(lower, body)

        // 7. Extract Available Balance
        var balance: Double? = null
        for (pattern in BALANCE_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val balStr = matcher.group(1)?.replace(",", "") ?: ""
                balance = balStr.toDoubleOrNull()
                if (balance != null) break
            }
        }

        // 8. Infer Category via FastText-Style Semantic Subword Classifier + User Memory
        val category = inferCategory(merchant, body, categoryOverrides)

        // 9. Format Date (Parse explicit date from SMS text if present)
        val dateStr = extractTransactionDate(body, timestamp)

        val cleanTitle = formatMerchantTitle(merchant, type, paymentMethod)

        // Smart Deduplication Fingerprint
        val tailKey = accountTail ?: "0000"
        val cleanMerchantKey = cleanTitle.lowercase(Locale.ROOT).replace("[^a-z0-9]".toRegex(), "")
        val dedupKey = "$type-${amount.toInt()}-$dateStr-$tailKey-$cleanMerchantKey"

        val inferredAlias = inferCardAlias(body, accountTail)

        return ParsedTransaction(
            id = "sms-" + timestamp + "-" + (amount.toInt()),
            title = cleanTitle,
            type = type,
            amount = amount,
            category = category,
            date = dateStr,
            rawText = body,
            balance = balance,
            accountTail = accountTail,
            paymentMethod = paymentMethod,
            currencySymbol = currencySymbol,
            dedupKey = dedupKey,
            cardAlias = inferredAlias
        )
    }

    fun inferCardAlias(body: String, tail: String?): String? {
        val lower = body.lowercase(Locale.ROOT)

        // 1. Comprehensive dictionary of 75+ popular Indian & Global credit card brands
        val dictName = when {
            // HDFC Bank
            lower.contains("pixel play") -> "HDFC Pixel Play"
            lower.contains("millennia") -> "HDFC Millennia"
            lower.contains("regalia gold") -> "HDFC Regalia Gold"
            lower.contains("regalia") -> "HDFC Regalia"
            lower.contains("infinia") -> "HDFC Infinia"
            lower.contains("swiggy hdfc") || (lower.contains("swiggy") && lower.contains("hdfc")) -> "Swiggy HDFC Card"
            lower.contains("tata neu infinity") -> "Tata Neu Infinity"
            lower.contains("tata neu plus") || lower.contains("tata neu") -> "Tata Neu Card"
            lower.contains("diners club black") -> "HDFC Diners Black"
            lower.contains("diners club") -> "HDFC Diners Card"
            lower.contains("moneyback") -> "HDFC MoneyBack+"
            lower.contains("freedom card") || lower.contains("freedom credit") -> "HDFC Freedom"
            lower.contains("shoppers stop") && lower.contains("hdfc") -> "Shoppers Stop HDFC"
            lower.contains("marriott bonvoy") -> "Marriott Bonvoy HDFC"

            // ICICI Bank
            lower.contains("amazon pay") && lower.contains("icici") -> "Amazon Pay ICICI"
            lower.contains("coral") && lower.contains("icici") -> "ICICI Coral"
            lower.contains("rubyx") -> "ICICI Rubyx"
            lower.contains("sapphiro") -> "ICICI Sapphiro"
            lower.contains("emeralde") -> "ICICI Emeralde"
            lower.contains("platinum chip") -> "ICICI Platinum Chip"
            lower.contains("hpcl super saver") || (lower.contains("hpcl") && lower.contains("icici")) -> "ICICI HPCL Card"
            lower.contains("makemytrip") && lower.contains("icici") -> "MakeMyTrip ICICI"

            // Axis Bank
            lower.contains("flipkart axis") || (lower.contains("flipkart") && lower.contains("axis")) -> "Flipkart Axis Card"
            lower.contains("airtel axis") || (lower.contains("airtel") && lower.contains("axis")) -> "Airtel Axis Card"
            lower.contains("axis bank ace") || lower.contains("axis ace") -> "Axis Ace"
            lower.contains("magnus") -> "Axis Magnus"
            lower.contains("atlas") && lower.contains("axis") -> "Axis Atlas"
            lower.contains("my zone") || lower.contains("myzone") -> "Axis MY ZONE"
            lower.contains("neo") && lower.contains("axis") -> "Axis Neo"
            lower.contains("horizon") && lower.contains("axis") -> "Axis Horizon"
            lower.contains("indianoil axis") -> "IndianOil Axis Card"

            // SBI Card
            lower.contains("simplyclick") -> "SBI SimplyCLICK"
            lower.contains("simplysave") -> "SBI SimplySAVE"
            lower.contains("bpcl octane") -> "BPCL Octane SBI"
            lower.contains("bpcl sbi") || (lower.contains("bpcl") && lower.contains("sbi")) -> "BPCL SBI Card"
            lower.contains("cashback sbi") -> "SBI Cashback Card"
            lower.contains("club vistara sbi") || (lower.contains("vistara") && lower.contains("sbi")) -> "Club Vistara SBI"
            lower.contains("air india sbi") -> "Air India SBI"
            lower.contains("sbi aurum") || lower.contains("aurum") -> "SBI Aurum"
            lower.contains("sbi elite") || (lower.contains("elite") && lower.contains("sbi")) -> "SBI Elite"
            lower.contains("sbi prime") || (lower.contains("prime") && lower.contains("sbi")) -> "SBI Prime"
            lower.contains("sbi pulse") || lower.contains("pulse sbi") -> "SBI Pulse"

            // Kotak Mahindra Bank
            lower.contains("kotak league") || lower.contains("league credit") -> "Kotak League"
            lower.contains("kotak royale") || lower.contains("royale credit") -> "Kotak Royale"
            lower.contains("kotak mojo") || lower.contains("mojo credit") -> "Kotak Mojo"
            lower.contains("kotak white") || lower.contains("white credit") -> "Kotak White"
            lower.contains("kotak zen") || lower.contains("zen credit") -> "Kotak Zen"
            lower.contains("6e rewards") && lower.contains("kotak") -> "IndiGo Kotak Card"

            // RBL Bank
            lower.contains("world safari") -> "RBL World Safari"
            lower.contains("rbl shoprite") -> "RBL Shoprite"
            lower.contains("rbl play") || lower.contains("play credit") -> "RBL Play"
            lower.contains("rbl pop") -> "RBL Pop"
            lower.contains("rbl cookie") -> "RBL Cookie"
            lower.contains("rbl binge") -> "RBL Binge"
            lower.contains("zomato rbl") || (lower.contains("zomato") && lower.contains("rbl")) -> "Zomato RBL Card"

            // IndusInd Bank
            lower.contains("indusind legend") || lower.contains("legend credit") -> "IndusInd Legend"
            lower.contains("platinum aura") -> "IndusInd Aura"
            lower.contains("indusind tiger") || lower.contains("tiger credit") -> "IndusInd Tiger"
            lower.contains("pinnacle") && lower.contains("indusind") -> "IndusInd Pinnacle"
            lower.contains("eazydiner") && lower.contains("indusind") -> "EazyDiner IndusInd"

            // IDFC FIRST Bank
            lower.contains("idfc first wealth") || lower.contains("first wealth") -> "IDFC FIRST Wealth"
            lower.contains("idfc first select") || lower.contains("first select") -> "IDFC FIRST Select"
            lower.contains("idfc first classic") || lower.contains("first classic") -> "IDFC FIRST Classic"
            lower.contains("first wow") || lower.contains("idfc first wow") -> "IDFC FIRST WOW"
            lower.contains("idfc first millennia") -> "IDFC FIRST Millennia"
            lower.contains("idfc first swyp") || lower.contains("first swyp") -> "IDFC FIRST SWYP"

            // AU Small Finance Bank
            lower.contains("au lit") || lower.contains("lit credit") -> "AU LIT Card"
            lower.contains("au zenith") || lower.contains("zenith credit") -> "AU Zenith"
            lower.contains("au vetta") || lower.contains("vetta credit") -> "AU Vetta"
            lower.contains("au altura") || lower.contains("altura credit") -> "AU Altura"
            lower.contains("ixigo au") || (lower.contains("ixigo") && lower.contains("au")) -> "Ixigo AU Card"

            // Bank of Baroda (BOBCARD)
            lower.contains("bob eterna") || lower.contains("eterna credit") -> "BOBCARD Eterna"
            lower.contains("bob premier") -> "BOBCARD Premier"
            lower.contains("bob select") -> "BOBCARD Select"

            // Fintech, Digital & RuPay UPI Cards
            lower.contains("scapia") -> "Scapia Federal Card"
            lower.contains("onecard") -> "OneCard"
            lower.contains("kiwi axis") || lower.contains("kiwi credit") -> "Kiwi Axis Card"
            lower.contains("uni card") || lower.contains("uni nx") -> "Uni Card"
            lower.contains("jupiter edge") -> "Jupiter Edge Card"
            lower.contains("fi federal") || (lower.contains("fi money") && lower.contains("card")) -> "Fi Federal Card"

            // Generic Bank Cards
            lower.contains("sbi card") -> "SBI Card"
            lower.contains("hdfc bank") && lower.contains("credit card") -> "HDFC Credit Card"
            lower.contains("icici bank") && lower.contains("credit card") -> "ICICI Credit Card"
            lower.contains("axis bank") && lower.contains("credit card") -> "Axis Credit Card"
            else -> null
        }
        if (dictName != null) return dictName

        // 2. Dynamic Card Name Extraction (Catches ANY unique card name on the internet)
        val dynamicPatterns = listOf(
            Pattern.compile("""(?:using|on|spent\s+on|made\s+on|charged\s+on)\s+(?:your\s+)?([A-Za-z0-9\+\s\-]{2,35}?)\s+(?:credit\s+card|card\b)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(?:using|with)\s+([A-Za-z0-9\+\s\-]{2,35}?)\s+card\b""", Pattern.CASE_INSENSITIVE)
        )
        for (pattern in dynamicPatterns) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                var rawName = matcher.group(1)?.trim() ?: ""
                rawName = rawName.replace("""(?i)\b(?:a\/c|account|bank|the)\b""".toRegex(), "").trim()
                rawName = rawName.replace("""\s+""".toRegex(), " ")
                if (rawName.length in 3..28 && !JUNK_WORDS.contains(rawName.lowercase(Locale.ROOT))) {
                    return rawName.split(" ").filter { it.isNotBlank() }
                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } + " Card"
                }
            }
        }
        return null
    }

    private fun detectPaymentMethod(lower: String, body: String): String {
        return when {
            lower.contains("credit card") && (lower.contains("upi") || lower.contains("vpa")) -> "UPI on Credit Card"
            lower.contains("upi") || lower.contains("vpa") || lower.contains("gpay") || lower.contains("phonepe") || lower.contains("paytm") -> "UPI"
            lower.contains("credit card") || lower.contains("spent on card") || lower.contains("cc a/c") -> "Credit Card"
            lower.contains("debit card") || lower.contains("atm") || lower.contains("cash withdrawal") || lower.contains("dc a/c") -> "Debit Card"
            lower.contains("netbanking") || lower.contains("neft") || lower.contains("imps") || lower.contains("rtgs") || lower.contains("transfer") -> "NetBanking"
            lower.contains("salary") || lower.contains("payroll") || lower.contains("deposit") -> "Direct Deposit"
            else -> if (body.contains("Card", ignoreCase = true)) "Credit Card" else "Card/UPI"
        }
    }

    fun inferCategory(merchant: String, body: String, overrides: Map<String, String>? = null): String {
        val cleanMerchant = merchant.trim()
        val cleanKey = cleanMerchant.lowercase(Locale.ROOT).replace("[^a-z0-9]".toRegex(), "")

        // 1. Adaptive User Override Check (Personal AI Memory)
        if (cleanKey.isNotBlank()) {
            overrides?.get(cleanKey)?.let { return it }
        }

        val fullText = "$cleanMerchant $body".lowercase(Locale.ROOT)

        // 2. FastText-Style Semantic Subword & Root Matcher (100% Offline, 0 MB Overhead)
        // Priority 1: Direct Income / Salary
        if (fullText.contains("salary") || fullText.contains("payroll") || fullText.contains("wages") ||
            fullText.contains("stipend") || fullText.contains("pension") || fullText.contains("bonus") ||
            fullText.contains("salary credited")) {
            return "Salary & Income"
        }

        // Priority 2: ATM / Cash Withdrawal
        if (fullText.contains("atm") || fullText.contains("cash wdl") || fullText.contains("cash withdrawal") ||
            fullText.contains("withdrawn at atm") || fullText.contains("nfs atm")) {
            return "Cash & ATM"
        }

        // Priority 3: Investments & Wealth
        val investTokens = listOf(
            "zerodha", "groww", "angelone", "angel broking", "upstox", "motilal", "hdfc sec",
            "icici direct", "kotak sec", "sharekhan", "kuvera", "etmoney", "indmoney", "coin",
            "mutual fund", "sip", "stocks", "shares", "equity", "demat", "nps", "ppf",
            "bonds", "dividend", "interest credited", "gold fund"
        )
        if (investTokens.any { fullText.contains(it) }) {
            return "Investments & SIP"
        }

        // Priority 4: Healthcare & Pharmacy
        val healthTokens = listOf(
            "apollo", "1mg", "pharmeasy", "netmeds", "practo", "medplus", "dr lal", "lal path",
            "metropolis", "thyrocare", "max health", "fortis", "manipal", "pharma", "pharmacy",
            "chemist", "medic", "medicos", "hospital", "clinic", "diagnostic", "pathology",
            "dental", "dentist", "eyecare", "opticals", "lenskart", "physio", "ayurved", "homeo",
            "lab test", "health", "dr."
        )
        if (healthTokens.any { fullText.contains(it) }) {
            return "Healthcare & Pharmacy"
        }

        // Priority 5: Groceries & Daily Essentials (Deeply covers Zepto, Blinkit, Instamart, etc.)
        val groceryTokens = listOf(
            "blinkit", "zepto", "kiranakart", "grofers", "blink commerce", "instamart", "bigbasket",
            "bbdaily", "dmart", "spencer", "more retail", "nature basket", "smart bazaar",
            "reliance fresh", "country delight", "freshtohome", "licious", "grocer", "kirana",
            "supermarket", "hypermarket", "provisions", "dairy", "milk", "vegetable", "veggie",
            "fruits", "poultry", "daily needs", "ration", "general store", "dunzo"
        )
        if (groceryTokens.any { fullText.contains(it) }) {
            return "Groceries & Essentials"
        }

        // Priority 6: Food & Dining
        val foodTokens = listOf(
            "swiggy", "zomato", "bundl", "mcdonald", "kfc", "burger", "domino", "pizza", "subway",
            "haldiram", "starbucks", "cafe", "coffee", "roaster", "tea", "chai", "chaayos",
            "chai point", "bakes", "bakery", "bites", "dhaba", "kitchen", "tandoor", "rasoi",
            "bhoj", "sweets", "mithai", "biryani", "bar", "pub", "brewery", "eatery", "diner",
            "shawarma", "waffle", "pastry", "restaurant", "restro", "canteen", "fastfood",
            "barbeque", "food"
        )
        if (foodTokens.any { fullText.contains(it) }) {
            return "Food & Dining"
        }

        // Priority 7: Travel, Commute & Fuel
        val travelTokens = listOf(
            "uber", "ola", "ani tech", "rapido", "roppen", "namma yatri", "yatri", "irctc", "railway",
            "makemytrip", "goibibo", "easemytrip", "cleartrip", "indigo", "air india", "spicejet", "akasa",
            "fuel", "petrol", "diesel", "cng", "hpcl", "bpcl", "iocl", "indian oil", "shell",
            "petroleum", "cabs", "rides", "auto", "taxi", "toll", "fastag", "metro", "parking",
            "flight", "airline", "airport", "rail", "chalo", "transit", "train"
        )
        if (travelTokens.any { fullText.contains(it) }) {
            return "Travel & Transport"
        }

        // Priority 8: Personal Care & Fitness
        val personalCareTokens = listOf(
            "cult.fit", "cultfit", "curefit", "gold gym", "anytime fitness", "urban company", "enrich",
            "jawed habib", "vlcc", "gym", "fitness", "workout", "yoga", "salon", "spa",
            "parlour", "barber", "grooming", "massage", "haircut"
        )
        if (personalCareTokens.any { fullText.contains(it) }) {
            return "Personal Care & Fitness"
        }

        // Priority 9: Utilities, Bills & Recharges
        val utilityTokens = listOf(
            "electricity", "bescom", "tata power", "cesc", "discom", "mseb", "uppcl", "airtel",
            "jio", "vodafone", "vi", "bsnl", "act fibernet", "hathway", "tata play", "dish tv",
            "cylinder", "indane", "bharat gas", "hp gas", "adani gas", "igl", "mgl", "billdesk",
            "bbps", "recharge", "broadband", "fiber", "wifi", "water board", "piped gas", "dth"
        )
        if (utilityTokens.any { fullText.contains(it) }) {
            return "Utilities & Bills"
        }

        // Priority 10: Housing & Rent
        val housingTokens = listOf(
            "rent", "nobroker", "nestaway", "magicbricks", "housing", "mygate", "apna complex",
            "adda", "landlord", "flat", "apartment", "society maintenance", "maintenance fee", "lease"
        )
        if (housingTokens.any { fullText.contains(it) }) {
            return "Housing & Rent"
        }

        // Priority 11: Tech & Cloud Subscriptions
        val techTokens = listOf(
            "netflix", "spotify", "prime", "hotstar", "disney", "apple.com", "google play",
            "google storage", "youtube", "openai", "chatgpt", "github", "aws", "azure",
            "digitalocean", "canva", "adobe", "domain", "hostinger", "godaddy", "cloud",
            "saas", "software"
        )
        if (techTokens.any { fullText.contains(it) }) {
            return "Tech & Subscriptions"
        }

        // Priority 12: Entertainment & Movies
        val entertainmentTokens = listOf(
            "bookmyshow", "bigtree", "pvr", "inox", "cinepolis", "miraj", "carnival", "playstation",
            "steam", "xbox", "epic games", "movie", "cinema", "theatre", "concert", "gaming"
        )
        if (entertainmentTokens.any { fullText.contains(it) }) {
            return "Entertainment & Leisure"
        }

        // Priority 13: Education & Coaching
        val eduTokens = listOf(
            "udemy", "coursera", "unacademy", "byju", "physicswallah", "khan academy",
            "school", "college", "university", "tuition", "coaching", "fees", "institute", "academy"
        )
        if (eduTokens.any { fullText.contains(it) }) {
            return "Education & Fees"
        }

        // Priority 14: Shopping & E-Commerce
        val shoppingTokens = listOf(
            "amazon", "flipkart", "myntra", "meesho", "nykaa", "ajio", "zara", "h&m",
            "uniqlo", "westside", "pantaloons", "shoppers stop", "croma", "reliance digital",
            "ikea", "decathlon", "store", "retail", "boutique", "mall", "fashion", "apparel", "footwear"
        )
        if (shoppingTokens.any { fullText.contains(it) }) {
            return "Shopping & Retail"
        }

        // Priority 15: General UPI & Peer-to-Peer
        if (fullText.contains("upi") || fullText.contains("vpa") || fullText.contains("sent to") ||
            fullText.contains("transfer to") || fullText.contains("gpay") || fullText.contains("phonepe")) {
            return "UPI & Transfers"
        }

        return if (fullText.contains("credited")) "Salary & Income" else "General"
    }

    private fun extractTransactionDate(body: String, fallbackTimestamp: Long): String {
        // Pattern 1: dd/MM/yy or dd/MM/yyyy e.g. "on 21/10/26" or "21/10/2026"
        val slashPattern = Pattern.compile("""\b(?:on\s+)?([0-3]?[0-9])[\/]([0-1]?[0-9])[\/](20[0-9]{2}|[0-9]{2})\b""")
        val slashMatcher = slashPattern.matcher(body)
        if (slashMatcher.find()) {
            val d = slashMatcher.group(1)?.toIntOrNull() ?: 1
            val m = slashMatcher.group(2)?.toIntOrNull() ?: 1
            var y = slashMatcher.group(3)?.toIntOrNull() ?: 2026
            if (y < 100) y += 2000
            return String.format(Locale.ROOT, "%04d-%02d-%02d", y, m, d)
        }

        // Pattern 2: dd-MM-yy or dd-MM-yyyy e.g. "21-10-26"
        val dashPattern = Pattern.compile("""\b(?:on\s+)?([0-3]?[0-9])[\-]([0-1]?[0-9])[\-](20[0-9]{2}|[0-9]{2})\b""")
        val dashMatcher = dashPattern.matcher(body)
        if (dashMatcher.find()) {
            val d = dashMatcher.group(1)?.toIntOrNull() ?: 1
            val m = dashMatcher.group(2)?.toIntOrNull() ?: 1
            var y = dashMatcher.group(3)?.toIntOrNull() ?: 2026
            if (y < 100) y += 2000
            return String.format(Locale.ROOT, "%04d-%02d-%02d", y, m, d)
        }

        // Pattern 3: dd-MMM-yy e.g. "21-Oct-26" or "21-Oct-2026"
        val mmmPattern = Pattern.compile("""\b(?:on\s+)?([0-3]?[0-9])[\-\s]([A-Za-z]{3})[\-\s](20[0-9]{2}|[0-9]{2})\b""", Pattern.CASE_INSENSITIVE)
        val mmmMatcher = mmmPattern.matcher(body)
        if (mmmMatcher.find()) {
            val d = mmmMatcher.group(1)?.toIntOrNull() ?: 1
            val mStr = mmmMatcher.group(2)?.lowercase(Locale.ROOT) ?: "jan"
            var y = mmmMatcher.group(3)?.toIntOrNull() ?: 2026
            if (y < 100) y += 2000
            val months = mapOf("jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "may" to 5, "jun" to 6, "jul" to 7, "aug" to 8, "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12)
            val m = months[mStr] ?: 1
            return String.format(Locale.ROOT, "%04d-%02d-%02d", y, m, d)
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(fallbackTimestamp))
    }

    private fun formatMerchantTitle(merchant: String, type: String, paymentMethod: String): String {
        val lower = merchant.lowercase(Locale.ROOT).trim()
        if (merchant.isBlank() || JUNK_WORDS.contains(lower) || lower.startsWith("ref") || lower.startsWith("rrn")) {
            return when {
                type == "credit" -> "Bank Deposit"
                paymentMethod.contains("UPI") -> "UPI Payment"
                paymentMethod.contains("Credit Card") -> "Credit Card Spend"
                paymentMethod.contains("Debit Card") -> "Debit Card Spend"
                else -> "Card/UPI Payment"
            }
        }
        // Handle ATM withdrawals cleanly
        if (lower.startsWith("atm") || lower.contains("cash wdl") || lower.contains("nfs atm")) {
            return "ATM Cash Withdrawal"
        }
        // Handle known entity mappings (e.g. "Zepto", "Blinkit", "Swiggy")
        for ((token, brand) in MERCHANT_ENTITY_MAP) {
            if (lower == token || lower.contains(token)) {
                return brand
            }
        }
        // Handle UPI VPA formats like paytm.s190dkl@pty or swiggy.payu@icici
        if (merchant.contains("@")) {
            return cleanVpaHandle(merchant.substringBefore("@").trim())
        }
        val cleanWords = merchant.split(" ")
            .map { it.trim() }
            .filter { it.isNotBlank() && !JUNK_WORDS.contains(it.lowercase(Locale.ROOT)) }
        if (cleanWords.isEmpty()) {
            return if (type == "credit") "Bank Deposit" else "Card/UPI Payment"
        }
        return cleanWords.take(3)
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}
