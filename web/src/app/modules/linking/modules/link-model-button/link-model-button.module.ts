import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LinkModelButtonComponent } from './components/link-model-button/link-model-button.component';
import { TranslateModule } from '@ngx-translate/core';
import { ModalsModule } from 'src/app/modules/notification/modules/modals/modals.module';
import { LinkingDialogModule } from '../linking-dialog/linking-dialog.module';


@NgModule({
  declarations: [LinkModelButtonComponent],
  imports: [
    CommonModule,
    TranslateModule,
    ModalsModule,
    LinkingDialogModule
  ],
  exports: [
    LinkModelButtonComponent
  ]
})
export class LinkModelButtonModule { }
