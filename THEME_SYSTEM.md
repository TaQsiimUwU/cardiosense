# CardioSense Color Theme System

## Overview
All colors in the CardioSense app are now managed through a centralized theming system that supports both light and dark modes.

## Color Structure

### Core Theme Colors (Color.kt)
- **Primary Colors**: CardioBlue (medical blue for primary UI elements)
- **Secondary Colors**: CardioTeal (accent color)
- **Status Colors**: Success Green, Warning Yellow, Error Red, Info Blue
- **ECG Colors**: Dark background with bright green waveform
- **Scanner Colors**: Pulsing blue radar animation
- **Signal Strength**: Green for good, gray for poor
- **Heart Icons**: Red with soft background

### Light vs Dark Theme
Each color has a light (40) and dark (80) variant:
- Light theme uses more saturated, darker colors
- Dark theme uses softer, lighter colors for better readability

### Custom Theme Extension (ThemeExtensions.kt)
Provides `MaterialTheme.cardioSenseColors` extension that automatically switches between light/dark variants based on system theme.

## Usage

### In Composables
```kotlin
// Success/Status indicator
tint = MaterialTheme.cardioSenseColors.successGreen

// ECG display
background = MaterialTheme.cardioSenseColors.ecgBackground
color = MaterialTheme.cardioSenseColors.ecgLineGreen

// Scanner animation
color = MaterialTheme.cardioSenseColors.scannerPulseOuter

// Error/Alert button
containerColor = MaterialTheme.cardioSenseColors.errorRed

// Heart illustration
tint = MaterialTheme.cardioSenseColors.heartRed
background = MaterialTheme.cardioSenseColors.heartRedBackground
```

## Screens with Previews

All screens now have both light and dark theme previews:

1. **PermissionsScreen** - Onboarding with permission requests
2. **ScannerScreen** - BLE device scanner with radar animation
3. **HomeScreen** - Live ECG monitoring dashboard
4. **ProfileScreen** - User settings and device status
5. **NavGraph** - Main navigation container

## Benefits

✅ **Consistent Design**: All colors come from the same source
✅ **Theme Support**: Automatic light/dark mode switching
✅ **Maintainable**: Change colors in one place
✅ **Accessible**: High contrast ratios for medical readability
✅ **Professional**: No hardcoded colors in UI components
