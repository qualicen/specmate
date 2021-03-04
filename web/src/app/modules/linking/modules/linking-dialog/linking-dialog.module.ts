import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LinkingDialogComponent } from './components/linking-dialog/linking-dialog.component';
import { SpecmateSharedModule } from 'src/app/modules/specmate/specmate.shared.module';
import { ModelSearchBarComponent } from './components/model-search-bar/model-search-bar.component';
import { EffectSelectorComponent } from './components/effect-selector/effect-selector.component';
import { NgbTypeaheadModule, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { BrowserModule } from '@angular/platform-browser';



@NgModule({
    declarations: [
        LinkingDialogComponent,
        ModelSearchBarComponent,
        EffectSelectorComponent],
    imports: [
        BrowserModule,
        CommonModule,
        SpecmateSharedModule,
        NgbTypeaheadModule,
        NgbDropdownModule,
        FormsModule,
        TranslateModule
    ]
})
export class LinkingDialogModule { }
