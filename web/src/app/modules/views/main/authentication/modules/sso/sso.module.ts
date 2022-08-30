import { HttpClient } from '@angular/common/http';
import { NgModule } from '@angular/core';

import { AuthModule, StsConfigHttpLoader, StsConfigLoader } from 'angular-auth-oidc-client';
import { map } from 'rxjs/operators';

const defaults = {
    redirectUrl: window.location.origin,
    postLogoutRedirectUri: window.location.origin,
    scope: 'openid profile offline_access ',
    responseType: 'code',
    silentRenew: true,
    useRefreshToken: true,
    renewTimeBeforeTokenExpiresInSeconds: 30
}

export const httpLoaderFactory = (httpClient: HttpClient) => {
    const config$ = httpClient.get<any>(`http://localhost:8081/services/rest/ssoconfig`).pipe(
        map((customConfig: any) => {
            return {
                ...defaults,
                ...customConfig
            };
        })
    );
    /**
     * See https://nice-hill-002425310.azurestaticapps.net/docs/documentation/configuration#using-multiple-http-configs
     * for an example with multiple http configs.
     */
    return new StsConfigHttpLoader(config$.toPromise());
};

@NgModule({
    imports: [
        AuthModule.forRoot({
            loader: {
                provide: StsConfigLoader,
                useFactory: httpLoaderFactory,
                deps: [HttpClient],
            },
        }),
    ],
    exports: [AuthModule],
})
export class SSOModule { }
