# 📂 Budget Buddy - POE Part 3 Documentation

**Project Name:** Budget Buddy  
**Development Platform:** Android Studio  
**Language:** Kotlin  
**Database:** Room SQLite  
**Design Language:** Material Design 3 (Material You)

---

## 🚀 1. Application Overview
Budget Buddy is a robust, personal financial management application designed to help users track spending, set financial boundaries, and improve their financial literacy through gamified elements. It provides a highly visual experience with real-time progress tracking and professional-grade reporting tools.

---

## 🎯 2. Rubric Fulfillment (Mapping)

| Rubric Criterion | Implementation Status | Technical Details |
| :--- | :--- | :--- |
| **1. Real Phone Support** | ✅ **Complete** | Uses `FileProvider` for secure camera access and internal storage for high-res receipt photos. |
| **2. Flawless Data Capture** | ✅ **Complete** | Integrated ViewBinding for crash-proof UI and robust input validation in `AddExpenseFragment`. |
| **3. Original Features** | ✅ **Complete** | **Feature 1:** PDF Report Export. **Feature 2:** Educational Financial Quiz. |
| **4. Graphs (Spending/Goals)** | ✅ **Complete** | Custom `PieChartView` in Reports showing spending distribution vs user-defined goals. |
| **5. Progress Dashboard** | ✅ **Complete** | Dual-gauge "Spent" vs "Remaining" visualizers with dynamic color alerts (Green to Red). |
| **6. Gamification** | ✅ **Complete** | 5-Tier Badge System (Bronze to Diamond) + Knowledge Badge earned via Quiz performance. |
| **7. Excellent consistent UI** | ✅ **Complete** | Unified Material 3 design, custom "BudgetBuddy Green" palette, and premium card layouts. |
| **8. Professional Demo** | ⏳ **User Task** | App is ready for recording with all features functional. |

---

## ✨ 3. Original Features (Requirement #3)

### **Feature 1: Professional PDF Export**
Located in the **View Expenses** screen. Users can filter transactions by date and generate a professional PDF document containing transaction details and receipt images.
*   **Implementation:** `PdfExporter.kt` uses the Android `PdfDocument` API to generate shareable reports.

### **Feature 2: Financial Literacy Achievement System**
Found in the **Achievements** screen. Users take a quiz to earn a "Knowledge Rank" badge (Guru, Expert, Novice) displayed on their profile card.
*   **Implementation:** `QuizFragment.kt` tracks scores and persists them via `SharedPreferences`.

---

## 🛠 4. Technical Architecture

### **Data Persistence (RoomDB)**
*   **User Table:** Secure login and profile data.
*   **Category Table:** User-customizable spending groups.
*   **Expense Table:** Stores amounts, dates, descriptions, and absolute paths to receipt photos.
*   **Goal Table:** Stores the monthly min/max spending limits.

### **UI & Navigation**
*   **Navigation Drawer:** A hamburger menu for high-level navigation.
*   **Bottom Navigation:** Quick-access tabs for the most frequent actions.
*   **Custom Views:** `CircularProgressView` (Dashboard) and `PieChartView` (Reports) built using the `Canvas` API.

---

## 📝 5. Final Submission Checklist
1.  **Release APK**: Found in `app/build/outputs/apk/debug/app-debug.apk`.
2.  **GitHub Repository Link**: Ensure your repo is public.
3.  **CI/CD Badge**: GitHub Actions workflow (`.github/workflows/android.yml`) should be passing.
4.  **Demo Video**: A 3-5 minute walkthrough explaining the features.

---
**Developed for University POE - Part 3**
