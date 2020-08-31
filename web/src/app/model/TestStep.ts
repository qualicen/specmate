	import './support/gentypes';
	import { Proxy } from './support/proxy';


	export class TestStep  {

		___nsuri: string = "http://specmate.com/20200605/model/testspecification";
		public url: string;
		public className: string = "TestStep";
		public static className: string = "TestStep";
		// Attributes
		public id: EString;
		public name: EString;
		public description: EString;
		public recycled: EBoolean;
		public hasRecycledChildren: EBoolean;
		public position: EInt;
		public expectedOutcome: EString;

		// References
		public referencedTestParameters: Proxy[];

		// Containment


	}

