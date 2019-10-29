import { mxgraph } from 'mxgraph';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});

export class ScrollbarHandler {

    private autoScrollCorrection = 50;

    private graphViewValidateOrig: (cell?: mxgraph.mxCell) => void;
    private graphSizeDidChangeOrig: () => void;

    constructor(private graph: mxgraph.mxGraph) { }

    public init(): void {
        // this.graph.panningHandler.ignoreCell = true;
        // this.graph.setPanning(true);

        this.graphViewValidateOrig = this.graph.view.validate;
        this.graphSizeDidChangeOrig = this.graph.sizeDidChange;

        this.graph['scrollTileSize'] = this.scrollTileSize;
        this.graph['getPagePadding'] = this.graphGetPagePadding.bind(this);
        this.graph['getPageSize'] = this.graphGetPageSize.bind(this);
        this.graph['getPageLayout'] = this.graphGetPageLayout.bind(this);
        this.graph.view.getBackgroundPageBounds = this.graphViewGetBackgroundPageBounds.bind(this);
        this.graph.getPreferredPageSize = this.graphGetPreferredPageSize.bind(this);
        this.graph.view.validate = this.graphViewValidate.bind(this);
        this.graph['sizeDidChange'] = this.graphSizeDidChange.bind(this);

        this.getInitialScrollPosition();
    }

    private getInitialScrollPosition(): mxgraph.mxPoint {
        console.log(this.graph.view.getBounds(this.graph.getModel().getChildCells(this.graph.getDefaultParent())));
        console.log(this.graph.view.getDrawPane());
        console.log(this.graph.view);
        document.getElementById('mxGraphContainer').scrollLeft -= 50;
        return new mx.mxPoint(0, 0);
    }

    /**
     * Specifies the size of the size for "tiles" to be used for a graph with
     * scrollbars but no visible background page. A good value is large
     * enough to reduce the number of repaints that is caused for auto-
     * translation, which depends on this value, and small enough to give
     * a small empty buffer around the graph. Default is 400x400.
     */
    private scrollTileSize = new mx.mxRectangle(0, 0, 400, 400);

    /**
     * Returns the padding for pages in page view with scrollbars.
     */
    private graphGetPagePadding(): mxgraph.mxPoint {
        return new mx.mxPoint(Math.max(0, Math.round(this.graph.container.offsetWidth - 34)),
            Math.max(0, Math.round(this.graph.container.offsetHeight - 34)));
    }
    /**
     * Returns the size of the page format scaled with the page size.
     */
    private graphGetPageSize(): mxgraph.mxRectangle {
        return (this.graph.pageVisible) ? new mx.mxRectangle(0, 0, this.graph.pageFormat.width * this.graph.pageScale,
            this.graph.pageFormat.height * this.graph.pageScale) : this.scrollTileSize;
    }

    /**
   * Returns a rectangle describing the position and count of the
   * background pages, where x and y are the position of the top,
   * left page and width and height are the vertical and horizontal
   * page count.
   */
    private graphGetPageLayout() {
        const size = (this.graph.pageVisible) ? this.graphGetPageSize() : this.scrollTileSize;
        const bounds = this.graph.getGraphBounds();
        if (bounds.width == 0 || bounds.height == 0) {
            return new mx.mxRectangle(0, 0, 1, 1);
        } else {
            // Computes untransformed graph bounds
            const x = Math.ceil(bounds.x / this.graph.view.scale - this.graph.view.translate.x);
            const y = Math.ceil(bounds.y / this.graph.view.scale - this.graph.view.translate.y);
            const w = Math.floor(bounds.width / this.graph.view.scale);
            const h = Math.floor(bounds.height / this.graph.view.scale);

            const x0 = Math.floor(x / size.width);
            const y0 = Math.floor(y / size.height);
            const w0 = Math.ceil((x + w) / size.width) - x0;
            const h0 = Math.ceil((y + h) / size.height) - y0;

            return new mx.mxRectangle(x0, y0, w0, h0);
        }
    }

