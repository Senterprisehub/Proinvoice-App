package com.example.model

enum class InvoiceStatus(val label: String, val badgeColorHex: Long, val textColorHex: Long) {
    DRAFT("Draft", 0xFFE2E8F0, 0xFF475569),
    SENT("Sent", 0xFFDBEAFE, 0xFF1E40AF),
    PAID("Paid", 0xFFD1FAE5, 0xFF065F46),
    OVERDUE("Overdue", 0xFFFEE2E2, 0xFF991B1B)
}

enum class DiscountType(val symbol: String) {
    PERCENT("%"),
    FLAT("Flat")
}

sealed interface AutoSaveStatus {
    object Idle : AutoSaveStatus
    object Saving : AutoSaveStatus
    data class Saved(val timestamp: Long, val timeFormatted: String) : AutoSaveStatus
    data class Error(val message: String) : AutoSaveStatus
}

enum class InvoiceLayout(
    val label: String,
    val subtitle: String,
    val icon: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val description: String
) {
    MODERN("Modern Slate", "Clean & Contemporary", "🚀", 0xFF82B1FF, 0xFF2979FF, "Clean geometric cards, crisp accent pill headers, floating totals banner"),
    CORPORATE("Corporate Pro", "Enterprise Standard", "🏢", 0xFF90CAF9, 0xFF1976D2, "Navy executive header, structured dual address blocks, official signoff section"),
    ELEGANT("Luxe Atelier", "Premium & Refined", "✨", 0xFFFFD54F, 0xFFFFA000, "Warm champagne gold accents, serif header typography, delicate hairline frames"),
    MINIMAL("Nordic Minimal", "Ultra-Clean Space", "🌿", 0xFF80CBC4, 0xFF00897B, "Generous negative space, soft teal accents, floating borderless line items"),
    BOLD("Bauhaus Neo", "High Contrast & Impact", "⚡", 0xFFFF8A80, 0xFFD50000, "Vibrant high-contrast dark banners, solid divider lines, bold summary highlights"),
    SOFT("Pastel Studio", "Warm Creative Studio", "🌸", 0xFFF48FB1, 0xFFD81B60, "Gentle rose-blush container cards, rounded badges, friendly modern typography"),
    REMITTANCE("Fintech Receipt", "Direct Pay & QR", "💳", 0xFF69F0AE, 0xFF00C853, "Emerald fintech aesthetic, payment tear-off slip, compact tabular rows"),
    FORMAL("Formal GST Tax", "Statutory & Tax Ready", "📋", 0xFFD0BCFF, 0xFF6750A4, "Complete GST/Tax statutory layout, HSN/SAC & CGST/SGST columns, authorized signatory")
}

data class InvoiceItem(
    val id: Long = 0,
    val description: String = "",
    val quantity: Double = 1.0,
    val rate: Double = 0.0,
    val taxable: Double = 0.0
) {
    val total: Double
        get() = quantity * taxable
}
