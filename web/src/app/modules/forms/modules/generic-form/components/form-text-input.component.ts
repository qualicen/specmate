import { Component, Input } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { FieldMetaItem } from 'src/app/model/meta/field-meta';
import { FormElement } from '../base/form-element';

@Component({
    moduleId: module.id.toString(),
    selector: '[form-text-input]',
    templateUrl: 'form-text-input.component.html'
})
export class FormTextInput extends FormElement {
    constructor(translate: TranslateService) {
        super(translate);
    }

    public get meta(): FieldMetaItem {
        return this._meta;
    }

    @Input()
    public set meta(meta: FieldMetaItem) {
        this._meta = meta;
    }

    public get form(): FormGroup {
        return this._form;
    }

    @Input()
    public set form(form: FormGroup) {
        this._form = form;
    }
}
