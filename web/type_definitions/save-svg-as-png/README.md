# save-svg-as-png-typings
TypeScript definitions for save-svg-as-png (https://github.com/exupero/saveSvgAsPng)

## Pre-requisite
As this is typings for `save-svg-as-png`, installation of `save-svg-as-png` is required
```
npm install save-svg-as-png
```

## Installation
```
npm install -D ksholla20/save-svg-as-png-typings
```

## Usage
```
import * as saveAsPng from "save-svg-as-png";
```

Once imported, usage is similar to original code in save-svg-as-png
```
saveAsPng.saveSvgAsPng(document.getElementById("diagram"), "diagram.png");
```