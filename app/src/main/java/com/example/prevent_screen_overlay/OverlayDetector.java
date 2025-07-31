package com.example.prevent_screen_overlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class OverlayDetector {
    private static final String TAG = "OverlayDetector";
    private static final long OVERLAY_CHECK_DELAY = 1000;

    private final Activity activity;
    private final Handler overlayHandler;
    private Runnable overlayCheckRunnable;
    private View overlayBlockView;
    private boolean isOverlayDetected = false;

    public OverlayDetector(Activity activity) {
        this.activity = activity;
        this.overlayHandler = new Handler(Looper.getMainLooper());
        initOverlayBlockView();
    }

    // 初始化重疊阻擋層
    private void initOverlayBlockView() {
        overlayBlockView = new View(activity);
        overlayBlockView.setBackgroundColor(Color.TRANSPARENT);
        overlayBlockView.setClickable(true);
        overlayBlockView.setFocusable(true);
        overlayBlockView.setVisibility(View.GONE);

        ViewGroup rootView = activity.findViewById(android.R.id.content);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        rootView.addView(overlayBlockView, params);
    }

    // 開始檢測畫面重疊
    public void startDetection() {
        if (overlayCheckRunnable == null) {
            overlayCheckRunnable = new Runnable() {
                @Override
                public void run() {
                    checkForOverlay();
                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                        overlayHandler.postDelayed(this, OVERLAY_CHECK_DELAY);
                    }
                }
            };
        }
        // ✅ 延遲 1 秒後開始執行第一輪檢查
        overlayHandler.postDelayed(overlayCheckRunnable, 1000);
    }

    // 停止檢測畫面重疊
    public void stopDetection() {
        if (overlayHandler != null && overlayCheckRunnable != null) {
            overlayHandler.removeCallbacks(overlayCheckRunnable);
        }
    }

    // 檢測是否有重疊應用程式
    private void checkForOverlay() {
        boolean currentOverlayState = isOverlayDetectedByMultipleMethods();

        if (currentOverlayState && !isOverlayDetected) {
            // 檢測到重疊，顯示警告
            onOverlayDetected();
        } else if (!currentOverlayState && isOverlayDetected) {
            // 重疊消失，恢復正常
            onOverlayRemoved();
        }
    }

    // 使用多種方法檢測重疊
    private boolean isOverlayDetectedByMultipleMethods() {
        // 方法1：檢查視窗焦點
        boolean focusLost = !activity.hasWindowFocus();

        // 方法2：檢查系統 UI 可見性
        boolean systemUiHidden = isSystemUiHidden();

        Log.d(TAG, "重疊檢測狀態 - 焦點丟失: " + focusLost +
                ", 系統UI隱藏: " + systemUiHidden);

        return focusLost || systemUiHidden;
    }

    private boolean isSystemUiHidden() {
        try {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = decorView.getSystemUiVisibility();

            boolean isFullscreen = (uiOptions & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0;
            boolean isHideNavigation = (uiOptions & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0;
            boolean isImmersive = (uiOptions & View.SYSTEM_UI_FLAG_IMMERSIVE) != 0;

            return isFullscreen || isHideNavigation || isImmersive;
        } catch (Exception e) {
            Log.e(TAG, "檢查系統UI可見性時發生錯誤", e);
            return false;
        }
    }

    private void onOverlayDetected() {
        isOverlayDetected = true;
        if (overlayBlockView != null) {
            overlayBlockView.setVisibility(View.VISIBLE);
        }
        showOverlayWarningDialog(activity);

        Log.w(TAG, "檢測到畫面重疊，已啟動安全防護");
    }

    private void onOverlayRemoved() {
        isOverlayDetected = false;
        if (overlayBlockView != null) {
            overlayBlockView.setVisibility(View.GONE);
        }
        Log.i(TAG, "畫面重疊已移除，恢復正常操作");
    }

    // Android 12 以下 顯示警告對話框
    public static void showOverlayWarningDialog(Activity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle("安全警告")
                .setMessage("偵測到螢幕覆蓋攻擊，請先關閉其他應用程式顯示的浮層（如聊天泡泡、懸浮工具），以確保帳號與資料安全。")
                .setCancelable(false)
                .setPositiveButton("確定", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Android 12 以上 顯示提示訊息（點擊確定後關閉對話框）
    public static void showOverlayProtectionInfo(Activity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle("安全提醒")
                .setMessage("系統已自動關閉其他應用程式的浮層（如聊天泡泡、懸浮工具），以確保帳號與資料安全。")
                .setCancelable(false)
                .setPositiveButton("確定", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @SuppressWarnings("unused")
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isOverlayDetected) {
            Log.w(TAG, "因檢測到畫面重疊，已阻止觸摸事件");
            return true;
        }
        return false;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG, "視窗焦點變化: " + hasFocus);
        if (!hasFocus) {
            overlayHandler.postDelayed(this::checkForOverlay, 100);
        }
    }

    public void onUserLeaveHint() {
        Log.d(TAG, "用戶離開提示觸發");
        overlayHandler.postDelayed(this::checkForOverlay, 200);
    }
}