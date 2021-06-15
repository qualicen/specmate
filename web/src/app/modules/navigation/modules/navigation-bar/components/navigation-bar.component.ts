import { Component } from '@angular/core';
import { ENV } from '../../../../../../environments/environment';
import { VERSION } from '../../../../../../environments/version';

@Component({
    selector: 'navigation-bar',
    moduleId: module.id.toString(),
    templateUrl: 'navigation-bar.component.html',
    styleUrls: ['navigation-bar.component.css']
})
export class NavigationBar {
    public get version(): string {
        return VERSION.NUMBER + ' (' + ENV.TYPE + ')';
    }
}
