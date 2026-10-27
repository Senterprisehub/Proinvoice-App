package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import com.example.model.DiscountType
import com.example.model.InvoiceLayout
import com.example.model.InvoiceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromInvoiceStatus(value: InvoiceStatus): String = value.name

    @TypeConverter
    fun toInvoiceStatus(value: String): InvoiceStatus = try {
        InvoiceStatus.valueOf(value)
    } catch (e: Exception) {
        InvoiceStatus.DRAFT
    }

    @TypeConverter
    fun fromDiscountType(value: DiscountType): String = value.name

    @TypeConverter
    fun toDiscountType(value: String): DiscountType = try {
        DiscountType.valueOf(value)
    } catch (e: Exception) {
        DiscountType.PERCENT
    }

    @TypeConverter
    fun fromInvoiceLayout(value: InvoiceLayout): String = value.name

    @TypeConverter
    fun toInvoiceLayout(value: String): InvoiceLayout = try {
        InvoiceLayout.valueOf(value)
    } catch (e: Exception) {
        InvoiceLayout.FORMAL
    }
}

@Database(
    entities = [
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        CatalogItemEntity::class,
        BusinessProfileEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun invoiceDao(): InvoiceDao
    abstract fun catalogItemDao(): CatalogItemDao
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pro_invoice_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            // Default App Settings
            db.appSettingsDao().insertOrUpdate(
                AppSettingsEntity(
                    id = 1,
                    currency = "₹",
                    defaultCgst = 9.0,
                    defaultSgst = 9.0,
                    defaultNotes = "Payment terms: Net 15 days.\nThank you for choosing our services!",
                    defaultLayout = InvoiceLayout.FORMAL,
                    isDarkMode = false,
                    adminUsername = "Admin2026",
                    adminPassword = "Password@0"
                )
            )

            // Default Businesses
            val acme = BusinessProfileEntity(
                name = "Apex Innovations Corp",
                address = "742 Evergreen Terrace, Sector 4, Silicon Hub, Bengaluru 560001",
                details = "GSTIN: 29AAAAA0000A1Z5\nPhone: +91 98765 43210\nEmail: billing@apexinno.com"
            )
            val bizId = db.businessProfileDao().insertProfile(acme)

            // Default Catalog items
            val item1 = CatalogItemEntity(name = "Afrezza 8U Inhaler Device", description = "With inhaler cartridges pack", rate = 4599.0)
            val item2 = CatalogItemEntity(name = "Cloud Strategy Consulting", description = "Architecture review & implementation", rate = 12500.0)
            val item3 = CatalogItemEntity(name = "Enterprise Software License", description = "Annual subscription per seat", rate = 28000.0)
            db.catalogItemDao().insertAll(listOf(item1, item2, item3))

            // Initial Sample Invoice
            val subtotal = 4599.0 / (1.0 + 0.18)
            val cgstAmt = subtotal * 0.09
            val sgstAmt = subtotal * 0.09
            val total = subtotal + cgstAmt + sgstAmt

            val sampleInvoice = InvoiceEntity(
                invoiceNumber = "INV-100482",
                dateIso = "2026-08-13 10:30",
                dueDateIso = "2026-08-28",
                poNumber = "PO-9941",
                billFrom = "Apex Innovations Corp\n742 Evergreen Terrace, Sector 4, Silicon Hub, Bengaluru 560001\nGSTIN: 29AAAAA0000A1Z5",
                billTo = "Global Health Solutions Ltd.\n12 Industrial Estate, Outer Ring Road\nBengaluru, KA 560103\nContact: procurement@globalhealth.org",
                notes = "Payment terms: Net 15 days.\nBank: HDFC Bank | A/C: 50200012345678 | IFSC: HDFC0001234\nThank you for choosing our services!",
                discountValue = 0.0,
                discountType = DiscountType.PERCENT,
                status = InvoiceStatus.SENT,
                layout = InvoiceLayout.FORMAL,
                currency = "₹",
                cgstPercent = 9.0,
                sgstPercent = 9.0,
                subtotal = subtotal,
                discountAmount = 0.0,
                cgstAmount = cgstAmt,
                sgstAmount = sgstAmt,
                grandTotal = total
            )
            val sampleItems = listOf(
                InvoiceItemEntity(
                    invoiceId = 0,
                    description = "Afrezza 8U Inhaler Device\nWith inhaler cartridges pack",
                    quantity = 1.0,
                    rate = 4599.0,
                    taxableRate = subtotal,
                    total = subtotal
                )
            )
            db.invoiceDao().saveFullInvoice(sampleInvoice, sampleItems)
        }
    }
}
