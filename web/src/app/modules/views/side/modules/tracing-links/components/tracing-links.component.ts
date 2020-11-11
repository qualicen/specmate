import { Component } from '@angular/core';
import { NgbTypeaheadSelectItemEvent } from '@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/filter';
import 'rxjs/add/operator/map';
import { of } from 'rxjs/observable/of';
import { Config } from 'src/app/config/config';
import { OperationMonitorService } from 'src/app/modules/notification/modules/operation-monitor/services/operation-monitor.service';
import { IContainer } from '../../../../../../model/IContainer';
import { ISpecmateModelObject } from '../../../../../../model/ISpecmateModelObject';
import { Proxy } from '../../../../../../model/support/proxy';
import { Arrays } from '../../../../../../util/arrays';
import { Id } from '../../../../../../util/id';
import { Search } from '../../../../../../util/search';
import { SpecmateDataService } from '../../../../../data/modules/data-service/services/specmate-data.service';
import { SelectedElementService } from '../../selected-element/services/selected-element.service';


@Component({
    moduleId: module.id.toString(),
    selector: 'tracing-links',
    templateUrl: 'tracing-links.component.html'
})
export class TracingLinks {

    public searching = false;
    public searchFailed = false;

    /** is the control collapsed? */
    public isCollapsed = false;

    /** constructor */
    public constructor(private dataService: SpecmateDataService,
        private selectedElementService: SelectedElementService,
        private operationMonitor: OperationMonitorService) { }

    /** getter */
    get model(): ISpecmateModelObject {
        return this.selectedElementService.selectedElement as ISpecmateModelObject;
    }

    /** formats a specmate object. called by typeahead */
    public formatter(toFormat: ISpecmateModelObject): string {
        return toFormat.name;
    }

    /** called when an item is selected in the typeahead */
    public selectItem(event: NgbTypeaheadSelectItemEvent, reqtypeahead: any): void {
        event.preventDefault();
        let trace: Proxy = new Proxy();
        trace.url = event.item.url;
        this.model.tracesTo.push(trace);
        this.dataService.updateElement(this.model, true, Id.uuid);
        reqtypeahead.value = '';
    }

    /** searches suggestions based on the typed text */
    public search = (text$: Observable<string>) => {
        return text$
            .debounceTime(300)
            .distinctUntilChanged()
            .map(term => term.trim())
            .filter(term => term.length >= Config.SEARCH_MINIMUM_LENGTH)
            .map(term => Search.processSearchQuery(term))
            .switchMap(async term => {
                try {
                    this.operationMonitor.forceModalClosed = true;
                    const result = await this.dataService.search(term, { 'type': 'Requirement' });
                    this.operationMonitor.forceModalClosed = false;
                    return result;
                } catch {
                    this.operationMonitor.forceModalClosed = false;
                    return of([]);
                }
            })
            .map((searchResult: IContainer[]) => searchResult.filter((result: IContainer) => {
                let existing: Proxy = this.model.tracesTo.find((t: Proxy) => t.url === result.url);
                return existing == undefined;
            }
            ));
    }

    /** Remove a trace-link */
    public delete(trace: Proxy): void {
        Arrays.remove(this.model.tracesTo, trace);
        this.dataService.updateElement(this.model, true, Id.uuid);
    }
}
