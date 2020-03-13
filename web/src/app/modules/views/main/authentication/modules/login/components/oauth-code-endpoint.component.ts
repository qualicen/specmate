import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { NavigatorService } from '../../../../../../navigation/modules/navigator/services/navigator.service';
import { AuthenticationService } from '../../auth/services/authentication.service';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';

@Component({
    moduleId: module.id.toString(),
    selector: 'oauth-code-endpoint',
    templateUrl: 'oauth-code-endpoint.component.html',
    styleUrls: []
})
export class OAuthCodeEndpoint implements OnInit {

    private code: string;

    constructor(private auth: AuthenticationService,
        private navigator: NavigatorService,
        private route: ActivatedRoute,
        private dataService: SpecmateDataService) { }

    ngOnInit(): void {
        this.route.queryParams.subscribe(params => this.onParamsReceived(params));
    }

    private onParamsReceived(params: Params): void {
        if (this.hasCode(params)) {
            this.code = params['code'];
            // TODO: Ask Specmate Backend to validate the code.
            this.navigator.navigate('default');
        } else {
            this.auth.authFailed = true;
            this.auth.deauthenticate();
        }
    }

    private hasCode(params: Params): boolean {
        return params['code'] != undefined;
    }
    private isError(params: Params): boolean {
        return params['error'] != undefined;
    }

}
