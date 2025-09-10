import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface Spec extends TurboModule {
    addListener(eventName: string): void; // Add addListener
    removeListeners(count: number): void; // Add removeListeners
    requestPhoneNumber(): Promise<string>; // Return Promise Response
    startSmsRetriever(): Promise<string>; // Return Promise Response
    requestSmsConsent(senderPhoneNumber: string|null): Promise<string>; // Return Promise Response
    simCardsInfo(options: string|null): Promise<string>; // Return Promise Response
    simCardPhoneNumber(subscriptionId: number): Promise<string>; // Return Promise Response
    //testSendMessage(message: string): void; //Send JS to Native without Retun
    //sendWithCallback(callback: (response: string) => void): void; //For callback Native to JS
}

export default TurboModuleRegistry.getEnforcing<Spec>('InstantpaySmsDetection');
