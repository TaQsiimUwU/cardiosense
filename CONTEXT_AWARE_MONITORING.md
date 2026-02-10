# Context-Aware Heart Rate Monitoring System

## Overview

This implementation solves the classic **"Context-Aware Monitoring"** problem by filtering heart rate alerts through physical activity context. The system asks: *"Is this heart rate physiologically justified by movement?"*

## Architecture Components

### 1. Activity Index Calculation (`ActivityClassifier.kt`)

**Purpose:** Quantify total motion intensity from 3-axis accelerometer data.

**Implementation:**
- **Signal Magnitude Area (SMA):** Combines X, Y, Z acceleration into a single scalar value
- **Formula:** `sqrt(ax² + ay² + az²) - 9.81` (removes gravity bias)
- **Smoothing:** 10-sample rolling average to prevent jitter
- **Classification Thresholds:**
  - `< 0.5 m/s²` → SEDENTARY (sitting, standing still, sleeping)
  - `< 2.0 m/s²` → LIGHT_ACTIVITY (slow walking)
  - `< 4.0 m/s²` → MODERATE_ACTIVITY (brisk walking, stairs)
  - `≥ 4.0 m/s²` → INTENSE_ACTIVITY (running, jumping)

**Key Methods:**
- `updateAndGetSMA(ax, ay, az)` - Updates classifier and returns smoothed SMA
- `classify(ax, ay, az)` - Returns ActivityState enum

---

### 2. Decision Matrix Logic (`ContextAwareMonitor.kt`)

**Purpose:** Combine Heart Rate + Activity State to determine alert necessity.

#### Decision Matrix Table

| Heart Rate | Activity Index | Conclusion | System Action |
|------------|----------------|------------|---------------|
| Normal (<100 BPM) | Any | Resting/Active | ✅ Log data, No Alert |
| Elevated (100-119) | High Movement | Light Exercise | ✅ Log data, No Alert |
| Elevated (100-119) | Low Movement | Monitoring | ⚠️ Monitor closely |
| **Critical (≥120)** | **High Movement** | **Exercise** | 🔇 **SUPPRESS ALERT** |
| **Critical (≥120)** | **Low Movement** | **Pathology** | 🚨 **TRIGGER ALERT** |

#### Cooldown Buffer (Recovery Phase)

**The Problem:** When a user stops running, IMU motion drops to zero instantly, but heart rate takes 2-5 minutes to recover. Without accounting for this, the system would trigger false alerts.

**The Solution:**
- Tracks last intense activity timestamp
- 5-minute cooldown window after moderate/intense activity
- During cooldown:
  - First 2 minutes: Suppress alerts completely (expected recovery)
  - After 2 minutes: If HR still critical, trigger alert (abnormal recovery)

**Key Methods:**
- `updateActivity(ax, ay, az)` - Must be called on every IMU sample
- `evaluateHeartRate(heartRate)` - Returns `MonitoringDecision` with alert recommendation

---

### 3. Heart Rate Extraction (`HeartRateExtractor.kt`)

**Purpose:** Calculate BPM from raw ECG signal using peak detection.

**Algorithm:**
1. **Adaptive Thresholding:** `threshold = mean + (stdDev × 0.5)`
2. **R-Peak Detection:** Find local maxima above threshold
3. **Minimum Peak Distance:** 300ms (prevents double-counting)
4. **BPM Calculation:** `60000 / avgPeakInterval`

**Requirements:**
- Minimum 5 seconds of data (500 samples at 100Hz)
- Sanity check: 30-250 BPM range

---

### 4. Critical Alert System

#### Full-Screen Intent (`CriticalAlertActivity.kt`)

**Built with Jetpack Compose** - Modern declarative UI that wakes the device and appears over the lock screen.

**Trigger Conditions:**
- HR ≥ 120 BPM at rest (sedentary/light activity)
- HR ≥ 120 BPM after 2+ minutes of recovery
- Critical arrhythmia detected by AI (AFib, MI)

**Screen Wake Implementation:**
```kotlin
private fun turnScreenOnAndKeyguardDismiss() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)
    } else {
        window.addFlags(
            FLAG_SHOW_WHEN_LOCKED or
            FLAG_TURN_SCREEN_ON or
            FLAG_KEEP_SCREEN_ON
        )
    }
}
```

**Compose UI Features:**
- **Material 3 Design**: Modern red alert screen with warning icon
- **Large Type**: High contrast white text on red background
- **Clear Actions**: Three prominent buttons with distinct purposes

