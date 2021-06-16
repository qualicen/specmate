import { Component } from '@angular/core';
import { Observable, of, OperatorFunction } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, switchMap, tap } from 'rxjs/operators';
import { Config } from 'src/app/config/config';
import { Search } from 'src/app/util/search';
import { Url } from 'src/app/util/url';
import { IContainer } from '../../../../../model/IContainer';
import { SpecmateDataService } from '../../../../data/modules/data-service/services/specmate-data.service';
import { NavigatorService } from '../../navigator/services/navigator.service';

@Component({
    moduleId: module.id.toString(),
    selector: 'item-search-bar',
    templateUrl: 'item-search-bar.component.html',
    styleUrls: ['item-search-bar.component.css']
})
export class ItemSearchBar {

    constructor(private dataService: SpecmateDataService,
        private navigator: NavigatorService) { }

    public searching = false;
    public searchFailed = false;

    public get selectedElement(): IContainer {
        return this.navigator.currentElement;
    }

    public set selectedElement(model: IContainer) {
        if (model !== undefined) {
            this.navigator.navigate(model);
        }
    }

    private itemMap: { [url: string]: string[] } = {};

    public parentNames(element: IContainer): string {
        const parentUrls = Url.allParents(element.url);
        const parentUrl = Url.parent(element.url);
        if (this.itemMap[parentUrl] === undefined) {
            this.itemMap[parentUrl] = [];
            for (let i = 0; i < parentUrls.length - 1; i++) {
                this.dataService.readElement(parentUrls[i], true)
                    .then(elem => this.itemMap[parentUrl][parentUrls.length - i - 2] = (elem['extId'] !== undefined ? elem['extId'] + ': ' : '') + elem.name);
            }
        }
        return this.itemMap[parentUrl]?.join(' ⊳ ');
    }

    public resultTitle(element: IContainer): string {
        return element.name;
    }

    search: OperatorFunction<string, readonly IContainer[]> = (text$: Observable<string>) =>
        text$.pipe(
            debounceTime(300),
            distinctUntilChanged(),
            tap(() => this.searching = true),
            switchMap(term => {
                if (term !== undefined && term !== null && term.length >= Config.SEARCH_MINIMUM_LENGTH) {
                    const query = Search.processSearchQuery(term);
                    return this.dataService.searchObs(query).pipe(
                        tap(() => this.searchFailed = false),
                        catchError(() => {
                            this.searchFailed = true;
                            return of([]);
                        }
                        )
                    );
                }
                return of([]);
            }
            ),
            tap(() => this.searching = false)
        )

    formatter = (elem: { name: string, extId?: string }) => (elem.extId !== undefined ? elem.extId + ': ' : '') +  elem.name;
}
