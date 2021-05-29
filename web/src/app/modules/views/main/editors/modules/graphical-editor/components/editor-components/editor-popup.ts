import { mxgraph } from 'mxgraph';
import { CEGConnection } from 'src/app/model/CEGConnection';
import { StyleChanger } from '../util/style-changer';
import { EditorStyle } from './editor-style';
import { Type } from 'src/app/util/type';
import { TranslateService } from '@ngx-translate/core';
import { IContainer } from 'src/app/model/IContainer';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});


export class EditorPopup {

    constructor(private graph: mxgraph.mxGraph, private contents: IContainer[], private translate: TranslateService) { }

    public init(): void {

        this.graph.popupMenuHandler['autoExpand'] = true;

        this.graph.popupMenuHandler.isSelectOnPopup = function (me) {
            return mx.mxEvent.isMouseEvent(me.getEvent());
        };

        // Installs context menu
        this.graph.popupMenuHandler['factoryMethod'] = this.provideMenu.bind(this);
    }

    private async provideMenu(menu: mxgraph.mxPopupMenuHandler, cell: mxgraph.mxCell, evt: PointerEvent) {

        if (cell === undefined || cell === null) {
            return;
        }

        let element: IContainer = undefined;
        while (element === undefined) {
            element = this.contents.find(element => element.url === cell.id);
        }

        let selectedCells = this.graph.getSelectionCells();
        selectedCells = selectedCells.filter(element => element.parent == this.graph.getDefaultParent());

        const deleteText = this.translate.instant('delete');
        menu.addItem(deleteText, null, () => {
            this.graph.removeCells(selectedCells);
        }, undefined, 'fa fa-trash-o', undefined, undefined);

        if (Type.is(element, CEGConnection) && selectedCells.length === 1) {

            const connection = element as CEGConnection;
            const icon = connection.negate ? 'fa fa-check' : 'fa fa-circle-o';

            const negateText = this.translate.instant('negated');
            menu.addItem(negateText, null, async () => {
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
