package model

sealed class Tab {
    data class Site(val index: Int) : Tab()
    object Settings : Tab()
}







