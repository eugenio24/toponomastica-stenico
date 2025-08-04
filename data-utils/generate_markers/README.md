# Marker Generation Script

This Python script generates custom marker icons for the *Toponomastica Stenico* app using the toponyms dataset (`Stenico.json`). It produces two sets of marker images used in the Android app:

- **Unselected state**  
- **Selected state**

Each marker is dynamically sized to fit the full name text without truncation, ensuring maximum readability on the map.

## Why use this script?

The app displays around 1700 toponyms on the map, each requiring a custom marker icon. Generating these markers dynamically at runtime would lead to performance bottlenecks and increased memory usage, negatively impacting the user experience.

This script pre-generates all marker icons **offline**, producing optimized PNG files ready to be bundled with the app.

- Renders vector-based marker icons for each toponym  
- Compresses images using `pngquant` to significantly reduce file size without compromising visual quality  

By doing this, the app avoids runtime rendering of text and shape layers, ensuring a **fast**, **smooth**, and **memory-efficient** map display even with thousands of markers visible at once.
