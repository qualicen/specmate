import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
  moduleId: module.id.toString(),
  selector: 'browser-version-checker',
  templateUrl: 'browser-version-checker.component.html',
  styleUrls: ['browser-version-checker.component.css']
})



export class BrowserVersionChecker {

  private unsupportedBrowser = [
    { browser: 'IE', minVersion: 11 },
    { browser: 'MSIE', minVersion: 10 },
    { browser: 'Chrome', minVersion: 42 },
    { browser: 'Firefox', minVersion: 44 },
    { browser: 'Edge', minVersion: 14 },
    { browser: 'Safari', minVersion: 9 },
    { browser: 'Opera', minVersion: 29 }];

  constructor(private translate: TranslateService) {

  }

  public get browserOutdated(): boolean {
    let currentBrowser = this.getBrowser().split('!!');
    for (let i = 0; i < this.unsupportedBrowser.length; i++) {
      const element = this.unsupportedBrowser[i];
      if (element.browser === currentBrowser[0]) {
        if (element.minVersion >= parseInt(currentBrowser[1])) {
          return true;
        }
      }
    }
    return false;
  }

  // Based on https://stackoverflow.com/a/5918791
  private getBrowser() {
    let ua = navigator.userAgent, tem,
      M = ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
    if (/trident/i.test(M[1])) {
      tem = /\brv[ :]+(\d+)/g.exec(ua) || [];
      return 'IE' + '!!' + (tem[1] || '');
    }
    if (M[1] === 'Chrome') {
      tem = ua.match(/\b(OPR|Edge)\/(\d+)/);
      if (tem != null) {
        return tem.slice(1).join('!!').replace('OPR', 'Opera');
      }
    }
    M = M[2] ? [M[1], M[2]] : [navigator.appName, navigator.appVersion, '-?'];
    if ((tem = ua.match(/version\/(\d+)/i)) != null) {
      M.splice(1, 1, tem[1]);
    }
    return M.join('!!');
  }
}
