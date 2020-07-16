import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LinkModelButtonComponent } from './components/link-model-button/link-model-button.component';
import { TranslateModule } from '@ngx-translate/core';


@NgModule({
  declarations: [LinkModelButtonComponent],
  imports: [
    CommonModule,
    TranslateModule
  ],
  exports: [
    LinkModelButtonComponent
  ]
})
export class LinkModelButtonModule { }
