import { Component, OnInit } from '@angular/core';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import { Subject } from 'rxjs/Subject';
import { TranslateService } from '../../../../../../../node_modules/@ngx-translate/core';
import { Config } from '../../../../../config/config';
import { Folder } from '../../../../../model/Folder';
import { IContainer } from '../../../../../model/IContainer';
import { IContentElement } from '../../../../../model/IContentElement';
import { Search } from '../../../../../util/search';
import { Type } from '../../../../../util/type';
import { SpecmateDataService } from '../../../../data/modules/data-service/services/specmate-data.service';
import { AuthenticationService } from '../../../../views/main/authentication/modules/auth/services/authentication.service';
import { NavigatorService } from '../../navigator/services/navigator.service';
import { Requirement } from 'src/app/model/Requirement';

enum ActiveTab { project, library, recycleBin }

@Component({
    moduleId: module.id.toString(),
    selector: 'project-explorer',
    templateUrl: 'project-explorer.component.html',
    styleUrls: ['project-explorer.component.css']
})
export class ProjectExplorer implements OnInit {
    ActiveTab = ActiveTab;

    public _rootElements: IContainer[];
    public _rootLibraries: IContainer[];
    public _rootRecycleBinProject: IContainer[];
    public _rootRecycleBinLibrary: IContainer[];
    public currentActiveTab = ActiveTab.project;

    private searchQueries: Subject<string>;
    protected searchResults: IContentElement[];

    private numProjectFoldersDisplayed = Config.ELEMENT_CHUNK_SIZE;
    private numLibraryFoldersDisplayed = Config.ELEMENT_CHUNK_SIZE;
    private numRecycleBinFoldersDisplayed = Config.ELEMENT_CHUNK_SIZE;

    public get currentElement(): IContainer {
        return this.navigator.currentElement;
    }

    public get rootElements(): IContainer[] {
        if (this._rootElements === undefined || this._rootElements === null) {
            return [];
        }
        return this._rootElements.slice(0, Math.min(this.numProjectFoldersDisplayed, this._rootElements.length));
    }

    public get rootLibraries(): IContainer[] {
        if (this._rootLibraries === undefined || this._rootLibraries === null) {
            return [];
        }
        return this._rootLibraries.slice(0, Math.min(this.numLibraryFoldersDisplayed, this._rootLibraries.length));
    }

    public get rootRecycleBinProject(): IContainer[] {
        if (this._rootRecycleBinProject === undefined || this._rootRecycleBinProject === null) {
            return [];
        }
        return this._rootRecycleBinProject.slice(0, Math.min(this.numRecycleBinFoldersDisplayed, this._rootRecycleBinProject.length));
    }

    public get rootRecycleBinLibrary(): IContainer[] {
        if (this._rootRecycleBinLibrary === undefined || this._rootRecycleBinLibrary === null) {
            return [];
        }
        return this._rootRecycleBinLibrary.slice(0, Math.min(this.numRecycleBinFoldersDisplayed, this._rootRecycleBinLibrary.length));
    }

    constructor(private translate: TranslateService, private dataService: SpecmateDataService,
        private navigator: NavigatorService, private auth: AuthenticationService) { }

    ngOnInit() {
        this.initialize();
        this.auth.authChanged.subscribe(() => {
            this.initialize();
        });
    }

    public get canLoadMoreProjectFolders(): boolean {
        if (this._rootElements === undefined || this._rootElements === null) {
            return false;
        }
        return this._rootElements.length > this.numProjectFoldersDisplayed;
    }

    public get canLoadMoreLibraryFolders(): boolean {
        if (this._rootLibraries === undefined || this._rootLibraries === null) {
            return false;
        }
        return this._rootLibraries.length > this.numLibraryFoldersDisplayed;
    }

    public get canLoadMoreRecycleBinFoldersProject(): boolean {
        if (this._rootRecycleBinProject === undefined || this._rootRecycleBinProject === null) {
            return false;
        }
        return this._rootRecycleBinProject.length > this.numRecycleBinFoldersDisplayed;
    }

    public get canLoadMoreRecycleBinFoldersLibrary(): boolean {
        if (this._rootRecycleBinLibrary === undefined || this._rootRecycleBinLibrary === null) {
            return false;
        }
        return this._rootRecycleBinLibrary.length > this.numRecycleBinFoldersDisplayed;
    }

    public search(query: string): void {
        this.searchQueries.next(query);
    }

    private async initialize(): Promise<void> {
        if (!this.auth.isAuthenticated) {
            this.clean();
            return;
        }

        // const project: IContainer = await this.dataService.readElement(this.auth.token.project);
        let libraryFolders: string[] = this.auth.session.libraryFolders;
        let projectContents: IContainer[] = await this.dataService.readContents(this.auth.token.project);

        // In this case, we were logged out automatically.
        if (projectContents === undefined) {
            this.clean();
            return;
        }

        this._rootElements = projectContents.filter(c => Type.is(c, Requirement) || (Type.is(c, Folder) && !(c as Folder).library));
        this._rootLibraries = projectContents.filter(c => Type.is(c, Folder) && (c as Folder).library && libraryFolders.indexOf(c.id) > -1);
        this._rootRecycleBinProject = projectContents.filter(c => Type.is(c, Folder) && !(c as Folder).library);
        this._rootRecycleBinLibrary = projectContents.filter(c => Type.is(c, Folder) && (c as Folder).library
            && libraryFolders.indexOf(c.id) > -1);

        let filter = { '-type': 'Folder' };

        // We clean this in case we're logged out. Thus, we need to reinit here.
        if (this.searchQueries === undefined) {
            this.searchQueries = new Subject<string>();
        }
        this.searchQueries
            .debounceTime(300)
            .distinctUntilChanged()
            .subscribe(query => {
                if (query && query.length >= 2) {
                    query = Search.processSearchQuery(query);
                    this.dataService.search(query, filter).then(results => {
                        this.searchResults = results;
                    });
                } else {
                    this.searchResults = [];
                }
            }
            );
    }

    public get recycleBinIsEmpty(): boolean {
        return this._rootRecycleBinProject.filter(e => e.hasRecycledChildren === true).length === 0 &&
            this._rootRecycleBinLibrary.filter(e => e.hasRecycledChildren === true).length === 0;
    }

    public loadMoreProjectFolders(): void {
        this.numProjectFoldersDisplayed += Config.ELEMENT_CHUNK_SIZE;
    }

    public loadMoreLibraryFolders(): void {
        this.numLibraryFoldersDisplayed += Config.ELEMENT_CHUNK_SIZE;
    }

    public loadMoreRecycleBinFolders(): void {
        this.numRecycleBinFoldersDisplayed += Config.ELEMENT_CHUNK_SIZE;
    }

    public switchToProject(): void {
        this.currentActiveTab = ActiveTab.project;
    }

    public switchToLibrary(): void {
        this.currentActiveTab = ActiveTab.library;
    }
    public switchToRecycleBin(): void {
        this.currentActiveTab = ActiveTab.recycleBin;
    }

    private clean(): void {
        this._rootElements = undefined;
        this._rootLibraries = undefined;
        this._rootRecycleBinProject = undefined;
        this._rootRecycleBinLibrary = undefined;
        this.searchQueries = undefined;
        this.searchResults = undefined;
    }

    public get projectName(): string {
        return this.auth.token.project;
    }

    private isRecycled(element: IContainer) {
        return element.recycled;
    }
}
