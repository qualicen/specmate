import { Component, OnInit } from '@angular/core';
import { User } from '../../../../../../../model/User';
import { NavigatorService } from '../../../../../../navigation/modules/navigator/services/navigator.service';
import { AuthenticationService } from '../../auth/services/authentication.service';
import { AuthProject } from '../../../../../../../model/AuthProject';

@Component({
    moduleId: module.id.toString(),
    selector: 'login',
    templateUrl: 'login.component.html',
    styleUrls: ['login.component.css']
})
export class Login implements OnInit {
    public username = '';
    public password = '';
    public _project: AuthProject;
    public projects: AuthProject[];

    public isAuthenticating = false;

    constructor(private auth: AuthenticationService, private navigator: NavigatorService) {
        auth.getProjects().then(res => this.projects = res);
    }

    public get project(): AuthProject {
        return this._project;
    }

    public get projectNames(): string[] {
        if (this.projects === undefined) {
            return [];
        }
        return this.projects.map(project => project.name);
    }

    public set selectedProject(projectName: string) {
        this._project = this.projects.find(project => project.name === projectName);
    }

    public get isOAuthProject(): boolean {
        return this.project !== undefined &&
            this.project.oauthUrl !== undefined &&
            this.project.oauthUrl !== null &&
            this.project.oauthUrl !== '';
    }

    ngOnInit() {
        this.tryNavigateAway();
    }

    private tryNavigateAway(): void {
        if (this.auth.isAuthenticated) {
            this.navigator.navigate('default');
        }
    }

    public async authenticate(): Promise<boolean> {
        if (!this.canLogin) {
            return Promise.resolve(false);
        }
        let user = new User();
        user.userName = this.username;
        user.passWord = this.password;
        user.projectName = this.project.name;
        this.isAuthenticating = true;
        await this.auth.authenticate(user);
        this.tryNavigateAway();
        this.isAuthenticating = false;
        return Promise.resolve(this.auth.isAuthenticated);
    }

    public get canLogin(): boolean {
        return this.project !== undefined && this.isFilled(this.username) && this.isFilled(this.password);
    }

    private isFilled(str: string): boolean {
        return str !== undefined && str !== null && str.length > 0 && str !== '';
    }

    public get isLoginFailed(): boolean {
        return this.auth.authFailed;
    }

    public get isInactivityLoggedOut(): boolean {
        return this.auth.inactivityLoggedOut;
    }
    public get isErrorLoggedOut(): boolean {
        return this.auth.errorLoggedOut;
    }
}
