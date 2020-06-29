import { Injectable } from '@angular/core';
import { CEGModel } from '../../../../../../model/CEGModel';
import { IContainer } from '../../../../../../model/IContainer';
import { Process } from '../../../../../../model/Process';
import { Requirement } from '../../../../../../model/Requirement';
import { TestProcedure } from '../../../../../../model/TestProcedure';
import { TestSpecification } from '../../../../../../model/TestSpecification';
import { Sort } from '../../../../../../util/sort';
import { Type } from '../../../../../../util/type';
import { Url } from '../../../../../../util/url';
import { SpecmateDataService } from '../../../../../data/modules/data-service/services/specmate-data.service';
import { NavigatorService } from '../../../../../navigation/modules/navigator/services/navigator.service';
import { AuthenticationService } from '../../../../main/authentication/modules/auth/services/authentication.service';
import { Folder } from '../../../../../../model/Folder';
import { ContentsContainerService } from 'src/app/modules/views/main/editors/modules/contents-container/services/content-container.service';

@Injectable()
export class AdditionalInformationService {

    public element: IContainer;
    private parents: IContainer[];
    private _exports: string[];
    private _testSpecifications: TestSpecification[];

    constructor(private dataService: SpecmateDataService,
        navigator: NavigatorService,
        private auth: AuthenticationService,
        private contentService: ContentsContainerService) {
        navigator.hasNavigated.subscribe((element: IContainer) => {
            this.element = element;
            this.load();
        });
        auth.authChanged.subscribe(() => this.reset());
        contentService.onModelDeleted.subscribe(
            () => { this.loadTestSpecifications(); });
    }

    private reset(): void {
        this.element = undefined;
        this.parents = undefined;
        this._exports = undefined;
        this._testSpecifications = undefined;
    }

    private async load(): Promise<void> {
        await this.loadParents();
        await this.loadTestSpecifications();
        await this.loadExports();
    }

    private async loadTestSpecifications(): Promise<void> {
        if (!this.canHaveTestSpecifications) {
            return;
        }
        if (this.isModel(this.element)) {
            const testSpecifications =
                await this.dataService.performQuery(this.element.url, 'listRecursive', { class: TestSpecification.className });
            this._testSpecifications = Sort.sortArray(testSpecifications).filter(testSpec => testSpec.recycled === false);
        } else {
            this._testSpecifications = undefined;
        }
    }

    private async loadParents(): Promise<void> {
        if (!this.auth.isAuthenticated) {
            return;
        }
        let parentUrls: string[] = [];
        let url: string = Url.parent(this.element.url);

        while (!Url.isRoot(url, this.auth.token.project)) {
            parentUrls.push(url);
            url = Url.parent(url);
        }

        this.parents = await Promise.all(parentUrls.map(url => this.dataService.readElement(url)));
    }

    private async loadExports(): Promise<void> {
        if (Type.is(this.element, TestSpecification) || Type.is(this.element, TestProcedure)) {
            this._exports = await this.dataService.performQuery(this.element.url, 'exporterlist', {});
        } else {
            this._exports = [];
        }
    }

    public get hasAdditionalInformation(): boolean {
        return this.requirement !== undefined || this.model !== undefined || this.testSpecification !== undefined;
    }

    public get model(): IContainer {
        if (!this.parents) {
            return undefined;
        }
        return this.parents.find((element: IContainer) => this.isModel(element));
    }

    public get requirement(): Requirement {
        if (!this.parents) {
            return undefined;
        }
        return this.parents.find((element: IContainer) => Type.is(element, Requirement)) as Requirement;
    }

    public get testSpecification(): TestSpecification {
        if (!this.parents) {
            return undefined;
        }
        return this.parents.find((element: IContainer) => Type.is(element, TestSpecification));
    }

    public get testSpecifications(): TestSpecification[] {
        return this._testSpecifications;
    }

    public get exports(): string[] {
        return this._exports;
    }

    public get canHaveTestSpecifications(): boolean {
        return Type.is(this.element, Requirement) || this.isModel(this.element) || Type.is(this.element, Folder);
    }

    public get canGenerateTestSpecifications(): boolean {
        return this.element && (this.isModel(this.element) || Type.is(this.element, Requirement) || Type.is(this.element, Folder));
    }

    public get canAddTestSpecifications(): boolean {
        return Type.is(this.element, Requirement);
    }

    public get canGenerateCEGModel(): boolean {
        return Type.is(this.element, CEGModel);
    }

    private isModel(element: IContainer): boolean {
        return Type.is(element, CEGModel) || Type.is(element, Process);
    }
}
