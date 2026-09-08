package com.harness.demo;

public class DemoApplication {

    public static void main(String[] args) {
        String rawName = args.length > 0 ? args[0] : "";
        InputNormalizer normalizer = new InputNormalizer();
        GreetingService greetingService = new GreetingService();
        BuildInfo buildInfo = new BuildInfo();
        String name = normalizer.normalize(rawName);
        String greeting = greetingService.greet(name);

        System.out.println("artifact=" + buildInfo.artifactName() + " version=" + buildInfo.version());
        System.out.println(greeting);
        System.out.println(buildInfo.buildSummary(greeting));
    }
}
