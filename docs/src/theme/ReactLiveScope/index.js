import React from 'react';
import { yl-toggle, FPUser } from 'yl-toggle-client-sdk-js';
// Add react-live imports you need here
const ReactLiveScope = {
  React,
  ...React,
  yl-toggle,
  FPUser,
};
export default ReactLiveScope;
