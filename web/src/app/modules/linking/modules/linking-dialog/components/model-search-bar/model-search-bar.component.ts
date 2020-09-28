import { Component, OnInit, Query, EventEmitter, Output } from '@angular/core';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { CEGModel } from 'src/app/model/CEGModel';
import { Observable } from 'rxjs';
import { debounceTime, distinctUntilChanged, map, mergeMap, filter } from 'rxjs/operators';
import { Search } from 'src/app/util/search';

@Component({
    selector: 'model-search-bar',
    templateUrl: './model-search-bar.component.html',
    styleUrls: ['./model-search-bar.component.css']
})
export class ModelSearchBarComponent implements OnInit {
    private static readonly SEARCH_FILTER = {
        'type': 'CEGModel'
    };

    private _model: CEGModel;

    get model(): CEGModel {
        return this._model;
    }

    set model(model: CEGModel) {
        this._model = model;
        this.selectedModel.emit(model);
    }

    @Output()
    private selectedModel = new EventEmitter<CEGModel>();

    formatter = (m: CEGModel) => m.name;

    search = (text$: Observable<string>) => text$.pipe(
        debounceTime(200),
        distinctUntilChanged(),
        filter(term => term.length >= 2),
        map(term => Search.processSearchQuery(term)),
        mergeMap(term => {
            return this.dataService.search(term, ModelSearchBarComponent.SEARCH_FILTER)
                .then(v => v as CEGModel[]);
        })
    )

    constructor(private dataService: SpecmateDataService) {
    }

    ngOnInit(): void {
    }

}
