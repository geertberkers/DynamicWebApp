import SwiftUI
import shared

@main
struct DynamicWebApp: App {
    // Keep access to UIKit lifecycle if needed
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ComposeRootView()
                .ignoresSafeArea()
        }
    }
}

