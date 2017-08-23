import { Requirement } from '../../../model/Requirement';
import { ConfirmationModal } from '../../core/forms/confirmation-modal.service';
import { NavigatorService } from '../../../services/navigator.service';
import { CEGEditor } from './ceg-editor.component';
import { Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { Location } from '@angular/common';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';

import { Config } from '../../../config/config';
import { SpecmateDataService } from '../../../services/specmate-data.service';

import { IContainer } from '../../../model/IContainer';

import { CEGModel } from '../../../model/CEGModel';

import { ITool } from './tools/i-tool';
import { DeleteTool } from './tools/delete-tool';

import { Url } from '../../../util/Url';
import { ConnectionTool } from './tools/connection-tool';
import { MoveTool } from './tools/move-tool';
import { NodeTool } from './tools/node-tool';
import { Type } from '../../../util/Type';
import { Arrays } from "../../../util/Arrays";

import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/reduce';
import { FieldMetaItem, MetaInfo } from "../../../model/meta/field-meta";
import { GenericForm } from "../../core/forms/generic-form.component";
import { EditorCommonControlService } from '../../../services/editor-common-control.service';

import { SpecmateViewBase } from '../../core/views/specmate-view-base';
import { TestSpecificationGenerator } from '../test-specification-generator';

@Component({
    moduleId: module.id,
    selector: 'model-editor',
    templateUrl: 'model-editor.component.html'
})
export class ModelEditor extends TestSpecificationGenerator {

    @ViewChild(CEGEditor)
    private cegEditor: CEGEditor;

    @ViewChild(GenericForm)
    private form: GenericForm;

    private model: CEGModel;
    private contents: IContainer[];

    /** Constructor */
    constructor(
        dataService: SpecmateDataService,
        navigator: NavigatorService,
        route: ActivatedRoute,
        modal: ConfirmationModal,
        editorCommonControlService: EditorCommonControlService,
        private changeDetectorRef: ChangeDetectorRef
    ) {
        super(dataService, modal, route, navigator, editorCommonControlService);
    }

    ngDoCheck() {
        super.ngDoCheck();
        this.changeDetectorRef.detectChanges();
        if(this.model && this.contents) {
            this.doCheckCanCreateTestSpec(this.model, this.contents);
        }
    }

    protected resolveRequirement(element: IContainer): Promise<Requirement> {
        return this.dataService.readElement(Url.parent(element.url)).then((element: IContainer) => element as Requirement);
    }

    protected onElementResolved(element: IContainer): void {
        super.onElementResolved(element);
        this.model = element;
        this.dataService.readContents(this.model.url).then((contents: IContainer[]) => this.contents = contents);
    }

    protected get isValid(): boolean {
        if (!this.cegEditor || !this.form) {
            return true;
        }
        return this.cegEditor.isValid && this.form.isValid;
    }
}
