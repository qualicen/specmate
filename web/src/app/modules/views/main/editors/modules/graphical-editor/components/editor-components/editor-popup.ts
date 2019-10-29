import { mxgraph } from 'mxgraph';
import { CEGConnection } from 'src/app/model/CEGConnection';
import { StyleChanger } from '../util/style-changer';
import { EditorStyle } from './editor-style';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { Type } from 'src/app/util/type';
import { replaceClass } from '../util/css-utils';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});


export class EditorPopup {
    constructor(private graph: mxgraph.mxGraph, private dataService: SpecmateDataService) { }

    public init(): void {

        this.graph.popupMenuHandler['autoExpand'] = true;

        this.graph.popupMenuHandler.isSelectOnPopup = function (me) {
            return mx.mxEvent.isMouseEvent(me.getEvent());
        };

        const mxPopupMenuShowMenu = mx.mxPopupMenu.prototype.showMenu;
        mx.mxPopupMenuHandler.prototype.showMenu = function () {
            mxPopupMenuShowMenu.apply(this, arguments);

            const containerElem = this.div as HTMLElement;
            containerElem.classList.remove('mxPopupMenu');
            containerElem.classList.add('graphPopupMenu');

            replaceClass('mxPopupMenu', 'graphPopupMenu');
        };


        // Installs context menu
        this.graph.popupMenuHandler['factoryMethod'] = this.provideMenu.bind(this);
    }

    private async provideMenu(menu: mxgraph.mxPopupMenuHandler, cell: mxgraph.mxCell, evt: PointerEvent) {

        if (cell === undefined || cell === null) {
            return;
        }

        menu.addItem('Delete', null, () => {
            this.graph.removeCells([cell]);
        }, undefined, undefined, undefined, undefined);

        const element = await this.dataService.readElement(cell.id, true);
        if (Type.is(element, CEGConnection)) {

            const connection = element as CEGConnection;
            const icon = connection.negate ? 'fa fa-check' : 'fa fa-circle-o';

            menu.addItem('Negate', null, async () => {
                this.graph.getModel().beginUpdate();
                if (connection.negate) {
                    StyleChanger.removeStyle(cell, this.graph, EditorStyle.ADDITIONAL_CEG_CONNECTION_NEGATED_STYLE);
                } else {
                    StyleChanger.addStyle(cell, this.graph, EditorStyle.ADDITIONAL_CEG_CONNECTION_NEGATED_STYLE);
                }
                this.graph.getModel().endUpdate();
            }, undefined, icon, undefined, undefined);
        }
    }

}

