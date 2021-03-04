import { Component, OnInit } from '@angular/core';
import { NgbTypeaheadSelectItemEvent } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { debounceTime, distinctUntilChanged, map } from 'rxjs/operators';
import { NavigatorService } from '../../../../../../navigation/modules/navigator/services/navigator.service';
import { AuthenticationService } from '../../auth/services/authentication.service';

@Component({
    moduleId: module.id.toString(),
    selector: 'choose-project',
    templateUrl: 'choose-project.component.html',
    styleUrls: ['choose-project.component.css']
})
export class ChooseProject implements OnInit {

    search = (text$: Observable<string>) =>
        text$.pipe(
            debounceTime(100),
            distinctUntilChanged(),
            map(term => term.length < 2 ? []
                : this.projects.filter(p => p.toLowerCase().indexOf(term.toLowerCase()) > -1).slice(0, 10))
        )

    public onSelected(event: NgbTypeaheadSelectItemEvent): void {
        // this.activate(event.item);
    }

    public get projects(): string[] {
        return this.auth.allowedProjects;
    }

    public get project(): string {
        return this.auth.project;
    }

    public set project(project: string) {
        this.activate(project);
    }

    constructor(private auth: AuthenticationService, private navigator: NavigatorService) { }

    ngOnInit() { }

    public activate(project: string): void {
        if (project === undefined) {
            return;
        }
        this.auth.changeProject(project);
        this.navigator.navigateToWelcome();
    }
}
