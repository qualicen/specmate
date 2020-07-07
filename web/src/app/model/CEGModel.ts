	import './support/gentypes';
	import { Proxy } from './support/proxy';


	export class CEGModel  {

		___nsuri: string = "http://specmate.com/20200605/model/requirements";
		public url: string;
		public className: string = "CEGModel";
		public static className: string = "CEGModel";
		// Attributes
		public id: EString;
		public name: EString;
		public description: EString;
		public recycled: EBoolean;
		public hasRecycledChildren: EBoolean;
		public modelRequirements: EString;

		// References
		
		public tracesTo: Proxy[];
		public tracesFrom: Proxy[];

		// Containment


	}

