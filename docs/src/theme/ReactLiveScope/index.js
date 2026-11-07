import React from 'react';
import { FPUser, YlToggle } from './sdk';
// Add react-live imports you need here
const ReactLiveScope = {
  React,
  ...React,
  YlToggle,
  FPUser,
};
export default ReactLiveScope;
