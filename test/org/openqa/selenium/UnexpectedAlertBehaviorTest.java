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

package org.openqa.selenium;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.openqa.selenium.WaitingConditions.elementTextToEqual;
import static org.openqa.selenium.remote.CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR;
import static org.openqa.selenium.testing.Driver.CHROME;
import static org.openqa.selenium.testing.Driver.HTMLUNIT;
import static org.openqa.selenium.testing.Driver.IE;
import static org.openqa.selenium.testing.Driver.MARIONETTE;
import static org.openqa.selenium.testing.Driver.PHANTOMJS;
import static org.openqa.selenium.testing.Driver.SAFARI;
import static org.openqa.selenium.testing.TestUtilities.catchThrowable;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.testing.Ignore;
import org.openqa.selenium.testing.JUnit4TestBase;
import org.openqa.selenium.testing.NeedsLocalEnvironment;
import org.openqa.selenium.testing.TestUtilities;
import org.openqa.selenium.testing.drivers.WebDriverBuilder;

@NeedsLocalEnvironment(reason = "Requires local browser launching environment")
@Ignore(PHANTOMJS)
@Ignore(value = SAFARI, reason = "issue 3862")
@Ignore(value = MARIONETTE, issue = "https://github.com/mozilla/geckodriver/issues/617")
public class UnexpectedAlertBehaviorTest extends JUnit4TestBase {

  private WebDriver driver2;
  private DesiredCapabilities desiredCaps = new DesiredCapabilities();

  @After
  public void quitDriver() throws Exception {
    if (driver2 != null) {
      driver2.quit();
    }
  }

  @Test
  public void canAcceptUnhandledAlert() {
    runScenarioWithUnhandledAlert(UnexpectedAlertBehaviour.ACCEPT, "This is a default value");
  }

  @Test
  @Ignore(value = CHROME, reason = "Unstable Chrome behavior")
  public void canDismissUnhandledAlert() {
    runScenarioWithUnhandledAlert(UnexpectedAlertBehaviour.DISMISS, "null");
  }

  @Test
  @Ignore(value = CHROME, reason = "Chrome uses IGNORE mode by default")
  public void dismissUnhandledAlertsByDefault() {
    runScenarioWithUnhandledAlert(null, "null");
  }

  @Test
  @Ignore(value = CHROME, reason = "Unstable Chrome behavior")
  public void canIgnoreUnhandledAlert() {
    Throwable t = catchThrowable(
        () -> runScenarioWithUnhandledAlert(UnexpectedAlertBehaviour.IGNORE, "Text ignored"));
    assertThat(t, instanceOf(UnhandledAlertException.class));
    driver2.switchTo().alert().dismiss();
  }

  @Test
  public void canSpecifyUnhandledAlertBehaviourUsingCapabilities() {
    desiredCaps.setCapability(UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
    driver2 = new WebDriverBuilder().setDesiredCapabilities(desiredCaps).get();

    runScenarioWithUnhandledAlert("This is a default value");
  }

  @Test
  @Ignore(value = IE, reason = "required capabilities not implemented")
  @Ignore(value = CHROME, reason = "required capabilities not implemented")
  public void requiredUnhandledAlertCapabilityHasPriorityOverDesired() {
    // TODO: Resolve why this test doesn't work on the remote server
    assumeTrue(TestUtilities.isLocal());

    desiredCaps.setCapability(UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.DISMISS);
    DesiredCapabilities requiredCaps = new DesiredCapabilities();
    requiredCaps.setCapability(UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
    WebDriverBuilder builder = new WebDriverBuilder().setDesiredCapabilities(desiredCaps).
        setRequiredCapabilities(requiredCaps);
    driver2 = builder.get();

    runScenarioWithUnhandledAlert("This is a default value");
  }

  private void runScenarioWithUnhandledAlert(UnexpectedAlertBehaviour behaviour,
      String expectedAlertText) {
    if (behaviour != null) {
      desiredCaps.setCapability(UNEXPECTED_ALERT_BEHAVIOUR, behaviour);
    }
    driver2 = new WebDriverBuilder().setDesiredCapabilities(desiredCaps).get();
    runScenarioWithUnhandledAlert(expectedAlertText, behaviour != UnexpectedAlertBehaviour.IGNORE);
  }

  private void runScenarioWithUnhandledAlert(String expectedAlertText) {
    runScenarioWithUnhandledAlert(expectedAlertText, true);
  }

  private void runScenarioWithUnhandledAlert(String expectedAlertText, Boolean ignoreUnhandledAlertException) {
    driver2.get(pages.alertsPage);
    driver2.findElement(By.id("prompt-with-default")).click();

    WebDriverWait wait = new WebDriverWait(driver2, 30);
    if (ignoreUnhandledAlertException) {
      wait.ignoring(UnhandledAlertException.class);
    }
    wait.until(elementTextToEqual(By.id("text"), expectedAlertText));
  }

}
