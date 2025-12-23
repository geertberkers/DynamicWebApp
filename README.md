# DynamicWebApp

A Kotlin Multiplatform app that wraps websites in a WebView with a customizable bottom navigation bar.

## Features

- **4 configurable website slots** – Add any website URL and the app will fetch its metadata (title, favicon).
- **Settings screen** – Configure all 4 sites with URL input and automatic metadata fetching.
- **Material 3 UI** – Modern, beautiful interface using Compose Multiplatform.
- **Cross‑platform** – Runs on both Android and iOS with shared business logic.

## Project Structure

```text
DynamicWebApp/
├── shared/              # Shared Kotlin Multiplatform module
│   ├── commonMain/      # Common code (models, data, UI)
│   ├── androidMain/     # Android-specific implementations
│   └── iosMain/         # iOS-specific implementations
├── androidApp/          # Android application module
└── iosApp/              # iOS application files
```

## Getting Started

### Prerequisites

- JDK 17+.
- Android Studio (for Android).
- Xcode on macOS (for iOS).

## Building

### Android

1. Open the project in Android Studio.
2. Let Gradle sync.
3. Select and run the `androidApp` run configuration on an emulator or device.

### iOS

#### Prerequisites

- Xcode (latest stable).
- XcodeGen installed: `brew install xcodegen`.

#### Steps

1. From the `iosApp` folder, generate the Xcode project:
   ```bash
   cd iosApp
   xcodegen generate
   ```
2. Open the generated project:
   ```bash
   open DynamicWebApp.xcodeproj
   ```
3. In Xcode, select the `DynamicWebApp` scheme and a simulator (or device).
4. Press **Run**.

The Xcode project can be configured to call Gradle (e.g. `./gradlew :shared:packForXcode`) in a prebuild script to build and embed the shared KMM framework. The Swift/SwiftUI host then renders the Kotlin `MainViewController()` from the shared module.

## Usage

1. Launch the app.
2. Go to the **Settings** tab (gear icon).
3. Enter a URL for any of the 4 site slots.
4. Tap **Save** – the app will fetch the website's title and favicon.
5. Switch back to the site tabs to view your configured websites.

## Loading Sites from JSON URL

You can load predefined sites from a remote JSON URL:

1. In Settings, click **Laad van URL**.
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

3. The app will download and merge these sites with the default and custom sites.

See `example-sites.json` for a complete example of the JSON format.

## Adding Custom Sites

1. Click the FAB (Floating Action Button) in Settings.
2. Fill in:
   - Category (optional), e.g. `📝 Productiviteit & Planning`
   - Name – display name for the site
   - Link – URL of the site
   - Description – brief description
3. Click **Toevoegen** to save.

# Welcome to your Lovable project

## Project info

**URL**: https://lovable.dev/projects/be3a54af-4f31-4068-994c-103a8cdba1e4

## How can I edit this code?

There are several ways of editing your application.

**Use Lovable**

Simply visit the [Lovable Project](https://lovable.dev/projects/be3a54af-4f31-4068-994c-103a8cdba1e4) and start prompting.

Changes made via Lovable will be committed automatically to this repo.

**Use your preferred IDE**

If you want to work locally using your own IDE, you can clone this repo and push changes. Pushed changes will also be reflected in Lovable.

The only requirement is having Node.js & npm installed - [install with nvm](https://github.com/nvm-sh/nvm#installing-and-updating)

Follow these steps:

```sh
# Step 1: Clone the repository using the project's Git URL.
git clone <YOUR_GIT_URL>

# Step 2: Navigate to the project directory.
cd <YOUR_PROJECT_NAME>

# Step 3: Install the necessary dependencies.
npm i

# Step 4: Start the development server with auto-reloading and an instant preview.
npm run dev
```

**Edit a file directly in GitHub**

- Navigate to the desired file(s).
- Click the "Edit" button (pencil icon) at the top right of the file view.
- Make your changes and commit the changes.

**Use GitHub Codespaces**

- Navigate to the main page of your repository.
- Click on the "Code" button (green button) near the top right.
- Select the "Codespaces" tab.
- Click on "New codespace" to launch a new Codespace environment.
- Edit files directly within the Codespace and commit and push your changes once you're done.

## What technologies are used for this project?

This project is built with:

- Vite
- TypeScript
- React
- shadcn-ui
- Tailwind CSS

## How can I deploy this project?

Simply open [Lovable](https://lovable.dev/projects/be3a54af-4f31-4068-994c-103a8cdba1e4) and click on Share -> Publish.

## Can I connect a custom domain to my Lovable project?

Yes, you can!

To connect a domain, navigate to Project > Settings > Domains and click Connect Domain.

Read more here: [Setting up a custom domain](https://docs.lovable.dev/features/custom-domain#custom-domain)
