---
name: LemonSubtitle Precision
colors:
  surface: '#131313'
  surface-dim: '#131313'
  surface-bright: '#393939'
  surface-container-lowest: '#0e0e0e'
  surface-container-low: '#1c1b1b'
  surface-container: '#201f1f'
  surface-container-high: '#2a2a2a'
  surface-container-highest: '#353534'
  on-surface: '#e5e2e1'
  on-surface-variant: '#c1c6d6'
  inverse-surface: '#e5e2e1'
  inverse-on-surface: '#313030'
  outline: '#8b909f'
  outline-variant: '#414754'
  surface-tint: '#adc7ff'
  primary: '#adc7ff'
  on-primary: '#002e68'
  primary-container: '#1a73e8'
  on-primary-container: '#ffffff'
  inverse-primary: '#005bc0'
  secondary: '#ffe490'
  on-secondary: '#3c2f00'
  secondary-container: '#f1c501'
  on-secondary-container: '#665200'
  tertiary: '#ffb691'
  on-tertiary: '#552100'
  tertiary-container: '#c55500'
  on-tertiary-container: '#0e0200'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#d8e2ff'
  primary-fixed-dim: '#adc7ff'
  on-primary-fixed: '#001a41'
  on-primary-fixed-variant: '#004493'
  secondary-fixed: '#ffe080'
  secondary-fixed-dim: '#edc200'
  on-secondary-fixed: '#231b00'
  on-secondary-fixed-variant: '#564500'
  tertiary-fixed: '#ffdbcb'
  tertiary-fixed-dim: '#ffb691'
  on-tertiary-fixed: '#341100'
  on-tertiary-fixed-variant: '#783100'
  background: '#131313'
  on-background: '#e5e2e1'
  surface-variant: '#353534'
  success: '#34A853'
  warning: '#EA4335'
  surface-dark: '#1E1E1E'
  surface-light: '#F5F5F5'
  background-light: '#FFFFFF'
typography:
  headline-lg:
    fontFamily: Roboto
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-md:
    fontFamily: Roboto
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  title-lg:
    fontFamily: Noto Sans
    fontSize: 20px
    fontWeight: '500'
    lineHeight: 28px
  body-lg:
    fontFamily: Noto Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Noto Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Roboto
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.1px
  code-sm:
    fontFamily: jetbrainsMono
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  baseline: 4px
  edge-margin: 16px
  gutter: 12px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 24px
---

## Brand & Style

The design system is engineered for a high-utility productivity environment, specifically tailored for mobile subtitle editing and video processing. The brand personality is **Professional, Systematic, and Efficient**, aligning with the rigorous requirements of transcription and translation workflows.

The visual style follows a **Modern Corporate** aesthetic, heavily influenced by Material 3 (M3) principles. It prioritizes clarity over decoration, using structured layouts and purposeful color application to guide the user through complex editing tasks. The interface should feel like a dependable tool—fast, responsive, and logically organized—evoking a sense of control and precision for the creator.

## Colors

The palette is anchored by a high-contrast **Google Blue** primary, symbolizing the app's technical reliability. While the default mode is dark to reduce eye strain during long editing sessions, a light mode is supported for outdoor use.

- **Primary:** Used for key action buttons, active states, and progress indicators.
- **Secondary (Lemon Accent):** A vibrant yellow-gold used sparingly for highlighting specific subtitle segments or "LemonSubtitle" branding elements to differentiate from standard system apps.
- **Functional Colors:** Success (Green) and Warning (Red) follow industry-standard values for immediate recognition of task completion or processing errors.
- **Surfaces:** In dark mode, we utilize a tiered grayscale approach (`#121212` for base, `#1E1E1E` for elevated cards) to create subtle depth without relying on heavy shadows.

## Typography

This design system employs a dual-font strategy to optimize for bi-lingual subtitle workflows. **Roboto** provides a clean, geometric structure for UI labels and Latin characters, while **Noto Sans (SC)** ensures maximum legibility for complex Chinese characters.

Key rules:
- **Subtitles:** Use `body-lg` with a custom text-shadow (defined in components) to ensure readability over video content.
- **Timing/Timecodes:** Use a monospaced font (JetBrains Mono) for timestamps to prevent "jitter" when numbers change rapidly during playback.
- **Hierarchy:** Maintain high contrast between titles and body text to help users navigate dense information panels.

## Layout & Spacing

The layout is built on a **4px baseline grid**, ensuring all elements align to a consistent rhythm. For mobile, we utilize a fluid grid system with a standard 16px edge margin.

- **Content Reflow:** On smaller devices, editing controls stack vertically. On larger foldables or tablets, the video preview and subtitle list should adopt a side-by-side (2-column) split.
- **Touch Targets:** All interactive elements (buttons, subtitle handles) must maintain a minimum 48x48dp hit area, even if the visual representation is smaller.
- **Density:** Provide a "Compact" mode for power users that reduces vertical padding in the subtitle list from 16px to 8px.

## Elevation & Depth

Visual hierarchy is established using **Tonal Layers** rather than heavy shadows, following Material 3 logic. 

1. **Level 0 (Background):** The base layer (`#121212` in dark mode).
2. **Level 1 (Surface):** Default container for list items and content blocks (`#1E1E1E`).
3. **Level 2 (Active/Floating):** Used for dialogs and menus, utilizing a very subtle 8% opacity primary color overlay on the surface color to indicate higher elevation.
4. **Outlines:** Use low-contrast borders (1px) for input fields and inactive cards to maintain a clean, flat aesthetic that doesn't distract from the video preview.

## Shapes

The design system uses a consistent **12dp (0.5rem)** corner radius for most UI components. This provides a balance between the "technical" feel of sharp corners and the "modern" feel of fully rounded shapes.

- **Standard Containers:** Cards, dialogs, and bottom sheets use the 12dp radius.
- **Inputs:** Text fields use the same 12dp radius to ensure a unified look.
- **Small Components:** Chips and badges may use a "Pill" (full) radius to distinguish them as distinct, removable metadata items.

## Components

### Buttons
- **Primary Action:** Solid `#1A73E8` fill with 12dp corners. Text in white, uppercase `label-lg`.
- **Secondary/Ghost:** Transparent fill with a 1px primary outline. Used for "Cancel" or "Export Settings."

### Subtitle Cards
- **Editing State:** When a subtitle is selected, the card should gain a 2px primary border and a subtle primary-tinted background.
- **Timecodes:** Displayed in the top right of each card using `code-sm` typography for alignment.

### Input Fields
- Filled style with a 1px bottom indicator in the inactive state, transitioning to a full 12dp rounded outline when focused.

### Timeline
- A horizontal scrollable area. Use a high-contrast playhead line (Primary color) and semi-transparent blocks to represent subtitle duration.

### Chips
- Used for "Tags" (e.g., [Speaker 1], [Music]). These should be compact, with an 8px padding and a "Pill" shape.

### Progress Indicators
- Use a linear progress bar for video rendering. Use the Primary color for the active bar and the Surface color for the track.