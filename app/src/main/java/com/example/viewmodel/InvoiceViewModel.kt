package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.dao.InvoiceWithItems
import com.example.data.local.entity.*
import com.example.data.repository.InvoiceRepository
import com.example.model.*
import com.example.util.DataBackupUtil
import com.example.util.NetworkConnectivityObserver
import com.example.util.NetworkStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class EditorState(
    val invoiceId: Long = 0,
    val invoiceNumber: String = "",
    val dateIso: String = "",
    val dueDateIso: String = "",
    val poNumber: String = "",
    val billFrom: String = "",
    val billTo: String = "",
    val notes: String = "",
    val discountValue: Double = 0.0,
    val discountType: DiscountType = DiscountType.PERCENT,
    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val layout: InvoiceLayout = InvoiceLayout.FORMAL,
    val currency: String = "$",
    val cgstPercent: Double = 0.0,
    val sgstPercent: Double = 0.0,
    val items: List<InvoiceItem> = listOf(InvoiceItem(description = "", quantity = 1.0, rate = 0.0, taxable = 0.0))
) {
    val subtotal: Double
        get() = items.sumOf { it.total }

    val discountAmount: Double
        get() = if (discountType == DiscountType.PERCENT) {
            subtotal * (discountValue / 100.0)
        } else {
            discountValue
        }.coerceAtMost(subtotal)

    val netAfterDiscount: Double
        get() = (subtotal - discountAmount).coerceAtLeast(0.0)

    val cgstAmount: Double
        get() = netAfterDiscount * (cgstPercent / 100.0)

    val sgstAmount: Double
        get() = netAfterDiscount * (sgstPercent / 100.0)

    val grandTotal: Double
        get() = netAfterDiscount + cgstAmount + sgstAmount
}

class InvoiceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = InvoiceRepository(AppDatabase.getDatabase(application, viewModelScope))
    private val networkObserver = NetworkConnectivityObserver(application)

    // Live Network Connectivity status (supports both offline & online seamlessly)
    val networkStatus: StateFlow<NetworkStatus> = networkObserver.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), networkObserver.getCurrentStatus())

    // Invoices flow
    val allInvoices: StateFlow<List<InvoiceWithItems>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Catalog Items flow
    val catalogItems: StateFlow<List<CatalogItemEntity>> = repository.allCatalogItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Business Profiles flow
    val businessProfiles: StateFlow<List<BusinessProfileEntity>> = repository.allBusinessProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings flow
    val appSettings: StateFlow<AppSettingsEntity> = repository.settingsFlow
        .map { it ?: AppSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

    // Filter and search query for dashboard
    private val _statusFilter = MutableStateFlow<InvoiceStatus?>(null)
    val statusFilter: StateFlow<InvoiceStatus?> = _statusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Admin authentication state (unlocked by default - no password required)
    private val _isAdminLoggedIn = MutableStateFlow(true)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // Toast / feedback message
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Active Editor State
    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    // Auto-Save Draft State & Engine
    private val _autoSaveStatus = MutableStateFlow<AutoSaveStatus>(AutoSaveStatus.Idle)
    val autoSaveStatus: StateFlow<AutoSaveStatus> = _autoSaveStatus.asStateFlow()

    private var isInitializingEditor = false

    init {
        viewModelScope.launch {
            _editorState
                .drop(1)
                .debounce(700L)
                .collectLatest { state ->
                    if (!isInitializingEditor && shouldAutoSave(state)) {
                        performAutoSave(state)
                    }
                }
        }
    }

    private fun shouldAutoSave(state: EditorState): Boolean {
        if (state.invoiceNumber.isBlank()) return false
        return state.invoiceId > 0L ||
                state.billTo.isNotBlank() ||
                state.billFrom.isNotBlank() ||
                state.notes.isNotBlank() ||
                state.poNumber.isNotBlank() ||
                state.items.any { it.description.isNotBlank() || it.rate > 0.0 }
    }

    private suspend fun performAutoSave(state: EditorState) {
        _autoSaveStatus.value = AutoSaveStatus.Saving
        try {
            val entity = InvoiceEntity(
                id = state.invoiceId,
                invoiceNumber = state.invoiceNumber.ifBlank { "INV-" + (100000 + Random().nextInt(900000)) },
                dateIso = state.dateIso,
                dueDateIso = state.dueDateIso,
                poNumber = state.poNumber,
                billFrom = state.billFrom,
                billTo = state.billTo,
                notes = state.notes,
                discountValue = state.discountValue,
                discountType = state.discountType,
                status = state.status,
                layout = state.layout,
                currency = state.currency,
                cgstPercent = state.cgstPercent,
                sgstPercent = state.sgstPercent,
                subtotal = state.subtotal,
                discountAmount = state.discountAmount,
                cgstAmount = state.cgstAmount,
                sgstAmount = state.sgstAmount,
                grandTotal = state.grandTotal,
                updatedAt = System.currentTimeMillis()
            )

            val itemEntities = state.items.map {
                InvoiceItemEntity(
                    id = it.id,
                    invoiceId = state.invoiceId,
                    description = it.description,
                    quantity = it.quantity,
                    rate = it.rate,
                    taxableRate = it.taxable,
                    total = it.total
                )
            }

            val savedId = repository.saveInvoice(entity, itemEntities)
            if (state.invoiceId != savedId) {
                isInitializingEditor = true
                _editorState.value = _editorState.value.copy(invoiceId = savedId)
                isInitializingEditor = false
            }
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            _autoSaveStatus.value = AutoSaveStatus.Saved(System.currentTimeMillis(), timeStr)
        } catch (e: Exception) {
            _autoSaveStatus.value = AutoSaveStatus.Error(e.localizedMessage ?: "Auto-save error")
        }
    }

    fun autoSaveDraftNow() {
        val state = _editorState.value
        if (shouldAutoSave(state)) {
            viewModelScope.launch {
                performAutoSave(state)
            }
        }
    }

    fun setStatusFilter(status: InvoiceStatus?) {
        _statusFilter.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startNewInvoice() {
        isInitializingEditor = true
        _autoSaveStatus.value = AutoSaveStatus.Idle
        val settings = appSettings.value
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dueFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance()
        val dateStr = dateFormat.format(now.time)
        now.add(Calendar.DAY_OF_YEAR, 15)
        val dueStr = dueFormat.format(now.time)

        val randomSuffix = (100000 + Random().nextInt(900000)).toString()
        val initialItems = listOf(InvoiceItem(description = "", quantity = 1.0, rate = 0.0, taxable = 0.0))

        _editorState.value = EditorState(
            invoiceId = 0,
            invoiceNumber = "INV-$randomSuffix",
            dateIso = dateStr,
            dueDateIso = dueStr,
            poNumber = "",
            billFrom = "",
            billTo = "",
            notes = settings.defaultNotes,
            discountValue = 0.0,
            discountType = DiscountType.PERCENT,
            status = InvoiceStatus.DRAFT,
            layout = settings.defaultLayout,
            currency = settings.currency,
            cgstPercent = settings.defaultCgst,
            sgstPercent = settings.defaultSgst,
            items = initialItems
        )
        isInitializingEditor = false
    }

    fun loadInvoiceForEditing(invoiceWithItems: InvoiceWithItems) {
        isInitializingEditor = true
        val inv = invoiceWithItems.invoice
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(inv.updatedAt))
        _autoSaveStatus.value = AutoSaveStatus.Saved(inv.updatedAt, timeStr)

        val items = invoiceWithItems.items.map {
            InvoiceItem(
                id = it.id,
                description = it.description,
                quantity = it.quantity,
                rate = it.rate,
                taxable = it.taxableRate
            )
        }.ifEmpty {
            listOf(InvoiceItem(description = "", quantity = 1.0, rate = 0.0, taxable = 0.0))
        }

        _editorState.value = EditorState(
            invoiceId = inv.id,
            invoiceNumber = inv.invoiceNumber,
            dateIso = inv.dateIso,
            dueDateIso = inv.dueDateIso,
            poNumber = inv.poNumber,
            billFrom = inv.billFrom,
            billTo = inv.billTo,
            notes = inv.notes,
            discountValue = inv.discountValue,
            discountType = inv.discountType,
            status = inv.status,
            layout = inv.layout,
            currency = inv.currency,
            cgstPercent = inv.cgstPercent,
            sgstPercent = inv.sgstPercent,
            items = items
        )
        isInitializingEditor = false
    }

    fun updateEditor(transform: (EditorState) -> EditorState) {
        _editorState.value = transform(_editorState.value)
    }

    fun updateItem(index: Int, itemTransform: (InvoiceItem) -> InvoiceItem) {
        val currentItems = _editorState.value.items.toMutableList()
        if (index in currentItems.indices) {
            val updated = itemTransform(currentItems[index])
            currentItems[index] = updated
            _editorState.value = _editorState.value.copy(items = currentItems)
        }
    }

    fun selectCatalogItemForLine(index: Int, catalogItem: CatalogItemEntity) {
        val cgst = _editorState.value.cgstPercent
        val sgst = _editorState.value.sgstPercent
        val totalTaxMultiplier = 1.0 + ((cgst + sgst) / 100.0)
        val computedTaxable = if (totalTaxMultiplier > 0) catalogItem.rate / totalTaxMultiplier else catalogItem.rate

        val fullDesc = if (catalogItem.description.isNotBlank()) {
            "${catalogItem.name}\n${catalogItem.description}"
        } else {
            catalogItem.name
        }

        updateItem(index) {
            it.copy(
                description = fullDesc,
                rate = catalogItem.rate,
                taxable = computedTaxable
            )
        }
    }

    fun applyBusinessToFrom(business: BusinessProfileEntity) {
        val parts = mutableListOf(business.name)
        if (business.address.isNotBlank()) parts.add(business.address)
        if (business.details.isNotBlank()) parts.add(business.details)
        _editorState.value = _editorState.value.copy(billFrom = parts.joinToString("\n"))
        showToast("Loaded business: ${business.name}")
    }

    fun addItem() {
        val currentItems = _editorState.value.items.toMutableList()
        currentItems.add(InvoiceItem(description = "", quantity = 1.0, rate = 0.0, taxable = 0.0))
        _editorState.value = _editorState.value.copy(items = currentItems)
    }

    fun removeItem(index: Int) {
        val currentItems = _editorState.value.items.toMutableList()
        if (currentItems.size > 1 && index in currentItems.indices) {
            currentItems.removeAt(index)
            _editorState.value = _editorState.value.copy(items = currentItems)
        } else {
            showToast("At least one item is required.")
        }
    }

    fun recalculateTaxablesOnTaxChange(cgst: Double, sgst: Double) {
        val totalTaxMultiplier = 1.0 + ((cgst + sgst) / 100.0)
        val currentItems = _editorState.value.items.map { item ->
            if (item.rate > 0) {
                val newTaxable = if (totalTaxMultiplier > 0) item.rate / totalTaxMultiplier else item.rate
                item.copy(taxable = newTaxable)
            } else item
        }
        _editorState.value = _editorState.value.copy(
            cgstPercent = cgst,
            sgstPercent = sgst,
            items = currentItems
        )
    }

    fun saveCurrentInvoice(onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val state = _editorState.value
            val entity = InvoiceEntity(
                id = state.invoiceId,
                invoiceNumber = state.invoiceNumber.ifBlank { "INV-" + (100000 + Random().nextInt(900000)) },
                dateIso = state.dateIso,
                dueDateIso = state.dueDateIso,
                poNumber = state.poNumber,
                billFrom = state.billFrom,
                billTo = state.billTo,
                notes = state.notes,
                discountValue = state.discountValue,
                discountType = state.discountType,
                status = state.status,
                layout = state.layout,
                currency = state.currency,
                cgstPercent = state.cgstPercent,
                sgstPercent = state.sgstPercent,
                subtotal = state.subtotal,
                discountAmount = state.discountAmount,
                cgstAmount = state.cgstAmount,
                sgstAmount = state.sgstAmount,
                grandTotal = state.grandTotal,
                updatedAt = System.currentTimeMillis()
            )

            val itemEntities = state.items.map {
                InvoiceItemEntity(
                    id = it.id,
                    invoiceId = state.invoiceId,
                    description = it.description,
                    quantity = it.quantity,
                    rate = it.rate,
                    taxableRate = it.taxable,
                    total = it.total
                )
            }

            val savedId = repository.saveInvoice(entity, itemEntities)
            showToast("Invoice saved successfully! ✓")
            onSaved(savedId)
        }
    }

    fun deleteInvoice(id: Long) {
        viewModelScope.launch {
            repository.deleteInvoice(id)
            showToast("Invoice deleted.")
        }
    }

    // Catalog Item CRUD
    fun saveCatalogItem(name: String, description: String, rate: Double, editId: Long = 0) {
        viewModelScope.launch {
            repository.saveCatalogItem(
                CatalogItemEntity(
                    id = editId,
                    name = name,
                    description = description,
                    rate = rate
                )
            )
            showToast(if (editId == 0L) "Item added!" else "Item updated!")
        }
    }

    fun deleteCatalogItem(id: Long) {
        viewModelScope.launch {
            repository.deleteCatalogItem(id)
            showToast("Item deleted.")
        }
    }

    // Business Profile CRUD
    fun saveBusinessProfile(name: String, address: String, details: String, editId: Long = 0) {
        viewModelScope.launch {
            repository.saveBusinessProfile(
                BusinessProfileEntity(
                    id = editId,
                    name = name,
                    address = address,
                    details = details
                )
            )
            showToast(if (editId == 0L) "Business added!" else "Business updated!")
        }
    }

    fun deleteBusinessProfile(id: Long) {
        viewModelScope.launch {
            repository.deleteBusinessProfile(id)
            showToast("Business deleted.")
        }
    }

    // Settings Updates
    fun updateSettings(
        currency: String,
        defaultCgst: Double,
        defaultSgst: Double,
        defaultNotes: String,
        defaultLayout: InvoiceLayout,
        isDarkMode: Boolean
    ) {
        viewModelScope.launch {
            val current = appSettings.value
            repository.updateSettings(
                current.copy(
                    currency = currency,
                    defaultCgst = defaultCgst,
                    defaultSgst = defaultSgst,
                    defaultNotes = defaultNotes,
                    defaultLayout = defaultLayout,
                    isDarkMode = isDarkMode
                )
            )
            showToast("Settings saved! ✓")
        }
    }

    fun changeAdminPassword(newPass: String) {
        viewModelScope.launch {
            val current = appSettings.value
            repository.updateSettings(current.copy(adminPassword = newPass))
            showToast("Admin password updated!")
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val current = appSettings.value
            val newMode = !current.isDarkMode
            repository.updateSettings(current.copy(isDarkMode = newMode))
            showToast(if (newMode) "Switched to Dark Theme 🌙" else "Switched to Light Theme ☀️")
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = appSettings.value
            if (current.isDarkMode != enabled) {
                repository.updateSettings(current.copy(isDarkMode = enabled))
                showToast(if (enabled) "Switched to Dark Theme 🌙" else "Switched to Light Theme ☀️")
            }
        }
    }

    // Admin Authentication (No password required - kept for compatibility)
    fun loginAdmin(username: String, pass: String): Boolean {
        _isAdminLoggedIn.value = true
        return true
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = true
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.resetDatabase()
            showToast("Reset to sample demo data.")
        }
    }

    // Offline & Online Data Export & Backup (Device-to-Device Migration)
    fun getBackupJsonString(): String {
        return DataBackupUtil.generateBackupJson(
            invoices = allInvoices.value,
            catalogItems = catalogItems.value,
            businesses = businessProfiles.value,
            settings = appSettings.value
        )
    }

    fun exportAndDownloadBackupFile(context: Context) {
        viewModelScope.launch {
            try {
                val json = getBackupJsonString()
                DataBackupUtil.shareBackupFile(context, json)
                showToast("Backup file ready! Download or share to any device.")
            } catch (e: Exception) {
                showToast("Failed to create backup file: ${e.localizedMessage}")
            }
        }
    }

    fun saveBackupToUri(context: Context, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val json = getBackupJsonString()
                DataBackupUtil.writeStringToUri(context, uri, json)
                showToast("Backup file saved successfully! ✓")
            } catch (e: Exception) {
                showToast("Failed to save file: ${e.localizedMessage}")
            }
        }
    }

    fun exportAndShareBackup(context: Context) {
        exportAndDownloadBackupFile(context)
    }

    fun exportAndShareCsv(context: Context) {
        viewModelScope.launch {
            try {
                val csv = DataBackupUtil.generateCsv(allInvoices.value)
                DataBackupUtil.shareTextData(
                    context = context,
                    title = "Invoices_Report_${System.currentTimeMillis()}.csv",
                    content = csv,
                    mimeType = "text/csv"
                )
                showToast("CSV report generated! Ready to share.")
            } catch (e: Exception) {
                showToast("Failed to create CSV: ${e.localizedMessage}")
            }
        }
    }

    fun importBackupFromUri(context: Context, uri: android.net.Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val jsonStr = DataBackupUtil.readStringFromUri(context, uri)
                val (success, summary) = performImport(jsonStr)
                onResult(success, summary)
            } catch (e: Exception) {
                val msg = "Failed to read backup file: ${e.localizedMessage}"
                showToast(msg)
                onResult(false, msg)
            }
        }
    }

    fun importBackupFromJson(jsonStr: String): Boolean {
        var result = false
        viewModelScope.launch {
            val (success, summary) = performImport(jsonStr)
            result = success
            showToast(summary)
        }
        return true
    }

    private suspend fun performImport(jsonStr: String): Pair<Boolean, String> {
        return try {
            val parsed = DataBackupUtil.parseBackupJson(jsonStr)
            repository.importFullData(
                invoices = parsed.invoices,
                items = parsed.catalogItems,
                businesses = parsed.businesses,
                settings = parsed.settings
            )
            val countInvoices = parsed.invoices.size
            val countItems = parsed.catalogItems.size
            val countBiz = parsed.businesses.size
            val msg = "Successfully restored $countInvoices invoices, $countItems catalog items, and $countBiz business profiles! ✓"
            showToast(msg)
            Pair(true, msg)
        } catch (e: Exception) {
            val err = "Invalid backup format: ${e.localizedMessage}"
            showToast(err)
            Pair(false, err)
        }
    }

    fun showToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.emit(msg)
        }
    }
}
