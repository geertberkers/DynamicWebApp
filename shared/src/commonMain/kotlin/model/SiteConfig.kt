package model

data class SiteConfig(
    val id: Int,
    val url: String = "",
    val title: String = "",
    val iconUrl: String? = null
) {
    val isConfigured: Boolean
        get() = url.isNotBlank()
}







