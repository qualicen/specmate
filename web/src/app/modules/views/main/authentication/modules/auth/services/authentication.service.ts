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
    private projectCookieName = AuthenticationService.SPECMATE_AUTH_COOKIE_BASE + 'project' + '-' + window.location.hostname;
    private sessionCookieName = AuthenticationService.SPECMATE_AUTH_COOKIE_BASE + 'session' + '-' + window.location.hostname;

    private isAuthenticatedState = this.determineIsAuthenticated();

    private serviceInterface: ServiceInterface;

    private _authChanged: EventEmitter<boolean>;

    public get token(): UserToken {
        const token = this.cookiesService.getCookie(this.tokenCookieName);
        const project = this.cookiesService.getCookie(this.projectCookieName);

        if (token !== undefined && project !== undefined) {
            const userToken = new UserToken(token, project, this.session);
            return userToken;
        }

        return UserToken.INVALID;
    }

    public set session(session: UserSession) {
        this.cookiesService.setCookie(this.sessionCookieName, session);
    }

    public get session(): UserSession {
        return this.cookiesService.getCookieObject(this.sessionCookieName) as UserSession;
    }

    private get isAllCookiesSet(): boolean {
        const hasTokenCookie = this.cookiesService.getCookie(this.tokenCookieName) !== undefined;
        const hasProjectCookie = this.cookiesService.getCookie(this.projectCookieName) !== undefined;
        const hasSessionCookie = this.cookiesService.getCookie(this.sessionCookieName) !== undefined;

        return hasTokenCookie && hasProjectCookie && hasSessionCookie;
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
        return this.token.project === project;
    }

    private clearToken(): void {
        this.cookiesService.removeCookie(this.tokenCookieName);
        this.cookiesService.removeCookie(this.projectCookieName);
        this.cookiesService.removeCookie(this.sessionCookieName);
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
        return this.session.allowedPathPattern
            .split('\|')
            .map(pattern => pattern.replace(Config.URL_BASE, ''))
            .map(pattern => pattern.replace(/\/\.\*/g, ''))
            .map(pattern => pattern.replace(/\(|\)/g, ''));
    }

    public async getProjectNames(): Promise<string[]> {
        return await this.serviceInterface.projectnames();
    }
}
