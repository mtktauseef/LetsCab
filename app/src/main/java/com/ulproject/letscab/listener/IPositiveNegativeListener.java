package com.ulproject.letscab.listener;

@FunctionalInterface
public interface IPositiveNegativeListener {


    void onPositive();

    //Using Language Level 8
    default void onNegative() {

    }
}
