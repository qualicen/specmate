import { Component, OnInit } from '@angular/core';
import { NavigatorService } from '../../../../../../navigation/modules/navigator/services/navigator.service';
import { AuthenticationService } from '../../auth/services/authentication.service';

@Component({
    moduleId: module.id.toString(),
    selector: 'choose-project',
    templateUrl: 'choose-project.component.html',
    styleUrls: ['choose-project.component.css']
})
export class ChooseProject implements OnInit {

    public get projects(): string[] {
        return this.auth.allowedProjects;
    }

    public get project(): string {
        return this.auth.project;
    }

    constructor(private auth: AuthenticationService, private navigator: NavigatorService) { }

    ngOnInit() { }

    public activate(project: string): void {
        this.auth.changeProject(project);
        this.navigator.navigateToWelcome();
    }
}
