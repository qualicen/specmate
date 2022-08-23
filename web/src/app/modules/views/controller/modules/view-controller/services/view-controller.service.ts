import { EventEmitter, Injectable } from '@angular/core';
import { Config } from '../../../../../../config/config';
import { Folder } from '../../../../../../model/Folder';
import { ProcessStep } from '../../../../../../model/ProcessStep';
import { Type } from '../../../../../../util/type';
import { AuthenticationService } from '../../../../main/authentication/modules/auth/services/authentication.service';
import { AdditionalInformationService } from '../../../../side/modules/links-actions/services/additional-information.service';
import { SelectedElementService } from '../../../../side/modules/selected-element/services/selected-element.service';
import { ActivatedRoute } from '@angular/router';

@Injectable()
export class ViewControllerService {

    public isHeadless = false;

    public viewChanged = new EventEmitter();
    
    private _isEditorMaximized = false;
    private _loggingOutputShown: boolean = Config.LOG_INITIALLY_SHOWN;

    public get isLoggedIn(): boolean {
        return this.auth.isAuthenticated;
    }

    public get navigationShown(): boolean {
        return this.isLoggedIn && true;
    }

    public get projectExplorerShown(): boolean {
        return this.isLoggedIn && !this.isHeadless;
    }

    public get historyShown(): boolean {
        return this.isLoggedIn && this.selectedElementService.hasSelection && !this.isTopLibraryFolder && !this.isHeadless;
    }

    public get loggingOutputShown(): boolean {
        return this.isLoggedIn && this._loggingOutputShown && !this.isHeadless;
    }
    public set loggingOutputShown(loggingOutputShown: boolean) {
        this._loggingOutputShown = loggingOutputShown;
    }

    public showLoggingOutput(): void {
        this.loggingOutputShown = true;
    }

    public hideLoggingOutput(): void {
        this.loggingOutputShown = false;
    }

    public maximizeEditor(): void {
        this._isEditorMaximized = true;
    }

    public unmaximizeEditor(): void {
        this._isEditorMaximized = false;
    }

    public get isEditorMaximized(): boolean {
        return this._isEditorMaximized;
    }

    public get propertiesShown(): boolean {
        return this.isLoggedIn && this.selectedElementService.hasSelection && !this.isTopLibraryFolder;
    }

    public get tracingLinksShown(): boolean {
        let selected = this.selectedElementService.selectedElement;
        if (this.isLoggedIn && selected !== undefined) {
            if (Type.is(selected, ProcessStep) && selected['tracesTo']) {
                return true && !this.isHeadless;
            }
        }

        return false;
    }

    public get linksActionsShown(): boolean {
        return this.isLoggedIn && this.additionalInformationService.hasAdditionalInformation && !this.isHeadless;
    }

    public get areFolderPropertiesEditable(): boolean {
        return !this.isTopLibraryFolder;
    }

    private get isTopLibraryFolder(): boolean {
        let selected = this.selectedElementService.selectedElement;
        if (Type.is(selected, Folder)) {
            return this.auth.session.libraryFolders.indexOf(selected.id) > -1;
        }
        return false;
    }

    constructor(
        private selectedElementService: SelectedElementService,
        private additionalInformationService: AdditionalInformationService,
        private auth: AuthenticationService,
        private route: ActivatedRoute) {
            this.route.queryParams.subscribe(params => {
                if(params['hl'] !== undefined) {
                    this.isHeadless = true;
                } else {
                    this.isHeadless = false;
                }
                this.viewChanged.emit();
            });
        }
}
