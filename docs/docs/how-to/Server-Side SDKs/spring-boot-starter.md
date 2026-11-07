---
sidebar_position: 2
---

# Spring Boot Starter

yl-toggle is an open source feature management service. 
This article describes how to use the yl-toggle SDK in a Spring Boot project.

:::note SDK quick links

In addition to this reference guide, we provide source code, API reference documentation, and sample applications at the following links:


| **Resource**  | **Location**                                                 |
| ------------- | ------------------------------------------------------------ |
| GitHub repository | [yl-toggle-spring-boot-starter](https://github.com/yl-toggle/yl-toggle-spring-boot-starter) |
| Sample applications | -                                                            |
| Published module | [Maven](https://mvnrepository.com/artifact/com.yl-toggle/yl-toggle-spring-boot-starter) |

:::


:::tip
For users who use yl-toggle for the first time, we strongly recommend that you return to this article after reading [Gradual Rollout Tutorial](../../tutorials/rollout_tutorial/)
:::

## Access yl-toggle

For the Spring Boot project, yl-toggle provides an out of the box starter to facilitate the rapid integration of yl-toggle in Spring Boot.

:::info
The service SDK uses asynchronous connection to the yl-toggle server to pull the decision rules,
and the decision rules will be cached locally. All interfaces exposed to user code only involve memory operations, 
so you don't need to worry about performance issues when calling.
:::

### Step 1. Install yl-toggle Starter

First, install the yl-toggle Starter as a dependency in your application.

#### Apache Maven

```xml
<dependency>
    <groupId>com.yl-toggle</groupId>
    <artifactId>yl-toggle-spring-boot-starter</artifactId>
    <version>1.4.0</version>
</dependency>
```

#### Gradle Groovy DSL

```text
implementation 'com.yl-toggle:yl-toggle-spring-boot-starter:1.4.0'
```

### Step 2. Config yl-toggle instance

After installing and importing Starter, configure the required yl-toggle instance according to the current environment.

```yaml
spring:
  yl-toggle:
    event-url: https://yl-toggle.io/server/api/events
    synchronizer-url: https://yl-toggle.io/server/api/server-sdk/toggles
    sdk-key: server-9e53c5db4fd75049a69df8881f3bc90edd58fb06
    refresh-interval: 5
```

| config                               | describe                          | required |
|:-------------------------------------|:----------------------------------|---------:|
| spring.yl-toggle.event-url        | events upload Url                 |        Y |
| spring.yl-toggle.synchronizer-url | toggle data synchronization Url              |        Y |
| spring.yl-toggle.sdk-key          | current environment SDK KEY       |        Y |
| spring.yl-toggle.refresh-interval | data synchronization frequency（s） |        N |



### Step 3. Use the feature toggle

You can use sdk to check which variation a particular user will receive for a given feature flag.

```java
@Resource
YlToggle fpClient;
```

```java
YTUser user=new YTUser();
user.with("ATTRIBUTE_NAME_IN_RULE",VALUE_OF_ATTRIBUTE);    // Call with() for each attribute used in Rule.
boolean boolValue=fpClient.boolValue("YOUR_TOGGLE_KEY",user,false);
if(boolValue){
// the code to run if the toggle is on
}else{
// the code to run if the toggle is off
}
```

### Step 4. Close yl-toggle Client

Close the client before exiting to ensure accurate data reporting.

```java
fpClient.close();
```

## Mock yl-toggle for Unittest

You can mock yl-toggle SDK returned value, to run unittest of your code.

### 1. Install powermock SDK:

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

### 2. Mock yl-toggle Toggle

#### Target Method

```java
@AllArgsConstructor
@Service
public class DemoService {

    yl-toggle fp;

    public boolean businessFunction(String userId, String tel) {
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
        boolean tester = demoService.businessFunction("user123", "12397347232");
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
