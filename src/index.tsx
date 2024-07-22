import { NativeModules, Platform, NativeEventEmitter } from 'react-native';

const LINKING_ERROR =
    `The package 'react-native-instantpay-sms-detection' doesn't seem to be linked. Make sure: \n\n` +
    Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
    '- You rebuilt the app after installing the package\n' +
    '- You are not using Expo Go\n';

const InstantpaySmsDetection = NativeModules.InstantpaySmsDetection
    ? NativeModules.InstantpaySmsDetection
    : new Proxy(
        {},
        {
            get() {
            throw new Error(LINKING_ERROR);
            },
        }
    );

let RNSmsRead:any = null;
if(Platform.OS === "android"){

    const InstantpaySmsDetectionEventEmitter = new NativeEventEmitter(InstantpaySmsDetection);

    const CONNECTIVITY_EVENT = ['StartSmsListener'];

    const _subscriptions = new Map();

    RNSmsRead = {
        addEventListener: (eventName:string, handler:any) => {

            let listener;

            if(CONNECTIVITY_EVENT.includes(eventName)){

                listener = InstantpaySmsDetectionEventEmitter.addListener(
                    eventName,
                    (appStateData) => {
                        handler(appStateData);
                    }
                );
            }
            else{

                console.warn('Trying to subscribe to unknown event: "' + eventName + '"');

                return {
                    remove: () => {}
                };
            }

            _subscriptions.set(handler, listener);

            return {
                remove: () => (RNSmsRead!=null) ? RNSmsRead.removeEventListener(eventName, handler) : null
            };
        },
        removeEventListener: (_eventName:string, handler:any) => {
            
            const listener = _subscriptions.get(handler);
            
            if (!listener) {
                return;
            }
            
            listener.remove();

            _subscriptions.delete(handler);
        },
        requestPhoneNumber: () => {
            return InstantpaySmsDetection.requestPhoneNumber();
        },
        startSmsRetriever:() =>{
            return InstantpaySmsDetection.startSmsRetriever();
        }
    }

    
}

export default RNSmsRead;
