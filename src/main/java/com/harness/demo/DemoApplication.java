package com.harness.demo;

public class DemoApplication {

    public static void main(String[] args) {
        String rawName = args.length > 0 ? args[0] : "";
        InputNormalizer normalizer = new InputNormalizer();
        GreetingService greetingService = new GreetingService();
        BuildInfo buildInfo = new BuildInfo();
        String name = normalizer.normalize(rawName);
        System.out.println("artifact=" + buildInfo.artifactName() + " version=" + buildInfo.version());
        System.out.println(greetingService.greet(name));
    }
}