**Action Buttons:**
1. **🆘 SEND SOS** - Notify emergency contacts, share location
2. **📞 CALL EMERGENCY** - Direct dial to 911 (or regional equivalent)
3. **I'm Fine (Dismiss)** - Pause monitoring for 10 minutes

**Alert Effects:**
- Alarm sound (system alarm tone)
- Vibration pattern (1s on, 0.5s off, repeating)
- Cannot be dismissed by back button

#### Notification Channel (`CriticalAlertNotification.kt`)

**Setup:**
- **Importance:** HIGH (Android 8+)
- **Sound:** Alarm tone (not ringtone)
- **Vibration:** Repeating pattern
- **Persistent:** Cannot be swiped away (`setOngoing(true)`)
- **Full-Screen Intent:** Launches Compose activity over lock screen

**Full-Screen Intent Trigger:**
```kotlin
val fullScreenPendingIntent = PendingIntent.getActivity(
    context, 0, fullScreenIntent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

NotificationCompat.Builder(context, CHANNEL_ID)
    .setFullScreenIntent(fullScreenPendingIntent, true) // KEY LINE
    .build()
```

---

## Data Flow

```
┌─────────────────┐
│  IMU Sensor     │
│  (50Hz)         │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│  ActivityClassifier     │
│  - Calculate SMA        │
│  - Classify State       │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐      ┌─────────────────┐
│  ContextAwareMonitor    │◄─────┤  ECG Sensor     │
│  - Store Activity State │      │  (100Hz)        │
│  - Track Cooldown       │      └────────┬────────┘
└────────┬────────────────┘               │
         │                                 ▼
         │                        ┌────────────────┐
         │                        │ HeartRate      │
         │                        │ Extractor      │
         │                        └───────┬────────┘
         │                                │
         ▼                                ▼
┌────────────────────────────────────────────┐
│  MonitoringService.evaluateContextAware()  │
│  Decision: Is (HR > Limit) AND (Sedentary)?│
└────────┬───────────────────────────────────┘
         │
    ┌────┴────┐
    │  FALSE  │ ─► Continue Monitoring
    └─────────┘
    ┌────┐
    │ TRUE│ ─► Trigger Critical Alert
    └────┘       │
                 ▼
         ┌──────────────────┐
         │ Full-Screen Alert│
         │ - Wake Screen    │
         │ - Play Alarm     │
         │ - Vibrate        │
         │ - Show Actions   │
         └──────────────────┘
```

---

## Integration Points

### MonitoringService.kt

**New Components:**
```kotlin
private lateinit var contextAwareMonitor: ContextAwareMonitor
private lateinit var heartRateExtractor: HeartRateExtractor
private lateinit var criticalAlertHelper: CriticalAlertNotification
```

**Data Flow:**
1. `onImuData()` → Updates `contextAwareMonitor.updateActivity()`
2. `onEcgData()` → Stores in `ecgBuffer`, calls `evaluateContextAwareMonitoring()` every second
3. `evaluateContextAwareMonitoring()`:
   - Extracts HR from buffer
   - Evaluates with context
   - Triggers alert if necessary

**Two Alert Pathways:**
1. **Context-Aware (Tachycardia):** High HR at rest
2. **AI-Driven (Arrhythmia):** AFib, MI, etc. (bypasses context check)

---

## Android Permissions Required

Add to `AndroidManifest.xml`:

```xml
<!-- Critical Alert permissions -->
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.CALL_PHONE" />
```

**Runtime Permissions (Request at app startup):**
- `USE_FULL_SCREEN_INTENT` (Android 11+)
- `CALL_PHONE` (for emergency dialing)

---

## Configuration Tuning

### Heart Rate Thresholds
```kotlin
// ContextAwareMonitor.kt
const val HR_ELEVATED_THRESHOLD = 100  // Start monitoring
const val HR_CRITICAL_THRESHOLD = 120  // Trigger alert at rest
```

**Recommendations:**
- Athletes: Increase to 130-140 BPM
- Elderly/Cardio patients: Decrease to 100-110 BPM
- Consider user-configurable settings

### Activity Thresholds
```kotlin
// ActivityClassifier.kt
private const val SEDENTARY_THRESHOLD = 0.5f
private const val LIGHT_ACTIVITY_THRESHOLD = 2.0f
private const val MODERATE_ACTIVITY_THRESHOLD = 4.0f
```

**Tuning Notes:**
- Values depend on sensor placement (wrist vs. chest)
- Chest straps have lower motion artifacts
- May need calibration per device

