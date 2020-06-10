# zcash-android-wallet
An Android wallet using the Zcash Android SDK that is maintained by ECC developers.

### Motivation
[Dogfooding](https://en.wikipedia.org/wiki/Eating_your_own_dog_food) - _transitive verb_ -  is the practice of an organization using its own product. This app was created to help us learn. 

Please take note: the wallet is not an official product by ECC, but rather a tool for learning about our libraries that it is built on. This means that we do not have robust infrasturcture or user support for this application. We open sourced it as a resource to make wallet development easier for the Zcash ecosystem. 

### Setup

#### Requirements
- [the code](https://github.com/zcash/zcash-android-wallet)
- [Android Studio](https://developer.android.com/studio/index.html) and/or adb with a phone or emulator
- anything else TBD


1. Open Android Studio and setup an emulator or connect your device
2. Clone the repo
3. Open the project and press play. It should just work.

To build from the command line, [setup ADB](https://www.xda-developers.com/install-adb-windows-macos-linux/) and connect your device. Then simply run this and it will both build and install the app:
```bash
cd /path/to/zcash-android-wallet
./gradlew
```

## Disclaimers
There are some known areas for improvement:

- This app is mainly intended for learning and improving the related libraries that it uses. There may be bugs.
- This wallet currently only supports receiving at shielded addresses, which makes it incompatible with wallets that do not support sending to shielded addresses. 
- Traffic analysis, like in other cryptocurrency wallets, can leak some privacy of the user.
- The wallet requires a trust in the server to display accurate transaction information. 
- This app has been developed and run exclusively on `mainnet` it might not work on `testnet`.  

See the [Wallet App Threat Model](https://zcash.readthedocs.io/en/latest/rtd_pages/wallet_threat_model.html)
for more information about the security and privacy limitations of the wallet.

If you'd like to sign up to help us test, reach out on discord and let us know! We're always happy to get feedback!

### License
MIT
