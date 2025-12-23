import UIKit

/// Basic app delegate used to keep UIKit lifecycle hooks available for Compose.
/// SwiftUI's `@main` entry point lives in `iOSApp.swift`.
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Hook for future integrations (push notifications, analytics, etc.).
        return true
    }
}

