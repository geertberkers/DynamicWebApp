package app

import androidx.compose.ui.window.ComposeUIViewController
import com.russhwolf.settings.AppleSettings
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

fun MainViewController(): platform.UIKit.UIViewController {
    val settings: Settings = AppleSettings(NSUserDefaults.standardUserDefaults)
    val httpClient = HttpClient(Darwin.create()) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    return ComposeUIViewController {
        App(settings = settings, httpClient = httpClient)
    }
}

