import { Component, OnInit } from '@angular/core';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { User } from '../../../../../../../model/User';
import { NavigatorService } from '../../../../../../navigation/modules/navigator/services/navigator.service';
import { AuthenticationService } from '../../auth/services/authentication.service';

@Component({
    moduleId: module.id.toString(),
    selector: 'login',
    templateUrl: 'login.component.html',
    styleUrls: ['login.component.css']
})
export class Login implements OnInit {
    public username = '';
    public password = '';
    public _project = '';
    public projectnames: string[];

    isAuthenticated = false;
    accessToken = 'unset';

    public isAuthenticating = false;

    constructor(private auth: AuthenticationService, private navigator: NavigatorService, private oidcSecurityService: OidcSecurityService) {
        auth.getProjectNames().then(res => this.projectnames = res);
    }

    public get project(): string {
        return this._project;
    }

    public set project(project: string) {
        this._project = project;
    }


    ngOnInit() {
        this.tryNavigateAway();
        this.oidcSecurityService.checkAuth().subscribe(({ isAuthenticated, userData }) => {
            console.info(isAuthenticated);
            this.isAuthenticated = isAuthenticated;
            console.info(userData);
            this.accessToken = this.oidcSecurityService.getAccessToken();
        });
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
        user.projectName = this.project;
        this.isAuthenticating = true;
        await this.auth.authenticate(user);
        this.tryNavigateAway();
        this.isAuthenticating = false;
        return Promise.resolve(this.auth.isAuthenticated);
    }

    public get canLogin(): boolean {
        return this.isFilled(this.username) && this.isFilled(this.password) && this.isFilled(this.project);
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

    public ssoLogin() {
        this.oidcSecurityService.authorize();
    }

    public ssoLogout() {
        this.oidcSecurityService.logoff();
    }
}
