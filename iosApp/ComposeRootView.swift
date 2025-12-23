import SwiftUI
import shared

/// Bridges the Kotlin `MainViewController()` into SwiftUI.
struct ComposeRootView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No-op: state is managed inside the Compose hierarchy.
    }
}

