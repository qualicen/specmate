import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { SpecmateSharedModule } from '../../../specmate/specmate.shared.module';
import { DocumentationExportButton } from './components/documentation-export-button.component';

@NgModule({
    imports: [
        // MODULE IMPORTS
        BrowserModule,
        SpecmateSharedModule
    ],
    declarations: [
        // COMPONENTS IN THIS MODULE
        DocumentationExportButton
    ],
    exports: [
        // THE COMPONENTS VISIBLE TO THE OUTSIDE
        DocumentationExportButton
    ],
    providers: [
        // SERVICES
    ],
    bootstrap: [
        // COMPONENTS THAT ARE BOOTSTRAPPED HERE
    ]
})
export class DocumentationExportButtonModule { }
