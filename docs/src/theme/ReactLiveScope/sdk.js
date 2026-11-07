export class FPUser {
  constructor(key, attrs = {}) {
    this.key = key;
    this.attrs = attrs;
  }
}

export class YlToggle {
  constructor(options = {}) {
    this.options = options;
  }

  getBooleanValue(toggleKey, defaultValue = false) {
    return defaultValue;
  }
}
