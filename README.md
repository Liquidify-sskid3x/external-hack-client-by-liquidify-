# Liquidify External Hack Client
# THERES BOUND TO BE BUGS. SUCH AS EAGLE NOT WORKING, AIMASSIST BEING BAD ON BABY SLIMES, AND PROFILES NOT WORKING. IF YOU CAN FIX THIS REACH OUT TO ME VIA ---> Liquidify.net <--- (my discord user) OR MAKE A FORK
![Java](https://img.shields.io/badge/Java-8%2B-orange)
![License](https://img.shields.io/badge/license-MIT-blue)
![Platform](https://img.shields.io/badge/platform-Windows-lightgrey)
# THIS ONLY WORKS ON 1.21.11 UNOBFUSCATED, NOT FABRIC OR FORGE OR VANILLA, 1.21.11 UNOBFUSCATED.
A Java-based external game modification client for Minecraft featuring ESP overlay, combat automation, and movement enhancements.

## ⚠️ Legal Disclaimer

**THIS SOFTWARE IS PROVIDED FOR EDUCATIONAL AND RESEARCH PURPOSES ONLY.**

- Using game modifications/hacks in online multiplayer games **violates most games' Terms of Service**
- Using this software **may result in permanent account bans**
- This software may be considered a violation of anti-cheat policies
- The developers and contributors are **not responsible** for any consequences resulting from the use of this software
- By using this software, you acknowledge that you do so **at your own risk**
- This software is intended for **single-player or educational environments only**

**WE CONDONE CHEATING IN ONLINE GAMES.**

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Development](#development)
- [Technical Details](#technical-details)
- [License](#license)
- [Contributing](#contributing)

---

## Features

### 🎯 Combat Modules

- **KillAura** - Automated combat system with configurable range and attack delay
  - Adjustable attack range (3.0-6.0 blocks)
  - Customizable attack delay (0-1000ms)
  - Smart target selection
  
- **AimAssist** - Smooth aim assistance toward valid targets
  - Configurable range (3.0-6.0 blocks)
  - Adjustable speed (0.1-1.0)
  - Natural-looking aim smoothing

### 👁️ Visual Modules

- **ESP (Entity ESP)** - External overlay window displaying entity information
  - Real-time 3D bounding boxes
  - Entity name labels
  - Adjustable render range (10-128 blocks)
  - Stream-proof rendering (Windows)
  
- **Fullbright** - Maximum gamma brightness for improved visibility
  - Instant toggle on/off
  - Preserves original gamma settings

### 🏃 Movement Modules

- **Fly** - Creative-style flight in survival mode
  - Toggle flight ability
  - Persistent across respawns
  
- **Sprint** - Automatic sprinting
  - Always sprint when moving forward
  - No stamina required
  
- **Eagle** - Auto-sneak at block edges
  - Prevents falling while bridging
  - Configurable edge distance (0.0-0.5 blocks)
  - Ground-only mode option

---

## Architecture

### Core Components

```
liquidify/
├── Agent.java              # JVM Agent entry point
├── Injector.java           # Process injection handler
├── LiquidifyClient.java    # Main client loop
├── gui/
│   ├── LiquidifyLauncher.java  # Startup GUI
│   ├── OverlayWindow.java      # Module control panel
│   ├── ESPWindow.java          # ESP overlay renderer
│   └── LiquidifyPanel.java     # Main control interface
├── modules/
│   ├── Module.java             # Base module class
│   ├── ModuleManager.java      # Module lifecycle manager
│   ├── Setting.java            # Settings system
│   ├── KillAura.java           # Combat automation
│   ├── AimAssist.java          # Aim assistance
│   ├── ESP.java                # ESP module
│   ├── Fly.java                # Flight module
│   ├── Sprint.java             # Auto-sprint module
│   ├── Eagle.java              # Edge detection module
│   └── Fullbright.java         # Gamma modifier
└── sdk/
    ├── MinecraftSDK.java       # Minecraft reflection wrapper
    └── Mappings.java           # Cross-version mappings
```

### Key Systems

**Injection System**
- Uses Java Attach API (`com.sun.tools.attach.VirtualMachine`)
- Attaches to running Minecraft JVM process
- Loads agent JAR dynamically at runtime
- No filesystem modification required

**Mappings System**
- Auto-detects Minecraft environment (Fabric vs Vanilla)
- Supports obfuscated and deobfuscated mappings
- Cross-version compatibility layer
- Reflection-based member access

**Module System**
- Modular feature architecture
- Hot-swappable modules
- Keybind support
- Per-module settings
- JSON configuration persistence

**ESP Rendering**
- Transparent overlay window
- 3D-to-2D projection mathematics
- Quaternion-based camera transformations
- Stream-proof rendering (Windows Display Affinity API)

---

## Installation

### Prerequisites

- **Java Development Kit (JDK) 8 or higher**
- **Windows Operating System** (for full feature support)
- **Minecraft Java Edition**
- **Administrator privileges** (for process injection)

### Building from Source

1. **Clone the repository**
   ```bash
   git clone https://github.com/Liquidify-sskid3x/external-hack-client-by-liquidify-.git
   cd external-hack-client-by-liquidify-
   ```

2. **Compile the source code**
   ```bash
   javac -cp ".;%JAVA_HOME%/lib/tools.jar" com/liquidify/**/*.java
   ```

3. **Create the JAR file**
   ```bash
   jar cfm liquidify.jar META-INF/MANIFEST.MF com/liquidify/**/*.class
   ```

### MANIFEST.MF Structure

```
Manifest-Version: 1.0
Agent-Class: com.liquidify.Agent
Can-Retransform-Classes: true
Can-Redefine-Classes: true
```

---

## Usage

### Quick Start

1. **Launch Minecraft** (any version)
2. **Run the injector**
   ```bash
   java -jar liquidify.jar
   ```
3. **Click "INJECT"** in the launcher window
4. **Wait for injection confirmation**
5. **Use the overlay panel** to toggle modules

### Default Keybinds

| Module | Key | Function |
|--------|-----|----------|
| ESP | `V` | Toggle entity ESP overlay |
| Fly | `F` | Toggle flight mode |
| Fullbright | `G` | Toggle maximum brightness |
| Sprint | `R` | Toggle auto-sprint |

### Module Controls

- **Toggle**: Click the ON/OFF button in the overlay panel
- **Keybind**: Press the assigned key (shown in parentheses)
- **Settings**: Adjust values in the panel or edit `config.json`

---

## Configuration

### Config File Location

```
C:\liquid\config.json
```

### Config Structure

```json
{
  "KillAura": {
    "key": 75,
    "enabled": false,
    "settings": {
      "Range": "3.8",
      "Attack Delay (ms)": "0"
    }
  },
  "ESP": {
    "key": 86,
    "enabled": true,
    "settings": {
      "Names": "true",
      "Range": "64.0"
    }
  },
  "AimAssist": {
    "key": -1,
    "enabled": false,
    "settings": {
      "Range": "4.0",
      "Speed": "0.5"
    }
  }
}
```

### Profile Management

**Save Profile**
```java
ModuleManager.saveProfile("myProfile");
```

**Load Profile**
```java
ModuleManager.loadProfile("config_1");
```

**List Profiles**
```java
List<String> profiles = ModuleManager.listProfiles();
```

Profiles are stored as:
- `config.json` (default)
- `config_1.json`, `config_2.json`, etc. (saved profiles)

---

## Development

### Adding New Modules

1. **Create a new module class**

```java
package com.liquidify.modules;

import com.liquidify.sdk.MinecraftSDK;
import com.liquidify.modules.Setting.*;

public class ExampleModule extends Module {
    private FloatSetting speed = new FloatSetting("Speed", 1.0f, 0.1f, 5.0f);
    private BooleanSetting enabled = new BooleanSetting("Auto Enable", false);
    
    public ExampleModule() {
        super("Example", "Movement");
        addSetting(speed);
        addSetting(enabled);
        setKeybind(69); // E key
    }
    
    @Override
    public void onEnable() {
        System.out.println("[Example] Module enabled!");
    }
    
    @Override
    public void onDisable() {
        System.out.println("[Example] Module disabled!");
    }
    
    @Override
    public void onUpdate() {
        // Module logic executed every game tick
        Object player = MinecraftSDK.getPlayer();
        if (player == null) return;
        
        // Your code here
    }
}
```

2. **Register the module**

```java
// In ModuleManager.init()
modules.add(new ExampleModule());
```

### Setting Types

**FloatSetting**
```java
FloatSetting range = new FloatSetting("Range", defaultValue, minValue, maxValue);
```

**BooleanSetting**
```java
BooleanSetting toggle = new BooleanSetting("Enable Feature", true);
```

**ModeSetting**
```java
ModeSetting mode = new ModeSetting("Mode", "Default", "Mode1", "Mode2", "Mode3");
```

### Accessing Minecraft Internals

```java
// Get player instance
Object player = MinecraftSDK.getPlayer();

// Get player position
double x = (double) MinecraftSDK.getXMethodEntity.invoke(player);
double y = (double) MinecraftSDK.getYMethodEntity.invoke(player);
double z = (double) MinecraftSDK.getZMethodEntity.invoke(player);

// Get all entities
Iterable<?> entities = MinecraftSDK.getEntities();

// Check if block is air
boolean isAir = MinecraftSDK.isAirAt(offsetX, offsetZ);

// Set player sprinting
MinecraftSDK.setSprintingMethod.invoke(player, true);
```

---

## Technical Details

### Injection Method

**Java Attach API**
- No game file modification
- Runtime code injection
- Attaches to existing JVM process
- Uses `VirtualMachine.attach(pid)`

**Process Detection**
```java
for (VirtualMachineDescriptor vmd : VirtualMachine.list()) {
    String name = vmd.displayName().toLowerCase();
    if (name.contains("minecraft") || name.contains("javaw")) {
        // Found Minecraft process
    }
}
```

### Cross-Version Compatibility

**Fabric Detection**
```java
try {
    Class.forName("net.fabricmc.loader.api.FabricLoader");
    isFabric = true;
} catch (ClassNotFoundException e) {
    isFabric = false; // Vanilla/Forge
}
```

**Mapping Examples**

| Vanilla (Mojang) | Fabric (Intermediary) |
|------------------|----------------------|
| `net.minecraft.client.Minecraft` | `net.minecraft.class_310` |
| `getPlayer()` | `method_1551()` |
| `player` | `field_1724` |

### ESP Rendering Pipeline

1. **Get camera data** (position, rotation, FOV)
2. **Calculate projection matrix** (perspective transform)
3. **Calculate view matrix** (camera transform)
4. **For each entity:**
   - Get AABB (bounding box)
   - Transform 8 corners to camera space
   - Project to screen space
   - Find 2D bounding rectangle
   - Draw box and label

**Projection Math**
```java
Matrix4 proj = Matrix4.perspective(fov, aspect, near, far);
Matrix4 view = Matrix4.fromQuaternion(qx, qy, qz, qw).invert();
float[] screenPos = project(worldX, worldY, worldZ, view, proj);
```

### Stream-Proof Technology

**Windows Display Affinity API**
```powershell
$t::SetWindowDisplayAffinity($hwnd, 0x11)
```

Flags:
- `0x00` - Normal (capturable)
- `0x01` - WDA_MONITOR (not capturable)
- `0x11` - WDA_EXCLUDEFROMCAPTURE (Windows 10+)

---

## License

### MIT License

```
MIT License

Copyright (c) 2025 Liquidify

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Third-Party Licenses

**Java Attach API**
- Part of Oracle JDK/OpenJDK
- GPL v2 with Classpath Exception

**Minecraft**
- © Mojang Studios / Microsoft
- This project is not affiliated with or endorsed by Mojang or Microsoft
- Minecraft™ is a trademark of Mojang Synergies AB

---

## Contributing

### Code of Conduct

- Be respectful and constructive
- Follow Java coding conventions
- Document your code thoroughly
- Test changes before submitting

### Pull Request Process

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Coding Standards

- **Indentation**: 4 spaces
- **Naming**: camelCase for methods/variables, PascalCase for classes
- **Comments**: JavaDoc for public methods
- **Error Handling**: Always catch and log exceptions

---

## Troubleshooting

### Common Issues

**"Minecraft ClassLoader not found"**
- Ensure Minecraft is running before injection
- Check that Java process is visible in Task Manager
- Run injector as Administrator

**"Injection Failed"**
- Verify `liquidify.jar` exists in the same directory
- Check Java version compatibility (JDK 8+)
- Disable antivirus temporarily (may block process injection)

**"ESP window not showing"**
- Ensure ESP module is enabled (press `V`)
- Check if entities are within render range
- Verify PowerShell execution policy allows scripts

**"Modules not working after Minecraft update"**
- Mappings may be outdated
- Check for updated mapping files
- Report version incompatibility as an issue

### Debug Logging

Log file location:
```
%USERPROFILE%\liquidify_agent.log
```

Enable verbose logging:
```java
System.setProperty("liquidify.debug", "true");
```

---

## Changelog

### Version 1.0.0 (2025)
- Initial release
- Core injection system
- Module framework
- ESP overlay with stream-proofing
- Combat modules (KillAura, AimAssist)
- Movement modules (Fly, Sprint, Eagle)
- Visual modules (ESP, Fullbright)
- Configuration system with profiles
- Fabric and Vanilla support

---

## Acknowledgments

- Java Attach API documentation
- Minecraft modding community
- Open-source reflection libraries
- Matrix mathematics references

---

## Contact & Support

**Project Repository**: https://github.com/Liquidify-sskid3x/external-hack-client-by-liquidify-

**Issues**: Please report bugs and feature requests via GitHub Issues

**Disclaimer**: This project is for educational purposes. The maintainers do not provide support for using this software to violate game terms of service.

---

## Responsible Disclosure

If you discover a security vulnerability within this project, please send an email to the maintainers. All security vulnerabilities will be promptly addressed.

---
# ts was made by ai bro...
**Remember: Use responsibly and ethically. Cheating enchances the gaming experience for everyone.**
