import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { AngularSplitModule } from 'angular-split';
import { DragulaModule } from 'ng2-dragula';
import { SpecmateSharedModule } from '../../../../../specmate/specmate.shared.module';
import { MaximizeButtonModule } from '../maximize-button/maximize-button.module';
import { TestCaseParameterMappingModule } from '../test-case-parameter-mapping/test-case-parameter-mapping.module';
import { RobotProcedureEditor } from './components/robot-procedure-editor.component';
import { RobotStepRow } from './components/robot-step-row.component';
import { TextareaAutosizeModule } from 'ngx-textarea-autosize';
import { RobotKeywordService } from './services/robot-keyword-service';

@NgModule({
  imports: [
    // MODULE IMPORTS
    BrowserModule,
    AngularSplitModule,
    MaximizeButtonModule,
    DragulaModule.forRoot(),
    TestCaseParameterMappingModule,
    FormsModule,
    SpecmateSharedModule,
    ReactiveFormsModule,
    TextareaAutosizeModule
  ],
  declarations: [
    // COMPONENTS IN THIS MODULE
    RobotProcedureEditor,
    RobotStepRow
  ],
  exports: [
    // THE COMPONENTS VISIBLE TO THE OUTSIDE
    RobotProcedureEditor
  ],
  providers: [
    // SERVICES
    RobotKeywordService
  ],
  bootstrap: [
    // COMPONENTS THAT ARE BOOTSTRAPPED HERE
  ]
})

export class RobotProcedureEditorModule { }
