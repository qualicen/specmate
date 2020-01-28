import { NgModule } from '@angular/core';
import { CommonControlsModule } from '../../../actions/modules/common-controls/common-controls.module';
import { I18NModule } from '../../../common/modules/i18n/i18n.module';
import { OperationMonitorModule } from '../../../notification/modules/operation-monitor/operation-monitor.module';
import { SpecmateSharedModule } from '../../../specmate/specmate.shared.module';
import { LogoutModule } from '../../../views/main/authentication/modules/logout/logout.module';
import { NavigationBar } from './components/navigation-bar.component';
import { BrowserVersionCheckerModule } from 'src/app/modules/actions/modules/browser-version-checker/browser-version-checker.module';

@NgModule({
  imports: [
    // MODULE IMPORTS
    CommonControlsModule,
    OperationMonitorModule,
    I18NModule,
    SpecmateSharedModule,
    LogoutModule,
    BrowserVersionCheckerModule
  ],
  declarations: [
    // COMPONENTS IN THIS MODULE
    NavigationBar
  ],
  exports: [
    // THE COMPONENTS VISIBLE TO THE OUTSIDE
    NavigationBar
  ],
  providers: [
    // SERVICES
  ],
  bootstrap: [
    // COMPONENTS THAT ARE BOOTSTRAPPED HERE
  ]
})

export class NavigationBarModule { }
