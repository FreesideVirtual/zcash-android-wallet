# zcash-android-wallet
Android wallet using the Zcash Android SDK that is maintained by core developers.

Please see the [wallet app threat
model](https://zcash.readthedocs.io/en/latest/rtd_pages/wallet_threat_model.html)
for important information about the security and privacy limitations of the
wallet.

### Motivation
[Dogfooding](https://en.wikipedia.org/wiki/Eating_your_own_dog_food) - _transitive verb_ -  is the practice of an organization using its own product. This app was created to help us learn. We aim to make it as beautiful as it is useful. Internally, we will continue to extensively use it to innovate and interate on everything from [protocol changes](https://electriccoin.co/blog/introducing-heartwood/) to [lottie animations](https://lottiefiles.com/popular). Of course, Zcash has a strong history of being open-source, even when it's difficult. It would be easier to keep this internal-only so that we could fill it with crash-reporting and feedback tools but, instead, we decided to disable those things and make it available as a community resource. Please take note:

## This is not a product. This is a tool.

But it is also something we're committed to maintaining and relentlessly improving. So that we can make our libraries that it is built on stronger and more useful.

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
- We strongly recommend that you only use this for small amounts of funds (less than 1 ZEC). Perhaps begin by using it to create a brand new wallet.
- Traffic analysis, like in other cryptocurrency wallets, can leak some privacy
  of the user.
- The wallet might display inaccurate transaction information if it is connected
  to an untrustworthy server.
- Since this was created as a dogfooding tool, think of it less like a wallet and more like a proof of concept, which can result in bugs up to and including loss-of-funds
- So **please backup your seed phrase** and wallet birthday (block height)
- This app has been developed and run exclusively on `mainnet` it might not work at all on `testnet`
- Getting feedback was one of the original design goals of this app so it is mainly intended for learning and improving the related libraries that it uses.

See the [Wallet App Threat Model](https://zcash.readthedocs.io/en/latest/rtd_pages/wallet_threat_model.html)
for more information about the security and privacy limitations of the wallet.



If you'd like to sign up to help us test, reach out on discord and let us know! We're always happy to get feedback!
### License
MIT
