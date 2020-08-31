	import './support/gentypes';
	import { Proxy } from './support/proxy';


	export class ProcessConnection  {

		___nsuri: string = "http://specmate.com/20200605/model/processes";
		public url: string;
		public className: string = "ProcessConnection";
		public static className: string = "ProcessConnection";
		// Attributes
		public id: EString;
		public name: EString;
		public description: EString;
		public recycled: EBoolean;
		public hasRecycledChildren: EBoolean;
		public condition: EString;
		public labelX: EDouble;
		public labelY: EDouble;

		// References
		
		public tracesTo: Proxy[];
		public tracesFrom: Proxy[];
		public source: Proxy;
		public target: Proxy;

		// Containment


	}

