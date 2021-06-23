import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { IconsModule } from '../../../common/modules/icons/icons.module';
import { SpecmateSharedModule } from '../../../specmate/specmate.shared.module';
import { NavigatorModule } from '../navigator/navigator.module';
import { ItemSearchBar } from './components/item-search-bar.component';

@NgModule({
  imports: [
    // MODULE IMPORTS
    BrowserModule,
    FormsModule,
    NavigatorModule,
    SpecmateSharedModule,
    IconsModule,
    NgbModule
  ],
  declarations: [
    // COMPONENTS IN THIS MODULE
    ItemSearchBar
  ],
  exports: [
    // THE COMPONENTS VISIBLE TO THE OUTSIDE
    ItemSearchBar
  ],
  providers: [
    // SERVICES
  ],
  bootstrap: [
    // COMPONENTS THAT ARE BOOTSTRAPPED HERE
  ]
})

export class ItemSearchModule { }
