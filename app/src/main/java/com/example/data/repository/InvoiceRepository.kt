package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.dao.InvoiceWithItems
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(private val database: AppDatabase) {
    private val invoiceDao = database.invoiceDao()
    private val catalogDao = database.catalogItemDao()
    private val businessDao = database.businessProfileDao()
    private val settingsDao = database.appSettingsDao()

    // Invoices
    val allInvoices: Flow<List<InvoiceWithItems>> = invoiceDao.getAllInvoicesWithItems()

    suspend fun getInvoiceById(id: Long): InvoiceWithItems? = invoiceDao.getInvoiceWithItemsById(id)

    suspend fun saveInvoice(invoice: InvoiceEntity, items: List<InvoiceItemEntity>): Long =
        invoiceDao.saveFullInvoice(invoice, items)

    suspend fun deleteInvoice(id: Long) = invoiceDao.deleteInvoiceById(id)

    // Catalog Items
    val allCatalogItems: Flow<List<CatalogItemEntity>> = catalogDao.getAllCatalogItems()

    suspend fun saveCatalogItem(item: CatalogItemEntity): Long =
        if (item.id == 0L) catalogDao.insertItem(item) else {
            catalogDao.updateItem(item)
            item.id
        }

    suspend fun deleteCatalogItem(id: Long) = catalogDao.deleteItemById(id)

    // Business Profiles
    val allBusinessProfiles: Flow<List<BusinessProfileEntity>> = businessDao.getAllBusinessProfiles()

    suspend fun saveBusinessProfile(profile: BusinessProfileEntity): Long =
        if (profile.id == 0L) businessDao.insertProfile(profile) else {
            businessDao.updateProfile(profile)
            profile.id
        }

    suspend fun deleteBusinessProfile(id: Long) = businessDao.deleteProfileById(id)

    // App Settings
    val settingsFlow: Flow<AppSettingsEntity?> = settingsDao.getSettingsFlow()

    suspend fun getSettings(): AppSettingsEntity =
        settingsDao.getSettings() ?: AppSettingsEntity().also { settingsDao.insertOrUpdate(it) }

    suspend fun updateSettings(settings: AppSettingsEntity) =
        settingsDao.insertOrUpdate(settings)

    // Reset / Import Database
    suspend fun resetDatabase() {
        invoiceDao.clearAllInvoices()
        invoiceDao.clearAllInvoiceItems()
        catalogDao.clearAllCatalogItems()
        businessDao.clearAllProfiles()
        AppDatabase.populateInitialData(database)
    }

    suspend fun importFullData(
        invoices: List<Pair<InvoiceEntity, List<InvoiceItemEntity>>>,
        items: List<CatalogItemEntity>,
        businesses: List<BusinessProfileEntity>,
        settings: AppSettingsEntity?
    ) {
        invoiceDao.clearAllInvoices()
        invoiceDao.clearAllInvoiceItems()
        catalogDao.clearAllCatalogItems()
        businessDao.clearAllProfiles()

        items.forEach { catalogDao.insertItem(it.copy(id = 0)) }
        businesses.forEach { businessDao.insertProfile(it.copy(id = 0)) }
        invoices.forEach { (inv, invItems) ->
            invoiceDao.saveFullInvoice(inv.copy(id = 0), invItems.map { it.copy(id = 0) })
        }
        settings?.let { settingsDao.insertOrUpdate(it) }
    }
}
