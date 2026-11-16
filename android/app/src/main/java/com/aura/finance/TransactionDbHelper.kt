package com.aura.finance

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

class TransactionDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "aura_finance.db"
        const val DATABASE_VERSION = 4

        const val TABLE_TRANSACTIONS = "transactions"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_TYPE = "type"
        const val COL_AMOUNT = "amount"
        const val COL_CATEGORY = "category"
        const val COL_DATE = "date"
        const val COL_RAW_TEXT = "raw_text"
        const val COL_ACCOUNT_TAIL = "account_tail"
        const val COL_PAYMENT_METHOD = "payment_method"
        const val COL_CURRENCY_SYMBOL = "currency_symbol"
        const val COL_DEDUP_KEY = "dedup_key"
        const val COL_NOTE = "note"
        const val COL_BALANCE = "balance"
        const val COL_CREATED_AT = "created_at"

        const val TABLE_SCHEDULES = "schedules"
        const val COL_SCH_ID = "id"
        const val COL_SCH_TITLE = "title"
        const val COL_SCH_AMOUNT = "amount"
        const val COL_SCH_DUE_DAY = "due_day"
        const val COL_SCH_CATEGORY = "category"
        const val COL_SCH_PAID = "paid"

        const val TABLE_CARD_MAPPINGS = "card_mappings"
        const val COL_MAP_TAIL = "tail"
        const val COL_MAP_ALIAS = "alias"

        const val TABLE_BUDGETS = "budgets"
        const val COL_BUDGET_CATEGORY = "category"
        const val COL_BUDGET_LIMIT = "monthly_limit"
        const val COL_BUDGET_THRESHOLD = "alert_threshold"
        const val COL_BUDGET_UPDATED_AT = "updated_at"

        const val TABLE_SAVINGS_GOALS = "savings_goals"
        const val COL_GOAL_ID = "id"
        const val COL_GOAL_TITLE = "title"
        const val COL_GOAL_TARGET_AMOUNT = "target_amount"
        const val COL_GOAL_TARGET_DATE = "target_date"
        const val COL_GOAL_CURRENT_AMOUNT = "current_amount"
        const val COL_GOAL_CATEGORY = "category"
        const val COL_GOAL_CREATED_AT = "created_at"

        const val TABLE_GOAL_CONTRIBUTIONS = "goal_contributions"
        const val COL_CONTRIB_ID = "id"
        const val COL_CONTRIB_GOAL_ID = "goal_id"
        const val COL_CONTRIB_AMOUNT = "amount"
        const val COL_CONTRIB_DATE = "date"
        const val COL_CONTRIB_NOTE = "note"
        const val COL_CONTRIB_CREATED_AT = "created_at"

        const val TABLE_DISMISSED_RECURRING = "dismissed_recurring"
        const val COL_DISMISSED_SIG = "signature"
        const val COL_DISMISSED_AT = "dismissed_at"

        const val TABLE_CATEGORY_RULES = "category_rules"
        const val COL_RULE_MERCHANT = "merchant_key"
        const val COL_RULE_CATEGORY = "category"
        const val COL_RULE_UPDATED_AT = "updated_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTxTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_TRANSACTIONS (
                $COL_ID TEXT PRIMARY KEY,
                $COL_TITLE TEXT,
                $COL_TYPE TEXT,
                $COL_AMOUNT REAL,
                $COL_CATEGORY TEXT,
                $COL_DATE TEXT,
                $COL_RAW_TEXT TEXT,
                $COL_ACCOUNT_TAIL TEXT,
                $COL_PAYMENT_METHOD TEXT,
                $COL_CURRENCY_SYMBOL TEXT,
                $COL_DEDUP_KEY TEXT,
                $COL_NOTE TEXT,
                $COL_BALANCE REAL,
                $COL_CREATED_AT INTEGER
            );
        """.trimIndent()

        val createSchTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_SCHEDULES (
                $COL_SCH_ID TEXT PRIMARY KEY,
                $COL_SCH_TITLE TEXT,
                $COL_SCH_AMOUNT REAL,
                $COL_SCH_DUE_DAY INTEGER,
                $COL_SCH_CATEGORY TEXT,
                $COL_SCH_PAID INTEGER
            );
        """.trimIndent()

        val createMapTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_CARD_MAPPINGS (
                $COL_MAP_TAIL TEXT PRIMARY KEY,
                $COL_MAP_ALIAS TEXT
            );
        """.trimIndent()

        val createBudgetTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_BUDGETS (
                $COL_BUDGET_CATEGORY TEXT PRIMARY KEY,
                $COL_BUDGET_LIMIT REAL,
                $COL_BUDGET_THRESHOLD REAL,
                $COL_BUDGET_UPDATED_AT INTEGER
            );
        """.trimIndent()

        val createGoalsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_SAVINGS_GOALS (
                $COL_GOAL_ID TEXT PRIMARY KEY,
                $COL_GOAL_TITLE TEXT,
                $COL_GOAL_TARGET_AMOUNT REAL,
                $COL_GOAL_TARGET_DATE TEXT,
                $COL_GOAL_CURRENT_AMOUNT REAL,
                $COL_GOAL_CATEGORY TEXT,
                $COL_GOAL_CREATED_AT INTEGER
            );
        """.trimIndent()

        val createContribTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_GOAL_CONTRIBUTIONS (
                $COL_CONTRIB_ID TEXT PRIMARY KEY,
                $COL_CONTRIB_GOAL_ID TEXT,
                $COL_CONTRIB_AMOUNT REAL,
                $COL_CONTRIB_DATE TEXT,
                $COL_CONTRIB_NOTE TEXT,
                $COL_CONTRIB_CREATED_AT INTEGER
            );
        """.trimIndent()

        val createDismissedTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_DISMISSED_RECURRING (
                $COL_DISMISSED_SIG TEXT PRIMARY KEY,
                $COL_DISMISSED_AT INTEGER
            );
        """.trimIndent()

        val createCategoryRulesTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_CATEGORY_RULES (
                $COL_RULE_MERCHANT TEXT PRIMARY KEY,
                $COL_RULE_CATEGORY TEXT,
                $COL_RULE_UPDATED_AT INTEGER
            );
        """.trimIndent()

        db.execSQL(createTxTable)
        db.execSQL(createSchTable)
        db.execSQL(createMapTable)
        db.execSQL(createBudgetTable)
        db.execSQL(createGoalsTable)
        db.execSQL(createContribTable)
        db.execSQL(createDismissedTable)
        db.execSQL(createCategoryRulesTable)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_tx_date ON $TABLE_TRANSACTIONS ($COL_DATE);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_tx_dedup ON $TABLE_TRANSACTIONS ($COL_DEDUP_KEY);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_contrib_goal ON $TABLE_GOAL_CONTRIBUTIONS ($COL_CONTRIB_GOAL_ID);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE $TABLE_TRANSACTIONS ADD COLUMN $COL_NOTE TEXT DEFAULT '';")
            } catch (ignored: Exception) {}
        }
        if (oldVersion < 3) {
            try {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS $TABLE_BUDGETS (
                        $COL_BUDGET_CATEGORY TEXT PRIMARY KEY,
                        $COL_BUDGET_LIMIT REAL,
                        $COL_BUDGET_THRESHOLD REAL,
                        $COL_BUDGET_UPDATED_AT INTEGER
                    );
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS $TABLE_SAVINGS_GOALS (
                        $COL_GOAL_ID TEXT PRIMARY KEY,
                        $COL_GOAL_TITLE TEXT,
                        $COL_GOAL_TARGET_AMOUNT REAL,
                        $COL_GOAL_TARGET_DATE TEXT,
                        $COL_GOAL_CURRENT_AMOUNT REAL,
                        $COL_GOAL_CATEGORY TEXT,
                        $COL_GOAL_CREATED_AT INTEGER
                    );
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS $TABLE_GOAL_CONTRIBUTIONS (
                        $COL_CONTRIB_ID TEXT PRIMARY KEY,
                        $COL_CONTRIB_GOAL_ID TEXT,
                        $COL_CONTRIB_AMOUNT REAL,
                        $COL_CONTRIB_DATE TEXT,
                        $COL_CONTRIB_NOTE TEXT,
                        $COL_CONTRIB_CREATED_AT INTEGER
                    );
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS $TABLE_DISMISSED_RECURRING (
                        $COL_DISMISSED_SIG TEXT PRIMARY KEY,
                        $COL_DISMISSED_AT INTEGER
                    );
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_contrib_goal ON $TABLE_GOAL_CONTRIBUTIONS ($COL_CONTRIB_GOAL_ID);")
            } catch (ignored: Exception) {}
        }
        if (oldVersion < 4) {
            try {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS $TABLE_CATEGORY_RULES (
                        $COL_RULE_MERCHANT TEXT PRIMARY KEY,
                        $COL_RULE_CATEGORY TEXT,
                        $COL_RULE_UPDATED_AT INTEGER
                    );
                """.trimIndent())
            } catch (ignored: Exception) {}
        }
    }

    fun insertOrUpdateTransaction(obj: JSONObject): Long {
        val db = writableDatabase
        val id = obj.optString("id", "tx-" + System.currentTimeMillis())
        val cv = ContentValues().apply {
            put(COL_ID, id)
            put(COL_TITLE, obj.optString("title", "Payment"))
            put(COL_TYPE, obj.optString("type", "debit"))
            put(COL_AMOUNT, obj.optDouble("amount", 0.0))
            put(COL_CATEGORY, obj.optString("category", "General & Miscellaneous"))
            put(COL_DATE, obj.optString("date", ""))
            put(COL_RAW_TEXT, obj.optString("rawText", ""))
            put(COL_ACCOUNT_TAIL, obj.optString("accountTail", ""))
            put(COL_PAYMENT_METHOD, obj.optString("paymentMethod", "UPI/Card"))
            put(COL_CURRENCY_SYMBOL, obj.optString("currencySymbol", "₹"))
            put(COL_DEDUP_KEY, obj.optString("dedupKey", ""))
            if (obj.has("note")) put(COL_NOTE, obj.optString("note", ""))
            if (obj.has("balance")) put(COL_BALANCE, obj.optDouble("balance", -1.0))
            put(COL_CREATED_AT, System.currentTimeMillis())
        }
        return db.insertWithOnConflict(TABLE_TRANSACTIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertTransactionsBatch(jsonArray: JSONArray): Int {
        val db = writableDatabase
        var count = 0
        db.beginTransaction()
        try {
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", "tx-" + System.currentTimeMillis() + "-" + i)
                val dedupKey = obj.optString("dedupKey", "")

                // Check if dedupKey or id already exists
                if (dedupKey.isNotBlank()) {
                    val cursor = db.query(TABLE_TRANSACTIONS, arrayOf(COL_ID), "$COL_DEDUP_KEY = ?", arrayOf(dedupKey), null, null, null)
                    val exists = cursor.count > 0
                    cursor.close()
                    if (exists) continue
                }

                val cv = ContentValues().apply {
                    put(COL_ID, id)
                    put(COL_TITLE, obj.optString("title", "Payment"))
                    put(COL_TYPE, obj.optString("type", "debit"))
                    put(COL_AMOUNT, obj.optDouble("amount", 0.0))
                    put(COL_CATEGORY, obj.optString("category", "General & Miscellaneous"))
                    put(COL_DATE, obj.optString("date", ""))
                    put(COL_RAW_TEXT, obj.optString("rawText", ""))
                    put(COL_ACCOUNT_TAIL, obj.optString("accountTail", ""))
                    put(COL_PAYMENT_METHOD, obj.optString("paymentMethod", "UPI/Card"))
                    put(COL_CURRENCY_SYMBOL, obj.optString("currencySymbol", "₹"))
                    put(COL_DEDUP_KEY, dedupKey)
                    put(COL_NOTE, obj.optString("note", ""))
                    if (obj.has("balance")) put(COL_BALANCE, obj.optDouble("balance", -1.0))
                    put(COL_CREATED_AT, System.currentTimeMillis())
                }
                val res = db.insertWithOnConflict(TABLE_TRANSACTIONS, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                if (res != -1L) count++
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        return count
    }

    fun getAllTransactions(): JSONArray {
        // Auto-purge any old promotional junk from SQLite upon retrieval
        try {
            purgeInvalidAndPromotional()
        } catch (ignored: Exception) {}

        val db = readableDatabase
        val array = JSONArray()
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                TABLE_TRANSACTIONS,
                null,
                null,
                null,
                null,
                null,
                "$COL_DATE DESC, $COL_CREATED_AT DESC"
            )
            while (cursor != null && cursor.moveToNext()) {
                val obj = JSONObject().apply {
                    put("id", cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)))
                    put("title", cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)))
                    put("type", cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)))
                    put("amount", cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT)))
                    put("category", cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)))
                    put("date", cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)))
                    put("rawText", cursor.getString(cursor.getColumnIndexOrThrow(COL_RAW_TEXT)))
                    put("accountTail", cursor.getString(cursor.getColumnIndexOrThrow(COL_ACCOUNT_TAIL)))
                    put("paymentMethod", cursor.getString(cursor.getColumnIndexOrThrow(COL_PAYMENT_METHOD)))
                    put("currencySymbol", cursor.getString(cursor.getColumnIndexOrThrow(COL_CURRENCY_SYMBOL)))
                    put("dedupKey", cursor.getString(cursor.getColumnIndexOrThrow(COL_DEDUP_KEY)))
                    put("note", cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE)) ?: "")
                    val bal = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_BALANCE))
                    if (bal >= 0) put("balance", bal)
                }
                array.put(obj)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return array
    }

    fun deleteTransaction(id: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_TRANSACTIONS, "$COL_ID = ?", arrayOf(id))
    }

    fun updateNote(id: String, note: String): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_NOTE, note)
        }
        return db.update(TABLE_TRANSACTIONS, cv, "$COL_ID = ?", arrayOf(id))
    }

    fun updateCategory(id: String, category: String): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_CATEGORY, category)
        }
        return db.update(TABLE_TRANSACTIONS, cv, "$COL_ID = ?", arrayOf(id))
    }

    fun clearAllTransactions(): Int {
        val db = writableDatabase
        return db.delete(TABLE_TRANSACTIONS, null, null)
    }

    /**
     * Purges promotional junk, loan proposals, and invalid transactions from SQLite database
     */
    fun purgeInvalidAndPromotional(): Int {
        val db = writableDatabase
        var deletedCount = 0
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_TRANSACTIONS, arrayOf(COL_ID, COL_RAW_TEXT, COL_TITLE), null, null, null, null, null)
            val idsToDelete = mutableListOf<String>()
            while (cursor != null && cursor.moveToNext()) {
                val id = cursor.getString(0)
                val rawText = cursor.getString(1) ?: ""
                val title = cursor.getString(2) ?: ""
                if (rawText.isNotBlank()) {
                    if (!SmsParser.isFinancialSms(title, rawText)) {
                        idsToDelete.add(id)
                    }
                }
            }
            cursor?.close()
            cursor = null
            for (id in idsToDelete) {
                db.delete(TABLE_TRANSACTIONS, "$COL_ID = ?", arrayOf(id))
                deletedCount++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return deletedCount
    }

    // Card Mappings
    fun saveCardMapping(tail: String, alias: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_MAP_TAIL, tail.trim())
            put(COL_MAP_ALIAS, alias.trim())
        }
        return db.insertWithOnConflict(TABLE_CARD_MAPPINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteCardMapping(tail: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_CARD_MAPPINGS, "$COL_MAP_TAIL = ?", arrayOf(tail.trim()))
    }

    fun getAllCardMappings(): JSONObject {
        val db = readableDatabase
        val obj = JSONObject()
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_CARD_MAPPINGS, null, null, null, null, null, null)
            while (cursor != null && cursor.moveToNext()) {
                val tail = cursor.getString(cursor.getColumnIndexOrThrow(COL_MAP_TAIL))
                val alias = cursor.getString(cursor.getColumnIndexOrThrow(COL_MAP_ALIAS))
                obj.put(tail, alias)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return obj
    }

    // Schedules
    fun saveSchedule(obj: JSONObject): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_SCH_ID, obj.optString("id", "sch-" + System.currentTimeMillis()))
            put(COL_SCH_TITLE, obj.optString("title", "Commitment"))
            put(COL_SCH_AMOUNT, obj.optDouble("amount", 0.0))
            put(COL_SCH_DUE_DAY, obj.optInt("dueDay", 5))
            put(COL_SCH_CATEGORY, obj.optString("category", "Utilities & Bills"))
            put(COL_SCH_PAID, if (obj.optBoolean("paid", false)) 1 else 0)
        }
        return db.insertWithOnConflict(TABLE_SCHEDULES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteSchedule(id: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_SCHEDULES, "$COL_SCH_ID = ?", arrayOf(id))
    }

    fun getAllSchedules(): JSONArray {
        val db = readableDatabase
        val array = JSONArray()
        try {
            db.query(TABLE_SCHEDULES, null, null, null, null, null, "$COL_SCH_DUE_DAY ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    val obj = JSONObject().apply {
                        put("id", cursor.getString(cursor.getColumnIndexOrThrow(COL_SCH_ID)))
                        put("title", cursor.getString(cursor.getColumnIndexOrThrow(COL_SCH_TITLE)))
                        put("amount", cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SCH_AMOUNT)))
                        put("dueDay", cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCH_DUE_DAY)))
                        put("category", cursor.getString(cursor.getColumnIndexOrThrow(COL_SCH_CATEGORY)))
                        put("paid", cursor.getInt(cursor.getColumnIndexOrThrow(COL_SCH_PAID)) == 1)
                    }
                    array.put(obj)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return array
    }

    // ==========================================
    // BUDGETS (SQLite Persistence)
    // ==========================================
    fun saveBudget(obj: JSONObject): Long {
        val db = writableDatabase
        val cat = obj.optString("category", "").trim()
        if (cat.isBlank()) return -1L
        val cv = ContentValues().apply {
            put(COL_BUDGET_CATEGORY, cat)
            put(COL_BUDGET_LIMIT, obj.optDouble("monthlyLimit", 0.0))
            put(COL_BUDGET_THRESHOLD, obj.optDouble("alertThreshold", 0.80))
            put(COL_BUDGET_UPDATED_AT, System.currentTimeMillis())
        }
        return db.insertWithOnConflict(TABLE_BUDGETS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteBudget(category: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_BUDGETS, "$COL_BUDGET_CATEGORY = ?", arrayOf(category.trim()))
    }

    fun getAllBudgets(): JSONArray {
        val db = readableDatabase
        val array = JSONArray()
        try {
            db.query(TABLE_BUDGETS, null, null, null, null, null, "$COL_BUDGET_CATEGORY ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    val obj = JSONObject().apply {
                        put("category", cursor.getString(cursor.getColumnIndexOrThrow(COL_BUDGET_CATEGORY)))
                        put("monthlyLimit", cursor.getDouble(cursor.getColumnIndexOrThrow(COL_BUDGET_LIMIT)))
                        put("alertThreshold", cursor.getDouble(cursor.getColumnIndexOrThrow(COL_BUDGET_THRESHOLD)))
                        put("updatedAt", cursor.getLong(cursor.getColumnIndexOrThrow(COL_BUDGET_UPDATED_AT)))
                    }
                    array.put(obj)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return array
    }

    // ==========================================
    // SAVINGS GOALS (SQLite Persistence)
    // ==========================================
    fun saveSavingsGoal(obj: JSONObject): Long {
        val db = writableDatabase
        val id = obj.optString("id", "goal-" + System.currentTimeMillis())
        val cv = ContentValues().apply {
            put(COL_GOAL_ID, id)
            put(COL_GOAL_TITLE, obj.optString("title", "Savings Goal"))
            put(COL_GOAL_TARGET_AMOUNT, obj.optDouble("targetAmount", 0.0))
            put(COL_GOAL_TARGET_DATE, obj.optString("targetDate", ""))
            put(COL_GOAL_CURRENT_AMOUNT, obj.optDouble("currentAmount", 0.0))
            put(COL_GOAL_CATEGORY, obj.optString("category", "General"))
            put(COL_GOAL_CREATED_AT, obj.optLong("createdAt", System.currentTimeMillis()))
        }
        return db.insertWithOnConflict(TABLE_SAVINGS_GOALS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteSavingsGoal(id: String): Int {
        val db = writableDatabase
        db.delete(TABLE_GOAL_CONTRIBUTIONS, "$COL_CONTRIB_GOAL_ID = ?", arrayOf(id))
        return db.delete(TABLE_SAVINGS_GOALS, "$COL_GOAL_ID = ?", arrayOf(id))
    }

    fun getAllSavingsGoals(): JSONArray {
        val db = readableDatabase
        val array = JSONArray()
        try {
            db.query(TABLE_SAVINGS_GOALS, null, null, null, null, null, "$COL_GOAL_TARGET_DATE ASC").use { cursor ->
                while (cursor.moveToNext()) {
                    val obj = JSONObject().apply {
                        put("id", cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_ID)))
                        put("title", cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_TITLE)))
                        put("targetAmount", cursor.getDouble(cursor.getColumnIndexOrThrow(COL_GOAL_TARGET_AMOUNT)))
                        put("targetDate", cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_TARGET_DATE)))
                        put("currentAmount", cursor.getDouble(cursor.getColumnIndexOrThrow(COL_GOAL_CURRENT_AMOUNT)))
                        put("category", cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_CATEGORY)))
                        put("createdAt", cursor.getLong(cursor.getColumnIndexOrThrow(COL_GOAL_CREATED_AT)))
                    }
                    array.put(obj)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return array
    }

    fun addGoalContribution(obj: JSONObject): Long {
        val db = writableDatabase
        val goalId = obj.optString("goalId", "")
        if (goalId.isBlank()) return -1L
        val amount = obj.optDouble("amount", 0.0)
        val id = obj.optString("id", "contrib-" + System.currentTimeMillis())

        db.beginTransaction()
        try {
            val cv = ContentValues().apply {
                put(COL_CONTRIB_ID, id)
                put(COL_CONTRIB_GOAL_ID, goalId)
                put(COL_CONTRIB_AMOUNT, amount)
                put(COL_CONTRIB_DATE, obj.optString("date", ""))
                put(COL_CONTRIB_NOTE, obj.optString("note", ""))
                put(COL_CONTRIB_CREATED_AT, System.currentTimeMillis())
            }
            val res = db.insertWithOnConflict(TABLE_GOAL_CONTRIBUTIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            if (res != -1L) {
                db.execSQL(
                    "UPDATE $TABLE_SAVINGS_GOALS SET $COL_GOAL_CURRENT_AMOUNT = $COL_GOAL_CURRENT_AMOUNT + ? WHERE $COL_GOAL_ID = ?",
                    arrayOf(amount, goalId)
                )
            }
            db.setTransactionSuccessful()
            return res
        } catch (e: Exception) {
            e.printStackTrace()
            return -1L
        } finally {
            db.endTransaction()
        }
    }

    fun getGoalContributions(goalId: String): JSONArray {
        val db = readableDatabase
        val array = JSONArray()
        try {
            db.query(
                TABLE_GOAL_CONTRIBUTIONS,
                null,
                "$COL_CONTRIB_GOAL_ID = ?",
                arrayOf(goalId),
                null,
                null,
                "$COL_CONTRIB_DATE DESC, $COL_CONTRIB_CREATED_AT DESC"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val obj = JSONObject().apply {
                        put("id", cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTRIB_ID)))
                        put("goalId", cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTRIB_GOAL_ID)))
                        put("amount", cursor.getDouble(cursor.getColumnIndexOrThrow(COL_CONTRIB_AMOUNT)))
                        put("date", cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTRIB_DATE)))
                        put("note", cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTRIB_NOTE)) ?: "")
                        put("createdAt", cursor.getLong(cursor.getColumnIndexOrThrow(COL_CONTRIB_CREATED_AT)))
                    }
                    array.put(obj)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return array
    }

    // ==========================================
    // DISMISSED RECURRING SUBSCRIPTIONS
    // ==========================================
    fun dismissRecurring(signature: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_DISMISSED_SIG, signature.trim().lowercase())
            put(COL_DISMISSED_AT, System.currentTimeMillis())
        }
        return db.insertWithOnConflict(TABLE_DISMISSED_RECURRING, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getDismissedRecurring(): JSONArray {
        val db = readableDatabase
        val array = JSONArray()
        try {
            db.query(TABLE_DISMISSED_RECURRING, arrayOf(COL_DISMISSED_SIG), null, null, null, null, null).use { cursor ->
                while (cursor.moveToNext()) {
                    array.put(cursor.getString(0))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return array
    }

    // ==========================================
    // ATOMIC FULL DATABASE RESTORE FROM JSON
    // ==========================================
    fun restoreFullDatabase(root: JSONObject): JSONObject {
        val db = writableDatabase
        val result = JSONObject()
        var txCount = 0
        var schCount = 0
        var budgetCount = 0
        var goalCount = 0
        var contribCount = 0

        db.beginTransaction()
        try {
            // 1. Transactions
            if (root.has("transactions")) {
                val txArray = root.getJSONArray("transactions")
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    val id = obj.optString("id", "tx-" + System.currentTimeMillis() + "-" + i)
                    val cv = ContentValues().apply {
                        put(COL_ID, id)
                        put(COL_TITLE, obj.optString("title", "Payment"))
                        put(COL_TYPE, obj.optString("type", "debit"))
                        put(COL_AMOUNT, obj.optDouble("amount", 0.0))
                        put(COL_CATEGORY, obj.optString("category", "General & Miscellaneous"))
                        put(COL_DATE, obj.optString("date", ""))
                        put(COL_RAW_TEXT, obj.optString("rawText", ""))
                        put(COL_ACCOUNT_TAIL, obj.optString("accountTail", ""))
                        put(COL_PAYMENT_METHOD, obj.optString("paymentMethod", "UPI/Card"))
                        put(COL_CURRENCY_SYMBOL, obj.optString("currencySymbol", "₹"))
                        put(COL_DEDUP_KEY, obj.optString("dedupKey", ""))
                        put(COL_NOTE, obj.optString("note", ""))
                        if (obj.has("balance")) put(COL_BALANCE, obj.optDouble("balance", -1.0))
                        put(COL_CREATED_AT, System.currentTimeMillis())
                    }
                    val res = db.insertWithOnConflict(TABLE_TRANSACTIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                    if (res != -1L) txCount++
                }
            }

            // 2. Schedules
            if (root.has("schedules")) {
                val schArray = root.getJSONArray("schedules")
                for (i in 0 until schArray.length()) {
                    val obj = schArray.getJSONObject(i)
                    val id = obj.optString("id", "sch-" + System.currentTimeMillis() + "-" + i)
                    val cv = ContentValues().apply {
                        put(COL_SCH_ID, id)
                        put(COL_SCH_TITLE, obj.optString("title", "Commitment"))
                        put(COL_SCH_AMOUNT, obj.optDouble("amount", 0.0))
                        put(COL_SCH_DUE_DAY, obj.optInt("dueDay", 5))
                        put(COL_SCH_CATEGORY, obj.optString("category", "Utilities & Bills"))
                        put(COL_SCH_PAID, if (obj.optBoolean("paid", false)) 1 else 0)
                    }
                    val res = db.insertWithOnConflict(TABLE_SCHEDULES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                    if (res != -1L) schCount++
                }
            }

            // 3. Budgets
            if (root.has("budgets")) {
                val bArr = root.getJSONArray("budgets")
                for (i in 0 until bArr.length()) {
                    val obj = bArr.getJSONObject(i)
                    val cat = obj.optString("category", "").trim()
                    if (cat.isNotBlank()) {
                        val cv = ContentValues().apply {
                            put(COL_BUDGET_CATEGORY, cat)
                            put(COL_BUDGET_LIMIT, obj.optDouble("monthlyLimit", 0.0))
                            put(COL_BUDGET_THRESHOLD, obj.optDouble("alertThreshold", 0.80))
                            put(COL_BUDGET_UPDATED_AT, System.currentTimeMillis())
                        }
                        val res = db.insertWithOnConflict(TABLE_BUDGETS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                        if (res != -1L) budgetCount++
                    }
                }
            }

            // 4. Savings Goals
            if (root.has("savingsGoals")) {
                val gArr = root.getJSONArray("savingsGoals")
                for (i in 0 until gArr.length()) {
                    val obj = gArr.getJSONObject(i)
                    val id = obj.optString("id", "goal-" + System.currentTimeMillis() + "-" + i)
                    val cv = ContentValues().apply {
                        put(COL_GOAL_ID, id)
                        put(COL_GOAL_TITLE, obj.optString("title", "Goal"))
                        put(COL_GOAL_TARGET_AMOUNT, obj.optDouble("targetAmount", 0.0))
                        put(COL_GOAL_TARGET_DATE, obj.optString("targetDate", ""))
                        put(COL_GOAL_CURRENT_AMOUNT, obj.optDouble("currentAmount", 0.0))
                        put(COL_GOAL_CATEGORY, obj.optString("category", "General"))
                        put(COL_GOAL_CREATED_AT, obj.optLong("createdAt", System.currentTimeMillis()))
                    }
                    val res = db.insertWithOnConflict(TABLE_SAVINGS_GOALS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                    if (res != -1L) goalCount++
                }
            }

            // 5. Goal Contributions
            if (root.has("goalContributions")) {
                val cArr = root.getJSONArray("goalContributions")
                for (i in 0 until cArr.length()) {
                    val obj = cArr.getJSONObject(i)
                    val id = obj.optString("id", "contrib-" + System.currentTimeMillis() + "-" + i)
                    val cv = ContentValues().apply {
                        put(COL_CONTRIB_ID, id)
                        put(COL_CONTRIB_GOAL_ID, obj.optString("goalId", ""))
                        put(COL_CONTRIB_AMOUNT, obj.optDouble("amount", 0.0))
                        put(COL_CONTRIB_DATE, obj.optString("date", ""))
                        put(COL_CONTRIB_NOTE, obj.optString("note", ""))
                        put(COL_CONTRIB_CREATED_AT, obj.optLong("createdAt", System.currentTimeMillis()))
                    }
                    val res = db.insertWithOnConflict(TABLE_GOAL_CONTRIBUTIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                    if (res != -1L) contribCount++
                }
            }

            // 6. Adaptive Category Rules
            var ruleCount = 0
            if (root.has("categoryRules")) {
                val rObj = root.optJSONObject("categoryRules")
                if (rObj != null) {
                    val keys = rObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val cat = rObj.optString(k, "General")
                        val cleanK = k.lowercase(java.util.Locale.ROOT).replace("[^a-z0-9]".toRegex(), "").trim()
                        if (cleanK.isNotBlank()) {
                            val cv = ContentValues().apply {
                                put(COL_RULE_MERCHANT, cleanK)
                                put(COL_RULE_CATEGORY, cat)
                                put(COL_RULE_UPDATED_AT, System.currentTimeMillis())
                            }
                            val res = db.insertWithOnConflict(TABLE_CATEGORY_RULES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                            if (res != -1L) ruleCount++
                        }
                    }
                }
            }

            db.setTransactionSuccessful()
            result.put("success", true)
            result.put("txCount", txCount)
            result.put("schCount", schCount)
            result.put("budgetCount", budgetCount)
            result.put("goalCount", goalCount)
            result.put("contribCount", contribCount)
        } catch (e: Exception) {
            e.printStackTrace()
            result.put("success", false)
            result.put("error", e.message ?: "Failed to restore database")
        } finally {
            db.endTransaction()
        }
        return result
    }

    // ==========================================
    // ADAPTIVE CATEGORY RULES (AI Local Memory)
    // ==========================================
    fun saveCategoryRule(merchantKey: String, category: String): Long {
        val cleanKey = merchantKey.lowercase(java.util.Locale.ROOT).replace("[^a-z0-9]".toRegex(), "").trim()
        if (cleanKey.isBlank()) return -1L
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_RULE_MERCHANT, cleanKey)
            put(COL_RULE_CATEGORY, category.trim())
            put(COL_RULE_UPDATED_AT, System.currentTimeMillis())
        }
        return db.insertWithOnConflict(TABLE_CATEGORY_RULES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllCategoryRules(): Map<String, String> {
        val rules = mutableMapOf<String, String>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CATEGORY_RULES, arrayOf(COL_RULE_MERCHANT, COL_RULE_CATEGORY), null, null, null, null, null)
        cursor.use {
            val keyIdx = it.getColumnIndex(COL_RULE_MERCHANT)
            val catIdx = it.getColumnIndex(COL_RULE_CATEGORY)
            while (it.moveToNext()) {
                if (keyIdx != -1 && catIdx != -1) {
                    val k = it.getString(keyIdx)
                    val c = it.getString(catIdx)
                    if (k != null && c != null) {
                        rules[k] = c
                    }
                }
            }
        }
        return rules
    }

    fun deleteCategoryRule(merchantKey: String): Int {
        val cleanKey = merchantKey.lowercase(java.util.Locale.ROOT).replace("[^a-z0-9]".toRegex(), "").trim()
        val db = writableDatabase
        return db.delete(TABLE_CATEGORY_RULES, "$COL_RULE_MERCHANT = ?", arrayOf(cleanKey))
    }
}
