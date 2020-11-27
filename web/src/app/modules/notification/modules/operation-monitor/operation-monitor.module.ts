import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { SpecmateSharedModule } from '../../../specmate/specmate.shared.module';
import { OperationMonitor } from './components/operation-monitor.component';
import { ModalsModule } from '../modals/modals.module';
import { OperationMonitorService } from './services/operation-monitor.service';

@NgModule({
  imports: [
    // MODULE IMPORTS
    BrowserModule,
    SpecmateSharedModule,
    ModalsModule
  ],
  declarations: [
    // COMPONENTS IN THIS MODULE
    OperationMonitor
  ],
  exports: [
    // THE COMPONENTS VISIBLE TO THE OUTSIDE
    OperationMonitor
  ],
  providers: [
    // SERVICES
    OperationMonitorService
  ],
  bootstrap: [
    // COMPONENTS THAT ARE BOOTSTRAPPED HERE
  ]
})

export class OperationMonitorModule { }
