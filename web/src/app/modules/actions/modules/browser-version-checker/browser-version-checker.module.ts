import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { TranslateModule } from '@ngx-translate/core';

import { BrowserVersionChecker } from './components/browser-version-checker.component';

@NgModule({
    imports: [
      BrowserModule,
      TranslateModule
    ],
    declarations: [
        BrowserVersionChecker,
    ],
    exports: [
        BrowserVersionChecker,
    ]
})
export class BrowserVersionCheckerModule {

}
