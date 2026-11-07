---
sidebar_position: 1
---

# Java SDK

yl-toggle is an open source feature management service. This SDK is used to control features in java programs. This SDK is designed primarily for use in multi-user systems such as web servers and applications.

:::note SDK quick links

In addition to this reference guide, we provide source code, API reference documentation, and sample applications at the following links:

| **Resource**        | **Location** |
| --------------------------------------- | ----------------- |
| SDK API documentation | [ SDK API docs](https://yl-toggle.github.io/server-sdk-java/) |
| GitHub repository | [Server-SDK for Java](https://github.com/yl-toggle/server-sdk-java) |
| Sample applications | [YlToggleDemo](https://github.com/yl-toggle/server-sdk-java/blob/main/src/main/java/com/yl-toggle/sdk/example/YlToggleDemo.java) (Java) |
|Published module|[Maven](https://mvnrepository.com/artifact/com.yl-toggle/server-sdk-java)|

:::

:::tip
For users who are using yl-toggle for the first time, we strongly recommend that you return to this article to continue reading after reading the [Gradual Rollout Tutorial](../../tutorials/rollout_tutorial/).
:::

## Step-by-Step Guide

Backend projects usually only need to instantiate a yl-toggle SDK (Client).

According to the requests of different users, call the yl-toggle Client to obtain the toggle result for each user.

:::info
The server-side SDK uses an asynchronous connection to the yl-toggle server to pull judgment rules, and the judgment rules will be cached locally. All interfaces exposed to user code only involve memory operations, so there is no need to worry about performance issues when calling.
:::

### Step 1. Install the Java SDK

First, install the yl-toggle SDK as a dependency in your application.

#### Apache Maven

```xml
<dependency>
    <groupId>com.yl-toggle</groupId>
    <artifactId>server-sdk-java</artifactId>
    <version>1.4.0</version>
</dependency>
```

#### Gradle Groovy DSL

```text
implementation 'com.yl-toggle:server-sdk-java:1.4.0'
```

### Step 2. Create a yl-toggle instance

After you install and import the SDK, create a single, shared instance of the yl-toggle sdk.

```java
public class Demo {
    private static final YTConfig config = YTConfig.builder()
        .remoteUri(/* yl-toggle Server URI */)
        .build();

    private static final YlToggle fpClient = new YlToggle(
        /* yl-toggle Server SDK Key */, config);
  
  	if（!fpClient.initialized()) {
				System.out.println("SDK failed to initialize!")
		}
}
```

### Step 3. Use the feature toggle

You can use sdk to check which variation a particular user will receive for a given feature flag.

```java
YTUser user = new YTUser();
    user.with("ATTRIBUTE_NAME_IN_RULE", VALUE_OF_ATTRIBUTE);    // Call with() for each attribute used in Rule.
    boolean boolValue = fpClient.boolValue("YOUR_TOGGLE_KEY", user, false);
    if (boolValue) {
    // the code to run if the toggle is on
    } else {
    // the code to run if the toggle is off
    }
```

### Step 4. Close yl-toggle Client before program exits

Close the client before exiting to ensure accurate data reporting.

```java
fpClient.close();
```

## Track Events

:::note
The Java SDK supports event tracking from version 2.0.1.
:::


The event tracking feature can record the actions taken by the user in the application as events.

Events are related to toggle's metrics. For more information about event analysis, please read [Event Analysis](../../tutorials/analysis).

```java
fpClient.track("YOUR_CUSTOM_EVENT_NAME", user);
// Providing a metric value to track
fpClient.track("YOUR_CUSTOM_EVENT_NAME", user, 5.5);
```

## Mock yl-toggle for Unit test

You can mock yl-toggle SDK returned value, to run unit test of your code.

### 1. Add powermock SDK to your project:

```xml
<dependency>
    <groupId>org.powermock</groupId>
    <artifactId>powermock-api-mockito2</artifactId>
    <version>2.0.9</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.powermock</groupId>
    <artifactId>powermock-module-junit4</artifactId>
    <version>2.0.9</version>
    <scope>test</scope>
</dependency>
```

### 2. Mock Toggle

#### Target Method

```java
@AllArgsConstructor
@Service
public class DemoService {

    yl-toggle fp;

    public boolean isTester(String userId, String tel) {
        YTUser fpUser = new YTUser(userId);
        fpUser.with("tel", tel);
        return fp.boolValue("is_tester", fpUser, false);
    }
}
```
#### Unit Test Code

```java
@RunWith(PowerMockRunner.class)
@PrepareForTest({yl-toggle.class})
public class YlToggleTest {

    @Test
    public void test() {
        yl-toggle fp = PowerMockito.mock(yl-toggle.class);
        DemoService demoService = new DemoService(fp);
        Mockito.when(fp.boolValue(anyString(), any(YTUser.class), anyBoolean())).thenReturn(true);
        boolean tester = demoService.isTester("user123", "12397347232");
        assert tester;
    }

}
```

## Customize SDK

:::tip
This paragraph applies to users who want to customize this SDK, or contribute code to this SDK through the open source community. Other users can skip this section.
:::

We provide an acceptance test of this SDK to ensure that the modified SDK is compatible with the native rules of yl-toggle.
Integration test cases are added as submodules of each SDK repository. So be sure to pull the submodule first to get the latest integration tests before running the tests.

```shell
git submodule update --init --recursive
mvn test
```
