# prevent-screen-overlay-android
Android 螢幕覆蓋攻擊防護實作範例（支援 Android 12+ setHideOverlayWindows 與 Android 12 以下自訂遮罩偵測機制）
Android implementation to prevent screen overlay attacks with support for Android 12+ (setHideOverlayWindows) and pre-12 custom overlay detection.

## 📌 一、螢幕覆蓋攻擊簡介

**螢幕覆蓋攻擊（Tapjacking/Overlay Attack）** 是指惡意應用程式在畫面上方放置透明或半透明的視圖，誘導使用者在不知情的情況下進行操作，例如點擊授權按鈕或輸入帳號密碼，造成資安風險。

---

## 📱 二、Android 系統版本差異與防護手段

### ✅ Android 12（API 31）以上

#### 1. `Window.setHideOverlayWindows(true)`
- 封鎖系統外應用的浮動視窗（如 Messenger 泡泡、懸浮工具）
- 建議在顯示敏感內容前呼叫
- 適用於：登入畫面、輸入密碼等情境

#### 2. `FLAG_SECURE`
禁止截圖、螢幕錄影、投影顯示。
```java
getWindow().setFlags(
    WindowManager.LayoutParams.FLAG_SECURE,
    WindowManager.LayoutParams.FLAG_SECURE
);
```

#### 3. `View.setFilterTouchesWhenObscured(true)`
- 可攔截被其他視窗遮蔽時的觸控事件。
- ⚠️ 但無法防護無點擊行為的區塊（如帳號密碼欄位，只輸入但不點擊時，仍有風險）。

### ✅ Android 12（API 31）以下

#### 1. 🚫 無 `setHideOverlayWindows()` 方法。

#### 2. ✅ 可使用 `setFilterTouchesWhenObscured(true)`：
- 可攔截被其他視窗遮蔽時的觸控事件。
- ⚠️ 但無法防護無點擊行為的區塊（如帳號密碼欄位，只輸入但不點擊時，仍有風險）。

#### 3. ✅ 可輔助透過以下方式判斷是否被遮蔽：

- `MotionEvent.isObscured()`
  - 判斷當前觸控事件是否被其他視窗遮蔽。
  - 僅對「正在被觸控的區域」有效，無法偵測靜態遮罩或未觸控時的覆蓋

- `Activity.hasWindowFocus()`
  - 檢查當前 Activity 是否擁有焦點。
  - 若被懸浮窗或對話框遮蔽，Activity 可能會失去焦點，可作為判斷覆蓋的輔助條件，但不保證百分百準確。
 
### AccessibilityService 偵測重疊（無障礙模式）
- 支援 Android 4.0（API 14）以上
- 可用於背景持續監控前景應用與浮層（overlay）狀態。
- ✅ 精確度高，可辨識出目前的前景應用與可疑覆蓋視窗。
- ⚠️ 權限敏感，需使用者在系統設定中手動開啟。
- ⚠️ Google Play 審核困難：若未清楚說明用途或過度存取，用 AccessibilityService 極容易導致上架被拒或要求補件。

#### 目前 Android 系統並無提供官方 API 可直接判斷「是否有 App 正在以懸浮視窗方式覆蓋當前畫面」。
- 開發者可以透過 AccessibilityService 來偵測其他 App 是否擁有「懸浮窗權限」（即 SYSTEM_ALERT_WINDOW），方法是讀取已安裝應用程式的授權狀態，但：
  - AccessibilityService 僅能監聽特定事件與畫面變化，無法得知是否「實際啟用了懸浮視窗」。
  - 不能直接得知目前哪些 App 正在畫面上顯示懸浮窗。
 
## 總結
此範例作為資安檢測修正紀錄與分享，歡迎參考與建議改進。
