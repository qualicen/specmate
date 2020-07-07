import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import 'rxjs/add/observable/of';
import { BatchOperation } from '../../../../../model/BatchOperation';
import { IContainer } from '../../../../../model/IContainer';
import { User } from '../../../../../model/User';
import { UserSession } from '../../../../../model/UserSession';
import { Objects } from '../../../../../util/objects';
import { Url } from '../../../../../util/url';
import { UserToken } from '../../../../views/main/authentication/base/user-token';

export class ServiceInterface {

    constructor(private http: HttpClient) { }

    public async checkConnection(project: string): Promise<void> {

        let params = new HttpParams();
        params = params.append('heartbeat', 'true');

        const result = await this.http.get(Url.urlCheckConnectivity(project), { params: params })
            .toPromise();
        if (result instanceof HttpErrorResponse) {
            return Promise.reject(result);
        }
    }

    public async authenticate(user: User): Promise<UserSession> {
        const session: UserSession = await this.http.post(Url.urlAuthenticate(), user).toPromise() as UserSession;
        return session;
    }


    public async deauthenticate(): Promise<void> {
        await this.http.get(Url.urlDeauthenticate(), { responseType: 'text' }).toPromise();
    }

    public async projectnames(): Promise<string[]> {
        return this.http.get<string[]>(Url.urlProjectNames()).toPromise();
    }

    public async performBatchOperation(batchOperation: BatchOperation, token: UserToken): Promise<void> {
        return this.http
            .post<void>(Url.batchOperationUrl(token), batchOperation)
            .toPromise();
    }

    public async createElement(element: IContainer, token: UserToken): Promise<void> {
        let payload: any = this.prepareElementPayload(element);
        await this.http.post(Url.urlCreate(element.url), payload).toPromise();
    }

    public async readElement(url: string, token: UserToken): Promise<IContainer> {
        const element = await this.http.get<IContainer>(Url.urlElement(url)).toPromise();
        return element;
    }

    public async readContents(url: string, token: UserToken): Promise<IContainer[]> {
        const contents = this.http.get<IContainer[]>(Url.urlContents(url)).toPromise();
        return contents;
    }

    public async updateElement(element: IContainer, token: UserToken): Promise<void> {
        let payload: any = this.prepareElementPayload(element);
        await this.http.put(Url.urlUpdate(element.url), payload).toPromise();
    }

    public async deleteElement(url: string, token: UserToken): Promise<void> {
        await this.http.delete(Url.urlDelete(url)).toPromise();
    }

    public async performOperationPOST(url: string, serviceSuffix: string, payload: any, token: UserToken): Promise<any> {
        return await this.http.post(Url.urlCustomService(url, serviceSuffix), payload).toPromise();
    }

    public async performOperationGET(url: string, serviceSuffix: string, payload: any, token: UserToken): Promise<any> {
        return await this.http.get(Url.urlCustomService(url, serviceSuffix)).toPromise();
    }

    public async performQuery(url: string, serviceSuffix: string, parameters: { [key: string]: string }, token: UserToken): Promise<any> {
        let urlParams = new HttpParams();
        for (let key in parameters) {
            if (parameters[key]) {
                urlParams = urlParams.append(key, parameters[key]);
            }
        }
        const result = await this.http
            .get(Url.urlCustomService(url, serviceSuffix), { params: urlParams }).toPromise();
        return result;
    }

    /** Perform a model search.
     * @param query     The query string
     * @param token     The current authentication token of the user
     * @param filter    Map from search fields (e.g. name) to queries.
     *                  If a search field begins with '-', this means results that match the query should be excluded.
     *                  Example: {'-name':'car'} --> Exclude results with 'car' in the name
     */
    public async search(query: string, token: UserToken, filter?: { [key: string]: string }): Promise<IContainer[]> {
        let urlParams: HttpParams = new HttpParams();
        let queryString = query ? '+(' + query + ')' : '';
        if (filter) {
            for (let key in filter) {
                let modifier = '+';
                let field = key;
                if (key.startsWith('-')) {
                    modifier = '-';
                    field = key.substring(1);
                }
                queryString = queryString + ' ' + modifier + '(' + field + ':' + filter[key] + ')';
            }
        }
        urlParams = urlParams.append('query', queryString);

        try {
            const response = await this.http
                .get<IContainer[]>(Url.urlCustomService(token.project, 'search'), { params: urlParams })
                .toPromise();
            return response;
        } catch (e) {
            this.handleError(e);
        }
    }

    private async handleError(error: any, url?: string): Promise<any> {
        console.error('Error in Service Interface! (details below) [' + url + ']');
        return Promise.reject(error);
    }

    private prepareElementPayload(element: IContainer): any {
        let payload: any = Objects.clone(element);
        payload.url = undefined;
        delete payload.url;
        if (!element.id) {
            payload['___proxy'] = 'true';
        }
        return payload;
    }
}
