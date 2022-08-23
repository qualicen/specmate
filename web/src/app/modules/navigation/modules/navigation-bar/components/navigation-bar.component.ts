import { Component, OnInit } from '@angular/core';
import { ViewControllerService } from 'src/app/modules/views/controller/modules/view-controller/services/view-controller.service';
import { ENV } from '../../../../../../environments/environment';
import { VERSION } from '../../../../../../environments/version';

@Component({
    selector: 'navigation-bar',
    moduleId: module.id.toString(),
    templateUrl: 'navigation-bar.component.html',
    styleUrls: ['navigation-bar.component.css']
})
export class NavigationBar implements OnInit {
    public showVersion = true;
    public showSearchBar = true;
    public showProjectChooser = true;
    public showOperationMonitor = true;
    public showLanguageChooser = true;
    public showDocumentationButton = true;
    public showLogout = true;

    constructor(private viewController: ViewControllerService) { }

    ngOnInit(): void {
        this.viewController.viewChanged.subscribe(() => {
            this.showVersion = !this.viewController.isHeadless;
            this.showSearchBar = !this.viewController.isHeadless;
            this.showProjectChooser = !this.viewController.isHeadless;
            //this.showOperationMonitor = !this.viewController.isHeadless;
            //this.showLanguageChooser = !this.viewController.isHeadless;
            //this.showDocumentationButton = !this.viewController.isHeadless;
            this.showLogout = !this.viewController.isHeadless;
        });
    }

    public get version(): string {
        return VERSION.NUMBER + ' (' + ENV.TYPE + ')';
    }
}
