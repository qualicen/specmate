// Angular Imports
import { NgModule } from '@angular/core';

// This Module's Components
import { EditorGridButtonComponent } from './components/editor-grid-button.component';
import { TranslateModule } from '@ngx-translate/core';
import { BrowserModule } from '@angular/platform-browser';

@NgModule({
    imports: [
      BrowserModule,
      TranslateModule

    ],
    declarations: [
        EditorGridButtonComponent,
    ],
    exports: [
        EditorGridButtonComponent,
    ]
})
export class EditorGridButtonModule {

}
