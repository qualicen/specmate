import { HttpClient } from '@angular/common/http';
import { EventEmitter, Injectable } from '@angular/core';
import { Config } from '../../../../../../../config/config';
import { User } from '../../../../../../../model/User';
import { Url } from '../../../../../../../util/url';
import { ServiceInterface } from '../../../../../../data/modules/data-service/services/service-interface';
import { UserToken } from '../../../base/user-token';
import { UserSession } from 'src/app/model/UserSession';
import { CookiesService } from 'src/app/modules/common/modules/cookies/services/cookies-service';

@Injectable()
export class AuthenticationService {

    private static SPECMATE_AUTH_COOKIE_BASE = 'specmate-auth-';

    private tokenCookieName = AuthenticationService.SPECMATE_AUTH_COOKIE_BASE + 'token' + '-' + window.location.hostname;

    private isAuthenticatedState = this.determineIsAuthenticated();

    private serviceInterface: ServiceInterface;

    private _authChanged: EventEmitter<boolean>;

    private readonly SELECTED_PROJECT_KEY = 'selectedProject';
    private readonly SESSION_KEY = 'session';

    public get token(): UserToken {
        const token = this.cookiesService.getCookie(this.tokenCookieName);
        const project = this.project;

        if (token !== undefined && project !== undefined) {
            const userToken = new UserToken(token, project, this.session);
            return userToken;
        }

        return UserToken.INVALID;
    }

    private _project: string;

    public get project(): string {
        if (this._project === undefined || this._project === null) {
            this._project = localStorage.getItem(this.SELECTED_PROJECT_KEY);
        }
        return this._project;
    }

    public changeProject(project: string): void {
        const projectClean = project.replace(/"/g, '');
        if (this.isAuthenticatedForProject(projectClean)) {
            localStorage.setItem(this.SELECTED_PROJECT_KEY, projectClean);
            this._project = project;
            this.authChanged.emit();
        }
    }

    public set session(session: UserSession) {
        localStorage.setItem(this.SESSION_KEY, JSON.stringify(session));
    }

    public get session(): UserSession {
        return JSON.parse(localStorage.getItem(this.SESSION_KEY));
    }

    private get isAllCookiesSet(): boolean {
        const session = localStorage.getItem(this.SESSION_KEY);
        const hasSession = session !== undefined && session !== null;
        return hasSession;
    }

    private _authFailed: boolean;
    public get authFailed(): boolean {
        return this._authFailed;
    }
    public set authFailed(authFailed: boolean) {
        this._authFailed = authFailed;
    }

    private _inactivityLoggedOut: boolean;
    public get inactivityLoggedOut(): boolean {
        return this._inactivityLoggedOut;
    }
    public set inactivityLoggedOut(inactivityLoggedOut: boolean) {
        this._inactivityLoggedOut = inactivityLoggedOut;
    }

    private _errorLoggedOut: boolean;
    public get errorLoggedOut(): boolean {
        return this._errorLoggedOut;
    }
    public set errorLoggedOut(errorLoggedOut: boolean) {
        this._errorLoggedOut = errorLoggedOut;
    }

    constructor(http: HttpClient, private cookiesService: CookiesService) {
        this.serviceInterface = new ServiceInterface(http);
        this.isAuthenticatedState = this.determineIsAuthenticated();
    }

    public get authChanged(): EventEmitter<boolean> {
        if (!this._authChanged) {
            this._authChanged = new EventEmitter<boolean>();
        }
        return this._authChanged;
    }

    public async authenticate(user: User): Promise<UserToken> {
        try {
            const wasAuthenticated: boolean = this.isAuthenticated;
            this.session = await this.serviceInterface.authenticate(user);
            this._project = user.projectName;
            this.isAuthenticatedState = this.determineIsAuthenticated();
            if (this.isAuthenticated) {
                if (wasAuthenticated !== this.isAuthenticated) {
                    this.isAuthenticatedState = true;
                    this.authChanged.emit(this.isAuthenticatedState);
                }
                this.authFailed = false;
                this.inactivityLoggedOut = false;
                this.errorLoggedOut = false;
                return this.token;
            }
        } catch (e) {
            this.authFailed = true;
        }
    }

    public get isAuthenticated(): boolean {
        return this.isAuthenticatedState;
    }

    private determineIsAuthenticated(): boolean {
        return this.isAllCookiesSet && !UserToken.isInvalid(this.token);
    }

    public isAuthenticatedForUrl(url: string): boolean {
        if (!this.isAuthenticated) {
            return false;
        }
        if (url === undefined) {
            return false;
        }
        if (url === '' || (Url.SEP + Config.WELCOME_URL).endsWith(url)) {
            return true;
        }
        return this.isAuthenticatedForProject(Url.project(url));
    }

    public isAuthenticatedForProject(project: string): boolean {
        if (project === null || project === undefined) {
            return false;
        }
        return this.allowedProjects.indexOf(project.replace(/"/g, '')) >= 0;
    }

    private clearToken(): void {
        localStorage.removeItem(this.SESSION_KEY);
    }

    public async deauthenticate(omitServer?: boolean): Promise<void> {
        await this.doDeauth(omitServer);
    }

    private async doDeauth(omitServer?: boolean): Promise<void> {
        const wasAuthenticated: boolean = this.isAuthenticated;
        this.authFailed = false;
        if (omitServer !== true) {
            try {
                // The cached token should never be invalid. If it is, we want to deuath prior to auth.
                await this.serviceInterface.deauthenticate();
            } catch (e) {
                // We silently ignore errors on invalidating cached tokens,
                // as this should not be relevant for security,
                // just for cleanliness.
            }
        }
        this.clearToken();
        this.isAuthenticatedState = this.determineIsAuthenticated();
        if (wasAuthenticated !== this.isAuthenticated) {
            this.authChanged.emit(this.isAuthenticatedState);
        }
    }

    public get allowedProjects(): string[] {
        if (this.session === undefined || this.session === null) {
            return [];
        }
        return this.session.allowedPathPattern
            .split('\|')
            .map(pattern => pattern.replace(Config.URL_BASE, ''))
            .map(pattern => pattern.replace(/\/\.\*/g, ''))
            .map(pattern => pattern.replace(/\(|\)/g, ''))
            .map(pattern => pattern.replace(/\//g, ''))
            .map(pattern => pattern.replace(/"/g, ''));
    }

    public async getProjectNames(): Promise<string[]> {
        return await this.serviceInterface.projectnames();
    }
}
