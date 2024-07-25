# react-native-instantpay-sms-detection

With the [SMS Detection API](https://developers.google.com/identity/sms-retriever/overview), You can automatically perform SMS-based user verification in your Android app without requiring users to manually type verification codes or granting any extra app permissions.

## Installation

```sh
npm install react-native-instantpay-sms-detection
```

## Basic Usage


```js
import RNSmsRead from 'react-native-instantpay-sms-detection';

// ...

const requestPhoneNumber = async () => {

    let result = await RNSmsRead.requestPhoneNumber();

    console.log(result);
}

const startSMSListen = async () => {

    removeListener();

    listenOnChangeState(); 

    let result = await RNSmsRead.startSmsRetriever();

    console.log(result);
}

const getSmsConsent = async () => {

    let result = await RNSmsRead.requestSmsConsent();

    console.log(result);
}

const listenOnChangeState = () => {
    RNSmsRead.addEventListener("StartSmsListener",handleConnection)
}

const removeListener = () => {
    RNSmsRead.removeEventListener("StartSmsListener",handleConnection)
}

handleConnection = (resp) => {
    console.log('response ', resp);
}
```

## Methods

| Method                          | Return             | Description                                             |
| :------------------------------ | :----------------- | :------------------------------------------------------ |
| requestPhoneNumber()            | `Promise<String>`  | Obtain the user's phone number (using the hint picket). |
| startSmsRetriever()             | `Promise<Boolean>` | Start to listen for SMS messages.                       |
| getSmsConsent()                 | `Promise<Boolean>` | Get consent from user to get message                    |
| addEventListener(               |                    |                                                         |
|    eventName,                   |                    |                                                         |
|    Function)                    | `Promise<String>`  | Get the SMS                                             |
| removeEventListener()           | `Void`             | Stop to listen for SMS messages.                        |

## License

MIT

---

Created By [Instantpay](https://www.instantpay.in)
