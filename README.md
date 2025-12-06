# DynamicWebApp

A Kotlin Multiplatform app that wraps websites in a WebView with a customizable bottom navigation bar.

## Features

- **4 configurable website slots** - Add any website URL and the app will fetch its metadata (title, favicon)
- **Settings screen** - Configure all 4 sites with URL input and automatic metadata fetching
- **Material 3 UI** - Modern, beautiful interface using Compose Multiplatform
- **Cross-platform** - Runs on both Android and iOS with shared business logic

## Project Structure

```
DynamicWebApp/
├── shared/              # Shared Kotlin Multiplatform module
│   ├── commonMain/      # Common code (models, data, UI)
│   ├── androidMain/     # Android-specific implementations
│   └── iosMain/         # iOS-specific implementations
├── androidApp/          # Android application module
└── iosApp/              # iOS application files
```

## Building

### Android

1. Open the project in Android Studio
2. Sync Gradle files
3. Run the `androidApp` configuration

### iOS

1. Open the project in Xcode (or use Gradle to build the framework)
2. The shared module compiles to a framework that can be used in Swift
3. Use `MainViewController()` as the root view controller

## Dependencies

- **Compose Multiplatform** - UI framework
- **Ktor Client** - HTTP client for fetching website metadata
- **Multiplatform Settings** - Persistent storage for site configurations
- **Material 3** - UI components

## Usage

1. Launch the app
2. Navigate to the Settings tab (gear icon)
3. Enter a URL for any of the 4 site slots
4. Tap "Save" - the app will fetch the website's title and favicon
5. Navigate back to the site tabs to view your configured websites

## Loading Sites from JSON URL

You can load predefined sites from a remote JSON URL:

1. In Settings, click "Laad van URL" button
2. Enter a URL pointing to a JSON file with the following structure:
```json
{
  "sites": [
    {
      "category": "📝 Productiviteit & Planning",
      "url": "https://example.com",
      "description": "Description of the site"
    }
  ]
}
```

3. The app will download and merge these sites with the default and custom sites

See `example-sites.json` for a complete example of the JSON format.

## Adding Custom Sites

1. Click the FAB (Floating Action Button) in Settings
2. Fill in:
   - Category (optional): e.g., "📝 Productiviteit & Planning"
   - Name: Display name for the site
   - Link: URL of the site
   - Description: Brief description
3. Click "Toevoegen" to save

