import React, { Component } from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import ToastExample  from './index'

export default class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
        };
    }

    recordVideo = () => {
        ToastExample.pickVideo()
        .then(data => {
          console.log("success reposne video",data);
          //success 
        })
        .catch(error => {
          console.log("errror reject", error);
          //failure
        });
    }

    render() {
        return (
            <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
                <TouchableOpacity onPress={() => this.recordVideo()}>
                    <Text> Open Video </Text>
                </TouchableOpacity>
            </View>
        );
    }
}
