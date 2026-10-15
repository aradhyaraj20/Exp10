# Experiment 10: Data Sharing & Contacts Management using Contacts ContentProvider

An Android application built in Kotlin demonstrating full integration with Android's system **Contacts ContentProvider** (`ContactsContract`), real-time search filtering, dynamic runtime permissions, and cross-app data sharing via **Implicit Intents** (`Intent.ACTION_SEND`).

---

## 📱 Features

- **➕ Enter & Save Contacts**: Type contact name and phone number to atomically insert raw contacts directly into the system `ContactsContract` database using `ContentProviderOperation` batching.
- **📖 Query & Read Contacts**: Retrieve saved phonebook contacts using `ContentResolver` and display them in customized list card views.
- **🔍 Real-Time Search Filtering**: Instantly search and filter contacts by name or phone number as you type using a `TextWatcher` listener and custom `Filterable` adapter.
- **👤 Contact Details & Dialer Action**: Tap any contact in the list to open a detailed modal dialog with quick action buttons to place calls (`Intent.ACTION_DIAL`) or share info.
- **📤 Cross-App Data Sharing**: Share contact information across third-party applications (Messages, Email, Notes, social apps) via implicit intents (`Intent.ACTION_SEND`).
- **🛡️ Runtime Permissions**: Handle `READ_CONTACTS` and `WRITE_CONTACTS` permissions dynamically using `ActivityResultContracts`.
- **🧪 Testing & Emulator Fallback**: Automatic fallback sample dataset when running on fresh emulators or clean devices without saved contacts.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **Compile / Target SDK**: 37 (Android 15)
- **Minimum SDK**: 24 (Android 7.0)
- **UI Architecture**: Android XML Layouts, AppCompat, Material Components, ConstraintLayout
- **Data Architecture**: `ContentResolver`, `ContactsContract` (`RawContacts`, `Data`), `ContentProviderOperation`
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)

---

## 📂 Project Structure

```
app/
 ├── src/main/
 │    ├── java/com/example/exp10/
 │    │    └── MainActivity.kt          # Main logic: ContentProvider operations, permissions, search filter, Intents
 │    ├── res/
 │    │    ├── layout/
 │    │    │    ├── activity_main.xml          # Main screen layout with input form, search, and list
 │    │    │    ├── item_contact.xml           # Custom card item layout for contact rows
 │    │    │    └── dialog_contact_details.xml # Contact details modal dialog layout
 │    │    └── values/
 │    │         └── strings.xml                # Application string resources
 │    └── AndroidManifest.xml                  # App manifest declaring READ_CONTACTS & WRITE_CONTACTS permissions
 └── build.gradle.kts                          # Module-level build configuration
```

---

## 🔑 Permissions Declared

The application declares and requests the following runtime permissions in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.WRITE_CONTACTS" />
```

---

## 🚀 How to Build & Run

1. **Clone or Open Project**: Open the `Exp10` project directory in **Android Studio**.
2. **Sync Gradle**: Allow Android Studio to sync dependencies and project configuration.
3. **Connect Device / Start Emulator**:
   - Ensure an Android Virtual Device (AVD) is running or connect a physical device with USB Debugging enabled.
4. **Build & Run**:
   - Click the green **Run ('app')** ▶ button in Android Studio or run the following command in terminal:
     ```bash
     ./gradlew :app:installDebug
     ```
5. **Usage Steps**:
   - Grant the `READ_CONTACTS` and `WRITE_CONTACTS` permissions when prompted.
   - Enter a **Name** and **Phone Number** in the "Add New Contact" section and tap **"Save Contact"**.
   - Use the **Search Bar** to filter contacts dynamically.
   - Tap any contact item to view full details or tap the **Share** icon to share contact details via external apps.

---

## 📄 License & Course Record

Developed as part of the **Android Application Development Laboratory (Experiment 10)**.

## images
![Screenshot 2026-10-15 120708.png](images/Screenshot%202026-10-15%20120708.png)
![WhatsApp Image 2026-10-15 at 12.23.26 PM.jpeg](images/WhatsApp%20Image%202026-10-15%20at%2012.23.26%20PM.jpeg)
![WhatsApp Image 2026-10-15 at 12.23.27 PM.jpeg](images/WhatsApp%20Image%202026-10-15%20at%2012.23.27%20PM.jpeg)
![WhatsApp Image 2026-10-15 at 12.23.27 PM (1).jpeg](images/WhatsApp%20Image%202026-10-15%20at%2012.23.27%20PM%20%281%29.jpeg)