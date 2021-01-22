import { Component, OnInit, Query, EventEmitter, Output, Input } from '@angular/core';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { CEGModel } from 'src/app/model/CEGModel';
import { Observable } from 'rxjs';
import { debounceTime, distinctUntilChanged, map, mergeMap, filter } from 'rxjs/operators';
import { Search } from 'src/app/util/search';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'model-search-bar',
    templateUrl: './model-search-bar.component.html',
    styleUrls: ['./model-search-bar.component.css']
})
export class ModelSearchBarComponent implements OnInit {
    private static readonly SEARCH_FILTER = {
        'type': 'CEGModel'
    };

    @Input()
    public parentModel: CEGModel;

    private _model: CEGModel;

    get model(): CEGModel {
        return this._model;
    }

    set model(model: CEGModel) {
        this._model = model;
        this.selectedModel.emit(model);
    }

    get hasModel(): boolean {
        return this.model !== undefined;
    }

    @Output()
    private selectedModel = new EventEmitter<CEGModel>();

    formatter = (m: CEGModel) => {
        if (m !== null) {
            return m.name;
        } else {
            return '❌ ' + this.translate.instant('noResultsFound');
        }
    }

    search = (text$: Observable<string>) => text$.pipe(
        debounceTime(200),
        distinctUntilChanged(),
        filter(term => term.length >= 2),
        map(term => Search.processSearchQuery(term)),
        mergeMap(term => {
            return this.dataService.search(term, ModelSearchBarComponent.SEARCH_FILTER)
                .then(v => {
                    const result = this.parentModel !== undefined ? v.filter(model => model.url !== this.parentModel.url) : v;
                    if (result.length > 0) {
                        return result as CEGModel[];
                    } else {
                        return [null];
                    }
                });
        })
    )

    constructor(private dataService: SpecmateDataService,
        private translate: TranslateService) {
    }

    ngOnInit(): void {
    }

}
