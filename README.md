# Nateev Camera: The Ultimate Pro Filmmaking & Photography Engine

**Version:** 2.1.1 [Latest] (Full-Sensor Super HD & Real-Time Video LUT)  
**Developer:** 777rei (reialwaysfreemium)  
**License:** MIT  
**Platform:** Android (Kotlin, Jetpack Compose, CameraX, Camera2Interop, OpenGL ES)  
**APK:** `nateev-pro-v2.1.1-rc.apk`

Nateev Camera is a professional-grade, offline-first photography and cinematic filmmaking application for Android. Built with a zero-cloud philosophy, it empowers filmmakers and photographers with granular hardware control, real-time OpenGL 3D LUT video grading with live/export parity, full-sensor Super HD still captures, computational imaging, and uncompromising privacy.

---

## Architecture & Features

### 1. The "Signature" Watermark Engine 📸
- **Dynamic EXIF Integration:** Automatically draws camera model, exact shutter speed, ISO stop, lens focal length, aperture, and timestamp onto captured images.
- **Smart Layouts:**
  - **The Cinema Slate Bar:** A clean white or black strip at the bottom of the photo containing camera specs, red/yellow Nateev emblem badge, and metadata (preserves 100% of original image pixels).
  - **The Corner Stamp:** Minimalist, resolution-scaled typography positioned at the corner.
  - **Minimal Text:** Streamlined horizontal timestamp and exposure banner.
- **Non-Destructive Sidecar Workflow:** Optional toggle to simultaneously save `IMG_CLEAN.jpg` for portfolio post-processing and `IMG_WM.jpg` for social media publication.

### 2. Professional Cinema Suite Assist Tools 🎬
- **False Color (IRE Exposure Mapping):** Real-time AGSL shader mapping scene luminance to industry-standard grading colors:
  - *Purple (0-10 IRE):* Underexposed / crushed shadows
  - *Dark Blue (10-25 IRE):* Deep shadows
  - *Teal (25-45 IRE):* Lower midtones
  - *Neon Pink (45-55 IRE):* **Optimal Skin Tone Reference Range** (essential for portrait & cinema lighting)
  - *Grey (55-70 IRE):* 18% neutral grey reference
  - *Yellow/Red (90-100 IRE):* Near-clipping and blown-out highlights
- **Real-Time Luma Waveform Monitor:** Spatial brightness graph matching the horizontal axis of the frame, instantly pinpointing localized lighting spikes and window blowouts.
- **Customizable Zebra Exposure Stripes:** Adjustable threshold slider (70% for facial highlights to 100% for sky clipping) and selectable alert colors.
- **Sobel Focus Peaking:** High-contrast edge filtering with Neon Green, Neon Red, and Cyan tint selections with sensitivity adjustment.

### 3. Computational Photography (Hardware-Averaging) 🧠
- **Simulated Long Exposure ("Silky Water" Mode):** Captures a rapid sequence of 16 frames at 1/30s and computes a running 64-bit weighted average across RGB pixel buffers on-device. Stationary objects remain tack sharp while water and clouds blur into smooth silk, without needing ND filters or tripods.
- **Macro Focus Bracketing (Focus Stacking):** Steps the lens actuator through near to far diopter distances (`setManualFocus`), saving an indexed image stack (`STACK_01`, `STACK_02`...) ready for focus stacking in Photoshop or Helicon Focus.

### 4. Metadata Authority & File Hygiene 🗂️
- **IPTC/EXIF Copyright Profile:** Injects Photographer Name (`TAG_ARTIST`), Copyright Notice (`TAG_COPYRIGHT`), and Contact Details (`TAG_USER_COMMENT`) into image headers.
- **Smart File Naming Template Editor:** Configurable naming conventions with dynamic tags:
  - `%DATE%`, `%TIME%`, `%ISO%`, `%SHUTTER%`, `%PROJECT%`
  - *Example:* `NATEEV_20261023_143015_ISO400_Cinema.jpg`

### 5. Sensory & UX 💎
- **Nateev Haptic Engine:**
  - *Tick:* Subtle tactile vibration as the manual ISO dial passes standard stops (50, 100, 200, 400, 800, 1600, 3200, 6400) or focus ring steps.
  - *Bump:* Defined haptic feedback when hitting slider limits or attaining horizontal level.
  - *Click:* Realistic shutter actuation thud on capture.
- **Red Mode:** One-tap night vision toggle converting all HUD icons, text, borders, grids, and waveforms to deep crimson (`#FF1744`), protecting rhodopsin dark adaptation during night sky shoots.
- **3D Leveled (Gyroscope 2.0):** Fighter-jet artificial horizon with high-refresh `SENSOR_DELAY_GAME` attitude wings, pitch ladder (+10°, +5°, -5°, -10°), and level indicator.

### 6. Core Pipeline Foundation
- Real-time 3D .cube LUT grading with **Tetrahedral interpolation** and instant binary ByteBuffer disk caching.
- Physical Multi-Lens sensor selection (`0.5x UW`, `1x Wide`, `2x Mid`, `5x Tele`) preventing OS auto-switching jumps.
- 180° Cinema Shutter Angle rule calculator.
- Dual-channel Left & Right audio VU meters with -20dB safety track headroom.
- MKV (crash-safe) & MP4 containers with H.265 / AVC / AV1 codecs.
- Instant Panic Button cache and buffer purge for sensitive field journalism.

### 7. Full-Sensor Super HD & Grade Video LUT (v2.1) 🚀
- **Full Sensor Readout (12MP–50MP):** Zero downscaling. Uses `ResolutionSelector` with `HIGHEST_AVAILABLE_STRATEGY` to utilize the physical camera's full raw resolution matrix.
- **Master Quality JPEG (100% Quality):** Configured with `ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY` and uncompressed 100% JPEG compression format. Delivers maximum clarity and dynamic range for large prints.
- **Hardware-Synchronized Video LUT SurfaceProcessor:** Implements an OpenGL ES `CameraEffect` bound simultaneously to `PREVIEW` and `VIDEO_CAPTURE`. What you see on the live viewfinder is rendered with the identical color matrix into the final recorded video file, ensuring zero discrepancy between monitor and export.
- **APK Distribution:** Standalone binary `Nateev-Pro-v2.1.1-rc.apk` compiled and available in the Release.

---

## Building the Project

```bash
./gradlew :app:assembleDebug
```

---

## Attribution & License

Developed with passion by **777rei (reialwaysfreemium)**.  
Licensed under the open-source **MIT License**.
