import { Component, Input, OnInit } from '@angular/core';
import * as $ from 'jquery';
import { Config } from '../../../../../config/config';
import { CEGModel } from '../../../../../model/CEGModel';
import { Folder } from '../../../../../model/Folder';
import { IContainer } from '../../../../../model/IContainer';
import { Process } from '../../../../../model/Process';
import { Requirement } from '../../../../../model/Requirement';
import { TestProcedure } from '../../../../../model/TestProcedure';
import { TestSpecification } from '../../../../../model/TestSpecification';
import { Key } from '../../../../../util/keycode';
import { Type } from '../../../../../util/type';
import { Url } from '../../../../../util/url';
import { SpecmateDataService } from '../../../../data/modules/data-service/services/specmate-data.service';
import { NavigatorService } from '../../navigator/services/navigator.service';
import { Id } from 'src/app/util/id';
import { TranslateService } from '@ngx-translate/core';
import { ContentsContainerService } from 'src/app/modules/views/main/editors/modules/contents-container/services/content-container.service';
import { ConfirmationModal } from 'src/app/modules/notification/modules/modals/services/confirmation-modal.service';
import { SpecmateType } from 'src/app/util/specmate-type';
import { CEGNode } from 'src/app/model/CEGNode';
import { CEGLinkedNode } from 'src/app/model/CEGLinkedNode';

@Component({
    moduleId: module.id.toString(),
    selector: 'element-tree',
    templateUrl: 'element-tree.component.html',
    styleUrls: ['element-tree.component.css']
})
export class ElementTree implements OnInit {

    @Input()
    public baseUrl: string;

    @Input()
    public parent: IContainer;

    @Input()
    public library = false;

    @Input()
    public recycleBin = false;

    public numChildrenDisplayed = Config.ELEMENT_CHUNK_SIZE;

    constructor(
        private dataService: SpecmateDataService,
        private navigator: NavigatorService,
        private translate: TranslateService,
        private modal: ConfirmationModal,
        private contentService: ContentsContainerService) { }

    public get contents(): IContainer[] {
        if (this._contents === undefined || this._contents === null) {
            return [];
        }
        return this._contents.slice(0, Math.min(this.numChildrenDisplayed, this._contents.length));
    }

    public get canLoadMore(): boolean {
        if (this._contents === undefined || this._contents === null) {
            return false;
        }
        return this._contents.length > this.numChildrenDisplayed;
    }

    private _currentElement: IContainer;

    public get currentElement(): IContainer {
        return this._currentElement;
    }

    @Input()
    public set currentElement(currentElement: IContainer) {
        this._currentElement = currentElement;
        if (this.isMustOpen) {
            this.initContents();
            this._collapsed = true;
        }
    }

    @Input()
    public withExpand: boolean;

    private _element: IContainer;
    public get element(): IContainer {
        return this._element;
    }

    @Input()
    public set element(element: IContainer) {
        if (this._element !== undefined && this._element.url === element.url) {
            return;
        }
        this._element = element;
        if (this.isMustOpen) {
            this.initContents();
        }
    }

    public _contents: IContainer[];

    public _expanded = false;
    public get expanded(): boolean {
        if (this._collapsed) {
            if (!this._expanded && this.isMustOpen) {
                this._expanded = true;
                this._collapsed = false;
            }
        }
        return this._expanded;
    }
    public set expanded(expanded: boolean) {
        this._expanded = expanded;
    }

    public get canExpand(): boolean {
        return (this.isCEGModelNode || this.isProcessNode || this.isRequirementNode || this.isFolderNode)
            && this.withExpand;
    }

    public get isMustOpen(): boolean {
        if (this._currentElement && this.element) {
            return Url.isParent(this.element.url, this._currentElement.url);
        }
        return false;
    }

    async ngOnInit() {
        const siblings = await this.dataService.readContents(Url.parent(this.baseUrl));
        const element = siblings.find(element => element.url === this.baseUrl);
        this.element = element;
        if (this.expanded || this.isMustOpen) {
            this.initContents();
        }
    }

    private _collapsed = true;

    public toggle(): void {
        if (this.expanded) {
            this.contract();
            this._collapsed = false;
        } else {
            this.expand();
        }
    }

    private expand(): void {
        this.expanded = true;
        if (this.expanded && !this._contents) {
            this.initContents();
        }
    }

    private contract(): void {
        this.expanded = false;
    }

    private async initContents(): Promise<void> {
        if (this._contents === undefined || this._contents.length === 0) {
            this._contents = await this.dataService.readContents(this.baseUrl, false);
        }
    }

    public get isRequirementNode(): boolean {
        return Type.is(this.element, Requirement);
    }

    public get isCEGModelNode(): boolean {
        return Type.is(this.element, CEGModel);
    }

    public get isFolderNode(): boolean {
        return Type.is(this.element, Folder);
    }

    public get isTestSpecificationNode(): boolean {
        return Type.is(this.element, TestSpecification);
    }

    public get isGeneratedTestSpecificationNode(): boolean {
        return this.isTestSpecificationNode && this.parent && (SpecmateType.isModel(this.parent));
    }

    public get isProcessNode(): boolean {
        return Type.is(this.element, Process);
    }

