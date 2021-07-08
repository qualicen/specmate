import { Component, Input } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { FieldMetaItem } from 'src/app/model/meta/field-meta';
import { FormElement } from '../base/form-element';

@Component({
    moduleId: module.id.toString(),
    selector: '[form-checkbox-input]',
    templateUrl: 'form-checkbox-input.component.html'
})
export class FormCheckboxInput extends FormElement {

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

    public get control() {
        return this._form.controls[this.meta.name];
    }

    public get value(): string {
        return this.control.value;
    }

    public set value(val: string) {
        this.control.setValue(val);
    }
}
