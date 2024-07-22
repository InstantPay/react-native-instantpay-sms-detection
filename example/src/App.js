import { useState, useEffect } from 'react';
import { StyleSheet, View, Text, Button } from 'react-native';
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

    handleConnection = (resp) => {
        //let {connectionState} = resp.type;  
        console.log('type ', resp);
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
