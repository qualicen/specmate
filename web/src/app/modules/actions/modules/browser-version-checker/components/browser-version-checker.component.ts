import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Config } from '../../../../../config/config';
import { detect } from 'detect-browser';

@Component({
  moduleId: module.id.toString(),
  selector: 'browser-version-checker',
  templateUrl: 'browser-version-checker.component.html',
  styleUrls: ['browser-version-checker.component.css']
})

export class BrowserVersionChecker {

  constructor(private translate: TranslateService) {
  }

  public get browserOutdated(): boolean {
    let currentBrowser = detect();
    if (currentBrowser === null || currentBrowser === undefined || currentBrowser.name === null || currentBrowser.name === undefined) {
      return true;
    }

    let supportedBrowsers = Config.SUPPORTED_BROWSERS;
    for (let i = 0; i < supportedBrowsers.length; i++) {
      const element = supportedBrowsers[i];
      if (currentBrowser.name === element.browser) {
        if (parseInt(currentBrowser.version.split('.')[0]) >= element.minVersion) {
          return false;
        }
      }
    }
    return true;
  }

  private get supportedBrowsers(): String {
    let supportedBrowsers = Config.SUPPORTED_BROWSERS;
    let chrome = 'Google Chrome ' + supportedBrowsers.find(element => element.browser === 'chrome').minVersion + '+';
    let firefox = 'Mozilla Firefox ' + supportedBrowsers.find(element => element.browser === 'firefox').minVersion + '+';
    return  chrome + ' or ' + firefox;
  }
}
