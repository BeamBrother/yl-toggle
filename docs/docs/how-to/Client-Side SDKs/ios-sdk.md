---
sidebar_position: 3
---

# iOS SDK

:::note SDK quick links
In addition to this reference guide, we provide source code, API reference documentation, and sample applications at the following links:

| **Resource**  | **Location**                                                 |
| ------------- | ------------------------------------------------------------ |
| GitHub repository | [Client Side SDK for iOS](https://github.com/yl-toggle/client-sdk-mobile/tree/main/sdk-ios) |
| Sample applications      | [demo-cocoapods](https://github.com/yl-toggle/client-sdk-mobile/tree/main/examples/demo-cocoapods)、[demo-objc](https://github.com/yl-toggle/client-sdk-mobile/tree/main/examples/demo-objc)、[demo-swiftpm](https://github.com/yl-toggle/client-sdk-mobile/tree/main/examples/demo-swiftpm) |
| Published module    | [CocoaPods](https://cocoapods.org/pods/yl-toggle)         |

:::

## Swift

### Step 1. Install SDK

Swift Package Manager:

    1. XCode -> File -> Add Packages -> input `https://github.com/yl-toggle/client-sdk-ios.git`
    2. click `Add Package`

Cocoapods:

    1. add `pod 'yl-toggle', :git => 'git@github.com:yl-toggle/client-sdk-ios.git'` to Podfile
    2. `pod install` or `pod update`

### Step 2. Create a yl-toggle instance

```swift
import yl-toggle


let url = YTUrlBuilder(remoteUrl: "https://yl-toggle.io/server").build();
let user = YTUser()
user.setAttr(key: "name", value: "bob")
let config = YTConfig(
    remoteUrl: url!,
    clientSdkKey: "client-9d885a68ca2955dfb3a7c95435c0c4faad70b50d",
    refreshInterval: 10,
    waitFirstResp: true
)
let fp = yl-toggle(config: config, user: user)
```

### Step 3. Use the feature toggle

```swift
let showFeature = fp.boolValue("toggle_key", false)
if showFeature {
    # application code to show the feature
} else {
    # the code to run if the feature is off
}
```

### Step 4. Track Events

:::note
The Swift SDK supports event tracking from version 2.0.2.
:::


The event tracking feature can record the actions taken by the user in the application as events.

Events are related to toggle's metrics. For more information about event analysis, please read [Event Analysis](../../tutorials/analysis).

```swift
fp.track(event: "YOUR_CUSTOM_EVENT_NAME")
// Providing a metric value to track
fp.track(event: "YOUR_CUSTOM_EVENT_NAME", value: 5.5)
```


### Step 5. Unit Testing (Optional)

```swift
let fp = yl-toggle.newForTest(toggles: "{ \"toggle_1\": true }")
let is_true = fp.boolValue(key: "toggle_1", defaultValue: false)
assert(is_true == true);
```

Find the Demo code in [example](https://github.com/yl-toggle/client-sdk-mobile/tree/main/examples/)

## Objective-C

### Step 1. Install SDK

Cocoapods

add `pod 'yl-toggle', :git => 'git@github.com:yl-toggle/client-sdk-ios.git'` to Podfile

`pod install` or `pod update`

### Step 2. Create a yl-toggle instance

```objective-c
#import "yl-toggle-Swift.h"

NSString *urlStr = @"https://yl-toggle.io/server";
YTUrl *url = [[[YTUrlBuilder alloc] initWithRemoteUrl: urlStr] build];
YTUser *user = [[YTUser alloc] init];
[user setAttrWithKey:@"name" value:@"bob"];
YTConfig *config = [[YTConfig alloc] initWithRemoteUrl: url
                                          clientSdkKey:@"client-9d885a68ca2955dfb3a7c95435c0c4faad70b50d"
                                       refreshInterval: 10
                                         waitFirstResp: true];
yl-toggle *fp = [[yl-toggle alloc] initWithConfig:config user:user];
```

### Step 3. Use the feature toggle

```objective-c
bool showFeature = [fp boolValueWithKey: @"toggle_key" defaultValue: false];
if (showFeature) {
    # application code to show the feature
} else {
    # the code to run if the feature is off
}
```

### Step 4. Track Events

:::note
The Objc SDK supports event tracking from version 2.0.2.
:::


The event tracking feature can record the actions taken by the user in the application as events.

Events are related to toggle's metrics. For more information about event analysis, please read [Event Analysis](../../tutorials/analysis).

```objective-c
[fp trackWithEvent:@"YOUR_CUSTOM_EVENT_NAME"];
// Providing a metric value to track
[fp trackWithEvent:@"YOUR_CUSTOM_EVENT_NAME" value:5.5];
 
```

### Step 5. Unit Testing (Optional)

```objective-c
#import "yl-toggle-Swift.h"

NSString *s = @"{ \"ab_test\": \"green\"}";
yl-toggle *fp = [[yl-toggle alloc] initWithTestJson: s];
NSString *value = [fp stringValueWithKey:@"ab_test" defaultValue:@"red"];
NSLog(@"value is %@", value);
```

Find the Demo code in [example](https://github.com/yl-toggle/client-sdk-mobile/tree/main/examples/)
