package com.nuwarobotics.example.custombehavior.listener;

public interface EventListener {
    void onCompleted(String RunnableName, String var);
    void onError(String RunnableName, String error);
}