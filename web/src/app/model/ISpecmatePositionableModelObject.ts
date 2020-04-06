	import './support/gentypes';
	import { Proxy } from './support/proxy';


	export interface ISpecmatePositionableModelObject {

		___nsuri: string;
		 url: string;
		 className: string;
		
		// Attributes
		 id: EString;
		 name: EString;
		 description: EString;
		 recycled: EBoolean;
		 hasRecycledChildren: EBoolean;
		 x: EDouble;
		 y: EDouble;
		 width: EDouble;
		 height: EDouble;

		// References
		
		 tracesTo: Proxy[];
		 tracesFrom: Proxy[];

		// Containment


	}

