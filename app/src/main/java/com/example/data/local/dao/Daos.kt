package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItemEntity>
)

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY updatedAt DESC")
    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: Long)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsForInvoice(invoiceId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Transaction
    suspend fun saveFullInvoice(invoice: InvoiceEntity, items: List<InvoiceItemEntity>): Long {
        val invoiceId = if (invoice.id == 0L) {
            insertInvoice(invoice)
        } else {
            updateInvoice(invoice)
            invoice.id
        }
        deleteItemsForInvoice(invoiceId)
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        insertInvoiceItems(itemsWithId)
        return invoiceId
    }

    @Query("DELETE FROM invoices")
    suspend fun clearAllInvoices()

    @Query("DELETE FROM invoice_items")
    suspend fun clearAllInvoiceItems()
}

@Dao
interface CatalogItemDao {
    @Query("SELECT * FROM catalog_items ORDER BY name ASC")
    fun getAllCatalogItems(): Flow<List<CatalogItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CatalogItemEntity): Long

    @Update
    suspend fun updateItem(item: CatalogItemEntity)

    @Query("DELETE FROM catalog_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CatalogItemEntity>)

    @Query("DELETE FROM catalog_items")
    suspend fun clearAllCatalogItems()
}

@Dao
interface BusinessProfileDao {
    @Query("SELECT * FROM business_profiles ORDER BY name ASC")
    fun getAllBusinessProfiles(): Flow<List<BusinessProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BusinessProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: BusinessProfileEntity)

    @Query("DELETE FROM business_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<BusinessProfileEntity>)

    @Query("DELETE FROM business_profiles")
    suspend fun clearAllProfiles()
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettingsEntity)
}
