import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { SpecmateSharedModule } from '../../../../../specmate/specmate.shared.module';
import { ToolPallette } from './components/tool-pallette.component';
import { ClipboardService } from './services/clipboard-service';
import { EditorToolsService } from './services/editor-tools.service';

@NgModule({
  imports: [
    // MODULE IMPORTS
    BrowserModule,
    SpecmateSharedModule
  ],
  declarations: [
    // COMPONENTS IN THIS MODULE
    ToolPallette
  ],
  exports: [
    // THE COMPONENTS VISIBLE TO THE OUTSIDE
    ToolPallette
  ],
  providers: [
    // SERVICES
    EditorToolsService,
    ClipboardService
  ],
  bootstrap: [
    // COMPONENTS THAT ARE BOOTSTRAPPED HERE
  ]
})

export class ToolPalletteModule { }