### Cooldown Duration
```kotlin
// ContextAwareMonitor.kt
private const val COOLDOWN_DURATION_MS = 5 * 60 * 1000L  // 5 minutes
```

**Physiological Guidelines:**
- Fit individuals: 2-3 minutes sufficient
- General population: 5 minutes safe default
- Cardiovascular patients: Consider 7-10 minutes

---

## Testing Scenarios

### Scenario 1: Running → Stop
**Expected Behavior:**
1. User runs: High HR (150 BPM) + High SMA → No Alert ✅
2. User stops: High HR (140 BPM) + Low SMA → No Alert (Cooldown) ✅
3. 2 min later: HR at 100 BPM → No Alert (Recovery normal) ✅

### Scenario 2: Sitting → Tachycardia
**Expected Behavior:**
1. User sits: Normal HR (70 BPM) + Low SMA → No Alert ✅
2. HR spikes to 130 BPM: High HR + Low SMA → **CRITICAL ALERT** 🚨

### Scenario 3: False Alarm Prevention
**Expected Behavior:**
1. User walks briskly: HR 110 BPM + Moderate SMA → No Alert ✅
2. User climbs stairs: HR 125 BPM + High SMA → No Alert (Exercise) ✅

### Scenario 4: Abnormal Recovery
**Expected Behavior:**
1. User runs: High HR + High SMA → No Alert ✅
2. User stops: Enter cooldown
3. 3 minutes later: HR still 130 BPM → **ALERT** (Recovery too slow) 🚨

---

## Known Limitations & Future Enhancements

### Current Limitations
1. **No motion artifact filtering:** Arm movements during talking/gesturing may cause false high SMA
2. **Fixed thresholds:** Doesn't adapt to individual baselines
3. **No HR variability analysis:** Could improve detection accuracy
4. **Simple peak detection:** May struggle with noisy signals

### Recommended Enhancements
1. **Machine Learning Activity Recognition:** Replace SMA thresholds with trained classifier
2. **Personalized Baselines:** Learn user's resting HR and exercise response
3. **Advanced ECG Processing:** Wavelet transforms for better R-peak detection
4. **Context Fusion:** Add GPS speed, time of day, historical patterns
5. **Smart Cooldown:** Adaptive recovery curves based on exercise intensity

---

## Files Created/Modified

### New Files
- `ActivityClassifier.kt` - SMA calculation and activity classification
- `ContextAwareMonitor.kt` - Decision matrix and cooldown logic
- `HeartRateExtractor.kt` - BPM calculation from ECG
- `CriticalAlertActivity.kt` - Full-screen alert UI (Jetpack Compose)
- `SOSActivity.kt` - Emergency response UI (Jetpack Compose)
- `CriticalAlertNotification.kt` - Notification with full-screen intent

### Modified Files
- `MonitoringService.kt` - Integrated context-aware monitoring
- `AndroidManifest.xml` - Added activities and permissions

---

## Quick Start Integration

1. **Permissions:** Request `USE_FULL_SCREEN_INTENT` at runtime
2. **Start Service:** `startForegroundService(Intent(context, MonitoringService::class.java))`
3. **Feed Data:**
   ```kotlin
   service.onImuData(ax, ay, az)  // Every IMU packet
   service.onEcgData(ecgValue)     // Every ECG sample
   ```
4. **System Handles:**
   - Real-time activity classification
   - Continuous heart rate extraction
   - Context-aware alert triggering

---

## Support & Debugging

**Enable Detailed Logging:**
```kotlin
// MonitoringService.kt - evaluateContextAwareMonitoring()
// Already includes diagnostic logging with TAG = "MonitoringService"
```

**Check Logs:**
```bash
adb logcat | grep MonitoringService
```

**Common Issues:**
- **No alerts:** Check HR thresholds match your test scenario
- **False alerts:** Tune activity thresholds or increase cooldown duration
- **Full-screen not showing:** Verify `USE_FULL_SCREEN_INTENT` permission granted
- **No sound:** Check notification channel settings and DND mode

---

## Medical Disclaimer

This system is designed for **monitoring and alerting purposes only**. It is **NOT a medical device** and should **NOT be used for clinical diagnosis**. Users experiencing cardiac symptoms should seek immediate professional medical care.

**Regulatory Compliance:**
- Not FDA cleared/approved
- Not intended to replace medical advice
- For educational/research purposes

---

*Implementation completed based on architectural specifications.*
*All components follow Android best practices and Material Design guidelines.*