    // Fits the number of background pages to the graph
    private graphViewGetBackgroundPageBounds() {
        const layout = this.graphGetPageLayout();
        const page = this.graphGetPageSize();

        return new mx.mxRectangle(this.graph.view.scale * (this.graph.view.translate.x + layout.x * page.width),
            this.graph.view.scale * (this.graph.view.translate.y + layout.y * page.height),
            this.graph.view.scale * layout.width * page.width,
            this.graph.view.scale * layout.height * page.height);
    }

    private graphGetPreferredPageSize() {
        const pages = this.graphGetPageLayout();
        const size = this.graphGetPageSize();

        return new mx.mxRectangle(0, 0, pages.width * size.width, pages.height * size.height);
    }

    /**
     * Guesses autoTranslate to avoid another repaint (see below).
     * Works if only the scale of the graph changes or if pages
     * are visible and the visible pages do not change.
     */
    private graphViewValidate() {
        if (this.graph.container != null && mx.mxUtils.hasScrollbars(this.graph.container)) {
            const pad = this.graphGetPagePadding();
            const size = this.graphGetPageSize();

            // Updating scrollbars here causes flickering in quirks and is not needed
            // if zoom method is always used to set the current scale on the graph.
            const tx = this.graph.view.translate.x;
            const ty = this.graph.view.translate.y;
            this.graph.view.translate.x = pad.x / this.graph.view.scale - (this.graph.view['x0'] || 0) * size.width;
            this.graph.view.translate.y = pad.y / this.graph.view.scale - (this.graph.view['y0'] || 0) * size.height;
        }

        this.graphViewValidateOrig.apply(this.graph.view, arguments);
    }

    private graphSizeDidChange() {
        if (this.graph.container != null && mx.mxUtils.hasScrollbars(this.graph.container)) {
            const pages = this.graphGetPageLayout();
            const pad = this.graphGetPagePadding();
            const size = this.graphGetPageSize();

            // Updates the minimum graph size
            const minw = Math.ceil(2 * pad.x / this.graph.view.scale + pages.width * size.width);
            const minh = Math.ceil(2 * pad.y / this.graph.view.scale + pages.height * size.height);

            const min = this.graph.minimumGraphSize;

            // LATER: Fix flicker of scrollbar size in IE quirks mode
            // after delayed call in window.resize event handler
            if (min == null || min.width != minw || min.height != minh) {
                this.graph.minimumGraphSize = new mx.mxRectangle(0, 0, minw, minh);
            }

            // Updates auto-translate to include padding and graph size
            const dx = pad.x / this.graph.view.scale - pages.x * size.width;
            const dy = pad.y / this.graph.view.scale - pages.y * size.height;

            if (!this.graph['autoTranslate'] && (this.graph.view.translate.x != dx || this.graph.view.translate.y != dy)) {
                this.graph['autoTranslate'] = true;
                this.graph.view['x0'] = pages.x;
                this.graph.view['y0'] = pages.y;
                // NOTE: THIS INVOKES THIS METHOD AGAIN. UNFORTUNATELY THERE IS NO WAY AROUND THIS SINCE THE
                // BOUNDS ARE KNOWN AFTER THE VALIDATION AND SETTING THE TRANSLATE TRIGGERS A REVALIDATION.
                // SHOULD MOVE TRANSLATE/SCALE TO VIEW.
                const tx = this.graph.view.translate.x;
                const ty = this.graph.view.translate.y;
                this.graph.view.setTranslate(dx, dy);
                this.graph.container.scrollLeft += (dx - tx) * this.graph.view.scale;
                this.graph.container.scrollTop += (dy - ty) * this.graph.view.scale;
                this.graph['autoTranslate'] = false;
                return;
            }
            this.graphSizeDidChangeOrig.apply(this.graph, arguments);
        }
    }
}
