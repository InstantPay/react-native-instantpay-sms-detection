import { Platform, NativeEventEmitter } from 'react-native';
import InstantpaySmsDetectionModule from './NativeInstantpaySmsDetection';

/* export function multiply(a: number, b: number): number {
    return InstantpaySmsDetection.multiply(a, b);
} */

let RNSmsRead:any = null;

if(Platform.OS === "android"){

    const InstantpaySmsDetectionEventEmitter = new NativeEventEmitter(InstantpaySmsDetectionModule);

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
            return InstantpaySmsDetectionModule.requestPhoneNumber();
        },
        startSmsRetriever:() =>{
            return InstantpaySmsDetectionModule.startSmsRetriever();
        },
        requestSmsConsent:(senderPhoneNumber = null) => {
            return InstantpaySmsDetectionModule.requestSmsConsent(senderPhoneNumber);
        },
        getSimCards:(options = '') => {

            if(options!=null && options!='' && typeof options == 'object'){
                options = JSON.stringify(options);
            }

            return InstantpaySmsDetectionModule.simCardsInfo(options);
        },
        getSimCardPhoneNumber: (simSlotId:number) => {
            return InstantpaySmsDetectionModule.simCardPhoneNumber(simSlotId);
        }
    }
}

export default RNSmsRead;
