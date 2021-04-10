import { HttpClient } from '@angular/common/http';
import { EventEmitter, Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { SimpleModal } from 'src/app/modules/notification/modules/modals/services/simple-modal.service';
import { Monitorable } from 'src/app/modules/notification/modules/operation-monitor/base/monitorable';
import { CEGConnection } from '../../../../../model/CEGConnection';
import { IContainer } from '../../../../../model/IContainer';
import { IModelConnection } from '../../../../../model/IModelConnection';
import { IModelNode } from '../../../../../model/IModelNode';
import { IPositionable } from '../../../../../model/IPositionable';
import { ProcessConnection } from '../../../../../model/ProcessConnection';
import { Arrays } from '../../../../../util/arrays';
import { Id } from '../../../../../util/id';
import { Type } from '../../../../../util/type';
import { Url } from '../../../../../util/url';
import { ServerConnectionService } from '../../../../common/modules/connection/services/server-connection-service';
import { AuthenticationService } from '../../../../views/main/authentication/modules/auth/services/authentication.service';
import { LoggingService } from '../../../../views/side/modules/log-list/services/logging.service';
import { Command } from './command';
import { DataCache } from './data-cache';
import { EOperation } from './e-operation';
import { Scheduler } from './scheduler';
import { ServiceInterface } from './service-interface';

/**
 * The interface to all data handling things.
 * It handles the cache and the service interface.
 *
 * In commands executed by the user via the gui, always set the virtual argument to true, and use the commit-method in a save button.
 * This makes changes being done only in the cache, not on the server.
 * In rare cases, e.g., creating a new model, the virtual flag can be omitted, since we want to store this directly on the server.
 *
 * Whenever the user discards local changes, clearCommits() needs to be called to prevent commits from other views are done.
 */
@Injectable()
export class SpecmateDataService extends Monitorable {

    public static OP_READ_ELEMENT = 'readElement';
    public static OP_READ_CONTENTS = 'readContents';
    public static OP_SEARCH = 'search';
    public static OP_UPDATE = 'update';
    public static OP_DELETE = 'delete';
    public static OP_COMMIT = 'commit';
    public static OP_PERFORM_OPERATION = 'performOperation';
    public static OP_PERFORM_QUERY = 'performQuery';

    public committed = new EventEmitter<void>();
    public elementChanged = new EventEmitter<string>(true);
    private cache: DataCache = new DataCache();
    private serviceInterface: ServiceInterface;
    private scheduler: Scheduler;

    constructor(http: HttpClient,
        private auth: AuthenticationService,
        private logger: LoggingService,
        private translate: TranslateService,
        private simpleModal: SimpleModal,
        private connectionService: ServerConnectionService) {
        super();
        this.serviceInterface = new ServiceInterface(http);
        this.scheduler = new Scheduler(this, this.logger, this.translate);
        this.auth.authChanged.subscribe(() => {
            if (!this.auth.isAuthenticated) {
                this.clear();
            }
        });
    }

    private clear(): void {
        this.clearCommits();
        this.cache.clear();
    }

    public createElement(element: IContainer, virtual: boolean, compoundId: string): Promise<void> {
        this.elementChanged.emit(element.url);
        if (virtual) {
            return Promise.resolve(this.createElementVirtual(element, compoundId));
        }
        return this.createElementServer(element);
    }

    public deleteCachedContent(url: string) {
        this.cache.deleteElement(url);
    }

    public readContents(url: string, virtual?: boolean): Promise<IContainer[]> {
        this.start(SpecmateDataService.OP_READ_CONTENTS + '-' + url);
        let getFromCache = this.cache.isCachedContents(url);
        if (this.scheduler.isVirtualElement(url)) {
            getFromCache = true;
        } else if (virtual === false) {
            getFromCache = false;
        }

        if (getFromCache) {
            let contents: IContainer[] = this.readContentsVirtual(url);
            if (contents) {
                return Promise.resolve(contents).then((loadedContents: IContainer[]) => this.readContentsComplete(loadedContents, url));
            } else if (this.scheduler.isVirtualElement(url)) {
                this.logger.info(this.translate.instant('triedToReadContensForVirtualElement'), url);
                this.cache.updateContents([], url);
                let virtualContents: IContainer[] = this.readContentsVirtual(url);
                return Promise.resolve(virtualContents)
                    .then((loadedContents: IContainer[]) => this.readContentsComplete(loadedContents, url));
            } else {
                this.logger.warn(this.translate.instant('triedToReadContensVirtuallyButCouldNotFindThemFallingBackToServer'), url);
            }
        }
        return this.readContentsServer(url).then((contents: IContainer[]) => this.readContentsComplete(contents, url));
    }

    public readContentsRecursiveVirtually(url: string): IContainer[] {
        const element = this.readElementVirtual(url);
        let toVisit = [element];
        const contents = [];
        while (toVisit.length > 0) {
            const currentElement = toVisit.pop();
            const currentContents = this.readContentsVirtual(currentElement.url);
            if (currentContents) {
                toVisit = toVisit.concat(currentContents);
            }
            contents.push(currentElement);
        }
        return contents;
    }

    private readContentsComplete(contents: IContainer[], url: string): IContainer[] {
        this.end(SpecmateDataService.OP_READ_CONTENTS + '-' + url);
        return contents;
    }

    public hasElement(url: string): boolean {
        return this.cache.isCachedElement(url);
    }

    public async readElement(url: string, virtual?: boolean): Promise<IContainer> {
        this.start(SpecmateDataService.OP_READ_ELEMENT + '-' + url);
        let readElementTask: Promise<IContainer> = undefined;
        if (virtual === undefined && (this.scheduler.isVirtualElement(url) || this.cache.isCachedElement(url)) || virtual) {
            let element: IContainer = this.readElementVirtual(url);
            if (element) {
                if (!((<any>element).live)) {
                    readElementTask = Promise.resolve(element);
                }
            } else {
                this.logger.warn(this.translate.instant('triedToReadElementVirtuallyButCouldNotFindItFallingBackToServer'), url);
            }
        }
        if (!readElementTask) {
            readElementTask = this.readElementServer(url);
        }
        const parentUrl = Url.parent(url);
        if (parentUrl === undefined) {
            return readElementTask.then(element => this.readElementComplete(element, url));
        }
        return this.readContents(parentUrl)
            .then(() => readElementTask)
            .then((element: IContainer) => this.readElementComplete(element, url));
    }

    public readElementComplete(element: IContainer, url: string): IContainer {
        this.end(SpecmateDataService.OP_READ_ELEMENT + '-' + url);
        this.scheduler.initElement(element);
        return element;
    }

    public async updateElement(element: IContainer, virtual: boolean, compoundId: string): Promise<void> {
        if (!this.hasElement(element.url)) {
            return;
        }
        this.start(SpecmateDataService.OP_UPDATE + '-' + element.url);
        if (virtual) {
            this.updateElementVirtual(element, compoundId);
        } else {
            await this.updateElementServer(element);
        }
        this.elementChanged.emit(element.url);
        this.end(SpecmateDataService.OP_UPDATE + '-' + element.url);
    }

    public async deleteElement(url: string, virtual: boolean, compoundId: string): Promise<void> {
        this.start(SpecmateDataService.OP_DELETE + '-' + url);
        this.elementChanged.emit(url);
        if (virtual || this.scheduler.isVirtualElement(url)) {
            this.deleteElementVirtual(url, compoundId);
        } else {
            await this.deleteElementServer(url);
        }
        this.end(SpecmateDataService.OP_DELETE + '-' + url);
    }

    public async clearModel(nodes: IContainer[], connections: IContainer[], compoundId = Id.uuid): Promise<void> {
        for (let i = connections.length - 1; i >= 0; i--) {
            await this.deleteElement(connections[i].url, true, compoundId);
        }
        for (let i = nodes.length - 1; i >= 0; i--) {
            await this.deleteElement(nodes[i].url, true, compoundId);
        }
    }

    public async sanitizeContentPositions(elements: (IContainer & IPositionable)[], update: boolean, compoundId?: string): Promise<void> {
        if (!compoundId) {
            compoundId = Id.uuid;
        }
        elements.forEach((element: IContainer & IPositionable, index: number) => {
            element.position = index;
            if (update) {
                this.updateElement(<IContainer>element, true, compoundId);
            }
        });
    }

    public clearCommits(): void {
        this.scheduler.clearCommits();
    }

    public get hasCommits(): boolean {
        return this.scheduler.hasCommits;
    }

    public get countCommits(): number {
        return this.scheduler.countOpenCommits;
    }

    public get unresolvedCommands(): Command[] {
        return this.scheduler.unresolvedCommands;
    }

    public async commit(taskName: string): Promise<void> {
        try {
            this.start(SpecmateDataService.OP_COMMIT);
            const batchOperation = this.scheduler.toBatchOperation();
            let elementsToRead = this.scheduler.getElementsToReload();
            await this.serviceInterface.performBatchOperation(batchOperation, this.auth.token);
            this.scheduler.resolveBatchOperation(batchOperation);
            this.scheduler.clearCommits();
            await this.reloadElemensAfterCommit(elementsToRead);
            this.scheduler.clearElementsToReload();
            this.end(SpecmateDataService.OP_COMMIT);
            this.committed.emit();
        } catch (error) {
            this.simpleModal.openOk(this.translate.instant('saveError.title'), this.translate.instant('saveError.retry'));
            console.error(error);
        }

    }

    public undo(): void {
        this.scheduler.undo();
    }

    public discardChanges(): void {
        this.scheduler.undoAll();
    }

    public undoCreate(url: string) {
        const element = this.readElementVirtual(url);
        if (Type.of(element) === CEGConnection.name || Type.of(element) === ProcessConnection.name) {
            const connection = element as IModelConnection;
            const source = this.readElementVirtual(connection.source.url) as IModelNode;
            const target = this.readElementVirtual(connection.target.url) as IModelNode;
            const outgoingConnection = source.outgoingConnections.find(proxy => proxy.url === connection.url);
            const incomingConnection = target.incomingConnections.find(proxy => proxy.url === connection.url);
            Arrays.remove(source.outgoingConnections, outgoingConnection);
            Arrays.remove(target.incomingConnections, incomingConnection);
            this.cache.addElement(source);
            this.cache.addElement(target);
        }
        this.cache.deleteElement(url);
    }

    public undoUpdate(originalValue: IContainer) {
        this.cache.addElement(originalValue);
    }

    public undoDelete(originalValue: IContainer) {
        this.cache.addElement(originalValue);
    }

    private createElementVirtual(element: IContainer, compoundId: string): void {
        this.scheduler.schedule(element.url, EOperation.CREATE, element, undefined, compoundId);
        return this.cache.addElement(element);
    }

    private readContentsVirtual(url: string): IContainer[] {
        return this.cache.readContents(url);
    }

    private readElementVirtual(url: string, forceServer?: boolean): IContainer {
        return this.cache.readElement(url);
    }

    private updateElementVirtual(element: IContainer, compoundId: string): void {
        this.scheduler.schedule(element.url, EOperation.UPDATE, element, undefined, compoundId);
        this.cache.addElement(element);
    }

    private deleteElementVirtual(url: string, compoundId: string): void {
        this.scheduler.schedule(url, EOperation.DELETE, undefined, this.readElementVirtual(url), compoundId);
        this.cache.deleteElement(url);
    }

    private createElementServer(element: IContainer): Promise<void> {
        if (!this.auth.isAuthenticatedForUrl(element.url)) {
            return Promise.resolve();
        }
        this.logStart(this.translate.instant('create'), element.url);
        return this.serviceInterface.createElement(element, this.auth.token).then(() => {
            this.scheduler.resolve(element.url);
            this.logFinished(this.translate.instant('create'), element.url);
        }).catch((error) => this.handleError(this.translate.instant('elementCouldNotBeSaved'), element.url, error));
    }

    private readContentsServer(url: string): Promise<IContainer[]> {
        if (!this.auth.isAuthenticatedForUrl(url)) {
            return Promise.resolve(undefined);
        }
        this.logStart(this.translate.instant('log.readContents'), url);
        return this.serviceInterface.readContents(url, this.auth.token).then((contents: IContainer[]) => {
            this.cache.updateContents(contents, url);
            contents.forEach((element: IContainer) => this.scheduler.initElement(element));
            this.logFinished(this.translate.instant('log.readContents'), url);
            return this.cache.readContents(url);
        }).catch((error) => this.handleError(this.translate.instant('contentsCouldNotBeRead'), url, error));
    }

    private async readElementServer(url: string): Promise<IContainer> {
        if (!this.auth.isAuthenticatedForUrl(url)) {
            return undefined;
        }
        this.logStart(this.translate.instant('log.readElement'), url);
        try {
            const element = await this.serviceInterface.readElement(url, this.auth.token);
            this.cache.addElement(element);
            return this.cache.readElement(url);
        } catch (error) {
            this.handleError(this.translate.instant('elementCouldNotBeRead'), url, error);
        } finally {
            this.logFinished(this.translate.instant('log.readElement'), url);
        }
    }

    private updateElementServer(element: IContainer): Promise<void> {
        if (!this.auth.isAuthenticatedForUrl(element.url)) {
            return Promise.resolve();
        }
        this.logStart(this.translate.instant('log.update'), element.url);
        return this.serviceInterface.updateElement(element, this.auth.token).then(() => {
            this.scheduler.resolve(element.url);
            this.logFinished(this.translate.instant('log.update'), element.url);
        }).catch((error) => this.handleError(this.translate.instant('elementCouldNotBeUpdated'), element.url, error));
    }

    private deleteElementServer(url: string): Promise<void> {
        if (!this.auth.isAuthenticatedForUrl(url)) {
            return Promise.resolve();
        }
        this.logStart(this.translate.instant('log.delete'), url);
        return this.serviceInterface.deleteElement(url, this.auth.token)
            .then(() => {
                this.scheduler.resolve(url);
                this.logFinished(this.translate.instant('log.delete'), url);
                this.readAllParentsServer(url);
            })
            .catch((error) => {
                this.handleError(this.translate.instant('elementCouldNotBeDeleted'), url, error);
            });
    }

    public performOperations(url: string, operation: string, payload?: any, httpGET?: boolean,
        optParameters?: { [key: string]: string }): Promise<any> {
        if (!this.auth.isAuthenticatedForUrl(url)) {
            return Promise.resolve(false);
        }
        this.start(SpecmateDataService.OP_PERFORM_OPERATION + '-' + url);
        let performFunction;
        if (httpGET) {
            performFunction = this.serviceInterface.performOperationGET;
        } else {
            performFunction = this.serviceInterface.performOperationPOST;
        }
        let params = optParameters ? optParameters : {};
        return performFunction.apply(this.serviceInterface, [url, operation, payload, params, this.auth.token])
            .then((result: any) => {
                this.end(SpecmateDataService.OP_PERFORM_OPERATION + '-' + url);
                return result;
            })
            .catch((error: any) => {
                this.end(SpecmateDataService.OP_PERFORM_OPERATION + '-' + url);
                this.handleError(this.translate.instant('operationCouldNotBePerformed') +
                    ' ' + this.translate.instant('operation') + ': ' + operation + ' ' +
                    this.translate.instant('payload') + ': ' + JSON.stringify(payload), url, error);
                return Promise.reject();
            }
            );
    }

    public performQuery(url: string, operation: string, parameters: { [key: string]: string; }): Promise<any> {
        if (!this.auth.isAuthenticatedForUrl(url)) {
            return Promise.resolve();
        }
        this.start(SpecmateDataService.OP_PERFORM_QUERY + '-' + url + '-' + operation);
        this.logStart(this.translate.instant('log.queryOperation') + ': ' + operation, url);
        return this.serviceInterface.performQuery(url, operation, parameters, this.auth.token)
            .then((result: any) => {
                this.end(SpecmateDataService.OP_PERFORM_QUERY + '-' + url + '-' + operation);
                this.logFinished(this.translate.instant('log.queryOperation') + ': ' + operation, url);
                return result;
            })
            .catch((error) => {
                this.end(SpecmateDataService.OP_PERFORM_QUERY + '-' + url + '-' + operation);
                this.handleError(this.translate.instant('queryCouldNotBePerformed') + ' ' + this.translate.instant('operation') + ': ' +
                    operation + ' ' + this.translate.instant('parameters') + ': ' + JSON.stringify(parameters), url, error);
                return Promise.reject();
            });
    }

    public search(query: string, filter?: { [key: string]: string }): Promise<IContainer[]> {
        if (!this.auth.isAuthenticated) {
            return Promise.resolve([]);
        }
        this.start(SpecmateDataService.OP_SEARCH);
        this.logStart(this.translate.instant('log.search') + ': ' + query, '');
        return this.serviceInterface.search(query, this.auth.token, filter)
            .then((result: IContainer[]) => {
                this.end(SpecmateDataService.OP_SEARCH);
                this.logFinished(this.translate.instant('log.search') + ': ' + query, '');
                return result;
            })
            .catch((error) => {
                this.end(SpecmateDataService.OP_SEARCH);
                return this.handleError(this.translate.instant('queryCouldNotBePerformed') + ' ' +
                    this.translate.instant('operation') + ' : search ' + query, '', error);
            });
    }

    private logStart(message: string, url: string): Promise<any> {
        this.logger.debug(this.translate.instant('log.trying') + ': ' + message, url);
        return Promise.resolve(undefined);
    }

    private logFinished(message: string, url: string): Promise<any> {
        this.logger.info(this.translate.instant('log.success') + ': ' + message, url);
        return Promise.resolve(undefined);
    }

    private handleError(message: string, url: string, error: any): Promise<any> {
        console.error(message);
        if (error.status === 401) {
            this.auth.deauthenticate();
        }
        this.connectionService.handleErrorResponse(error, url);
        return Promise.resolve(undefined);
    }

    public async recycleElement(url: string): Promise<void> {
        await this.performOperations(url, 'recycle');
        await this.readAllParentsServer(url);
    }

    public async restoreElement(url: string): Promise<void> {
        await this.performOperations(url, 'restore');
        this.readAllParentsServer(url);
    }

    public async readAllParentsServer(url: string) {
        let parents = Url.allParents(url);
        for (let i = 0; i < parents.length; i++) {
            let parent = parents[i];
            await this.readElement(parent, false);
        }
    }

    private async reloadElemensAfterCommit(elementUrls: string[]) {
        for (const url of elementUrls) {
            this.cache.deleteElement(url);
            await this.readElement(url, false);
        }
    }
}
