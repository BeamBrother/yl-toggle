---
sidebar_position: 4
---

# Node.js SDK

yl-toggle is an open source feature management service. This SDK is used to control features in Node.js programs.
This SDK is designed primarily for use in multi-user systems such as web servers and applications.

:::note SDK quick links

| **Resource**  | **Location**                                                 |
| ------------- | ------------------------------------------------------------ |
| SDK API documentation  | [ SDK API docs](https://yl-toggle.github.io/server-sdk-node/) |
| GitHub repository | [Server-SDK for Node.js](https://github.com/yl-toggle/server-sdk-node) |
| Sample applications      | [Demo code](https://github.com/yl-toggle/server-sdk-node/blob/main/example/demo.js) (JavaScript) |
| Published module    | [ npm](https://www.npmjs.com/package/yl-toggle-server-sdk-node) |

:::

:::tip
For users who are using yl-toggle for the first time, we strongly recommend that you return to this article to continue reading after reading the [Gradual Rollout Tutorial](../../tutorials/rollout_tutorial/).
:::

:::tip Notice
For users who needs to use metric analysis, please upgrade JavaScript SDK to version [ 2.0.1 ](https://www.npmjs.com/package/yl-toggle-server-sdk-node/v/2.0.1).
:::

## Step-by-Step Guide

Backend projects usually only need to instantiate a yl-toggle SDK (Client).

According to the requests of different users, call the yl-toggle Client to obtain the toggle result for each user.

:::info
The server-side SDK uses an asynchronous connection to the yl-toggle server to pull judgment rules, and the judgment rules will be cached locally. All interfaces exposed to user code only involve memory operations, so there is no need to worry about performance issues when calling.
:::

### Step 1. Install the Node.js SDK

First, install the yl-toggle SDK as a dependency in your application.

#### NPM

```bash
npm install yl-toggle-server-sdk-node --save
```

### Step 2. Create a yl-toggle instance

After you install the SDK, import it, then create a single, shared instance of the yl-toggle SDK.

```javascript
const fp = new YlToggle({
    remoteUrl: 'https://yl-toggle.io/server',  // or https://127.0.0.1:4007
    serverSdkKey: /* YT_SERVER_SDK_KEY mentioned above */,
    refreshInterval: 5000,
});

await fp.start(1000);  // wait for up to 1 second for the first refresh
// await fp.start();  // init with time limit
```

### Step 3. Use the feature toggle

You can use sdk to check which variation a particular user will receive for a given feature flag.

```javascript
const user = new User(/* uniqueUserId for percentage rollout */);
user.with('ATTRIBUTE_NAME_IN_RULE', VALUE_OF_ATTRIBUTE);  // call with() for each attribute, or extendAttrs(attributeMap) for attributes

const boolValue = fp.booleanValue('YOUR_TOGGLE_KEY', user, false);
if (boolValue) {
   // the code to run if the toggle is on
} else {
   // the code to run if the toggle is off
}
```

### Step 4. Close yl-toggle Client before program exits

Close the client before exiting to ensure accurate data reporting.

```java
await fp.close();
```

## Track Events

:::note
The Node.js SDK supports event tracking from version 2.0.1.
:::


The event tracking feature can record the actions taken by the user in the application as events.

Events are related to toggle's metrics. For more information about event analysis, please read [Event Analysis](../../tutorials/analysis).

```java
fp.track("YOUR_CUSTOM_EVENT_NAME", user);
// Providing a metric value to track
fp.track("YOUR_CUSTOM_EVENT_NAME", user, 5.5);
```

## Customize SDK

:::tip
This paragraph applies to users who want to customize this SDK, or contribute code to this SDK through the open source community. Other users can skip this section.
:::

We provide an acceptance test of this SDK to ensure that the modified SDK is compatible with the native rules of yl-toggle.
Integration test cases are added as submodules of each SDK repository. So be sure to pull the submodule first to get the latest integration tests before running the tests.

```shell
git submodule update --init --recursive
git pull --recurse-submodules
npm run test
```