    public get isTestProcedureNode(): boolean {
        return Type.is(this.element, TestProcedure);
    }

    public get isActive(): boolean {
        if (!this.element || !this.navigator.currentElement) {
            return false;
        }
        return this.element.url === this.navigator.currentElement.url;
    }

    public get hasLink(): boolean {
        return (!this.isFolderNode || (this.library)) && !this.element.recycled;
    }

    public get hasRestore(): boolean {
        return this.element.recycled && this.parent && this.parent.recycled === false;
    }

    private get isRoot(): boolean {
        return this.parent === undefined;
    }

    public get showElement(): boolean {
        return (this.isCEGModelNode || this.isProcessNode || this.isRequirementNode
            || this.isTestSpecificationNode || this.isFolderNode || this.isTestProcedureNode)
            && ((!this.recycleBin && !this.element.recycled) || (this.recycleBin && this.element.hasRecycledChildren));
    }

    public loadMore(): void {
        this.numChildrenDisplayed += Config.ELEMENT_CHUNK_SIZE;
    }

    public async delete(): Promise<void> {
        try {

            let linkingNodeUrls: string[] = [];
            let linkedNodeUrls: string[] = [];
            if (Type.is(this.element, CEGModel)) {
                const contents = await this.dataService.readContents(this.element.url, false);
                linkingNodeUrls = contents
                    .filter(element => (element as CEGNode).linksFrom?.length > 0)
                    .map(element => (element as CEGNode).linksFrom)
                    .reduce((a, b) => a.concat(b), [])
                    .map(proxy => proxy.url);
                linkedNodeUrls = contents
                    .map(element => (element as CEGLinkedNode).linkTo?.url)
                    .filter(url => url !== undefined);
            }

            let message = this.translate.instant('doYouReallyWantToDeletePermanent', { name: this.element.name });
            if (Type.is(this.element, Folder)) {
                message = this.translate.instant('doYouReallyWantToDeleteFolderPermanent', { name: this.element.name });
            }
            if (linkingNodeUrls.length > 0) {
                message += '\n\n' + this.translate.instant('linkedNodesInModel');
            }
            await this.modal.openOkCancel('ConfirmationRequired', message);

            const compoundId = Id.uuid;
            for (const url of linkingNodeUrls) {
                const linkingNode = await this.dataService.readElement(url, true) as CEGLinkedNode;
                delete linkingNode.linkTo;
                await this.dataService.updateElement(linkingNode, true, compoundId);
            }
            for (const linkedNodeUrl of linkedNodeUrls) {
                const linkedNode = await this.dataService.readElement(linkedNodeUrl, true) as CEGNode;
                const urlsToDelete = linkedNode
                    .linksFrom.filter(proxy => Url.isParent(this.element.url, proxy.url))
                    .map(proxy => proxy.url);
                for (const urlToDelete of urlsToDelete) {
                    const index = linkedNode.linksFrom.findIndex(proxy => proxy.url === urlToDelete);
                    linkedNode.linksFrom.splice(index, 1);
                }
                await this.dataService.updateElement(linkedNode, true, compoundId);
            }

            await this.dataService.deleteElement(this.element.url, true, compoundId);
            await this.dataService.commit(this.translate.instant('delete'));
            await this.dataService.readContents(this.parent.url, false);
        } catch (e) { }
    }

    public async restore(): Promise<void> {
        try {
            let message = this.translate.instant('doYouReallyWantToRestore', { name: this.element.name });
            await this.modal.openOkCancel('ConfirmationRequired', message);
            await this.dataService.restoreElement(this.element.url);
            await this.dataService.readElement(this.element.url, false);
            this.contentService.isDeleted();
            this.initContents();
        } catch (e) { }
    }

    public handleKey(event: KeyboardEvent, shouldToggle?: boolean): void {
        if ([Key.SPACEBAR, Key.ARROW_RIGHT, Key.ARROW_LEFT, Key.ARROW_DOWN, Key.ARROW_UP].indexOf(event.keyCode) >= 0) {
            event.preventDefault();
            event.stopPropagation();
        }

        switch (event.keyCode) {
            case Key.SPACEBAR:
                this.toggle();
                break;

            case Key.ARROW_RIGHT:
                if (shouldToggle) {
                    this.expand();
                } else {
                    this.move(1, event.srcElement as Element);
                }
                break;

            case Key.ARROW_LEFT:
                if (shouldToggle) {
                    this.contract();
                } else {
                    this.move(-1, event.srcElement as Element);
                }
                break;

            case Key.ARROW_UP:
                this.move(-1, event.srcElement as Element);
                break;

            case Key.ARROW_DOWN:
                this.move(1, event.srcElement as Element);
                break;
        }
    }

    private move(off: number, elem: Element): void {
        const canFocus = $(':focusable');
        const currentIndex = canFocus.index(elem);
        let index = currentIndex + off;
        if (index >= canFocus.length) {
            index = 0;
        }
        const targetElem = canFocus.eq(index);
        targetElem.focus();
    }
}

// register jQuery extension
$.extend($['expr'][':'], {
    focusable: function (el: any, index: any, selector: any) {
        return $(el).is('a, button, :input, [tabindex]');
    }
});
