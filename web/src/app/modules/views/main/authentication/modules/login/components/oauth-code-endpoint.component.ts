import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { NavigatorService } from '../../../../../../navigation/modules/navigator/services/navigator.service';
import { AuthenticationService } from '../../auth/services/authentication.service';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { ServiceInterface } from 'src/app/modules/data/modules/data-service/services/service-interface';
import { HttpClient } from '@angular/common/http';
import { User } from 'src/app/model/User';

@Component({
    moduleId: module.id.toString(),
    selector: 'oauth-code-endpoint',
    templateUrl: 'oauth-code-endpoint.component.html',
    styleUrls: []
})
export class OAuthCodeEndpoint implements OnInit {

    private code: string;

    constructor(private auth: AuthenticationService, private navigator: NavigatorService, private route: ActivatedRoute) { }

    ngOnInit(): void {
        this.route.queryParams.subscribe(params => this.onParamsReceived(params));
    }

    private async onParamsReceived(params: Params): Promise<boolean> {
        if (this.isValid(params)) {
            const code = params['code'] as string;
            const state = params['state'] as string;
            const project = state.split('|')[0];
            const sessionId = state.split('|')[1];
            const user = new User();
            user.userName = sessionId;
            user.passWord = code;
            user.projectName = project;
            await this.auth.authenticate(user, true);
            if (this.auth.isAuthenticated) {
                this.navigator.navigate('default');
            }
            return Promise.resolve(this.auth.isAuthenticated);
        } else {
            this.auth.authFailed = true;
            this.auth.deauthenticate(true);
            this.navigator.navigateToWelcome();
        }
    }

    private isValid(params: Params): boolean {
        return params.hasOwnProperty('code') && params.hasOwnProperty('state');
    }
}
