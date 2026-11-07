---
sidebar_position: 6
---

# Flutter SDK

:::note SDK quick links

In addition to this reference guide, we provide source code, and sample applications at the following links:

| **Resource**  | **Location**                                                 |
| ------------- | ------------------------------------------------------------ |
| GitHub repository | [Client Side SDK for Flutter](https://github.com/yl-toggle/client-sdk-flutter) |
| Sample applications  | [Demo code](https://github.com/yl-toggle/client-sdk-flutter/tree/main/example) (Flutter)|

:::

## Flutter

### Step 1. Install SDK

```yaml
dependencies:
    yl-toggle:
      git:
        url: https://github.com/yl-toggle/client-sdk-flutter.git
```

### Step 2. Create a yl-toggle instance

```dart
import 'package:yl-toggle/yl-toggle.dart';

var fpUser = YTUser();
fp = yl-toggle(
    "https://yl-toggle.io/server",
    "client-9d885a68ca2955dfb3a7c95435c0c4faad70b50d", // change to your sdk key
    fpUser,
    10 * 1000,
    2 * 1000);
await fp.start();
```

### Step 3.  Use the feature toggle

``` dart
bool showFeature = fp.boolValue("toggle_key", false);
if (showFeature) {
    // application code to show the feature
} else {
    // the code to run if the feature is off
}
```

### Step 4. Track Events

The event tracking feature can record the actions taken by the user in the application as events.

Events are related to toggle's metrics. For more information about event analysis, please read [Event Analysis](../../tutorials/analysis).

```dart
fp.track("YOUR_CUSTOM_EVENT_NAME");
// Providing a metric value to track
fp.track("YOUR_CUSTOM_EVENT_NAME", 5.5);
```
