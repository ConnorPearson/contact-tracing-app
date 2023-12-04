# Proximity Alert: Android BLE COVID Exposure App

# Overview
Proximity Alert is a native Android application designed to leverage Bluetooth Low Energy (BLE) technology for COVID-19 exposure notification. It utilizes the signal strength of BLE between devices to estimate proximity and assess potential exposure risks. The app employs a QR code system for check-ins and a red/green light interface to indicate exposure status.

# Features
- **BLE Proximity Detection**: Uses Bluetooth signal strength to determine the closeness of other devices.
- **QR Code Check-Ins**: Enables easy and quick check-ins at various locations via QR codes.
- **Exposure Notification**: Alerts users with a red or green light indicator based on potential exposure to COVID-19.

# Prerequisites
- Android SDK
- BLE-compatible Android device

# Installation
1. Clone the repository:
   \```
   git clone https://github.com/[your-username]/proximity-alert.git
   \```
2. Open the project in Android Studio.
3. Build and run the application on a BLE-compatible Android device.

# Usage
- **Scanning for Devices**: The app continuously scans for nearby BLE devices and records their signal strengths.
- **Check-In with QR Code**: Use the in-app scanner to scan QR codes at participating locations.
- **Exposure Notification**: The app notifies users of their exposure risk based on proximity data.

# Contributing
Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

# License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

# Acknowledgements
- Bluetooth Low Energy technology
- Android Development Community
- Health and safety workers

