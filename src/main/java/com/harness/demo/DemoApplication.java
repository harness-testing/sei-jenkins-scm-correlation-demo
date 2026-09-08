package com.harness.demo;

public class DemoApplication {

    public static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "world";
        GreetingService greetingService = new GreetingService();
        System.out.println(greetingService.greet(name));
    }
}
