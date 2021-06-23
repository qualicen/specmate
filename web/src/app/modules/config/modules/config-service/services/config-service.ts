import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ServiceInterface } from 'src/app/modules/data/modules/data-service/services/service-interface';
import { Monitorable } from 'src/app/modules/notification/modules/operation-monitor/base/monitorable';
import { AuthenticationService } from 'src/app/modules/views/main/authentication/modules/auth/services/authentication.service';

export type ConfigProperties = {
    enableProjectExplorer?: string
};

@Injectable()
export class ConfigService extends Monitorable {

    private serviceInterface: ServiceInterface;
    private values: ConfigProperties;

    constructor(private auth: AuthenticationService, http: HttpClient) {
        super();
        this.serviceInterface = new ServiceInterface(http);
    }

    public async config(): Promise<ConfigProperties> {
        if (this.values === undefined) {
            this.values = (await this.serviceInterface.config(this.auth.token)) as ConfigProperties;
        }
        return this.values;
    }
}
