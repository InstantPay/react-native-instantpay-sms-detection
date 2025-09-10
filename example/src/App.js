import { useState, useEffect } from 'react';
import { StyleSheet, View, Text, Button, PermissionsAndroid } from 'react-native';
import RNSmsRead from 'react-native-instantpay-sms-detection';

export default function App() {
    const [result, setResult] = useState();

    useEffect(() => {
        
    }, []);

    const getPhoneNumber = async () => {

       let out = await RNSmsRead.requestPhoneNumber();

       console.log('out :',out);
    }

    const startSMSListen = async () => {

        removeListener();

        listenOnChangeState(); 

        let out = await RNSmsRead.startSmsRetriever();

        console.log('out :',out);
    } 

    const listenOnChangeState = () => {
        RNSmsRead.addEventListener("StartSmsListener",handleConnection)
    }

    const removeListener = () => {
        RNSmsRead.removeEventListener("StartSmsListener",handleConnection)
    }

    const handleConnection = (resp) => {
        //let {connectionState} = resp.type;  
        console.log('type ', resp);
    }

    const getSmsConsent = async () => {

        let out = await RNSmsRead.requestSmsConsent();

        console.log('out :',out);
    }

    const getSimInfo = async () => {

        let out = await RNSmsRead.getSimCards();

        console.log('simInfo',out);
    }

    const getSimPhoneNumber = async () => {

        /* const granted = await PermissionsAndroid.request(
            PermissionsAndroid.PERMISSIONS.READ_PHONE_NUMBERS,
            {
              title: 'Cool Photo App Camera Permission',
              message:
                'Cool Photo App needs access to your camera ' +
                'so you can take awesome pictures.',
              buttonNeutral: 'Ask Me Later',
              buttonNegative: 'Cancel',
              buttonPositive: 'OK',
            },
          );
          if (granted === PermissionsAndroid.RESULTS.GRANTED) {
            console.log('You can use the camera');
          } else {
            console.log('Camera permission denied');
          } */


        let out = await RNSmsRead.getSimCardPhoneNumber(1);

        console.log('simInfo',out);
    }

    return (
        <View style={styles.container}>
            <Text>Result: {result}</Text>
            <Button 
                title='Request Phone Number'
                onPress={() => getPhoneNumber()}
                
            /> 
            <Text></Text>
            <Button 
                title='Start SMS Listener'
                onPress={() => startSMSListen()}
            /> 
            <Text></Text>
            <Button 
                title='SMS Consent'
                onPress={() => getSmsConsent()}
            /> 
            <Text></Text>
            <Button 
                title='SIM Info'
                onPress={() => getSimInfo()}
            /> 
            <Text></Text>
            <Button 
                title='SIM Phone Number'
                onPress={() => getSimPhoneNumber()}
            /> 
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    box: {
        width: 60,
        height: 60,
        marginVertical: 20,
    },
});