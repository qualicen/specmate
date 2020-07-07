import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { SpecmateSharedModule } from '../../../../../specmate/specmate.shared.module';
import { MaximizeButtonModule } from '../maximize-button/maximize-button.module';
import { ToolPalletteModule } from '../tool-pallette/tool-pallette.module';
import { GraphicalEditor } from './components/graphical-editor.component';
import { EditorGridButtonModule } from '../editor-grid-button/editor-grid-button.module';
import { GraphicalEditorService } from './services/graphical-editor.service';
import { ModelImageService } from './services/model-image.service';

@NgModule({
  imports: [
    // MODULE IMPORTS
    MaximizeButtonModule,
    ToolPalletteModule,
    SpecmateSharedModule,
    BrowserModule,
    EditorGridButtonModule
  ],
  declarations: [
    // COMPONENTS IN THIS MODULE
    GraphicalEditor
  ],
  exports: [
    // THE COMPONENTS VISIBLE TO THE OUTSIDE
    GraphicalEditor
  ],
  providers: [
    GraphicalEditorService,
    ModelImageService
  ],
  bootstrap: [
    // COMPONENTS THAT ARE BOOTSTRAPPED HERE
  ]
})

export class GraphicalEditorModule { }
