package com.exxeta.correomqtt.plugin.manager;

import com.exxeta.correomqtt.business.exception.CorreoMqttException;

public class PluginExecutionException extends CorreoMqttException {

    public PluginExecutionException(Exception e) {
        super(e);
    }

    @Override
    public String getInfo() {
        return "A plugin threw an exception: " + getMessage();
    }
}
