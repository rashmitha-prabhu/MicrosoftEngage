package com.example.teamsprototype.services;

public class ChannelNameGenerator {
    public static String randomString(){

//        Generates random 10 character string to be used as channel name

        StringBuilder sb = new StringBuilder(10);
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        for(int i=0; i<10; i++){
            int n = (int)(alphabet.length()*Math.random());
            sb.append(alphabet.charAt(n));
        }
        return sb.toString();
    }
}
