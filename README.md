# Genshin Bills Tracker & Recovery Vault 🛡️💎

> A privacy-focused, 100% offline-capable local Java web application designed to track personal spending in **Genshin Impact**, analyze purchase statistics, and securely vault official purchase receipts and order IDs required for **HoYoverse Customer Support (CS) Account Recovery**.

---

## Key Highlights

- **✨ Smart Invoice Screenshot Scanner**:
  - Drag and drop your invoice screenshot (Google Play Store, Apple App Store, PayPal, Codashop, Razer Gold, or PC Worldpay).
  - **Local OCR & Pattern Recognizer**: Parses order numbers (`GPA.xxxx-xxxx-xxxx-xxxxx`, Apple Order IDs, PayPal transaction IDs), amounts, currencies, purchase dates, and item types (Welkin Moon, Battle Pass, Genesis Crystals) directly in your browser.
  - **Optional Gemini Vision AI Mode**: Add your Google Gemini API key in Settings for AI-powered recognition of cropped or non-standard receipts.
- **🛡️ HoYoverse Recovery Dossier Generator**:
  - Automatically structures your account recovery answers matching HoYoverse's exact Account Retrieval Questionnaire.
  - Highlights your **First Purchase Proof** (the #1 deciding verification factor for HoYoverse CS).
  - **One-Click Dossier Export**: Downloads a ready-to-use `.zip` package containing formatted answers (`hoyoverse_recovery_info.txt`) and cleanly numbered receipt files (`01_FIRST_PURCHASE_...`, `02_RECENT_...`).
- **📊 Financial Spending Analytics**:
  - Lifetime total spend and monthly spending trends (interactive Chart.js graphs).
  - Breakdown by item category (Welkin Moon vs Battle Pass vs Crystals).
  - Multi-currency support (USD, VND, EUR, JPY, GBP) with automatic normalization into your base currency.
- **🔒 100% Private, Local & Zero-Config**:
  - All sensitive financial data, partial card numbers, and screenshots remain strictly on your local computer.
  - Powered by an embedded file-persisted **H2 Database** (`./data/bills.mv.db`) with zero external database servers to install.
- **📦 Full Backup & Restore**:
  - One-click full `.zip` export and restore of all database records and image files.

---

## Tech Stack & Architecture

- **Backend**: Java 17 LTS, Spring Boot 3.3.4 (Spring Web MVC, Spring Data JPA, Jakarta Validation)
- **Database**: Embedded file-persisted H2 Database
- **Frontend**: Responsive Thymeleaf views, Tailwind CSS, Chart.js, FontAwesome 6
- **OCR Engine**: Tesseract.js (WebAssembly client-side) + Java Regex & Heuristic Parser
- **Build Tool**: Apache Maven Wrapper (`mvnw` / `mvnw.cmd`)

---

## Quick Start (How to Run)

### 1. Prerequisites
- **Java 17 or higher** installed on your system (`java -version`).
- No global Maven installation is required (the included `mvnw.cmd` handles everything).

### 2. Launch the Application

In your terminal (PowerShell or Command Prompt):

```powershell
.\mvnw.cmd spring-boot:run
```

Once started, open your web browser at:
```
http://localhost:8080
```

### 3. Package into Standalone Executable JAR

To build a standalone executable `.jar` file:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Run the built JAR directly with:
```powershell
java -jar target/genshin-bills-tracker-1.0.0.jar
```

---

## Core Usage Guide

### 1. Register Your Account Profile
1. Navigate to **Accounts** &rarr; **Add Genshin Account**.
2. Enter your in-game UID (e.g. `800123456`), Server region, and Nickname.
3. Fill in your Registration Date and Device (used for HoYoverse Recovery Questionnaire).

### 2. Record / Scan a Purchase Bill
1. Navigate to **Add / Scan Bill**.
2. **Drag & Drop** your receipt screenshot into the scanner box.
3. The scanner will automatically detect the **Order ID**, **Amount**, **Item Category**, and **Date**.
4. Check **⭐ First Purchase on this Account** if this was the first money spent on this UID.
5. Click **Save Bill Record**.

### 3. Account Recovery Center
1. Navigate to **Recovery Center**.
2. Select your account from the dropdown.
3. Review your **First Purchase Proof** and registration specs.
4. Click **Copy Text** to immediately copy pre-formatted answers to paste into HoYoverse's Customer Support form.
5. Click **Download Recovery Dossier (.ZIP)** to get the complete package containing all relevant receipts and recovery instructions.

### 4. Settings & Vault Backup
1. Navigate to **Settings**.
2. Set your **Base Currency** and exchange rates.
3. (Optional) Enter a **Gemini API Key** to enable AI Vision receipt scanning.
4. Click **Export Backup Archive (.ZIP)** at any time to save a full backup of all your receipts and database.

---

## License & Privacy Notice
All receipt files and database data are stored locally in the `./data/` folder on your machine. This software is completely offline and does not transmit financial details to any third-party server.
