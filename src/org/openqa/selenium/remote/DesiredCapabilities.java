// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.remote;

import static org.openqa.selenium.remote.CapabilityType.ACCEPT_INSECURE_CERTS;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_NAME;
import static org.openqa.selenium.remote.CapabilityType.LOGGING_PREFS;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM;
import static org.openqa.selenium.remote.CapabilityType.SUPPORTS_JAVASCRIPT;
import static org.openqa.selenium.remote.CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR;
import static org.openqa.selenium.remote.CapabilityType.UNHANDLED_PROMPT_BEHAVIOUR;
import static org.openqa.selenium.remote.CapabilityType.VERSION;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogLevelMapping;
import org.openqa.selenium.logging.LoggingPreferences;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class DesiredCapabilities implements Serializable, Capabilities {
  private final Map<String, Object> capabilities = new HashMap<>();

  public DesiredCapabilities(String browser, String version, Platform platform) {
    setCapability(BROWSER_NAME, browser);
    setCapability(VERSION, version);
    setCapability(PLATFORM, platform);
  }

  public DesiredCapabilities() {
    // no-arg constructor
  }

  public DesiredCapabilities(Map<String, ?> rawMap) {
    capabilities.putAll(rawMap);
    if (rawMap.containsKey(UNEXPECTED_ALERT_BEHAVIOUR)) {
      capabilities.put(UNHANDLED_PROMPT_BEHAVIOUR, rawMap.get(UNEXPECTED_ALERT_BEHAVIOUR));
    }

    if (rawMap.containsKey(LOGGING_PREFS) && rawMap.get(LOGGING_PREFS) instanceof Map) {
      LoggingPreferences prefs = new LoggingPreferences();
      Map<String, String> prefsMap = (Map<String, String>) rawMap.get(LOGGING_PREFS);

      for (String logType : prefsMap.keySet()) {
        prefs.enable(logType, LogLevelMapping.toLevel(prefsMap.get(logType)));
      }
      capabilities.put(LOGGING_PREFS, prefs);
    }

    Object value = capabilities.get(PLATFORM);
    if (value instanceof String) {
      try {
        capabilities.put(PLATFORM, Platform.fromString((String) value));
      } catch (WebDriverException ex) {
        // unrecognized platform, fallback to string
      }
    }
  }

  public DesiredCapabilities(Capabilities other) {
    if (other != null) {
      merge(other);
    }
  }

  public DesiredCapabilities(Capabilities... others) {
    for (Capabilities caps : others) {
      if (caps != null) {
        merge(caps);
      }
    }
  }

  public void setBrowserName(String browserName) {
    setCapability(BROWSER_NAME, browserName);
  }

  public void setVersion(String version) {
    setCapability(VERSION, version);
  }

  public void setPlatform(Platform platform) {
    setCapability(PLATFORM, platform);
  }

  public void setJavascriptEnabled(boolean javascriptEnabled) {
    setCapability(SUPPORTS_JAVASCRIPT, javascriptEnabled);
  }

  public boolean acceptInsecureCerts() {
    if (capabilities.containsKey(ACCEPT_INSECURE_CERTS)) {
      Object raw = capabilities.get(ACCEPT_INSECURE_CERTS);
      if (raw instanceof String) {
        return Boolean.parseBoolean((String) raw);
      } else if (raw instanceof Boolean) {
        return ((Boolean) raw).booleanValue();
      }
    }
    return true;
  }

  public void setAcceptInsecureCerts(boolean acceptInsecureCerts) {
    setCapability(ACCEPT_INSECURE_CERTS, acceptInsecureCerts);
  }

  public Object getCapability(String capabilityName) {
    return capabilities.get(capabilityName);
  }

  /**
   * Merges the extra capabilities provided into this DesiredCapabilities instance. If capabilities
   * with the same name exist in this instance, they will be overridden by the values from the
   * extraCapabilities object.
   *
   * @param extraCapabilities Additional capabilities to be added.
   * @return DesiredCapabilities after the merge
   */
  @Override
  public DesiredCapabilities merge(Capabilities extraCapabilities) {
    if (extraCapabilities != null) {
      capabilities.putAll(extraCapabilities.asMap());
      if (extraCapabilities.getCapability(UNEXPECTED_ALERT_BEHAVIOUR) != null) {
        capabilities.put(UNHANDLED_PROMPT_BEHAVIOUR, extraCapabilities.getCapability(UNEXPECTED_ALERT_BEHAVIOUR));
      }
    }
    return this;
  }

  public void setCapability(String capabilityName, boolean value) {
    capabilities.put(capabilityName, value);
  }

  public void setCapability(String capabilityName, String value) {
    if (PLATFORM.equals(capabilityName)) {
      try {
        capabilities.put(capabilityName, Platform.fromString(value));
      } catch (WebDriverException ex) {
        capabilities.put(capabilityName, value);
      }
    } else {
      if (UNEXPECTED_ALERT_BEHAVIOUR.equals(capabilityName)) {
        capabilities.put(UNHANDLED_PROMPT_BEHAVIOUR, value);
      }
      capabilities.put(capabilityName, value);
    }
  }

  public void setCapability(String capabilityName, Platform value) {
    capabilities.put(capabilityName, value);
  }

  public void setCapability(String key, Object value) {
    if (PLATFORM.equals(key) && value instanceof String) {
      capabilities.put(key, Platform.fromString((String) value));
    } else {
      capabilities.put(key, value);
    }
  }

  public Map<String, ?> asMap() {
    return Collections.unmodifiableMap(capabilities);
  }

  public static DesiredCapabilities android() {
    return new DesiredCapabilities(BrowserType.ANDROID, "", Platform.ANDROID);
  }

  public static DesiredCapabilities chrome() {
    return new DesiredCapabilities(BrowserType.CHROME, "", Platform.ANY);
  }

  public static DesiredCapabilities firefox() {
    DesiredCapabilities capabilities = new DesiredCapabilities(
        BrowserType.FIREFOX,
        "",
        Platform.ANY);
    capabilities.setCapability("acceptInsecureCerts", true);

    return capabilities;
  }

  public static DesiredCapabilities htmlUnit() {
    return new DesiredCapabilities(BrowserType.HTMLUNIT, "", Platform.ANY);
  }

  public static DesiredCapabilities edge() {
    return new DesiredCapabilities(BrowserType.EDGE, "", Platform.WINDOWS);
  }
  public static DesiredCapabilities internetExplorer() {
    DesiredCapabilities capabilities = new DesiredCapabilities(
        BrowserType.IE, "", Platform.WINDOWS);
    capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
    return capabilities;
  }

  public static DesiredCapabilities iphone() {
    return new DesiredCapabilities(BrowserType.IPHONE, "", Platform.MAC);
  }

  public static DesiredCapabilities ipad() {
    return new DesiredCapabilities(BrowserType.IPAD, "", Platform.MAC);
  }

  /**
   * @return DesiredCapabilities for opera
   * @deprecated Use #operaBlink
   */
  @Deprecated
  public static DesiredCapabilities opera() {
    return new DesiredCapabilities(BrowserType.OPERA, "", Platform.ANY);
  }

  public static DesiredCapabilities operaBlink() {
    return new DesiredCapabilities(BrowserType.OPERA_BLINK, "", Platform.ANY);
  }

  public static DesiredCapabilities safari() {
    return new DesiredCapabilities(BrowserType.SAFARI, "", Platform.MAC);
  }

  public static DesiredCapabilities phantomjs() {
    return new DesiredCapabilities(BrowserType.PHANTOMJS, "", Platform.ANY);
  }

  @Override
  public String toString() {
    return String.format("Capabilities [%s]", shortenMapValues(capabilities));
  }

  private Map<String, Object> shortenMapValues(Map<String, Object> map) {
    Map<String, Object> newMap = new HashMap<>();

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      if (entry.getValue() instanceof Map) {
        newMap.put(entry.getKey(),
                   shortenMapValues((Map<String, Object>) entry.getValue()));
      } else {
        String value = String.valueOf(entry.getValue());
        if (value.length() > 1024) {
          value = value.substring(0, 29) + "...";
        }
        newMap.put(entry.getKey(), value);
      }
    }

    return newMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DesiredCapabilities)) {
      return false;
    }

    DesiredCapabilities that = (DesiredCapabilities) o;

    return capabilities.equals(that.capabilities);
  }

  @Override
  public int hashCode() {
    return capabilities.hashCode();
  }
}
